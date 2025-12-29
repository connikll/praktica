package com.example.uptimemonitor.service;

import com.example.uptimemonitor.domain.HealthCheck;
import com.example.uptimemonitor.domain.MonitoredApp;
import com.example.uptimemonitor.repo.HealthCheckRepository;
import com.example.uptimemonitor.repo.MonitoredAppRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PollingService {

  private final MonitoredAppRepository apps;
  private final HealthCheckRepository checks;
  private final HealthClient client;

  public PollingService(MonitoredAppRepository apps, HealthCheckRepository checks, HealthClient client) {
    this.apps = apps;
    this.checks = checks;
    this.client = client;
  }

  /**
   * Requirement: "Сбор метрик доступности должен осуществляться периодически по cron".
   */
  @Scheduled(cron = "${monitor.cron:*/30 * * * * *}")
  public void pollAll() {
    List<MonitoredApp> enabled = apps.findAllByEnabledTrue();
    for (MonitoredApp app : enabled) {
      HealthCheck hc = client.check(app);
      checks.save(hc);
    }
  }
}
