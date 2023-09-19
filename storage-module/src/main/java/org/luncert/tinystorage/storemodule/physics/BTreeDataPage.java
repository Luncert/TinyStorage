package org.luncert.tinystorage.storemodule.physics;

// TODO
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
    return super.valOf(child);
  }

  @Override
  public void set(int child, long key, Object valueOrNext) {

  }

  @Override
  public void add(long key, Object valueOrNext) {

  }

  @Override
  public void add(int child, long key, Object valueOrNext) {

  }

  @Override
  public void remove(int child) {

  }

  @Override
  public BTreeNode split() {
    throw new UnsupportedOperationException();
  }
}
