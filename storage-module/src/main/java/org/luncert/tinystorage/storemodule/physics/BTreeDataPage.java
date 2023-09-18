package org.luncert.tinystorage.storemodule.physics;

public class BTreeDataPage extends BTreePage {

  public BTreeDataPage(PagePool pagePool, MappedFile mappedFile) {
    super(pagePool, mappedFile);
  }

  @Override
  protected int calcValueSize(int position) {
    return 0;
  }
}
