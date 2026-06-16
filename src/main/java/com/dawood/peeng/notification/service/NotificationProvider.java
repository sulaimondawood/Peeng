package com.dawood.peeng.notification.service;

import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.notification.model.NotificationChannelConfig;

public interface NotificationProvider {

    void sendDownAlert(
            Incident incident,
            NotificationChannelConfig config
    );

    void sendRecoveryAlert(
            Incident incident,
            NotificationChannelConfig config
    );

}
