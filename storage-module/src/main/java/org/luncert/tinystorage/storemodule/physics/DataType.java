package org.luncert.tinystorage.storemodule.physics;

import java.nio.ByteBuffer;
import lombok.Getter;

@Getter
public enum DataType {

  BYTE(1, (b, p) -> b.get(b.position()), (b, p, data) -> b.put(p, (Byte) data)),
  CHAR(1, (b, p) -> b.getChar(b.position()), (b, p, data) -> b.putChar(p, (Character) data)),
  SHORT(2, (b, p) -> b.getShort(b.position()), (b, p, data) -> b.putShort(p, (Short) data)),
  INT(4, (b, p) -> b.getInt(b.position()), (b, p, data) -> b.putInt(p, (Integer) data)),
  LONG(8, (b, p) -> b.getLong(b.position()), (b, p, data) -> b.putLong(p, (Long) data)),
  FLOAT(4, (b, p) -> b.getFloat(b.position()), (b, p, data) -> b.putFloat(p, (Float) data)),
  DOUBLE(8, (b, p) -> b.getDouble(b.position()), (b, p, data) -> b.putDouble(p, (Double) data))
  ;

  private final int size;
  private final DataReader reader;
  private final DataWriter writer;

  DataType(int size, DataReader reader, DataWriter writer) {
    this.size = size;
    this.reader = reader;
    this.writer = writer;
  }

  @FunctionalInterface
  public interface DataReader {

    Object read(ByteBuffer buffer, int position);
  }

  @FunctionalInterface
  public interface DataWriter {

    void write(ByteBuffer buffer, int position, Object data);
  }
}
