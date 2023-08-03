package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.descriptor.DescribedObject;
import org.luncert.tinystorage.storemodule.descriptor.TsDesc;

import javax.annotation.Nonnull;
import org.reactivestreams.Subscriber;

public interface TinyStorage extends DescribedObject<TsDesc> {

  void append(String bucketId, Record record);

  void subscribe(@Nonnull String bucketId,
                 @Nonnull TimeRange timeRange,
                 @Nonnull Subscriber<? extends Record> consumer);

  void readStorePhysicalFile(String fileName, Subscriber<String> consumer);
}
