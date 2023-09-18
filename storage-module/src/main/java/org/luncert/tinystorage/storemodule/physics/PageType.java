package org.luncert.tinystorage.storemodule.physics;

import java.util.function.BiFunction;
import lombok.Getter;

@Getter
public enum PageType {

  BTREE_INDEX_PAGE((byte) 0x0, BTreeIndexPage::new),
  BTREE_DATA_PAGE((byte) 0x1, BTreeDataPage::new)
  ;

  private static final PageType[] PAGE_TYPES = new PageType[]{
      BTREE_INDEX_PAGE
  };
  private final byte typeId;
  private final BiFunction<PagePool, File.MappedFile, Page> provider;

  PageType(byte typeId, BiFunction<PagePool, File.MappedFile, Page> provider) {
    this.typeId = typeId;
    this.provider = provider;
  }

  public static PageType valueOf(byte typeId) {
    if (typeId < 0 || typeId > PAGE_TYPES.length) {
      throw new IllegalArgumentException("invalid type id " + typeId);
    }
    return PAGE_TYPES[typeId];
  }
}
