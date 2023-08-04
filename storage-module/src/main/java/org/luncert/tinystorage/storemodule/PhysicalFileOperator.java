package org.luncert.tinystorage.storemodule;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import lombok.Getter;
import lombok.SneakyThrows;
import org.luncert.tinystorage.storemodule.common.Utils;

class PhysicalFileOperator implements ReadOperator {

  private final InputStream source;
  @Getter
  private final TsFileHeader fileHeader = new TsFileHeader();
  @Getter
  private int bufferPos = Integer.MAX_VALUE;
  private int pos;
  
  PhysicalFileOperator(InputStream source) {
    this.source = source;
    fileHeader.setReadOnly((readByte() & 0x1) == 1);
    fileHeader.setStartAt(Utils.byteArrayToLong(i -> readByte(), 8));
    fileHeader.setEndAt(Utils.byteArrayToLong(i -> readByte(), 8));
    bufferPos = Utils.byteArrayToInt(i -> readByte(), 4);
  }
  
  @Override
  @SneakyThrows
  public long readLong() {
    long low = readInt();
    long high = readInt();
    return (high << 32) + (4294967295L & low);
  }

  @Override
  @SneakyThrows
  public int readInt() {
    int value = rb() & 255;
    value += (rb() & 255) << 8;
    value += (rb() & 255) << 16;
    value += (rb() & 255) << 24;
    return value;
  }

  @Override
  @SneakyThrows
  public String readString() {
    return readString(Charset.defaultCharset());
  }

  @Override
  @SneakyThrows
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

  @Override
  @SneakyThrows
  public void readBytes(byte[] buf) {
    int n = source.read(buf);
    if (n != buf.length) {
      throw new EOFException(buf.length + " bytes expected, actually read " + n);
    }
    pos += n;
  }

  @Override
  @SneakyThrows
  public byte readByte() {
    return rb();
  }

  private byte rb() throws IOException {
    if (pos >= bufferPos) {
      throw new EOFException();
    }
    int n = source.read();
    if (n == -1) {
      throw new EOFException();
    }
    pos++;
    return (byte) (n & 0xff);
  }
}
