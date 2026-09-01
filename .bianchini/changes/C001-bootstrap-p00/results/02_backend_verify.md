# Backend verification

Final run observed at: 2026-09-01T13:06:19-03:00

Command: `mvnw.cmd -B verify`

Working directory: `backend`

Exit code: `0`

Result: **BUILD SUCCESS**

Compilation and packaging passed with Java 21 and Spring Boot 4.1.1. Testcontainers 2.0.5
attempted Docker discovery and found no valid Docker environment. Consequently, both methods in
`BootstrapIntegrationTest` were skipped by the explicit `disabledWithoutDocker` contract:

- tests run: 2;
- failures: 0;
- errors: 0;
- skipped: 2.

This evidence proves the backend build but does **not** prove PostgreSQL startup, Flyway execution,
repeatability, or migration checksum stability.

The final test source uses the Testcontainers 2.0 PostgreSQL namespace and compiled without the
deprecated compatibility API warning.
