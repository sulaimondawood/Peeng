package com.dawood.peeng.notification.respository;

import com.dawood.peeng.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepositiory  extends JpaRepository<Notification, UUID> {
}
