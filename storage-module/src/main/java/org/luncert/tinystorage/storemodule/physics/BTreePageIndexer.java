package org.luncert.tinystorage.storemodule.physics;

import lombok.NonNull;

import java.util.UUID;

public class BTreePageIndexer implements PageIndexer {

  // max children per B-tree node = M-1
  // (must be even and greater than 2)
  private static final int M = 4;

  private final PagePool pagePool;

  private BTreeNode root;
  private int height;
  private int n;

  public BTreePageIndexer(PagePool pagePool, String rootPageId) {
    this.pagePool = pagePool;
    this.root = (BTreePage) pagePool.load(rootPageId);
  }

  /**
   * Returns the value associated with the given key.
   *
   * @param key the key
   * @return the value associated with the given key if the key is in the symbol table
   * and {@code null} if the key is not in the symbol table
   * @throws IllegalArgumentException if {@code key} is {@code null}
   */
  public Object get(long key) {
    return search(root, key, height);
  }

  private Object search(BTreeNode x, long key, int ht) {
    // external node
    if (ht == 0) {
      for (int j = 0; j < x.m(); j++) {
        if (eq(key, x.keyOf(j))) {
          return x.valOf(j);
        }
      }
    }
    // internal node
    else {
      for (int j = 0; j < x.m(); j++) {
        if (j + 1 == x.m() || less(key, x.keyOf(j + 1))) {
          return search(x.nextOf(j), key, ht - 1);
        }
      }
    }
    return null;
  }

  /**
   * Inserts the key-value pair into the symbol table, overwriting the old value
   * with the new value if the key is already in the symbol table.
   * If the value is {@code null}, this effectively deletes the key from the symbol table.
   *
   * @param  key the key
   * @param  val the value
   * @throws IllegalArgumentException if {@code key} is {@code null}
   */
  public void put(long key, Object val) {
    BTreeNode u = insert(root, key, val, height);
    n++;
    if (u == null) return;

    // need to split root
    BTreeNode t = new BTreeIndexPage(pagePool, File.open(UUID.randomUUID().toString()));
    t.set(0, root.keyOf(0), root.id());
    t.set(1, u.keyOf(0), u.id());
    root = t;
    height++;
  }

  private BTreeNode insert(BTreeNode h, final long key, final Object val, int ht) {
    int j;

    // external node
    if (ht == 0) {
      for (j = 0; j < h.m(); j++) {
        if (less(key, h.keyOf(j))) break;
      }

      h.add(j, key, val);
    }
    // internal node
    else {
      long k = key;
      String next = null;

      for (j = 0; j < h.m(); j++) {
        if ((j+1 == h.m()) || less(key, h.keyOf(j + 1))) {
          BTreeNode u = insert(h.nextOf(j++), key, val, ht-1);
          if (u == null) return null;
          k = u.keyOf(0);
          next = u.id();
          break;
        }
      }

      h.add(j, k, next);
    }

    return h.m() < M ? null : split(h);
  }

  // split node in half
  private BTreeNode split(BTreeNode h) {
    BTreeNode t = new BTreeIndexPage(pagePool, File.open(UUID.randomUUID().toString()));
    int half = M / 2;
    h.setM(half);
    for (int j = 0; j < half; j++) {
      t.add(h.keyOf(half + j), h.nextOf(half + j));
    }
    return t;
  }

  // comparison functions - make Comparable instead of Key to avoid casts
  @SuppressWarnings({"unchecked", "rawtypes"})
  private boolean less(Comparable k1, Comparable k2) {
    return k1.compareTo(k2) < 0;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private boolean eq(Comparable k1, Comparable k2) {
    return k1.compareTo(k2) == 0;
  }
}
