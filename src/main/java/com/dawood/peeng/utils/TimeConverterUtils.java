package com.dawood.peeng.utils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TimeConverterUtils {

  public static long toSeconds(String value, TimeUnit unit) {

    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Interval cannot be empty");
    }

  }

}
