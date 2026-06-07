package com.dawood.peeng.monitor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.monitor.models.MonitorCheck;

public interface MonitorCheckRepository extends JpaRepository<MonitorCheck, UUID> {

}
