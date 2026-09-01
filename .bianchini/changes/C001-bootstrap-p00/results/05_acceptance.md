# Acceptance matrix

Observed at: 2026-09-01T12:59:00-03:00

| AC | Status | Evidence |
|---|---|---|
| AC-001 | BLOCKED | Clean full bootstrap requires unavailable Docker runtime. |
| AC-002 | PASS | Maven Wrapper 3.9.16 completed backend verify with Java 21. |
| AC-003 | PASS | `npm ci` completed from lockfile with Node 24.20.0/npm 11.19.0. |
| AC-004 | PASS | TypeScript plus Vite 8.1.0 production build completed. |
| AC-005 | BLOCKED | `postgres:18.6` is configured but could not be started. |
| AC-006 | BLOCKED | V0001 runtime discovery/application requires PostgreSQL. |
| AC-007 | BLOCKED | Restart/checksum behavior requires PostgreSQL. |
| AC-008 | BLOCKED | Backend runtime startup with its required DB was not executed. |
| AC-009 | BLOCKED | Liveness endpoint was configured but not exercised. |
| AC-010 | BLOCKED | Readiness with DB/migrations was not exercised. |
| AC-011 | BLOCKED | Controlled DB-down probe was not exercised. |
| AC-012 | BLOCKED | Docker image builds were not executed. |
| AC-013 | BLOCKED | Compose smoke was not executed. |
| AC-014 | BLOCKED | Frontend container HTTP was not exercised. |
| AC-015 | BLOCKED | Failure exit is verified; success exit requires Compose smoke. |
| AC-016 | NOT_VERIFIED | Workflow exists, but repository has no remote and no real GitHub Actions run exists. |
| AC-017 | PASS | Ignore rules cover runtime env, targets, node_modules, dist and Bianchini runtime; no real secrets found. |
| AC-018 | PASS | Source and migration audit found no P01+, business endpoint/table, JPA/ORM, or TanStack Query. |
| AC-019 | PASS | Durable factual evidence exists in this results directory. |
| AC-020 | PASS | Change SUMMARY records the partial result and blockers. |
| AC-021 | PASS | Bianchini 0.4 installation, manifest, CLI, required skills, and live skill discovery passed. |
| AC-022 | PASS | Git scope audit found no material out-of-scope modification. |
| AC-023 | BLOCKED | AC-001..AC-022 are not all PASS; state remains S0. |
| AC-024 | BLOCKED | S1 was not reached; P01 remains blocked. |
