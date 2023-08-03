package org.luncert.tinystorage.storemodule;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.luncert.tinystorage.storemodule.descriptor.DescribedObject;
import org.luncert.tinystorage.storemodule.descriptor.TsBucketDesc;
import org.luncert.tinystorage.storemodule.descriptor.TsCacheDesc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class TsCache implements DescribedObject<TsCacheDesc> {

  // TODO: listen on expire event

  // bucketId (logSourceId) -> LogBucket
  private final LoadingCache<String, TsBucket> cache;

  // TODO remove bucket from space manager after expiration
  TsCache(TsRuntime runtime) {
    cache = CacheBuilder.newBuilder()
        .expireAfterAccess(7, TimeUnit.DAYS)
        .build(CacheLoader.from(bucketId -> runtime.getSpaceManager().createBucket(bucketId)));
  }

  TsBucket getBucket(String bucketId) {
    return cache.getUnchecked(bucketId);
  }

  @Override
  public TsCacheDesc getDescriptor() {
    Map<String, TsBucketDesc> buckets = new HashMap<>();
    for (Map.Entry<String, TsBucket> entry : cache.asMap().entrySet()) {
      buckets.put(entry.getKey(), entry.getValue().getDescriptor());
    }
    return new TsCacheDesc(buckets);
  }
}
