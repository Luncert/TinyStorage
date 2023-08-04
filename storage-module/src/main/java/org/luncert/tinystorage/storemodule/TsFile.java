package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.ConcurrentBuffer.ReadContext;
import org.luncert.tinystorage.storemodule.descriptor.DescribedObject;
import org.luncert.tinystorage.storemodule.descriptor.TsFileDesc;
import org.luncert.tinystorage.storemodule.exception.TinyStorageException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;

@ToString
public class TsFile implements DescribedObject<TsFileDesc> {

  @Getter
  private final String id;

  @Getter
  private final TsFileHeader header = new TsFileHeader();

  private final TsRuntime runtime;

  private RandomAccessFile handle;

  private ConcurrentBuffer buffer;

  private boolean closed = false;

  TsFile(String id, TsRuntime runtime) {
    this.id = id;
    this.runtime = runtime;

    try {
      handle = new RandomAccessFile(Paths.get(runtime.getDataStorePath(), id).toString(), "rw");
      // position equals to write cursor,
      // and it is saved in file header
      MappedByteBuffer mappedByteBuffer = handle.getChannel().map(FileChannel.MapMode.READ_WRITE,
          0, runtime.getMaxFileSize());
      buffer = new ConcurrentBuffer(runtime, mappedByteBuffer);
    } catch (IOException e) {
      throw new TinyStorageException("cannot open file", e);
    }

    buffer.loadFileHeader(header);
  }

  boolean acceptTimeRange(TimeRange timeRange) {
    return timeRange.accept(header.getStartAt()) || timeRange.accept(header.getEndAt());
  }

  TimeRange getTimeRange() {
    return new TimeRange(header.getStartAt(), header.getEndAt());
  }

  void setStartAt(long startAt) {
    header.setStartAt(startAt);
  }

  long getStartAt() {
    return header.getStartAt();
  }

  @SneakyThrows
  int size() {
    checkFileStatus();
    return buffer.size();
  }

  int append(Record record) throws IOException {
    checkFileStatus();

    int n = buffer.append(record);
    if (n > 0) {
      header.setEndAt(record.getTimestamp());
    } else {
      // when file reaches the size limit, it will be marked readonly,
      // and won't accept data any more.
      header.setReadOnly(true);
      buffer.saveFileHeader(header);
    }

    return n;
  }

  ReadContext createReader(boolean syncWithWriter) throws IOException {
    checkFileStatus();

    return buffer.requireReadCursor(syncWithWriter);
  }

  void flush() {
    buffer.saveFileHeader(header);
    buffer.flush();
  }

  /**
   * Close log file and release resources.
   * @return True if no active reader, else False
   */
  boolean close() throws IOException {
    if (!closed && buffer.isAbleToRelease()) {
      // save header
      buffer.saveFileHeader(header);
      // release resources
      handle.close();
      handle = null;
      buffer = null;
      closed = true;
    }

    return closed;
  }

  boolean reset() {
    if (buffer.isAbleToRelease()) {
      // reset header
      header.setReadOnly(false);
      header.setStartAt(0);
      header.setEndAt(0);
      buffer.saveFileHeader(header);
      // reset buffer position
      buffer.reset();

      closed = false;

      return true;
    }
    return false;
  }

  private void checkFileStatus() throws IOException {
    if (closed) {
      throw new IOException("file has been closed");
    }
  }

  @Override
  public TsFileDesc getDescriptor() {
    return TsFileDesc.builder()
        .id(id)
        .readOnly(header.isReadOnly())
        .startAt(header.getStartAt())
        .endAt(header.getEndAt())
        .build();
  }
}
