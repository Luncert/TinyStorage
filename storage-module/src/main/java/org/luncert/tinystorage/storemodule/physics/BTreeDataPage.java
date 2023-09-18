package org.luncert.tinystorage.storemodule.physics;

public class BTreeDataPage extends BTreePage {

  public BTreeDataPage(PagePool pagePool, MappedFile mappedFile) {
    super(pagePool, mappedFile);
  }

  @Override
  protected int calcValueSize(int position) {
    return 0;
  }

  @Override
  public Object valOf(int child) {
    // TODO: deserialize
    return super.valOf(child);
  }

  @Override
  public void put(int child, long key, Object value) {
    // TODO: serialize
  }
}
