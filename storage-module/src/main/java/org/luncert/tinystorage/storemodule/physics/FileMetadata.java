package org.luncert.tinystorage.storemodule.physics;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

public abstract class FileMetadata extends FileIO {

  protected final int headerSize;
  private final int[] dataOffsets;
  private final DataType[] dataTypes;

  public FileMetadata(ByteBuffer buffer, DataType... metadataDataTypes) {
    super(buffer);
    int fieldAmount = metadataDataTypes.length;
    dataTypes = metadataDataTypes;
    dataOffsets = new int[fieldAmount];

    dataOffsets[0] = 0;
    for (int i = 1; i < fieldAmount; i++) {
      dataOffsets[i] = dataOffsets[i - 1] + metadataDataTypes[i - 1].getSize();
    }
    headerSize = dataOffsets[fieldAmount - 1] + metadataDataTypes[fieldAmount - 1].getSize();
  }

  private void checkIndex(int dataId) {
    if (dataId < 0 || dataId >= dataOffsets.length) {
      throw new NoSuchElementException();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getMetadata(int dataId) {
    checkIndex(dataId);
    return (T) dataTypes[dataId].getReader().read(buffer, dataOffsets[dataId]);
  }

  public void setMetadata(int dataId, boolean data) {
    checkIndex(dataId);
    dataTypes[dataId].getWriter().write(buffer, dataOffsets[dataId], data);
  }

  public void setMetadata(int dataId, char data) {
    checkIndex(dataId);
    dataTypes[dataId].getWriter().write(buffer, dataOffsets[dataId], data);
  }

  public void setMetadata(int dataId, byte data) {
    checkIndex(dataId);
    dataTypes[dataId].getWriter().write(buffer, dataOffsets[dataId], data);
  }

  public void setMetadata(int dataId, short data) {
    checkIndex(dataId);
    dataTypes[dataId].getWriter().write(buffer, dataOffsets[dataId], data);
  }

  public void setMetadata(int dataId, int data) {
    checkIndex(dataId);
    dataTypes[dataId].getWriter().write(buffer, dataOffsets[dataId], data);
  }

  public void setMetadata(int dataId, long data) {
    checkIndex(dataId);
    dataTypes[dataId].getWriter().write(buffer, dataOffsets[dataId], data);
  }
}
