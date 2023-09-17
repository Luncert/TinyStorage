package org.luncert.tinystorage.storemodule.orm;

import org.luncert.tinystorage.storemodule.ReadOperator;
import org.luncert.tinystorage.storemodule.TsReader;
import org.luncert.tinystorage.storemodule.TsWriter;
import org.luncert.tinystorage.storemodule.WriteOperator;

import java.io.EOFException;

public class EntityReaderAndWriter<E> implements TsReader<E>, TsWriter<E> {

    @Override
    public E read(ReadOperator operator) throws EOFException {
        return null;
    }

    @Override
    public void write(E record, WriteOperator operator) {

    }
}
