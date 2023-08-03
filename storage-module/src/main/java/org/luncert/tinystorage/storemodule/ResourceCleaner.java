package org.luncert.tinystorage.storemodule;

@FunctionalInterface
public interface ResourceCleaner {
  
  /**
   * When resource is required to close itself,
   * it should run this cleaner to notify all
   * resource holders that it is closed now.
   * Then these holders could do something in
   * the run method.
   * This method should be implemented by resource
   * holders and be invoked by resource itself.
   */
  void run();
}
