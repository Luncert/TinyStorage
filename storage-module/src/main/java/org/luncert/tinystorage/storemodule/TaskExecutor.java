package org.luncert.tinystorage.storemodule;

@FunctionalInterface
public interface TaskExecutor {

  void submit(Runnable task);
}
