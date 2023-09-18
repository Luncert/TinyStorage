package org.luncert.tinystorage.storemodule.physics;

public abstract class AbstractPagePool implements PagePool {

  protected Page loadPage(String id) {
    File.MappedFile mappedFile = File.open(id);
    PageType pageType = PageType.valueOf(mappedFile.readByte(0));
    return pageType.getProvider().apply(this, mappedFile);
  }
}
