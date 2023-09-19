package org.luncert.tinystorage.storemodule.physics;

// internal nodes: only use key and next
// external nodes: only use key and value
public interface BTreeNode {

  // max children per B-tree node = M-1
  // (must be even and greater than 2)
  int M = 4;

  String id();

  int m();

  long keyOf(int child);

  Object valOf(int child);

  BTreeNode nextOf(int child);

  void set(int child, long key, Object valueOrNext);

  void add(long key, Object valueOrNext);

  void add(int child, long key, Object valueOrNext);

  void remove(int child);

  BTreeNode split();
}
