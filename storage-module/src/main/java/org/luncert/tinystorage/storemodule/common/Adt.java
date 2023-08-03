package org.luncert.tinystorage.storemodule.common;

/**
 * Adt.
 */
public interface Adt<K, V> {

  void add(K key, V value);

  void update(K oldKey, K newKey);

  V delete(K key);

  V delete(int i);

  V deleteMin();

  V deleteMax();

  V getMinimum();

  V getMaximum();

  int size();

  void clear();
}