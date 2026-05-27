package com.dawood.peeng.monitor.enums;

public enum MonitorType {
  HTTP, // website/api monitoring
  PORT, // TCP port monitoring
  HEARTBEAT, // cron/worker monitoring,
  PING, // ICMP monitoring
}
