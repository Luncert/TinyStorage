package org.luncert.tinystorage.storemodule.exception;

public class InvalidConfigException extends RuntimeException {

  public InvalidConfigException(String message) {
    super(message);
  }

  public InvalidConfigException(Throwable t) {
    super(t);
  }
}
