package com.example.uptimemonitor.repo;

import com.example.uptimemonitor.domain.MonitoredApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoredAppRepository extends JpaRepository<MonitoredApp, Long> {
  List<MonitoredApp> findAllByEnabledTrue();
}
