package org.luncert.tinystorage.storemodule.physics;

public abstract class BTreePage extends Page implements BTreeNode {

  private final int[] childOffsets;
  protected int m;

  public BTreePage(PagePool pagePool, MappedFile mappedFile) {
    super(pagePool, mappedFile, DataType.INT, DataType.BOOLEAN);
    m = getMetadata(0);
    childOffsets = new int[m];
    childOffsets[0] = headerSize;
  }

  @Override
  public String id() {
    return getId();
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

  protected int getOffset(int child) {
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
    throw new UnsupportedOperationException();
  }

  @Override
  public BTreeNode nextOf(int child) {
    throw new UnsupportedOperationException();
  }
}
