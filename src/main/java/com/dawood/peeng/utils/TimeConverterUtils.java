package com.dawood.peeng.utils;

import java.util.concurrent.TimeUnit;

public class TimeConverterUtils {

  public static long toSeconds(long value, TimeUnit unit) {
    return unit.toSeconds(value);
  }

}
