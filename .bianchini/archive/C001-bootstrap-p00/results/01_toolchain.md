# Toolchain evidence

Observed at: 2026-09-01T12:54:05-03:00

| Component | Required | Effective result | Status |
|---|---:|---:|---|
| Java | 21 | Temurin 21.0.12.1 | VERIFIED |
| Maven Wrapper | 3.9.16 | 3.9.16 | VERIFIED |
| Spring Boot | 4.1.1 | 4.1.1 | VERIFIED |
| Flyway | 12.4.0 | 12.4.0 | VERIFIED |
| PostgreSQL JDBC | 42.7.13 | 42.7.13 | VERIFIED |
| Testcontainers | 2.0.5 | 2.0.5 | VERIFIED |
| Node.js | 24.20.0 | 24.20.0 | VERIFIED |
| npm | 11.19.0 | 11.19.0 | VERIFIED |
| Vite | 8.1.0 | 8.1.0 | VERIFIED |
| PostgreSQL image | 18.6 | `postgres:18.6` in Compose and Testcontainers | CREATED / NOT RUN |
| Docker / Compose | v2 | executable not found | BLOCKED |

Commands and results:

- `backend/mvnw.cmd -version` from `backend`: exit 0; Maven 3.9.16 on Java 21.
- `backend/mvnw.cmd -B dependency:tree -Dincludes=org.flywaydb:*,org.postgresql:postgresql,org.testcontainers:*` from `backend`: exit 0; exact managed versions shown above.
- `node --version`, `npm --version`, and `npm ls vite --depth=0` from `frontend`: exit 0; exact versions shown above.
- Docker executable lookup, including standard Docker Desktop and Podman locations: no executable found.
