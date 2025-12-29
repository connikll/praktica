package com.example.uptimemonitor.domain;

public enum ErrorType {
  NONE,
  REMOTE_STATUS_NOT_UP,
  TIMEOUT,
  CONNECTION_REFUSED,
  DNS,
  HTTP_4XX,
  HTTP_5XX,
  INVALID_RESPONSE,
  UNKNOWN
}
