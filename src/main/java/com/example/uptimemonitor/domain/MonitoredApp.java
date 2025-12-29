package com.example.uptimemonitor.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "monitored_app")
public class MonitoredApp {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "base_url", nullable = false, length = 500)
  private String baseUrl;

  @Column(name = "health_path", nullable = false, length = 200)
  private String healthPath = "/actuator/health";

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  protected MonitoredApp() {}

  public MonitoredApp(String name, String baseUrl, String healthPath, boolean enabled) {
    this.name = name;
    this.baseUrl = baseUrl;
    this.healthPath = (healthPath == null || healthPath.isBlank()) ? "/actuator/health" : healthPath;
    this.enabled = enabled;
    this.createdAt = Instant.now();
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getBaseUrl() { return baseUrl; }
  public String getHealthPath() { return healthPath; }
  public boolean isEnabled() { return enabled; }
  public Instant getCreatedAt() { return createdAt; }

  public void setName(String name) { this.name = name; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public void setHealthPath(String healthPath) { this.healthPath = (healthPath == null || healthPath.isBlank()) ? "/actuator/health" : healthPath; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
