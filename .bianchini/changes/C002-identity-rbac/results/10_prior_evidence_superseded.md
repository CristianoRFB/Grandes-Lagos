# Historical evidence — superseded, retained verbatim below

This historical report was written at commit `654e5ea`. It is retained for
traceability, not accepted as current proof. In particular, the attribution of
readiness failure to a surviving pooled connection was not supported. The
confirmed cause is the YAML indentation introduced in `c76d405`: health settings
were nested under `server`, leaving `management` empty. See the resumed evidence
for the observed configuration RED/GREEN and runtime revalidation.

The old test counts are different historical executions, not a final combined
suite. All old pending CI claims are stale. No C002 `plan complete` ran; this file
has been moved out of the reserved `results/P01.md` name to avoid implying a
native plan completion. Its previous content follows unchanged.

# C002/P01 technical evidence

Status: implementation and verification in progress; ready for review 06 only
after final CI completion. `plan complete` and `cycle-close` were not executed.

## Local evidence

- JDK: Temurin 21.0.12.1; Maven 3.9.16.
- Docker: Engine 29.7.2, PostgreSQL Testcontainers image `postgres:18.6`.
- `mvnw -B verify`: 4 tests, 0 failures, 0 errors, 0 skipped after V0002/V0003 correction.
- `IdentitySecurityIntegrationTest`: 3 tests pass, covering invalid login 401,
  unauthenticated 401, authenticated 200 and same-session grant revocation 403.
- `IdentityContractTest`: 2 tests pass for scope matching and R01 no wildcard.
- Flyway clean chain V0001→V0002→V0003 passes; second migrate executes zero.
- V0001 SHA-256 remains `4A97F977EC3287E1794BEBD61E3E92D62338ED7482C991097183CEF89E89BC82`.

## CI evidence

Run `34527255614` on `c76d405` failed at `Verify P00 stack` because readiness
remained healthy after PostgreSQL stop while the pooled backend connection was
still alive. The corrective probe commit is `48d7a2f`; the follow-up run failed
because restarting the backend also removed liveness. The current forward fix
`6848674` uses the local Hikari lifetime and preserves the liveness probe.
Run `34528971607` covers `c7a72c0`; run `34529035643` covers `6848674` and was
still running when this evidence was written. Final CI conclusion is pending.

## Scope

No P02 tables/entities, ORM/JPA, frontend source, real credentials or wildcard
permissions were introduced. C002 remains active and GL remains S1.
