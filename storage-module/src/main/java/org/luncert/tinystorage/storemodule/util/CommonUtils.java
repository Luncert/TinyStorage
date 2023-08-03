package org.luncert.tinystorage.storemodule.util;

import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

public class CommonUtils {
  
  private CommonUtils() {
  }
  
  private static final Pattern TIME_DURATION_PATTERN = Pattern
      .compile("^[1-9][0-9]*[smhd]$");
  
  public static long toByteCount(String size) {
    if (size == null || size.length() == 0) {
      throw new IllegalArgumentException("empty size string: " + size);
    }
    
    int i = 0;
    while (i < size.length()) {
      if (!Character.isDigit(size.charAt(i))) {
        break;
      }
      i++;
    }
    
    if (i == 0 || i == size.length()) {
      throw new IllegalArgumentException("invalid size string: " + size);
    }
    
    long value = Long.parseLong(size.substring(0, i));
    String unit = size.substring(i).toUpperCase();
    switch (unit) {
      case "EB":
        value *= FileUtils.ONE_EB;
        break;
      case "PB":
        value *= FileUtils.ONE_PB;
        break;
      case "TB":
        value *= FileUtils.ONE_TB;
        break;
      case "GB":
        value *= FileUtils.ONE_GB;
        break;
      case "MB":
        value *= FileUtils.ONE_MB;
        break;
      case "KB":
        value *= FileUtils.ONE_KB;
        break;
      case "B":
        break;
      default:
        throw new IllegalArgumentException("invalid size unit in: " + size);
    }
    
    return value;
  }
  
  /**
   * Parse time duration.
   * @param raw time string
   * @return milliseconds
   */
  public static long parseTimeDuration(String raw) {
    if (!TIME_DURATION_PATTERN.matcher(raw).matches()) {
      throw new IllegalArgumentException("invalid time duration: " + raw);
    }
    
    long v = 0; // milliseconds
    for (char c : raw.toCharArray()) {
      if (48 <= c && c <= 57) {
        v = v * 10 + c - 48;
      } else {
        switch (c) {
          case 'd':
            v *= 24 * 60 * 60 * 1000;
            break;
          case 'h':
            v *= 60 * 60 * 1000;
            break;
          case 'm':
            v *= 60 * 1000;
            break;
          case 's':
            v *= 1000;
            break;
          default:
            throw new IllegalArgumentException("invalid time unit: " + c);
        }
      }
    }
    
    return v;
  }
}
