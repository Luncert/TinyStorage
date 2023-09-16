package org.luncert.tinystorage.storemodule;

import java.io.EOFException;

@FunctionalInterface
public interface TsReader<T> {

  T read(ReadOperator operator) throws EOFException;
}
