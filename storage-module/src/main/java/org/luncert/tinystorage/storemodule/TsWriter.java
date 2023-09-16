package org.luncert.tinystorage.storemodule;

@FunctionalInterface
public interface TsWriter<T> {

  void write(T record, WriteOperator operator);
}
