package org.luncert.tinystorage.storemodule.physics;

import static org.luncert.tinystorage.storemodule.physics.DataType.INT;
import static org.luncert.tinystorage.storemodule.physics.DataType.LONG;

import java.util.UUID;

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
  public void set(int child, long key, Object valueOrNext) {
    int offset = getOffset(child);
    writeLong(offset, key);
    writeString(offset + LONG.getSize(), ((BTreeNode) valueOrNext).id());
  }

  @Override
  public void add(long key, Object valueOrNext) {
    int offset = getOffset(m());
    writeLong(offset, key);
    writeString(offset + LONG.getSize(), ((BTreeNode) valueOrNext).id());
  }

  @Override
  public void add(int child, long key, Object valueOrNext) {
    for (int i = m(); i > child; i--) {
      moveChild(i - 1, i);
    }
    set(child, key, valueOrNext);
    m++;
  }

  @Override
  public void remove(int child) {
    for (int i = child, limit = m(); i < limit; i++) {
      moveChild(i + 1, i);
    }
    m--;
  }

  @Override
  public BTreeNode split() {
    BTreeNode t = new BTreeIndexPage(pagePool, File.open(UUID.randomUUID().toString()));
    int half = m = M / 2;
    for (int j = 0; j < half; j++) {
      t.add(keyOf(half + j), nextOf(half + j));
    }
    return t;
  }

  protected void moveChild(int child, int to) {
    int src = getOffset(child);
    int tgt = getOffset(to);
    for (int i = 0, limit = LONG.getSize() + INT.getSize() + 36; i < limit; i++) {
      buffer.put(tgt + i, buffer.get(src + i));
    }
  }
}
