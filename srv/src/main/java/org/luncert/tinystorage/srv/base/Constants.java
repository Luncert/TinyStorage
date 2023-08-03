package org.luncert.tinystorage.srv.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

  public static final String ANONYMOUS_CHANNEL = "anonymous";
  public static final String STREAMING_CHANNEL = "streaming";
  public static final long LOG_TRANSPORT_INTERVAL = 50;
}
