package org.luncert.tinystorage.storemodule.physics;

import static org.luncert.tinystorage.storemodule.physics.DataType.LONG;

public class BTreeIndexPage extends BTreePage {

  public BTreeIndexPage(PagePool pagePool, MappedFile mappedFile) {
    super(pagePool, mappedFile);
  }

  @Override
  protected int calcValueSize(int position) {
    // uuid BTreeNode
    return 36;
  }

  // helper field to iterate over array entries, lazy loading.
  @Override
  public BTreeNode nextOf(int child) {
    String pageId = readString(getOffset(child));
    return (BTreePage) pagePool.load(pageId);
  }

  @Override
  public void put(int child, long key, String next) {
    int offset = getOffset(child);
    writeLong(offset, key);
    writeString(offset + LONG.getSize(), next);
  }
}
