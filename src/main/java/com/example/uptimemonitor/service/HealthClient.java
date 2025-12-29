package com.example.uptimemonitor.service;

import com.example.uptimemonitor.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.Exceptions;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class HealthClient {

  private final WebClient webClient;
  private final int timeoutSeconds;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public HealthClient(WebClient webClient, @org.springframework.beans.factory.annotation.Value("${monitor.request-timeout-seconds:5}") int timeoutSeconds) {
    this.webClient = webClient;
    this.timeoutSeconds = timeoutSeconds;
  }

  public HealthCheck check(MonitoredApp app) {
    Instant now = Instant.now();
    long startNs = System.nanoTime();

    String url = normalizeUrl(app.getBaseUrl(), app.getHealthPath());

    try {
      String body = webClient.get()
          .uri(url)
          .accept(MediaType.APPLICATION_JSON)
          .retrieve()
          .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(), resp ->
              resp.bodyToMono(String.class).defaultIfEmpty("")
                  .flatMap(b -> reactor.core.publisher.Mono.error(
                      new WebClientResponseException(resp.statusCode().value(),
                          "HTTP " + resp.statusCode().value(),
                          resp.headers().asHttpHeaders(),
                          b.getBytes(),
                          null)
                  )))
          .bodyToMono(String.class)
          .timeout(Duration.ofSeconds(timeoutSeconds))
          .block();

      int rt = (int) Duration.ofNanos(System.nanoTime() - startNs).toMillis();

      Map<String, Object> json;
      try {
        json = objectMapper.readValue(body, new TypeReference<>() {});
      } catch (Exception parseEx) {
        return new HealthCheck(app, now, CheckStatus.DOWN, 200, ErrorType.INVALID_RESPONSE,
            "Invalid JSON: " + shortMsg(parseEx.getMessage()), rt);
      }

      Object statusObj = json.get("status");
      if (statusObj == null) {
        return new HealthCheck(app, now, CheckStatus.DOWN, 200, ErrorType.INVALID_RESPONSE,
            "Missing 'status' field", rt);
      }

      String status = String.valueOf(statusObj);
      if ("UP".equalsIgnoreCase(status)) {
        return new HealthCheck(app, now, CheckStatus.UP, 200, ErrorType.NONE, null, rt);
      }

      return new HealthCheck(app, now, CheckStatus.DOWN, 200, ErrorType.REMOTE_STATUS_NOT_UP,
          "Remote status: " + status, rt);

    } catch (WebClientResponseException wex) {
      int rt = (int) Duration.ofNanos(System.nanoTime() - startNs).toMillis();
      ErrorType type = (wex.getStatusCode().is5xxServerError()) ? ErrorType.HTTP_5XX : ErrorType.HTTP_4XX;
      return new HealthCheck(app, now, CheckStatus.DOWN, wex.getRawStatusCode(), type, shortMsg(wex.getMessage()), rt);
    } catch (Exception ex) {
      int rt = (int) Duration.ofNanos(System.nanoTime() - startNs).toMillis();
      ErrorType type = classify(ex);
      return new HealthCheck(app, now, CheckStatus.DOWN, null, type, shortMsg(ex.getMessage()), rt);
    }
  }

  private static String normalizeUrl(String baseUrl, String path) {
    String b = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    String p = (path == null || path.isBlank()) ? "/actuator/health" : path;
    p = p.startsWith("/") ? p : "/" + p;
    return b + p;
  }

  private static String shortMsg(String msg) {
    if (msg == null) return null;
    msg = msg.replaceAll("\\s+", " ").trim();
    return msg.length() <= 800 ? msg : msg.substring(0, 800);
  }

  private static ErrorType classify(Exception ex) {
    Throwable root = Exceptions.unwrap(ex);
    if (root instanceof TimeoutException) return ErrorType.TIMEOUT;
    if (root instanceof java.net.SocketTimeoutException) return ErrorType.TIMEOUT;
    if (root instanceof UnknownHostException) return ErrorType.DNS;
    if (root instanceof ConnectException) return ErrorType.CONNECTION_REFUSED;
    if (root instanceof WebClientRequestException wre) {
      Throwable c = wre.getCause();
      if (c instanceof UnknownHostException) return ErrorType.DNS;
      if (c instanceof ConnectException) return ErrorType.CONNECTION_REFUSED;
      if (c instanceof java.net.SocketTimeoutException) return ErrorType.TIMEOUT;
    }
    return ErrorType.UNKNOWN;
  }
}
