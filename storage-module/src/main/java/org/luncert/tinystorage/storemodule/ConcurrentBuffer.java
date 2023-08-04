package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.common.Utils;

import java.io.Closeable;
import java.io.EOFException;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import lombok.SneakyThrows;

public class ConcurrentBuffer {

  private static final long READ_BLOCK_TIMEOUT = 1000;

  private final TsRuntime runtime;

  private final MappedByteBuffer buffer;

  private final AtomicInteger readerCount = new AtomicInteger();

  private final WriteContext writeContext = new WriteContext();

  private final Signal bufferUpdateSignal = new Signal();

  private Function<Record, Integer> appendFunc;

  private final Function<Record, Integer> funcAppend;

  private static final Function<Record, Integer> FUNC_LOCK_WRITE = r -> 0;

  @SuppressWarnings("unchecked")
  ConcurrentBuffer(TsRuntime runtime, MappedByteBuffer buffer) {
    this.runtime = runtime;
    this.buffer = buffer;
    funcAppend = r -> {
      bufferUpdateSignal.set();

      TsWriter<Record> writer = (TsWriter<Record>) runtime.getWriter();

      int pos = buffer.position();
      try {
        writer.write(r, writeContext);
      } catch (BufferOverflowException e) {
        buffer.position(pos);
        appendFunc = FUNC_LOCK_WRITE;
        return 0;
      }

      return buffer.position() - pos;
    };
    determineAppendFunc();
  }

  private void determineAppendFunc() {
    boolean isReadOnly = (buffer.get(0) & 0x1) == 1;
    if (isReadOnly) {
      appendFunc = FUNC_LOCK_WRITE;
    } else {
      appendFunc = funcAppend;
    }
  }

  private boolean isWriteLocked() {
    synchronized (this) {
      return appendFunc.equals(FUNC_LOCK_WRITE);
    }
  }

  /**
   * Load file header from buffer.
   */
  void loadFileHeader(TsFileHeader fileHeader) {
    fileHeader.setReadOnly((buffer.get(0) & 0x1) == 1);
    fileHeader.setStartAt(Utils.byteArrayToLong(i -> buffer.get(1 + i), 8));
    fileHeader.setEndAt(Utils.byteArrayToLong(i -> buffer.get(9 + i), 8));
    buffer.position(Math.max(Utils.byteArrayToInt(i -> buffer.get(17 + i), 4), TsFileHeader.HEADER_SIZE));
  }

  /**
   * Save file header to buffer.
   */
  void saveFileHeader(TsFileHeader fileHeader) {
    buffer.put(0, fileHeader.isReadOnly() ? (byte) 1 : (byte) 0);
    Utils.longToByteArray(fileHeader.getStartAt(), (i, b) -> buffer.put(1 + i, b), 8);
    Utils.longToByteArray(fileHeader.getEndAt(), (i, b) -> buffer.put(9 + i, b), 8);
    Utils.intToByteArray(buffer.position(), (i, b) -> buffer.put(17 + i, b), 4);
  }

  /**
   * Return buffer readable bytes count.
   */
  int size() {
    return buffer.position();
  }

  void reset() {
    buffer.position(TsFileHeader.HEADER_SIZE);
    appendFunc = funcAppend;
  }

  void flush() {
    buffer.force();
  }

  synchronized int append(Record record) {
    return appendFunc.apply(record);
  }

  public ReadContext requireReadCursor(boolean syncWithWriter) {
    readerCount.incrementAndGet();
    return new ReadContext(TsFileHeader.HEADER_SIZE, syncWithWriter);
  }

  private void releaseReader() {
    readerCount.decrementAndGet();
  }

  /**
   * Check whether buffer is ready to release.
   * @return False if there are active buffer readers.
   */
  public boolean isAbleToRelease() {
    return readerCount.get() == 0;
  }

  /**
   * ReadContext is not thread-safe.
   */
  public class ReadContext implements ReadOperator, Iterable<Record>, Iterator<Record>, Closeable {

    private int readOffset;

