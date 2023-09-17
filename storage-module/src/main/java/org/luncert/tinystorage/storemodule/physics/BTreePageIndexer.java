package org.luncert.tinystorage.storemodule.physics;

import lombok.NonNull;

import java.util.Optional;

public class BTreePageIndexer<Key extends Comparable<Key>> implements PageIndexer {

  // max children per B-tree node = M-1
  // (must be even and greater than 2)
  private static final int M = 4;

  private final PagePool pagePool;

  private Node root;
  private int height;
  private int n;

  // internal nodes: only use key and next
  // external nodes: only use key and value
  public interface Node {

    int m();

    Comparable<?> keyOf(int child);

    Object valOf(int child);

    Node nextOf(int child);
  }

  public BTreePageIndexer(PagePool pagePool, String rootPageId) {
    this.pagePool = pagePool;
    Optional<Page> opt = pagePool.load(rootPageId);
    assert opt.isPresent();
    this.root = new Node(1);
  }

  /**
   * Returns the value associated with the given key.
   *
   * @param key the key
   * @return the value associated with the given key if the key is in the symbol table
   * and {@code null} if the key is not in the symbol table
   * @throws IllegalArgumentException if {@code key} is {@code null}
   */
  public Page get(@NonNull Key key) {
    return search(root, key, height);
  }

  private Page search(Node x, Key key, int ht) {
    Entry[] children = x.children;

    // external node
    if (ht == 0) {
      for (int j = 0; j < x.m; j++) {
        if (eq(key, children[j].key)) {
          return (Page) children[j].val;
        }
      }
    }
    // internal node
    else {
      for (int j = 0; j < x.m; j++) {
        if (j + 1 == x.m || less(key, children[j + 1].key)) {
          return search(children[j].next, key, ht - 1);
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
  public void put(Key key, Page val) {
    if (key == null) throw new IllegalArgumentException("argument key to put() is null");
    Node u = insert(root, key, val, height);
    n++;
    if (u == null) return;

    // need to split root
    Node t = new Node(2);
    t.children[0] = new Entry(root.children[0].key, null, root);
    t.children[1] = new Entry(u.children[0].key, null, u);
    root = t;
    height++;
  }

  private Node insert(Node h, Key key, Page val, int ht) {
    int j;
    Entry t = new Entry(key, val, null);

    // external node
    if (ht == 0) {
      for (j = 0; j < h.m; j++) {
        if (less(key, h.children[j].key)) break;
      }
    }

    // internal node
    else {
      for (j = 0; j < h.m; j++) {
        if ((j+1 == h.m) || less(key, h.children[j+1].key)) {
          Node u = insert(h.children[j++].next, key, val, ht-1);
          if (u == null) return null;
          t.key = u.children[0].key;
          t.val = null;
          t.next = u;
          break;
        }
      }
    }

    for (int i = h.m; i > j; i--)
      h.children[i] = h.children[i-1];
    h.children[j] = t;
    h.m++;
    if (h.m < M) return null;
    else         return split(h);
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
