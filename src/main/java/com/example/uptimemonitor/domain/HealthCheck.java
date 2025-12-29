package com.example.uptimemonitor.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "health_check")
public class HealthCheck {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "app_id")
  private MonitoredApp app;

  @Column(name = "checked_at", nullable = false)
  private Instant checkedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private CheckStatus status;

  @Column(name = "http_status")
  private Integer httpStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "error_type", nullable = false, length = 40)
  private ErrorType errorType;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @Column(name = "response_time_ms")
  private Integer responseTimeMs;

  protected HealthCheck() {}

  public HealthCheck(MonitoredApp app,
                     Instant checkedAt,
                     CheckStatus status,
                     Integer httpStatus,
                     ErrorType errorType,
                     String errorMessage,
                     Integer responseTimeMs) {
    this.app = app;
    this.checkedAt = checkedAt;
    this.status = status;
    this.httpStatus = httpStatus;
    this.errorType = errorType;
    this.errorMessage = errorMessage;
    this.responseTimeMs = responseTimeMs;
  }

  public Long getId() { return id; }
  public MonitoredApp getApp() { return app; }
  public Instant getCheckedAt() { return checkedAt; }
  public CheckStatus getStatus() { return status; }
  public Integer getHttpStatus() { return httpStatus; }
  public ErrorType getErrorType() { return errorType; }
  public String getErrorMessage() { return errorMessage; }
  public Integer getResponseTimeMs() { return responseTimeMs; }
}
