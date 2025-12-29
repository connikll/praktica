# Uptime Monitor (учебный пример)

Минимальный сервис мониторинга доступности, который:
- хранит пользователей в БД и использует **HTTP Basic Auth**
- даёт админам API для добавления приложений на мониторинг
- по cron периодически опрашивает `/actuator/health` (или любой указанный `healthPath`)
- сохраняет результаты опросов в БД
- отдаёт отчёт: интервалы недоступности и процент доступности за период

## Требования
- Java 17+
- Docker (для PostgreSQL)
- Maven 3.9+

## Быстрый старт

### 1) Поднять PostgreSQL
```bash
docker compose up -d
```

### 2) Запустить приложение
```bash
mvn spring-boot:run
```

Приложение стартует на `http://localhost:8080`.

## Дефолтные пользователи
При первом запуске создаются (если таблица `app_user` пустая):
- **admin / admin** (роль ADMIN)
- **user / user** (роль USER)

## API

### Добавить приложение (ADMIN)
```bash
curl -u admin:admin -X POST http://localhost:8080/api/apps \
  -H "Content-Type: application/json" \
  -d '{"name":"demo","baseUrl":"http://localhost:8081","healthPath":"/actuator/health","enabled":true}'
```

### Список приложений (USER/ADMIN)
```bash
curl -u user:user http://localhost:8080/api/apps
```

### Отчёт по доступности (USER/ADMIN)
`from` и `to` — ISO-8601 (UTC или с таймзоной), например: `2025-01-01T00:00:00Z`

```bash
curl -u user:user "http://localhost:8080/api/apps/1/report?from=2025-01-01T00:00:00Z&to=2025-01-02T00:00:00Z"
```

## Настройки
В `application.yml`:
- `monitor.cron` — cron-расписание опроса
- `monitor.request-timeout-seconds` — таймаут запроса к health endpoint

## Примечания по расчётам
- UP, если в JSON есть поле `"status"` и оно равно `"UP"` (регистр не важен).
- Любые сетевые ошибки/таймауты/HTTP 4xx-5xx/невалидный JSON/другие статусы — считаются DOWN и попадают в статистику ошибок.

