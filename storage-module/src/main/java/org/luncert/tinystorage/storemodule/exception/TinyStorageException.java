package org.luncert.tinystorage.storemodule.exception;

public class TinyStorageException extends RuntimeException {

  public TinyStorageException() {
  }

  public TinyStorageException(String message) {
    super(message);
  }

  public TinyStorageException(String message, Throwable t) {
    super(message, t);
  }
}
