package org.luncert.tinystorage.storemodule.physics;

import java.nio.ByteBuffer;

public abstract class FileMetadata extends FileIO {

  private final int headerSize;
  private final int[] metadataOffsets;

  public FileMetadata(ByteBuffer buffer, int... metadataFieldTypeSizes) {
    super(buffer);
    int fieldAmount = metadataFieldTypeSizes.length;
    metadataOffsets = new int[fieldAmount];

    metadataOffsets[0] = metadataFieldTypeSizes[0];
    for (int i = 1; i < fieldAmount; i++) {
      metadataOffsets[i] = metadataOffsets[i - 1] + metadataFieldTypeSizes[i];
    }
    headerSize = metadataOffsets[fieldAmount - 1] + metadataFieldTypeSizes[fieldAmount - 1];
  }

  @SuppressWarnings("unchecked")
  public <T> T getMetadata(int dataId) {
    return (T) null;
  }

  public void setMetadata(int dataId, String data) {

  }

  public void setMetadata(int dataId, boolean data) {

  }

  public void setMetadata(int dataId, char data) {

  }

  public void setMetadata(int dataId, byte data) {

  }

  public void setMetadata(int dataId, short data) {

  }

  public void setMetadata(int dataId, int data) {

  }

  public void setMetadata(int dataId, long data) {

  }

  public void setMetadata(int dataId, byte[] data) {

  }
}
