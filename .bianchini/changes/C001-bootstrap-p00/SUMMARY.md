# C001 / P00 execution summary

## Outcome

Status: **PARTIAL — NOT DONE**

Bianchini semantic system state: **S0**

P01: **BLOCKED**

The approved P00 bootstrap artifacts are implemented in the isolated execution worktree. Backend,
frontend, Testcontainers, PostgreSQL/Flyway, image, Compose, HTTP, failure/recovery, cleanup and
clean-bootstrap gates pass locally. A real GitHub Actions run remains unavailable because the
greenfield repository has no remote. AC-016 therefore remains NOT_VERIFIED and prevents S1.

## Implemented

- Spring Boot 4.1.1 backend on Java 21 with Actuator, JDBC and Flyway; no JPA/ORM.
- Maven Wrapper 3.9.16 and managed Flyway 12.4.0, JDBC 42.7.13, Testcontainers 2.0.5.
- Comment-only `V0001__bootstrap.sql` with no application DDL/DML.
- PostgreSQL 18.6 Testcontainers integration tests for migration-once/checksum and clean rebuild.
- React/TypeScript shell using Node 24.20.0, npm 11.19.0 and Vite 8.1.0.
- Backend/frontend Dockerfiles, PostgreSQL 18.6 Compose stack and synthetic environment example.
- PostgreSQL 18 volume mounted at its official 18+ parent path, `/var/lib/postgresql`.
- Bounded, project-scoped `p00-up`, `p00-verify`, and `p00-down` scripts.
- One sequential GitHub Actions `p00` job on Ubuntu 24.04 with the approved action versions.
- Durable verification, acceptance, security and blocker evidence.
- Spring Boot 4 modular Flyway and MVC starters required by the verified runtime.
- Explicit LF checkout rule for the Linux-executed Maven wrapper.

## Verified

- Bianchini 0.4 precheck and live skill discovery: PASS.
- Approved coherence digest and isolated execution workspace: PASS.
- QA-001 source correction: PASS; the mandatory Testcontainers gate fails closed without Docker.
- QA-001 GREEN: PASS against the real named-pipe Docker provider and PostgreSQL 18.6: 2 tests,
  0 failures, 0 errors and 0 skips.
- Native C001 naming/layout and workspace resolution: PASS for the unique `C001-bootstrap-p00` directory.
- Frontend lockfile install, lint, typecheck and production build: PASS.
- Managed dependency versions: PASS.
- Shell syntax and YAML syntax: PASS.
- `p00-verify` failure behavior without Docker: PASS (exit 1).
- Docker image builds and Compose config/runtime: PASS.
- PostgreSQL 18.6 and comment-only Flyway V0001 applied exactly once: PASS.
- Backend liveness/readiness, restart, database failure and recovery: PASS.
- Frontend container HTTP 200: PASS.
- Canonical `p00-verify` in initial, clean-retry and detached clean-worktree runs: PASS.
- Project-scoped teardown and empty post-cleanup inventory: PASS.
- Security and implementation-scope audit: PASS.

## Not verified / blocked

- Actual GitHub Actions execution and green `p00` job.

## Deviations and errors

- The pinned Bianchini CLI has native-Windows path/`fsync` limitations. It was called via a
  temporary external compatibility shim that only disables directory `fsync`; upstream and repo
  content were not modified. The correction precheck resolved semantic `C001` to the unique
  physical directory `C001-bootstrap-p00`; the shim was removed after validation.
- A fresh `coherence check` without a new semantic report returned exit 0 and zero structural
  findings but emitted `SEMANTIC_REVIEW_UNAVAILABLE`. Its transient writes were discarded and the
  already approved coherence checkpoint from HEAD was preserved.
- The CLI's generic `validate-state`/`status` paths are for its older `method_version: 2` state
  schema and reject the 0.4 index. The applicable 0.4 `workspace check` was used and returned
  `valid: true`; one earlier check mistakenly targeted the planning root and was corrected to the
  execution root.
- The first Maven Wrapper generation attempt passed an unquoted PowerShell `-D` argument and
  failed argument parsing; the corrected quoted invocation generated Maven Wrapper 3.9.16.
- During final warning cleanup, `testCompile` was first entered instead of Maven's `test-compile`,
  and a generic declaration was briefly used with the non-generic Testcontainers 2.0 PostgreSQL
  class. Both were corrected; the final `mvnw.cmd -B verify` completed successfully without that
  deprecation warning.
- `create-vite@8.1.0` initially scaffolded its independently versioned default Vite release; the
  package was then pinned to the CR-required `vite@8.1.0`, resolved and built successfully.
- npm emitted a deprecation warning for the scaffolded ESLint 9.39.5 transitive selection; lint
  passed and npm audit reported zero vulnerabilities. Dependency upgrades outside the pinned P00
  stack were not introduced.
- The first Docker-backed QA-001 GREEN attempt found no Flyway bean under Spring Boot 4.1.1.
  Commit `120a43b` changed the dependency to `spring-boot-starter-flyway`; the retry passed.
- The first post-build `p00-up` found that the backend exited normally because no embedded web
  server was present. Commit `8365abf` added `spring-boot-starter-webmvc`; the next run reached
  healthy and the affected Maven gate remained green.
- The first detached clean-worktree run hit a Docker Hub TLS handshake timeout before resource
  creation. Its retry exposed CRLF checkout of `backend/mvnw` under `core.autocrlf=true` and failed
  with `./mvnw: not found`. Commit `4bf64af` forces LF for `mvnw`; a recreated clean worktree passed
  `p00-up`, `p00-verify` and `p00-down`.
- `docker desktop status` printed `Status stopped`, while `docker version`, `docker info`, Compose
  and all runtime gates succeeded. The Desktop subcommand output is retained as stale/inconsistent
  environment evidence. Compose v5.5.0 was available rather than the baseline v2 wording.

## Gate decision

All locally executable acceptance gates pass, but AC-016 is NOT_VERIFIED because no real GitHub
Actions run exists. Therefore AC-001..AC-022 are not all PASS. Per the approved CR, the plan remains
executing, C001 is not complete, the semantic system state remains S0, and P01 remains blocked. See
`results/05_acceptance.md` and `results/08_docker_runtime_gates.md`.
