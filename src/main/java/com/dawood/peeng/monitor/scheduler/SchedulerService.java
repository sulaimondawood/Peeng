package com.dawood.peeng.monitor.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

  @Scheduled(fixedRate = 10000)
  public void scheduleChecks() {

  }

}
