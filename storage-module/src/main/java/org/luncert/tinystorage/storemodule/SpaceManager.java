package org.luncert.tinystorage.storemodule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.luncert.tinystorage.storemodule.descriptor.DescribedObject;
import org.luncert.tinystorage.storemodule.descriptor.SpaceManagerDesc;
import org.luncert.tinystorage.storemodule.exception.ResourceExhaustedException;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.UUID;

public class SpaceManager implements DescribedObject<SpaceManagerDesc> {

  private final TsRuntime runtime;

  private long used;

  private final PriorityQueue<TsBucket> priorityQueue = new PriorityQueue<>(
      Comparator.comparingLong(TsBucket::getStartAt));
  private final Map<String, TsFile> fileMap = new ConcurrentHashMap<>();

  SpaceManager(TsRuntime runtime) {
    this.runtime = runtime;
    runtime.setSpaceManager(this);
  }

  TsBucket createBucket(String id) {
    TsBucket bucket = new TsBucket(id, runtime);
    priorityQueue.add(bucket);
    return bucket;
  }

  synchronized TsFile createFile() {
    TsFile tsFile = allocNewFile();
    fileMap.put(tsFile.getId(), tsFile);
    return tsFile;
  }

  Optional<TsFile> getFile(String id) {
    return Optional.ofNullable(fileMap.get(id));
  }

  private TsFile allocNewFile() {
    if (runtime.getMaxFileSize() > runtime.getMaxStoreSize() - used) {
      // recycle file
      for (TsBucket bucket : priorityQueue) {
        Optional<TsFile> optional = bucket.recycleFile(true);
        if (optional.isPresent()) {
          // update bucket priority
          priorityQueue.remove(bucket);
          priorityQueue.add(bucket);
          return optional.get();
        }
      }

      // build dump message
      StringBuilder builder = new StringBuilder("cannot recycle disk resource, "
          + "used: " + used + "B " + (100D * used / runtime.getMaxStoreSize()) + "%, "
          + "buckets: " + priorityQueue.size() + "\n\t");
      for (TsBucket bucket : priorityQueue) {
        builder.append("\n\t").append(bucket.getId())
            .append(" size: ").append(bucket.size())
            .append(" file num: ").append(bucket.fileNum());
      }
      throw new ResourceExhaustedException(builder.toString());
    }

    used += runtime.getMaxFileSize();
    return new TsFile(UUID.randomUUID().toString(), runtime);
  }

  @Override
  public SpaceManagerDesc getDescriptor() {
    return new SpaceManagerDesc(used);
  }
}
