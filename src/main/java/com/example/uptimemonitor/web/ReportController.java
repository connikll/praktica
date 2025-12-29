package com.example.uptimemonitor.web;

import com.example.uptimemonitor.domain.ErrorType;
import com.example.uptimemonitor.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apps/{appId}")
public class ReportController {

  private final AvailabilityService availability;

  public ReportController(AvailabilityService availability) {
    this.availability = availability;
  }

  /**
   * API for JSON report:
   * - periods of unavailability for a given time
   * - availability percentage for a given time
   */
  @GetMapping("/report")
  public ReportDto report(
      @PathVariable long appId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
  ) {
    AvailabilityService.AvailabilityReport r = availability.report(appId, from, to);

    List<DowntimeIntervalDto> intervals = r.downtimeIntervals().stream()
        .map(i -> new DowntimeIntervalDto(i.start(), i.end(), i.duration().toSeconds()))
        .toList();

    return new ReportDto(
        r.appId(),
        r.appName(),
        r.from(),
        r.to(),
        round2(r.availabilityPercent()),
        r.total().toSeconds(),
        r.up().toSeconds(),
        r.down().toSeconds(),
        intervals,
        r.errorsSummary()
    );
  }

  private static double round2(double v) {
    return Math.round(v * 100.0) / 100.0;
  }

  public record DowntimeIntervalDto(Instant start, Instant end, long durationSeconds) {}

  public record ReportDto(
      long appId,
      String appName,
      Instant from,
      Instant to,
      double availabilityPercent,
      long totalSeconds,
      long upSeconds,
      long downSeconds,
      List<DowntimeIntervalDto> downtimeIntervals,
      Map<ErrorType, Long> errorsSummary
  ) {}
}
