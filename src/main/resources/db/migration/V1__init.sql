CREATE TABLE IF NOT EXISTS app_user (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(200) NOT NULL,
  role VARCHAR(20) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS monitored_app (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  base_url VARCHAR(500) NOT NULL,
  health_path VARCHAR(200) NOT NULL DEFAULT '/actuator/health',
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS health_check (
  id BIGSERIAL PRIMARY KEY,
  app_id BIGINT NOT NULL REFERENCES monitored_app(id) ON DELETE CASCADE,
  checked_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(10) NOT NULL,
  http_status INT,
  error_type VARCHAR(40) NOT NULL,
  error_message VARCHAR(1000),
  response_time_ms INT
);

CREATE INDEX IF NOT EXISTS idx_health_check_app_time ON health_check(app_id, checked_at);
