package org.luncert.tinystorage.storemodule;

import java.nio.charset.Charset;

public interface ReadOperator {

  long readLong();

  int readInt();

  String readString();

  String readString(Charset charset);

  byte[] readByteArray();

  void readBytes(byte[] buf);

  byte readByte();
}
