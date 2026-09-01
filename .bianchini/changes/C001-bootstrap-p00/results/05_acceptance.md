# Acceptance matrix

Observed through: 2026-09-01T20:01:11-03:00

| AC | Status | Rerun | Evidence |
|---|---|---|---|
| AC-001 | PASS | YES | A detached clean worktree at `4bf64af` completed `p00-up`, `p00-verify` and `p00-down`; it was removed cleanly. |
| AC-002 | PASS | YES | Temurin 21.0.12.1 and Maven Wrapper 3.9.16 ran the real Testcontainers/PostgreSQL gate: 2 tests, 0 failures, 0 errors, 0 skips. |
| AC-003 | PASS | NO | `npm ci` completed from lockfile with Node 24.20.0/npm 11.19.0; Docker builds also consumed the same lockfile. |
| AC-004 | PASS | YES | TypeScript plus Vite 8.1.0 production build passed in main and isolated Docker builds. |
| AC-005 | PASS | YES | Real `postgres:18.6` started healthy and reported PostgreSQL 18.6. |
| AC-006 | PASS | YES | Flyway discovered and applied comment-only V0001 once on an empty PostgreSQL 18.6 database. |
| AC-007 | PASS | YES | Migration history remained one successful V0001 row with checksum `-1252058078` through restart and recovery. |
| AC-008 | PASS | YES | Backend container started and remained active with its required database. |
| AC-009 | PASS | YES | Liveness returned HTTP 200/UP at baseline, after backend restart and while the database was down. |
| AC-010 | PASS | YES | Readiness returned HTTP 200/UP with PostgreSQL and migrations available. |
| AC-011 | PASS | YES | With PostgreSQL stopped, readiness returned 503/DOWN while liveness stayed 200/UP; readiness recovered after restart. |
| AC-012 | PASS | YES | Backend and frontend Docker image builds completed successfully. |
| AC-013 | PASS | YES | Compose brought all three services healthy in initial, clean-retry and isolated runs. |
| AC-014 | PASS | YES | Frontend container returned HTTP 200 with the `GL Operations — P00` shell. |
| AC-015 | PASS | YES | Canonical `p00-verify` exited 0 in initial, clean-retry and isolated runs. |
| AC-016 | NOT_VERIFIED | N/A | Workflow exists, but repository has no remote and no real GitHub Actions run was executed. |
| AC-017 | PASS | NO | Ignore rules cover runtime env, targets, node_modules, dist and Bianchini runtime; no real secrets found. |
| AC-018 | PASS | YES | Runtime schema query returned zero public application tables; no P01+, business endpoint, JPA/ORM or TanStack Query was introduced. |
| AC-019 | PASS | YES | Durable factual evidence exists under the unique native directory `.bianchini/changes/C001-bootstrap-p00/results`, including `08_docker_runtime_gates.md`. |
| AC-020 | PASS | YES | SUMMARY records runtime results, corrections and the remaining AC-016 blocker. |
| AC-021 | PASS | YES | Workspace and model checks returned valid after corrections; semantic `C001` remains unambiguous and the approved coherence checkpoint is preserved. |
| AC-022 | PASS | YES | Git scope audit found only P00 bootstrap, focused runtime corrections and durable evidence. |
| AC-023 | BLOCKED | N/A | AC-016 is not PASS; state must remain S0. |
| AC-024 | BLOCKED | N/A | S1 was not reached; P01 remains blocked. |
