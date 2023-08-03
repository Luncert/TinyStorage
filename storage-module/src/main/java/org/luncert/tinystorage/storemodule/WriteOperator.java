package org.luncert.tinystorage.storemodule;

import java.nio.charset.Charset;

public interface WriteOperator {

  // little endian

  void appendLong(long value);

  void appendInt(int value);

  void appendString(String str);

  void appendString(String str, Charset charset);

  void appendBytes(byte[] bytes);

  void appendByte(byte b);
}
