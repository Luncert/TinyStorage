package org.luncert.tinystorage.storemodule.physics;

public abstract class BTreePage extends Page implements BTreeNode {

  private final int m;
  private final int[] childOffsets;

  public BTreePage(PagePool pagePool, MappedFile mappedFile) {
    super(pagePool, mappedFile, DataType.INT, DataType.BOOLEAN);
    m = getMetadata(0);
    childOffsets = new int[m];
    childOffsets[0] = headerSize;
  }

  /**
   * Children size.
   */
  @Override
  public int m() {
    return m;
  }

  @Override
  public long keyOf(int child) {
    return readLong(getOffset(child));
  }

  private int getOffset(int child) {
    if (childOffsets[child] > 0) {
      return childOffsets[child];
    }

    int prevChildOffset = getOffset(child - 1);
    childOffsets[child] = prevChildOffset + calcValueSize(prevChildOffset);
    return childOffsets[child];
  }

  protected abstract int calcValueSize(int position);

  @Override
  public Object valOf(int child) {
    if (this instanceof BTreeIndexPage) {
      throw new UnsupportedOperationException();
    }
    // TODO: deserialize
    return null;
  }

  // helper field to iterate over array entries, lazy loading.
  @Override
  public BTreeNode nextOf(int child) {
    if (this instanceof BTreeDataPage) {
      throw new UnsupportedOperationException();
    }
    String pageId = readString(getOffset(child));
    return (BTreePage) pagePool.load(pageId);
  }
}
