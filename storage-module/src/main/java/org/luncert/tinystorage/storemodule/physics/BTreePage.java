package org.luncert.tinystorage.storemodule.physics;

public class BTreePage extends Page implements BTreePageIndexer.Node {

  private int m;

  public BTreePage(String id) {
    super(id);
  }

  /**
   * Children size.
   */
  @Override
  public int m() {
    return m;
  }

  @Override
  public Comparable<?> keyOf(int child) {
    return null;
  }

  @Override
  public Object valOf(int child) {
    return null;
  }

  // helper field to iterate over array entries, lazy loading.
  @Override
  public BTreePageIndexer.Node nextOf(int child) {
    return null;
  }
}
