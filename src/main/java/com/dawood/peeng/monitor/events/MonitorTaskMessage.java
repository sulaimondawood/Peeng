package com.dawood.peeng.monitor.events;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MonitorTaskMessage {

    private UUID monitorId;

    private UUID tenantId;

}
