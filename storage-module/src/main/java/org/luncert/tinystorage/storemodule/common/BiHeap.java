package org.luncert.tinystorage.storemodule.common;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * BiHeap.
 */
public class BiHeap<K, V> implements Adt<K, V>, Iterable<V> {

  private static class Node<K, V> {
    K key;
    V value;

    Node(K key, V value) {
      this.key = key;
      this.value = value;
    }
  }

  private static final int DEFAULT_OPACITY = 16;
  private Node<K,V>[] data;
  private int size;
  private final boolean minHeap;
  private final BiFunction<K, K, Integer> compare;

  @SuppressWarnings("unchecked")
  public BiHeap(boolean minHeap, BiFunction<K, K, Integer> compare) {
    this.minHeap = minHeap;
    this.compare = compare;
    data = (Node<K,V>[]) new Node[DEFAULT_OPACITY];
  }

  @SuppressWarnings("unchecked")
  private void adjustCapacity() {
    if (size + 1 == data.length) {
      Node<K,V>[] tmp = new Node[data.length + data.length >>> 1];
      System.arraycopy(data, 0, tmp, 0, size);
      data = tmp;
    }
  }

  private boolean percolatePart(int p, int c) {
    Node<K, V> tmp;
    int r = compare.apply(data[p].key, data[c].key);
    if (minHeap && r > 0 || !minHeap && r < 0) {
      tmp = data[p];
      data[p] = data[c];
      data[c] = tmp;
      return true;
    }
    return false;
  }

  private void percolateDown(int i) {
    int lc = i * 2 + 1;
    int rc = i * 2 + 2;
    if (lc < size && percolatePart(i, lc)) {
      percolateDown(lc);
    }
    if (rc < size && percolatePart(i, rc)) {
      percolateDown(rc);
    }
  }

  private void percolateUp(int i) {
    int parent = (i - 1) / 2;
    while (i > 0 && percolatePart(parent, i)) {
      i = parent;
      parent = (i - 1) / 2;
    }
  }

  public void update(K oldKey, K newKey) {
    Node<K,V> node;
    for (int i = 0; i < size; i++) {
      node = data[i];
      if (node.key == oldKey || node.key.equals(oldKey)) {
        node.key = newKey;
        percolateUp(i);
        percolateDown(i);
      }
    }
  }

  public void add(K key, V value) {
    adjustCapacity();
    data[size] = new Node<>(key, value);
    percolateUp(size++);
  }

  private void sizeCheck() {
    if (size == 0) {
      throw new UnsupportedOperationException("operation on empty heap");
    }
  }

  private int getFirstLeafNode() {
    // 计算前 h - 1 层的容量和最后一层的节点数
    int s1 = (int) (size - (Math.pow(Math.log(size), 2) - 1));
    int l = size - s1;
    // 计算 h - 1 层叶子节点数
    return (l % 2 == 0 ? l / 2 : l / 2 + 1) - 1;
  }

  private int getMinFromLeafNode() {
    int min = getFirstLeafNode();
    for (int i = min + 1; i < size; i++) {
      if (compare.apply(data[min].key, data[i].key) > 0) {
        min = i;
      }
    }
    return min;
  }

  private int getMaxFromLeafNode() {
    int max = getFirstLeafNode();
    for (int i = max + 1; i < size; i++) {
      if (compare.apply(data[max].key, data[i].key) < 0) {
        max = i;
      }
    }
    return max;
  }

  private void remove(int i) {
    data[i] = data[--size];
    data[size] = null;
    percolateDown(i);
  }

  public V delete(int i) {
    if (i < 0 || i >= size) {
      throw new NoSuchElementException("index: " + i);
    }
    V ret = data[i].value;
    remove(i);
    return ret;
  }

  public V delete(K key) {
    sizeCheck();
    Node<K,V> node;
    for (int i = 0; i < size; i++) {
      node = data[i];
      if (node.key == key || node.key.equals(key)) {
        V ret = node.value;
        remove(i);
        return ret;
      }
    }
    return null;
  }

  public V deleteMin() {
    sizeCheck();
    if (minHeap) {
      return delete(0);
    } else {
      return delete(getMinFromLeafNode());
    }
  }

  public V deleteMax() {
    sizeCheck();
    if (!minHeap) {
      return delete(0);
    } else {
      return delete(getMaxFromLeafNode());
    }
  }

  public V getMinimum() {
    sizeCheck();
    if (minHeap) {
      return data[0].value;
    } else {
      return data[getMinFromLeafNode()].value;
    }
  }

  public V getMaximum() {
    sizeCheck();
    if (!minHeap) {
      return data[0].value;
    } else {
      return data[getMaxFromLeafNode()].value;
    }
  }

  public int size() {
    return size;
  }

  public void clear() {
    data = null;
    size = 0;
  }

  @Override
  public Iterator<V> iterator() {
    return new Itr();
  }

  private class Itr implements Iterator<V> {

    private int cursor = 0;

    @Override
    public boolean hasNext() {
      return cursor < size;
    }

    @Override
    public V next() {
      return data[cursor++].value;
    }
  }

  /**
   * Heap Sort.
   */
  public static <E> void sort(E[] data, boolean useMinHeap, BiFunction<E, E, Integer> compare) {
    Adt<E, E> adt = new BiHeap<>(useMinHeap, compare);

    for (E d : data) {
      adt.add(d, d);
    }

    Supplier<E> supplier = useMinHeap ? adt::deleteMin : adt::deleteMax;
    for (int i = 0; i < data.length; i++) {
      data[i] = supplier.get();
    }
  }
}