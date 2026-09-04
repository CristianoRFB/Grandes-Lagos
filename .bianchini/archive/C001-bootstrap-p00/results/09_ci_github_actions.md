# C001/P00 GitHub Actions CI evidence

Observed on: 2026-09-02, America/Sao_Paulo, through 18:33:37-03:00.

## Local Git preflight

- Repository: `C:\Users\MICRO-10\gl-operations-c001-execution`.
- Branch: `bm/c001-p00`.
- Expected and actual technical HEAD: `3b8240e043e3a245d540cac6b88c7d39ae4857e3`.
- Worktree: clean.
- Remotes before this gate: none.
- Bianchini `model validate --repo . --change C001`: exit 0, `valid: true`, no differences.
- Bianchini `workspace check --repo . --change C001`: exit 0, `valid: true`.
- Physical change directory remained the unique `.bianchini/changes/C001-bootstrap-p00`.

## Workflow inspection

- Path: `.github/workflows/ci.yml`.
- Name: `P00 CI`.
- Triggers: unfiltered `push`, unfiltered `pull_request`, and `workflow_dispatch`.
- No branch or path filters.
- One required job: `p00`, `ubuntu-24.04`, timeout 30 minutes.
- Top-level permissions: `contents: read`.
- No `continue-on-error` is present.
- The job contains checkout, Java 21/Temurin, Node 24.20.0, npm 11.19.0, Maven verify,
  frontend install/build, Compose validation/image build, `p00-up`, `p00-verify`, and always-run
  `p00-down` cleanup.
- Because `push` has no branch filter, the minimum trigger path was the existing local branch to
  the same remote branch; no `main` ref, PR, dispatch, workflow edit, or branch rename was needed.

## Authorization, remote, and push

- GitHub CLI: 2.96.0, present and authenticated to `github.com` through the OS keyring.
- Effective account: `CristianoRFB`; Git protocol: HTTPS.
- Repository: `CristianoRFB/Grandes-Lagos`, public, initially empty, viewer permission `ADMIN`.
- No credential or token value was persisted or disclosed in this evidence.
- Authorized remote URL: `https://github.com/CristianoRFB/Grandes-Lagos.git`.
- `origin` was absent and was added once with the exact authorized URL.
- Refspec: `refs/heads/bm/c001-p00:refs/heads/bm/c001-p00`.
- `git push --dry-run` with that refspec: exit 0.
- Real normal push with the same refspec: exit 0; no force option used.
- Remote ref after push: `3b8240e043e3a245d540cac6b88c7d39ae4857e3` at
  `refs/heads/bm/c001-p00`.
- GitHub selected `bm/c001-p00` as the default branch after the first push to the empty repository.

## Technical GitHub Actions run

- Repository: `CristianoRFB/Grandes-Lagos`.
- Workflow: `P00 CI`.
- Run ID/number: `33685505704` / `1`.
- URL: `https://github.com/CristianoRFB/Grandes-Lagos/actions/runs/33685505704`.
- Event: `push`.
- Ref: `bm/c001-p00`.
- Tested SHA: `3b8240e043e3a245d540cac6b88c7d39ae4857e3`.
- Created/started: `2026-09-02T21:30:22Z`.
- Completed: job at `2026-09-02T21:33:36Z`; run updated at `2026-09-02T21:33:37Z`.
- Status: `completed`.
- Conclusion: `success`.
- Required job `p00`, job ID `100431915785`: `success`, duration 3m09s.
- Every required and post/cleanup step concluded `success`; no required step was skipped.

## Gate evidence from the run logs

- GitHub token permissions: `Contents: read`.
- `actions/checkout@v4`: step success.
- `actions/setup-java@v5`: step success; Temurin `21.0.12.1`.
- Maven Wrapper: Apache Maven `3.9.16`.
- `actions/setup-node@v4`: step success; Node `v24.20.0`.
- npm pin and toolchain: `11.19.0`.
- Docker Compose on runner: `v2.38.2`.
- Testcontainers: `2.0.5`, connected to the runner Docker daemon.
- PostgreSQL: real `postgres:18.6` container; Flyway logs reported PostgreSQL 18.6 for the
  primary and independent rebuild databases.
- Backend result: 2 tests, 0 failures, 0 errors, 0 skipped; Maven `BUILD SUCCESS`.
- Frontend: `npm ci` success and Vite `8.1.0` production build success.
- Compose config and backend/frontend image builds: success.
- Runtime services: PostgreSQL, backend, and frontend reached healthy.
- `p00-verify`: liveness/readiness/frontend/Flyway/restart/DB-down/recovery all PASS; final
  `P00 verification passed`.
- Cleanup: frontend, backend, PostgreSQL, project volume, and network removed successfully.
- Non-blocking annotation: GitHub reported that `actions/checkout@v4` and `actions/setup-node@v4`
  target the deprecated Node.js 20 action runtime and were forced onto Node.js 24 by the runner.
  The steps and run still concluded success; no P00 gate was masked or skipped.

## AC-016 decision

AC-016: **PASS**.

The real GitHub Actions run belongs to the authorized repository, ran the exact accepted technical
SHA, executed the correct P00 workflow, completed successfully, and provides job/step/log evidence
for all mandatory gates without required skips or `continue-on-error` masking.

This decision does not complete C001. Per the closed recovery scope, final review and state
transition remain outside this execution: C001 stays NOT DONE, STATE stays S0/executing, P01 stays
BLOCKED, and plan completion stays PENDING.
