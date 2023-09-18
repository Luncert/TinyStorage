package org.luncert.tinystorage.storemodule.physics;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.luncert.tinystorage.storemodule.config.Configuration;

public abstract class ByteBufferIO {

  protected ByteBuffer buffer;

  public ByteBufferIO() {
  }

  public ByteBufferIO(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  public void readFully(int position, byte[] b) {
    readFully(position, b, 0, b.length);
  }

  public void readFully(int position, byte[] b, int off, int len) {
    buffer.position(position);
    buffer.get(b, off, len);
  }

  public boolean readBoolean(int position) {
    return buffer.get(position) == 0x1;
  }

  public byte readByte(int position) {
    return buffer.get(position);
  }

  public int readUnsignedByte(int position) {
    return buffer.get(position);
  }

  public short readShort(int position) {
    return buffer.getShort(position);
  }

  public int readUnsignedShort(int position) {
    return buffer.getShort(position);
  }

  public char readChar(int position) {
    return buffer.getChar(position);
  }

  public int readInt(int position) {
    return buffer.getInt(position);
  }

  public long readLong(int position) {
    return buffer.getLong(position);
  }

  public float readFloat(int position) {
    return buffer.getFloat(position);
  }

  public double readDouble(int position) {
    return buffer.getDouble(position);
  }

  public String readString(int position) {
    int size = readInt(position);
    byte[] buf = new byte[size];
    readFully(position + 4, buf, 0, size);
    return new String(buf, Configuration.get().getCharset());
  }

  public String readUTF(int position) {
    int size = readInt(position);
    byte[] buf = new byte[size];
    readFully(position + 4, buf, 0, size);
    return new String(buf, StandardCharsets.UTF_8);
  }

  public void write(int position, int b) {
    buffer.putInt(position, b);
  }

  public void write(int position, byte[] b) {
    write(position, b, 0, b.length);
  }

  public void write(int position, byte[] b, int off, int len) {
    buffer.putInt(len);
    buffer.position(position + 4);
    buffer.put(b, off, len);
  }

  public void writeBoolean(int position, boolean v) {
    buffer.put(position, (byte) (v ? 0x1 : 0x0));
  }

  public void writeByte(int position, int v) {
    buffer.put(position, (byte) v);
  }

  public void writeShort(int position, int v) {
    buffer.putShort(position, (short) v);
  }

  public void writeChar(int position, int v) {
    buffer.putChar(position, (char) v);
  }

  public void writeInt(int position, int v) {
    buffer.putInt(position, v);
  }

  public void writeLong(int position, long v) {
    buffer.putLong(position, v);
  }

  public void writeFloat(int position, float v) {
    buffer.putFloat(position, v);
  }

  public void writeDouble(int position, double v) {
    buffer.putDouble(position, v);
  }

  public void writeString(int position, String s) {
    write(position, s.getBytes(Configuration.get().getCharset()));
  }

  public void writeUTF(int position, String s) {
    write(position, s.getBytes(StandardCharsets.UTF_8));
  }
}
