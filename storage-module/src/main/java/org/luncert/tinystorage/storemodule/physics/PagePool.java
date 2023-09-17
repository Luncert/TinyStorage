package org.luncert.tinystorage.storemodule.physics;

import java.util.Optional;

public interface PagePool {

    Optional<Page> load(String id);
}
