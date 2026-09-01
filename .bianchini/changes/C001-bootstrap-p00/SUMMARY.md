# C001 / P00 execution summary

## Outcome

Status: **PARTIAL — NOT DONE**

Bianchini semantic system state: **S0**

P01: **BLOCKED**

The approved P00 bootstrap artifacts are implemented in the isolated execution worktree. Backend
and frontend build gates passed with the exact required toolchain. Runtime/container acceptance is
blocked because this host has no Docker engine or Compose executable. A real GitHub Actions run is
also unavailable because the greenfield repository has no remote. These missing gates prevent S1.

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

## Verified

- Bianchini 0.4 precheck and live skill discovery: PASS.
- Approved coherence digest and isolated execution workspace: PASS.
- QA-001 source correction: PASS; the mandatory Testcontainers gate now fails closed without Docker.
- QA-001 RED: PASS with Maven exit 1, skipped=0 and Docker/Testcontainers error; GREEN remains blocked.
- Native C001 naming/layout and workspace resolution: PASS for the unique `C001-bootstrap-p00` directory.
- Frontend lockfile install, lint, typecheck and production build: PASS.
- Managed dependency versions: PASS.
- Shell syntax and YAML syntax: PASS.
- `p00-verify` failure behavior without Docker: PASS (exit 1).
- Security and implementation-scope audit: PASS.

## Not verified / blocked

- Docker image builds and Compose config/runtime gates.
- PostgreSQL startup, Flyway application and repeatability against a real PostgreSQL 18.6 instance.
- Backend/frontend container startup and HTTP health behavior.
- Clean Compose bootstrap, teardown and successful `p00-verify` exit.
- Actual GitHub Actions execution and green `p00` job.
- QA-001 GREEN with Testcontainers and PostgreSQL 18.6 on a real Docker provider.

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

## Gate decision

AC-001..AC-022 are not all PASS. Per the approved CR, the plan remains executing, C001 is not
complete, the semantic system state remains S0, and P01 remains blocked. See `results/05_acceptance.md`.
