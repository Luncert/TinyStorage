package org.luncert.tinystorage.storemodule.common;

public class Utils {

  @FunctionalInterface
  public interface ByteSupplier {

    byte get(int index);
  }

  @FunctionalInterface
  public interface ByteConsumer {

    void set(int index, byte out);
  }

  public static long byteArrayToLong(ByteSupplier src, int n) {
    long out = 0;

    for (int i = 0; i < n; ++i) {
      int shift = i * 8;
      long bits = (255L & (long) src.get(i)) << shift;
      long mask = 255L << shift;
      out = out & ~mask | bits;
    }

    return out;
  }

  public static void longToByteArray(long src, ByteConsumer dst, int n) {
    for (int i = 0; i < n; ++i) {
      int shift = i * 8;
      dst.set(i, (byte)((int)(255L & src >> shift)));
    }
  }

  public static int byteArrayToInt(ByteSupplier src, int n) {
    int out = 0;

    for (int i = 0; i < n; ++i) {
      int shift = i * 8;
      int bits = (255 & src.get(i)) << shift;
      int mask = 255 << shift;
      out = out & ~mask | bits;
    }

    return out;
  }

  public static void intToByteArray(int src, ByteConsumer dst, int n) {
    for (int i = 0; i < n; ++i) {
      int shift = i * 8;
      dst.set(i, (byte)(255 & src >> shift));
    }
  }
}
