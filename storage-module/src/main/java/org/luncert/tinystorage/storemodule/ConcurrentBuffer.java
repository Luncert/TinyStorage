package org.luncert.tinystorage.storemodule;

import lombok.Getter;
import lombok.NonNull;
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

  @Getter
  private final Header header = new Header();

  private final MappedByteBuffer buffer;

  private final AtomicInteger readerCount = new AtomicInteger();

  private final WriteContext writeContext = new WriteContext();

  private Function<Object, Integer> appendFunc;

  private final Function<Object, Integer> funcAppend;

  private static final Function<Object, Integer> FUNC_LOCK_WRITE = r -> 0;

  ConcurrentBuffer(TsRuntime runtime, MappedByteBuffer buffer) {
    this.runtime = runtime;
    this.buffer = buffer;
    funcAppend = r -> {
      synchronized (this) {
        notifyAll();
      }

      TsWriter<Object> writer = runtime.getWriter();

      int pos = buffer.position();
      try {
        writer.write(r, writeContext);
      } catch (BufferOverflowException e) {
        buffer.position(pos);
        // when file reaches the size limit, it will be marked readonly,
        // and won't accept data anymore.
        appendFunc = FUNC_LOCK_WRITE;
        header.readOnly = true;
        saveFileHeader();
        return 0;
      }

      return buffer.position() - pos;
    };
    determineAppendFunc();

    loadFileHeader();
  }

  @Getter
  public static class Header {

    // readOnly + startAt + endAt + writePosition
    static final int HEADER_SIZE = 1 + 8 + 8 + 4;

    private boolean readOnly;
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
  private void loadFileHeader() {
    header.readOnly = (buffer.get(0) & 0x1) == 1;
    // header.startAt = Utils.byteArrayToLong(i -> buffer.get(1 + i), 8);
    // header.endAt = Utils.byteArrayToLong(i -> buffer.get(9 + i), 8);
    buffer.position(Math.max(Utils.byteArrayToInt(i -> buffer.get(17 + i), 4), Header.HEADER_SIZE));
  }

  /**
   * Save file header to buffer.
   */
  private void saveFileHeader() {
    buffer.put(0, header.isReadOnly() ? (byte) 1 : (byte) 0);
    // Utils.longToByteArray(header.getStartAt(), (i, b) -> buffer.put(1 + i, b), 8);
    // Utils.longToByteArray(header.getEndAt(), (i, b) -> buffer.put(9 + i, b), 8);
    Utils.intToByteArray(buffer.position(), (i, b) -> buffer.put(17 + i, b), 4);
  }

  /**
   * Return buffer readable bytes count.
   */
  int size() {
    return buffer.position();
  }

  void reset() {
    header.readOnly = false;
    saveFileHeader();
    buffer.position(Header.HEADER_SIZE);
    appendFunc = funcAppend;
  }

  void flush() {
    saveFileHeader();
    buffer.force();
  }

  void close() {
    saveFileHeader();
  }

  synchronized int append(Object record) {
    return appendFunc.apply(record);
  }

  public ReadContext requireReadCursor(boolean syncWithWriter) {
    readerCount.incrementAndGet();
    return new ReadContext(Header.HEADER_SIZE, syncWithWriter);
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
  public class ReadContext implements ReadOperator, Iterable<Object>, Iterator<Object>, Closeable {

    private int readOffset;

    private final boolean syncWithWriter;

    private boolean hasNext = true;

    private Object nextItem;

    ReadContext(int readOffset, boolean syncWithWriter) {
      this.readOffset = readOffset;
      this.syncWithWriter = syncWithWriter;
    }

    @Override
    @NonNull
    public Iterator<Object> iterator() {
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
    public Object next() {
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
      readBytes(payload);
      return new String(payload, charset);
    }

    @Override
    public byte[] readByteArray() {
      int size = readInt();
      byte[] r = new byte[size];
      readBytes(r);
      return r;
    }

    @SneakyThrows
    public void readBytes(byte[] buf) {
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
        try {
          if (watch(READ_BLOCK_TIMEOUT)) {
            throw new EOFException();
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
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
      buffer.put((byte)(value & 255));
      buffer.put((byte) ((value = value >> 8) & 255));
      buffer.put((byte) ((value = value >> 8) & 255));
      buffer.put((byte) ((value = value >> 8) & 255));
      buffer.put((byte) ((value = value >> 8) & 255));
      buffer.put((byte) ((value = value >> 8) & 255));
      buffer.put((byte) ((value = value >> 8) & 255));
      buffer.put((byte) (value >> 8 & 255));
    }

    public void appendInt(int value) {
      buffer.put((byte)(value & 255));
      buffer.put((byte)((value = value >> 8) & 255));
      buffer.put((byte)((value = value >> 8) & 255));
      buffer.put((byte)(value >> 8 & 255));
    }

    public void appendString(String str) {
      appendString(str, Charset.defaultCharset());
    }

    public void appendString(String str, Charset charset) {
      appendBytes(str.getBytes(charset));
    }

    public void appendBytes(byte[] bytes) {
      appendInt(bytes.length);
      buffer.put(bytes);
    }

    public void appendByte(byte b) {
      buffer.put(b);
    }
  }

  protected boolean watch(long timeout) throws InterruptedException {
    long timestamp = System.currentTimeMillis();
    synchronized (this) {
      wait(timeout);
    }
    return System.currentTimeMillis() - timestamp >= timeout;
  }
}
