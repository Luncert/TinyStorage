package org.luncert.tinystorage.storemodule.physics;

// internal nodes: only use key and next
// external nodes: only use key and value
public interface BTreeNode {

  String id();

  void setM(int m);

  int m();

  long keyOf(int child);

  Object valOf(int child);

  BTreeNode nextOf(int child);

  void set(int child, long key, Object valueOrNext);

  void add(long key, Object valueOrNext);

  void add(int child, long key, Object valueOrNext);

  void remove(int child);
}
