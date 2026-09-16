# C002/P01 — CI RED / security completion recovery

Status: IN PROGRESS. This is implementation evidence, not a native plan result,
approval, homologation, release or closeout. C002 remains active, GL remains S1,
P02 NOT STARTED. Neither `plan complete` nor `cycle-close` is authorized or run.

## Entry and scope

- Worktree: `C:\Users\MICRO-10\gl-operations-c002-p01`.
- Branch: `bm/c002-p01`; authorized remote: `https://github.com/CristianoRFB/Grandes-Lagos.git`.
- Entry to this recovery: `654e5ea9ba9e02fd8a274c6a56dbc4e2bd9dec2f`, clean.
- The requested `c76d405a79661c6914febd5c95ac49e64b16d043` is an ancestor;
  legitimate continuation commits were preserved. No reset/rebase/amend/force.
- User continuation on 2026-09-11 resumed the already-owned dirty changes after
  an executor interruption; no unrelated user change was replaced.
- Scope: Identity backend, accepted session/CSRF/capability contracts, forward
  migration, tests, CI, P00 regression and C002 evidence. No frontend feature,
  real user seed, P02, JPA, JWT, external identity provider or production deploy.

## Confirmed RED and cause

Authoritative original run:
[34527255614](https://github.com/CristianoRFB/Grandes-Lagos/actions/runs/34527255614),
head `c76d405a79661c6914febd5c95ac49e64b16d043`, conclusion `failure`, job `p00`,
step `Verify P00 stack`. Retrieved with `gh run view --json ...` and `--log-failed`.

Sanitized excerpt:

```text
PASS: backend readiness with database
PASS: backend readiness after restart
P00 verification failed: readiness fails without database
Process completed with exit code 1.
```

The causal diff in `c76d405` inserted `server.servlet.session.timeout` immediately
after `management:`. The preexisting health fields then belonged to `server`,
leaving `management` empty. Readiness no longer included `db`. It was not a proven
Hikari stale-connection problem. The earlier backend-restart experiment removed
liveness and did not fix the cause. The speculative 10-second Hikari max lifetime
was below Hikari's minimum and has been removed.

| Focused reproduction | Observed result |
|---|---|
| `mvnw.cmd -B -Dtest=HealthConfigurationTest test` before fix, 2026-09-10 | Exit 1; 1 test, 1 failure, 0 errors/skips; expected `readinessState,db`, actual null |
| Same command after fix | Exit 0; 1 test, 0 failures/errors/skips |
| `mvnw.cmd -B -Dtest=IdentityMigrationTest test` before V0004 | Exit 1; 28 tests, 1 failure, 0 errors/skips; forged normalization accepted instead of SQL constraint failure |
| `mvnw.cmd -B -Dtest=IdentityContractTest,IdentitySecurityIntegrationTest test` before security completion | Exit 1; 19 tests, 18 failures, 0 errors/skips; missing CSRF bootstrap/Problem Details and invalid scope accepted |

Commands above ran in `backend/` with user-local Temurin 21.0.12.1+1 and Maven
Wrapper 3.9.16. Logs are kept outside Git in `C:\Users\MICRO-10\Downloads\`:
`c002-ci-original-red.log`, `c002-health-config-red.log`,
`c002-health-config-green.log`, `c002-migration-red.log`, `c002-security-red.log`.
These files preserve the RED history; request/response dumping is disabled in
security tests to avoid recording passwords, CSRF or session values.

## Correction and impact

- `4f025d9`: restores `management.endpoint.health` and adds the configuration regression.
- V0004 adds only `ck_iam_user_normalized_username` requiring
  `normalized_username = lower(btrim(username))`. The unique key can no longer be
  bypassed with a caller-forged normalized value. Existing inconsistent data makes
  migration fail visibly; no silent repair/backfill or destructive operation.
- V0001/V0002/V0003 are published and immutable. Tests assert their raw SHA-256:
  V1 `4a97f977ec3287e1794bebd61e3e92d62338ed7482c991097183cef89e89bc82`;
  V2 `d328f19d6261e6265b8c2f27225cff50bacb82816ce7a76b026d3324e559f1bc`;
  V3 `897823c9892305dffad544b022a1c7f4e42a3062c8109f2e34bfb36f56674177`.
- P00 outage verification now requires an actual HTTP 503, not any network error.
  The local connection-acquisition timeout is two seconds so this health result
  can be observed within the five-second probe window. Liveness must stay 200.
- Impact radius: the active Identity seam and P00 readiness/CI consumer only.
  No change to canonical model ownership/interfaces or future phase contracts.

## Verification in this recovery

On 2026-09-16, after restoring the user-local Temurin 21.0.12.1+1 runtime and
starting Docker Desktop 4.89 / Engine 29.7.2, the final local backend command
`./mvnw -B verify` in `backend/` passed with 59 tests, 0 failures, 0 errors and
0 skipped. Testcontainers used `postgres:18.6`; Flyway applied V0001..V0004 in
clean schemas and retry migration executed zero. The final log is kept outside
Git at `C:\Users\MICRO-10\Downloads\c002-mvn-verify.log`.

The strict P01 report guard then passed 56 required executions:

```text
IdentityContractTest: 3
IdentitySecurityIntegrationTest: 16
IdentityHttpIntegrationTest: 5
IdentityPolicyTest: 4
IdentityMigrationTest: 28
failures=0 errors=0 skipped=0 in every suite
```

Its own five synthetic guard tests passed. The guard is run after a clean Maven
test invocation and requires each named XML suite, minimum counts, and zero
failure/error/skip elements. Frontend `node:24.20.0-alpine` with npm 11.19.0
also passed `npm ci`, `npm run build` and `npm run lint`; log:
`C:\Users\MICRO-10\Downloads\c002-frontend-build.log`.

The remaining closeout handoff is factual review 06; no native plan completion or
closeout is implied by these technical gates.

## Compose / P00 runtime

On 2026-09-16, `scripts/p00-up.sh` built both images from the committed source
and started PostgreSQL 18.6, the Spring backend and the frontend with all health
checks healthy. `scripts/p00-verify.sh` then passed every assertion:

```text
backend liveness
backend readiness with database
frontend HTTP
Flyway V0001 recorded exactly once
backend readiness after restart
readiness fails without database (HTTP 503)
liveness remains healthy without database
readiness recovers with database
Flyway V0001 recorded exactly once
P00 verification passed
```

The final `scripts/p00-down.sh` removed all three containers, the Compose volume
and network; `docker compose ... ps -a` returned no rows. Runtime log:
`C:\Users\MICRO-10\Downloads\c002-p00-verify.log`.

## GitHub Actions evidence

Push of technical commits `2f5e765` and `564a94e` produced the real run
[35124406067](https://github.com/CristianoRFB/Grandes-Lagos/actions/runs/35124406067)
on exact head `564a94eb047f728d7f5103642fb5b359a4467533`. It concluded `success`;
job `p00` and every step passed. Sanitized log evidence is retained outside Git
at `C:\Users\MICRO-10\Downloads\c002-ci-35124406067.log`:

- `Verify backend`: 59 tests, 0 failures/errors/skips, `BUILD SUCCESS`.
- `P01 security gate`: suites 3 + 16 + 5 + 4 + 28, total 56, all zero
  failures/errors/skips; `P01 gate passed: 56 required test executions`.
- Frontend dependencies/build and Compose validation/image builds passed.
- `Verify P00 stack`: all liveness/readiness/frontend/Flyway/restart/outage/
  recovery assertions passed, including `readiness fails without database
  (HTTP 503)` and final `P00 verification passed`.
- `Clean up P00 stack` passed.

This proves a technical GREEN candidate on that SHA. It does not resolve the
historical Bianchini evidence gaps described below and is not a closeout.

## Historical governance findings for Router / 06

- The isolated Git worktree exists and is outside the primary branch. However,
  native `workspace check --repo <worktree>` returns exit 1:
  `DIRTY_WORKSPACE: metadados de execução ausentes ou ambíguos`.
  Do not reconstruct/fabricate a historical native workspace checkpoint.
- Read-only pinned `bm.py model validate --repo <worktree> --change C002` returns
  exit 0, `valid=true`, no differences. Current S1 digest:
  `5f117ffe83b10ef3e6ff1e9f397a62c43684aaefbfe7f893921e9f7a9c286247`.
  Expected/calculated C002 digest:
  `8672153488bd5eccbec883cdf07d8f3ef3e43f355414feefc5863592106625d0`.
  Approved coherence digest remains:
  `7500d455472e283934892670336cd43e661c99ffdd5fcdfaea596b3643f8d011`.
- Specs (`1a7e916`) and approval (`19889a8`) precede source (`bba9cd9`) in Git,
  but the frozen plan is incomplete: only AC001–004 listed, broad acceptance,
  missing complete route/response/error contracts, empty migration metadata,
  missing rollback/future constraints/auth journey. Model prose still says P00.
  Current tests cannot retroactively prove AC003 or the original pre-source checks.
- The recorded SCOPE says “sessões HTTP JDBC”, whereas the master explicitly
  allows ordinary server-side HttpSession without Spring Session. This ambiguity
  is reported for review; no unapproved JDBC session store has been introduced.
- Native creation of C002 and original workspace prechecks are not established by
  repository artifacts alone. An approval timestamp is not a CLI execution log.
- Historical `results/P01.md` was renamed to `10_prior_evidence_superseded.md`,
  preserving its original text and marking the unsupported cause/old counts as
  superseded. No native completion result has been manufactured.
- STATE, approved model/plan/coherence and C001 archive remain unchanged. The
  recovery authorization permits continuing corrections without replanning;
  it does not resolve these approval-evidence gaps or authorize closeout.

## Acceptance revalidation

| AC | Evidence status |
|---|---|
| AC-P01-001 | C001 S1 baseline retained; technical regression green |
| AC-P01-002 | Native C002 creation not independently evidenced in repository |
| AC-P01-003 | NOT_VERIFIED: frozen plan lacks the complete artifact/route/migration contract |
| AC-P01-004 | NOT_VERIFIED: native workspace check fails missing/ambiguous metadata |
| AC-P01-005..021 | TECHNICALLY_PASS: source, 59-test verify, Testcontainers and P00 gates |
| AC-P01-022 | TECHNICALLY_PASS: 401/200/403, revocation, logout, CSRF and real HTTP timeout |
| AC-P01-023 | PASS: real run 35124406067 on exact SHA 564a94e |
| AC-P01-024..026 | PASS by source review and scoped diff; no secrets/P02/domain creep found |
| AC-P01-027 | BLOCKED by AC-P01-002..004 evidence, and still forbidden before 06 review |
| AC-P01-028 | PASS: P02 remains NOT STARTED |

The technical implementation is ready for 06 review. Closeout eligibility remains
blocked until the Router/06 process resolves or explicitly accepts the historical
governance evidence gaps; this recovery does not rewrite them.
