package org.luncert.tinystorage.storemodule.orm;

import java.util.HashMap;
import java.util.Map;

public class EntityManager {

    private final Map<Class<?>, EntityReaderAndWriter<?>> readerAndWriterMap = new HashMap<>();
}
