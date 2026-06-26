package com.dawood.peeng.monitor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.dawood.peeng.monitor.enums.MonitorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.monitor.models.Monitor;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {

  List<Monitor> findAllByActiveTrueAndNextCheckAtLessThanEqual(LocalDateTime time);

  @Query("""
    SELECT m FROM Monitor m
    WHERE m.tenant.id = :tenantId
    AND (:status IS NULL OR m.status =:status)
    AND (:keyword IS NULL 
    OR LOWER(m.name) like LOWER(CONCAT('%',:keyword,'%'))
    OR LOWER(m.url) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
  Page<Monitor> findAllMonitors(@Param("tenantId") UUID tenantId, @Param("status") MonitorStatus status, @Param("keyword") String keyword, Pageable pageable);

}
