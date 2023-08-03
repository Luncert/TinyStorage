package org.luncert.tinystorage.storemodule;

import java.util.function.Predicate;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TimeRange {
  
  public enum TimeRangeType {
    /**
     * All unset.
     */
    UNSET,
  
    /**
     * Start is a past timestamp and end is unset.
     */
    PAST_START,
  
    /**
     * Start is a future timestamp and end is unset.
     */
    FUTURE_START,
  
    /**
     * Start is unset and end is a past timestamp.
     */
    PAST_END,
  
    /**
     * Start is unset and end is a future timestamp.
     */
    FUTURE_END,
  
    /**
     * All have value and are past timestamp.
     */
    PAST,
  
    /**
     * All have value, start is past timestamp and end is future timestamp.
     */
    CROSS,
  
    /**
     * All have value and are future timestamp.
     */
    FUTURE,
  }
  
  public static final long UNSET_VALUE = -1;
  
  public static final TimeRange UNSET = new TimeRange(UNSET_VALUE, UNSET_VALUE);
  
  public static boolean isValid(long timestamp) {
    return timestamp != UNSET_VALUE;
  }
  
  /**
   * Inclusive.
   */
  private final long start;
  
  /**
   * Inclusive.
   */
  private final long end;
  
  private final Predicate<Long> predicate;
  
  private final TimeRangeType type;
  
  public static TimeRange of(long start, long end) {
    return new TimeRange(start, end);
  }
  
  public TimeRange(long start, long end) {
    this.start = start;
    this.end = end;
  
    long now = System.currentTimeMillis();
    
    if (isValid(start)) {
      if (isValid(end)) {
        if (start >= end) {
          throw new IllegalArgumentException("start should be smaller than end");
        }
        
        predicate = timestamp -> (start <= timestamp && timestamp <= end);
        
        if (start <= now) {
          if (end <= now) {
            type = TimeRangeType.PAST;
          } else {
            type = TimeRangeType.CROSS;
          }
        } else {
          type = TimeRangeType.FUTURE;
        }
      } else {
        predicate = timestamp -> start <= timestamp;
        
        if (start <= now) {
          type = TimeRangeType.PAST_START;
        } else {
          type = TimeRangeType.FUTURE_START;
        }
      }
    } else {
      if (isValid(end)) {
        predicate = timestamp -> timestamp <= end;
  
        if (end <= now) {
          type = TimeRangeType.PAST_END;
        } else {
          type = TimeRangeType.FUTURE_END;
        }
      } else {
        predicate = timestamp -> true;
        
        type = TimeRangeType.UNSET;
      }
    }
  }
  
  public boolean accept(long timestamp) {
    return predicate.test(timestamp);
  }

  public boolean isUnset() {
    return this == UNSET;
  }
}
