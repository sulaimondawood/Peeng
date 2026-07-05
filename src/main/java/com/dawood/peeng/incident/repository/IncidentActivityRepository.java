package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.models.IncidentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IncidentActivityRepository extends JpaRepository<IncidentActivity, UUID> {
}
