package org.luncert.tinystorage.storemodule.physics;

// internal nodes: only use key and next
// external nodes: only use key and value
public interface BTreeNode {

  int m();

  long keyOf(int child);

  Object valOf(int child);

  BTreeNode nextOf(int child);
}
