package org.luncert.tinystorage.storemodule.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestBiHeap {

  @Test
  public void test() {
    Adt<Integer, String> adt = new BiHeap<>(true, Integer::compareTo);
    adt.add(1, "Mike");
    adt.add(2, "Tom");
    adt.add(3, "Tony");
    Assert.assertEquals("Tony", adt.getMaximum());
    Assert.assertEquals("Mike", adt.getMinimum());
    Assert.assertEquals("Mike", adt.deleteMin());
    Assert.assertEquals("Tom", adt.getMinimum());

    adt.add(0, "L");
    Assert.assertEquals("L", adt.getMinimum());

    adt.update(2, 9);
    Assert.assertEquals("Tom", adt.getMaximum());
    adt.update(9, -1);
    Assert.assertEquals("Tom", adt.getMinimum());
  }

  @Test
  public void test1() {
    Integer[] data = {1, 3, 6, 32, 0, 55, 3};
    BiHeap.sort(data, true, Integer::compareTo);
    Assert.assertArrayEquals(new Integer[]{0, 1, 3, 3, 6, 32, 55}, data);
  }
}