package org.luncert.tinystorage.storemodule.physics;

import lombok.Getter;

@Getter
public abstract class Page extends File {

    private final PageType type;

    public Page(String id, PageType type) {
        super(id);
        this.type = type;
    }

    public enum PageType {
        INDEX,
        DATA
    }
}
