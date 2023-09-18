package org.luncert.tinystorage.storemodule.physics;

import lombok.Getter;

@Getter
public abstract class Page extends File {

    protected final PagePool pagePool;

    public Page(PagePool pagePool, MappedFile mappedFile, DataType... metadataDataTypes) {
        super(mappedFile, 1, metadataDataTypes);
        this.pagePool = pagePool;
    }
}
