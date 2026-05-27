package com.dawood.peeng.monitor;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.monitor.models.Monitor;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {

}
