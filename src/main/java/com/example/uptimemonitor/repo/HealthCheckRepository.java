package com.example.uptimemonitor.repo;

import com.example.uptimemonitor.domain.HealthCheck;
import com.example.uptimemonitor.domain.MonitoredApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {

  List<HealthCheck> findByAppIdAndCheckedAtBetweenOrderByCheckedAtAsc(Long appId, Instant from, Instant to);

  Optional<HealthCheck> findTopByAppIdAndCheckedAtLessThanOrderByCheckedAtDesc(Long appId, Instant before);

  @Query("select hc.errorType, count(hc) from HealthCheck hc " +
      "where hc.app.id = :appId and hc.checkedAt between :from and :to and hc.status = com.example.uptimemonitor.domain.CheckStatus.DOWN " +
      "group by hc.errorType")
  List<Object[]> countDownByErrorType(@Param("appId") Long appId, @Param("from") Instant from, @Param("to") Instant to);
}