    private final boolean syncWithWriter;

    private boolean hasNext = true;

    private Record nextItem;

    ReadContext(int readOffset, boolean syncWithWriter) {
      this.readOffset = readOffset;
      this.syncWithWriter = syncWithWriter;
    }

    @Override
    public Iterator<Record> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      if (hasNext) {
        try {
          nextItem = runtime.getReader().read(this);
          return nextItem != null;
        } catch (EOFException ignored) {
          hasNext = false;
          nextItem = null;
          releaseReader();
        }
      }

      return false;
    }

    @Override
    public Record next() {
      if (nextItem == null) {
        throw new NoSuchElementException();
      }

      return nextItem;
    }

    public void close() {
      hasNext = false;
      nextItem = null;
      releaseReader();
    }

    public long readLong() {
      long low = readInt();
      long high = readInt();
      return (high << 32) + (4294967295L & low);
    }

    @SneakyThrows
    public int readInt() {
      requireBytes(4);
      int value = rb() & 255;
      value += (rb() & 255) << 8;
      value += (rb() & 255) << 16;
      value += (rb() & 255) << 24;
      return value;
    }

    public String readString() {
      return readString(Charset.defaultCharset());
    }

    public String readString(Charset charset) {
      int size = readInt();
      byte[] payload = new byte[size];
      readFully(payload);
      return new String(payload, charset);
    }

    @SneakyThrows
    public void readFully(byte[] buf) {
      requireBytes(buf.length);
      for (int i = 0; i < buf.length; i++) {
        buf[i] = rb();
      }
    }

    @SneakyThrows
    public byte readByte() {
      requireBytes(1);
      return rb();
    }

    private void requireBytes(final int n) throws EOFException {
      int readableCount = buffer.position() - readOffset;
      while (readableCount < n) {
        if (!syncWithWriter || isWriteLocked()) {
          throw new EOFException();
        }
        // wait for buffer update signal
        if (bufferUpdateSignal.watch(READ_BLOCK_TIMEOUT)) {
          throw new EOFException();
        }
        readableCount = buffer.position() - readOffset;
      }
    }

    private byte rb() {
      return buffer.get(readOffset++);
    }
  }

  public class WriteContext implements WriteOperator {

    // little endian

    public void appendLong(long value) {
      buffer.put((byte)((int)(value & 255L)));
      buffer.put((byte)((int)(value >> 8 & 255L)));
      buffer.put((byte)((int)(value >> 16 & 255L)));
      buffer.put((byte)((int)(value >> 24 & 255L)));
      buffer.put((byte)((int)(value >> 32 & 255L)));
      buffer.put((byte)((int)(value >> 40 & 255L)));
      buffer.put((byte)((int)(value >> 48 & 255L)));
      buffer.put((byte)((int)(value >> 56 & 255L)));
    }

    public void appendInt(int value) {
      buffer.put((byte)(value & 255));
      buffer.put((byte)(value >> 8 & 255));
      buffer.put((byte)(value >> 16 & 255));
      buffer.put((byte)(value >> 24 & 255));
    }

    public void appendString(String str) {
      appendString(str, Charset.defaultCharset());
    }

    public void appendString(String str, Charset charset) {
      appendInt(str.length());
      appendBytes(str.getBytes(charset));
    }

    public void appendBytes(byte[] bytes) {
      buffer.put(bytes);
    }

    public void appendByte(byte b) {
      buffer.put(b);
    }
  }

  private static class Signal {

    void set() {
      synchronized (this) {
        notifyAll();
      }
    }

    @SneakyThrows
    void watch() {
      synchronized (this) {
        wait();
      }
    }

    /**
     * Watch signal update with timeout.
     * @param timeout milliseconds
     * @return True if action ends due to timeout
     */
    @SneakyThrows
    boolean watch(long timeout) {
      long timestamp = System.currentTimeMillis();
      synchronized (this) {
        wait(timeout);
      }
      return System.currentTimeMillis() - timestamp >= timeout;
    }
  }
}
