package com.dawood.peeng.monitor.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dawood.peeng.monitor.models.Monitor;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {

  @Query("""
      select from
      """)
  List<Monitor> findAllActiveMonitorsDueForChecks();

}
