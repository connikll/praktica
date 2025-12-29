package com.example.uptimemonitor.service;

import com.example.uptimemonitor.domain.*;
import com.example.uptimemonitor.repo.HealthCheckRepository;
import com.example.uptimemonitor.repo.MonitoredAppRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class AvailabilityService {

  private final MonitoredAppRepository apps;
  private final HealthCheckRepository checks;

  public AvailabilityService(MonitoredAppRepository apps, HealthCheckRepository checks) {
    this.apps = apps;
    this.checks = checks;
  }

  @Transactional(readOnly = true)
  public AvailabilityReport report(long appId, Instant from, Instant to) {
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("'from' must be before 'to'");
    }

    MonitoredApp app = apps.findById(appId)
        .orElseThrow(() -> new NoSuchElementException("App not found: " + appId));

    List<HealthCheck> window = checks.findByAppIdAndCheckedAtBetweenOrderByCheckedAtAsc(appId, from, to);
    Optional<HealthCheck> prev = checks.findTopByAppIdAndCheckedAtLessThanOrderByCheckedAtDesc(appId, from);

    CheckStatus current = prev.map(HealthCheck::getStatus).orElse(CheckStatus.DOWN);
    Instant cursor = from;

    long upMs = 0;
    long downMs = 0;

    List<DowntimeInterval> intervals = new ArrayList<>();
    Instant downStart = (current == CheckStatus.DOWN) ? from : null;

    for (HealthCheck hc : window) {
      Instant t = hc.getCheckedAt();
      if (t.isBefore(cursor)) continue; // should not happen

      long segmentMs = Duration.between(cursor, t).toMillis();
      if (segmentMs > 0) {
        if (current == CheckStatus.UP) upMs += segmentMs;
        else downMs += segmentMs;
      }

      if (hc.getStatus() != current) {
        // close/open downtime interval
        if (current == CheckStatus.DOWN && downStart != null) {
          intervals.add(new DowntimeInterval(downStart, t));
          downStart = null;
        }
        if (hc.getStatus() == CheckStatus.DOWN) {
          downStart = t;
        }
        current = hc.getStatus();
      }

      cursor = t;
    }

    // tail segment to "to"
    long tailMs = Duration.between(cursor, to).toMillis();
    if (tailMs > 0) {
      if (current == CheckStatus.UP) upMs += tailMs;
      else downMs += tailMs;
    }

    if (current == CheckStatus.DOWN && downStart != null) {
      intervals.add(new DowntimeInterval(downStart, to));
    }

    long totalMs = upMs + downMs;
    double availability = totalMs == 0 ? 0.0 : (upMs * 100.0) / totalMs;

    Map<ErrorType, Long> errors = new EnumMap<>(ErrorType.class);
    for (Object[] row : checks.countDownByErrorType(appId, from, to)) {
      ErrorType type = (ErrorType) row[0];
      Long cnt = (Long) row[1];
      errors.put(type, cnt);
    }

    return new AvailabilityReport(
        app.getId(),
        app.getName(),
        from,
        to,
        availability,
        Duration.ofMillis(totalMs),
        Duration.ofMillis(upMs),
        Duration.ofMillis(downMs),
        intervals,
        errors
    );
  }

  public record DowntimeInterval(Instant start, Instant end) {
    public Duration duration() {
      return Duration.between(start, end);
    }
  }

  public record AvailabilityReport(
      long appId,
      String appName,
      Instant from,
      Instant to,
      double availabilityPercent,
      Duration total,
      Duration up,
      Duration down,
      List<DowntimeInterval> downtimeIntervals,
      Map<ErrorType, Long> errorsSummary
  ) {}
}
