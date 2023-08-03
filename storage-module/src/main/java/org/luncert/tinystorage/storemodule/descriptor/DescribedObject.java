package org.luncert.tinystorage.storemodule.descriptor;

public interface DescribedObject<T extends Descriptor> {

  T getDescriptor();
}
