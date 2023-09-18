package org.luncert.tinystorage.storemodule.physics;

import java.util.NoSuchElementException;

public abstract class MetadataAccessor extends ByteBufferIO {

  protected final int metadataOffset;
  protected final int headerSize;
  private final int[] dataOffsets;
  private final DataType[] dataTypes;

  public MetadataAccessor(DataType... metadataDataTypes) {
    this(0, metadataDataTypes);
  }

  public MetadataAccessor(int metadataOffset, DataType... metadataDataTypes) {
    this.metadataOffset = metadataOffset;
    int fieldAmount = metadataDataTypes.length;
    dataTypes = metadataDataTypes;
    dataOffsets = new int[fieldAmount];

    dataOffsets[0] = metadataOffset;
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
