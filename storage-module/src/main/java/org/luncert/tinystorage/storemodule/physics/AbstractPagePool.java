package org.luncert.tinystorage.storemodule.physics;

public abstract class AbstractPagePool implements PagePool {

  protected Page loadPage(String id) {
    Page page = new Page(id);
  }
}
