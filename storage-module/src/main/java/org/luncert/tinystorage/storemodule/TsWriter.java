package org.luncert.tinystorage.storemodule;

@FunctionalInterface
public interface TsWriter<T extends Record> {

  void write(T record, WriteOperator operator);
}
