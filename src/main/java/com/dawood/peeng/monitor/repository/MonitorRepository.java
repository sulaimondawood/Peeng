package com.dawood.peeng.monitor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.monitor.models.Monitor;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {

  List<Monitor> findAllByActiveTrueAndNextCheckAtBefore(LocalDateTime time);

}
