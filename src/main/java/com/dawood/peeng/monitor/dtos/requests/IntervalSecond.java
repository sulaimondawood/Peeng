package com.dawood.peeng.monitor.dtos.requests;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntervalSecond {

  private Long intervalSeconds;

  private TimeUnit unit;

}
