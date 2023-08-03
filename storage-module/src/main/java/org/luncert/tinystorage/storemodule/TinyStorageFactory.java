package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.exception.InvalidConfigException;

import org.luncert.tinystorage.storemodule.util.CommonUtils;
import java.io.File;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TinyStorageFactory {

  public static TinyStorage createTinyStorage(TsConfig config) {
    Objects.requireNonNull(config.getReader());
    Objects.requireNonNull(config.getWriter());

    File file = new File(config.getDataStorePath());
    // check or init data store path
    if (file.exists()) {
      if (!file.canWrite()) {
        throw new InvalidConfigException("cannot access data store path: " + config.getDataStorePath());
      }
      if (!file.isDirectory()) {
        throw new InvalidConfigException("data store path must be a directory, check: " + config.getDataStorePath());
      }
    } else {
      if (!file.mkdirs()) {
        throw new InvalidConfigException("failed to init data store path with unknown error, check: " + config.getDataStorePath());
      }
    }

    // parse size fields
    long maxStoreSize;
    long maxFileSize;

    try {
      maxStoreSize = CommonUtils.toByteCount(config.getMaxStoreSize());
    } catch (IllegalArgumentException e) {
      throw new InvalidConfigException(e);
    }

    try {
      maxFileSize = CommonUtils.toByteCount(config.getMaxFileSize());
    } catch (IllegalArgumentException e) {
      throw new InvalidConfigException(e);
    }
    if (maxFileSize > Integer.MAX_VALUE) {
      throw new InvalidConfigException("MaxFileSize out of integer range");
    }

    TsRuntime runtime = TsRuntime.builder()
        .dataStorePath(config.getDataStorePath())
        .maxStoreSize(maxStoreSize)
        .maxFileSize((int) maxFileSize)
        .reader(config.getReader())
        .writer(config.getWriter())
        .taskExecutor(config.getTaskExecutor())
        .build();

    new SpaceManager(runtime);

    return new TinyStorageImpl(runtime);
  }
}
