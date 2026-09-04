# Container and runtime gates

Observed at: 2026-09-01T12:59:00-03:00

Historical status at the observation time: **BLOCKED**

Superseded on 2026-09-01 by the successful provider/runtime evidence in
`08_docker_runtime_gates.md`. The entries below are retained as the earlier fail-closed checkpoint.

- Docker executable was not present on `PATH` or in the standard Docker Desktop locations.
- No Docker or Podman service/runtime was available.
- Compose config/build, PostgreSQL startup, application startup, HTTP probes, Flyway runtime,
  clean rebuild, and teardown could not be executed.
- `bash -n scripts/p00-up.sh scripts/p00-verify.sh scripts/p00-down.sh`: exit 0.
- `scripts/p00-verify.sh` without Docker: exit 1 with
  `P00 verification failed: Docker is not available`.
- YAML syntax parsing for `infra/compose.yml` and `.github/workflows/ci.yml`: PASS.
- The PostgreSQL 18 named volume uses the official 18+ mount target `/var/lib/postgresql`.

Created artifacts are not treated as runtime verification.
