package org.luncert.tinystorage.storemodule;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import lombok.SneakyThrows;

class PhysicalFileOperator implements ReadOperator {

  private final InputStream source;
  
  PhysicalFileOperator(InputStream source) {
    this.source = source;
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
    readFully(payload);
    return new String(payload, charset);
  }

  @Override
  @SneakyThrows
  public void readFully(byte[] buf) {
    int n = source.read(buf);
    if (n != buf.length) {
      throw new EOFException(buf.length + " bytes expected, actually read " + n);
    }
  }

  @Override
  @SneakyThrows
  public byte readByte() {
    return rb();
  }

  private byte rb() throws IOException {
    int n = source.read();
    if (n == -1) {
      throw new EOFException();
    }
    return (byte) (n & 0xff);
  }
}
