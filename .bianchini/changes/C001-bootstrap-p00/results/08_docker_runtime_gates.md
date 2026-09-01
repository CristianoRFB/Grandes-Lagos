# C001/P00 Docker runtime gates

Observed on: 2026-09-01, America/Sao_Paulo, through 20:01:11-03:00.

## Provider and Compose preflight

- `docker version`: exit 0; client/server 29.7.2, API 1.55, Linux engine on Docker Desktop 4.89.0.
- `docker compose version`: exit 0; v5.5.0.
- `docker info`: exit 0; Linux/amd64, WSL2 kernel, 8 CPUs and 7.732 GiB memory.
- `docker desktop status`: exit 0 but printed `Status stopped`; this contradicted the successful daemon probes and was treated as stale CLI status evidence.
- `docker compose ... config`: exit 0 for project `gl-operations-p00`, PostgreSQL 18.6, backend, frontend, declared ports and project volume.
- Entry inventory found no pre-existing project container or volume.

## QA-001 GREEN and runtime corrections

The first Docker-backed `backend\\mvnw.cmd -B verify` exposed a real error: both integration
tests failed because Spring Boot 4.1.1 had no `Flyway` bean. The source used `flyway-core`, but
Boot 4's Flyway auto-configuration is modular. Commit `120a43b` replaced it with
`spring-boot-starter-flyway`, preserving `flyway-database-postgresql`.

The retry used Temurin 21.0.12.1, Maven Wrapper 3.9.16 and Testcontainers 2.0.5 through the real
Windows named-pipe Docker provider. PostgreSQL 18.6 started and the final result was:

- tests: 2;
- failures: 0;
- errors: 0;
- skipped: 0;
- Maven exit: 0 / BUILD SUCCESS.

The first successful image build was followed by a runtime RED: `p00-up` exited 1 because the
backend process ended normally without an embedded web server. Commit `8365abf` added the focused
Spring Boot 4 `spring-boot-starter-webmvc`; the affected Maven gate returned 2/0/0/0 and the next
`p00-up` reached three healthy services.

The first fresh detached-worktree attempt timed out during a Docker Hub TLS handshake before
creating resources. Its retry exposed a repository defect: with global `core.autocrlf=true`,
`backend/mvnw` was checked out as CRLF and the Linux build reported `./mvnw: not found`.
Commit `4bf64af` added `mvnw text eol=lf`; a recreated checkout had a 11,790-byte LF wrapper and
completed the clean bootstrap.

## PostgreSQL and Flyway

- Runtime server: `postgres (PostgreSQL) 18.6 (Debian 18.6-1.pgdg13+2)`.
- `flyway_schema_history`: exactly one successful row:
  `1|0001|bootstrap|V0001__bootstrap.sql|-1252058078|t`.
- Successful Flyway row count remained 1 after backend restart, database stop/start, canonical
  verification, clean retry and isolated bootstrap.
- Public application-table count excluding `flyway_schema_history`: 0.
- V0001 source remains comment-only/no-op; SHA-256:
  `4A97F977EC3287E1794BEBD61E3E92D62338ED7482C991097183CEF89E89BC82`.

## Images, Compose and HTTP

- Backend and frontend image builds: exit 0.
- Representative post-fix image IDs/sizes before later BuildKit attestations changed manifest-list
  IDs: backend `796e9caf...`, 97,076,669 bytes; frontend `74cf4d79...`, 2,274,580 bytes.
- `p00-up`: exit 0; postgres, backend and frontend healthy.
- Baseline liveness: HTTP 200, `{"status":"UP"}`.
- Baseline readiness: HTTP 200, `{"status":"UP"}`.
- Frontend: HTTP 200 with title `GL Operations — P00` and Vite asset references.
- Backend restart: readiness recovered on bounded attempt 7; both health groups returned 200/UP.
- PostgreSQL stopped: readiness returned 503/DOWN on attempt 1; liveness stayed 200/UP and the
  backend process remained running.
- PostgreSQL restarted: readiness recovered to 200/UP; Flyway history still contained one row.
- Canonical `scripts/p00-verify.sh`: exit 0 and final `P00 verification passed`.

## Cleanup, clean retry and isolated bootstrap

- First cleanup: `p00-down` exit 0; no project container, volume or network remained.
- Main-worktree clean retry: `p00-up` exit 0, `p00-verify` exit 0, `p00-down` exit 0; no project
  resources remained.
- Detached clean checkout at `4bf64af`: after the line-ending correction, `p00-up` exit 0,
  `p00-verify` exit 0 and `p00-down` exit 0.
- The detached worktree was removed through `git worktree remove`; final resource inventory was
  empty.

## Governance boundary

- Bianchini `workspace check --repo . --change C001`: exit 0, `valid: true` after each correction.
- `model validate --repo . --change C001`: exit 0, `valid: true`, no differences.
- Approved `COHERENCE.md` checkpoint was preserved; no new semantic report or state promotion was
  attempted.
- AC-016 remains NOT_VERIFIED because no real GitHub Actions run was authorized or available.
- Therefore C001 remains NOT DONE, system state remains S0, P01 remains BLOCKED and plan completion
  remains pending.
