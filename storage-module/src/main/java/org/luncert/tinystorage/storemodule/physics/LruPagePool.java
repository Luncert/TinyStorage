package org.luncert.tinystorage.storemodule.physics;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

public class LruPagePool extends AbstractPagePool {

  private final LruCache cache;

  public LruPagePool(int capacity) {
    this.cache = new LruCache(capacity);
  }

  private static class LruCache extends LinkedHashMap<String, SoftReference<Page>> {

    private final int capacity;

    public LruCache(int capacity) {
      super(capacity, 0.75f, true);
      this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, SoftReference<Page>> eldest) {
      return size() > capacity;
    }
  }

  @Override
  public Page load(String id) {
    SoftReference<Page> ref = cache.compute(id, (k, v) -> {
      if (v == null || v.get() == null) {
        return new SoftReference<>(load(id));
      }
      return v;
    });
    return ref.get();
  }
}
