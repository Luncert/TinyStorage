package org.luncert.tinystorage.storemodule.physics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LruPagePool extends LinkedHashMap<String, Page> implements PagePool {

  private final int capacity;

  public LruPagePool(int capacity) {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<String, Page> eldest) {
    return size() > capacity;
  }

  @Override
  public Optional<Page> load(String id) {
    return Optional.ofNullable(get(id));
  }
}
