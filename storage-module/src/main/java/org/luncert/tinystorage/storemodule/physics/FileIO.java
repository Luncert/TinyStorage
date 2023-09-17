package org.luncert.tinystorage.storemodule.physics;

import org.luncert.tinystorage.storemodule.config.Configuration;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class FileIO implements DataOutput, DataInput {

  private final ByteBuffer buffer;
  private int readOffset = 0;

  public FileIO(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public void setReadOffset(int offset) {
    readOffset = offset;
  }

  public void setWriteOffset(int offset) {
    buffer.position(offset);
  }

  @Override
  public void readFully(byte[] b) throws IOException {
    readFully(b, 0, b.length);
  }

  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    requireBytes(len);
    buffer.get(b, off, len);
  }

  @Override
  public int skipBytes(int n) throws IOException {
    int old = readOffset;
    readOffset = Math.min(readOffset + n, buffer.position());
    return readOffset - old;
  }

  @Override
  public boolean readBoolean() throws IOException {
    requireBytes(1);
    return buffer.get(readOffset++) == 0x1;
  }

  @Override
  public byte readByte() throws IOException {
    requireBytes(1);
    return buffer.get(readOffset++);
  }

  @Override
  public int readUnsignedByte() throws IOException {
    requireBytes(1);
    return buffer.get(readOffset++);
  }

  @Override
  public short readShort() throws IOException {
    requireBytes(2);
    return buffer.getShort(readOffset++);
  }

  @Override
  public int readUnsignedShort() throws IOException {
    requireBytes(2);
    return buffer.getShort(readOffset++);
  }

  @Override
  public char readChar() throws IOException {
    requireBytes(1);
    return buffer.getChar(readOffset++);
  }

  @Override
  public int readInt() throws IOException {
    requireBytes(4);
    return buffer.getInt(readOffset++);
  }

  @Override
  public long readLong() throws IOException {
    requireBytes(8);
    return buffer.getLong(readOffset++);
  }

  @Override
  public float readFloat() throws IOException {
    requireBytes(4);
    return buffer.getFloat(readOffset++);
  }

  @Override
  public double readDouble() throws IOException {
    requireBytes(8);
    return buffer.getDouble(readOffset++);
  }

  @Override
  public String readLine() throws IOException {
    throw new UnsupportedOperationException();
  }

  public String readString() throws IOException {
    int size = readInt();
    byte[] buf = new byte[size];
    readFully(buf, 0, size);
    return new String(buf, Configuration.get().getCharset());
  }

  @Override
  public String readUTF() throws IOException {
    int size = readInt();
    byte[] buf = new byte[size];
    readFully(buf, 0, size);
    return new String(buf, StandardCharsets.UTF_8);
  }

  private void requireBytes(final int n) throws EOFException {
    int readableCount = buffer.position() - readOffset;
    if (readableCount < n) {
      throw new EOFException();
    }
  }

  @Override
  public void write(int b) throws IOException {
    buffer.putInt(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    buffer.putInt(len);
    buffer.put(b, off, len);
  }

  @Override
  public void writeBoolean(boolean v) throws IOException {
    buffer.put((byte) (v ? 0x1 : 0x0));
  }

  @Override
  public void writeByte(int v) throws IOException {
    buffer.put((byte) v);
  }

  @Override
  public void writeShort(int v) throws IOException {
    buffer.putShort((short) v);
  }

  @Override
  public void writeChar(int v) throws IOException {
    buffer.putChar((char) v);
  }

  @Override
  public void writeInt(int v) throws IOException {
    buffer.putInt(v);
  }

  @Override
  public void writeLong(long v) throws IOException {
    buffer.putLong(v);
  }

  @Override
  public void writeFloat(float v) throws IOException {
    buffer.putFloat(v);
  }

  @Override
  public void writeDouble(double v) throws IOException {
    buffer.putDouble(v);
  }

  @Override
  public void writeBytes(String s) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeChars(String s) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void writeString(String s) throws IOException {
    write(s.getBytes(Configuration.get().getCharset()));
  }

  @Override
  public void writeUTF(String s) throws IOException {
    write(s.getBytes(StandardCharsets.UTF_8));
  }
}
