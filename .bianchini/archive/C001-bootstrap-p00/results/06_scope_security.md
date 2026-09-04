# Security and scope audit

Observed at: 2026-09-01T12:59:00-03:00

Status: **PASS for implemented scope**

- `V0001__bootstrap.sql` contains exactly one comment and no DDL/DML.
- No business `/api/**`, domain table, JPA/ORM, RBAC/auth implementation, TanStack Query,
  real data, or real secret was introduced.
- PostgreSQL tag is `18.6` in Compose and Testcontainers.
- GitHub workflow uses `actions/setup-java@v5`; `@v4` is absent for setup-java.
- The frozen 00–30 baseline was not copied or modified.
- Bianchini remains externally installed; no engine, CLI, or skill was vendored.
- `.env`, `.bianchini/.runtime/`, `backend/target/`, `frontend/node_modules/`, and
  `frontend/dist/` are ignored.
- `.env.example`, durable Bianchini artifacts, and results are intended for version control.
