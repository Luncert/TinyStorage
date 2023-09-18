package org.luncert.tinystorage.storemodule.physics;

public class BTreeIndexPage extends BTreePage {

  public BTreeIndexPage(PagePool pagePool, MappedFile mappedFile) {
    super(pagePool, mappedFile);
  }

  @Override
  protected int calcValueSize(int position) {
    return 0;
  }
}
