package com.dawood.peeng.notification.respository;

import com.dawood.peeng.notification.model.NotificationChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationChannelConfigRepository extends JpaRepository<NotificationChannelConfig, UUID> {
}
