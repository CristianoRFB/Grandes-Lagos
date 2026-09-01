# C001/P00 correction precheck

Observed at: 2026-09-01T16:39:52-03:00 through 2026-09-01T16:40:43-03:00.

## QA-001

- Source uses `@Testcontainers`; `disabledWithoutDocker = true` is absent.
- The preserved RED ran `backend\\mvnw.cmd -B verify` with Java 21 and Maven Wrapper 3.9.16.
- With Docker unavailable it returned exit 1, with tests=1, skipped=0, failures=0 and errors=1.
- Testcontainers 2.0.5 failed while resolving `postgres:18.6`, proving the mandatory gate is fail-closed.
- GREEN remains blocked until a real Docker provider is available.

## GOV-001 naming recovery

- `git restore --source=HEAD --staged --worktree -- .bianchini` restored the approved physical layout.
- Semantic change ID: `C001`.
- Unique physical change directory: `.bianchini/changes/C001-bootstrap-p00`.
- STATE, pointers and `COHERENCE.change` consistently reference `C001-bootstrap-p00`.
- Durable results and SUMMARY remain under that unique physical change directory.

## Bianchini validation

- Native `workspace check --repo . --change C001`: exit 0, `valid: true`.
- Native `coherence check --repo . --change C001`: the resolver found the physical change but Windows directory fsync returned permission denied.
- The audited process-local shim, SHA-256 `4AB697ED1B4C2CC5F2F5C10F94A68543AC5E22AA1403BE7F2C3CC02E9D1F3840`, was then used only to bypass unsupported directory fsync.
- Under the shim, workspace check returned exit 0 and coherence check returned exit 0, resolving `C001-bootstrap-p00` with zero structural findings.
- Because no new semantic report was supplied, that invocation emitted `SEMANTIC_REVIEW_UNAVAILABLE`; its transient STATE/COHERENCE writes were restored to the already approved HEAD checkpoint.
- The upstream installation was not patched, updated, copied or vendored.
- The temporary shim was removed and no PYTHONPATH/PYTHONSTARTUP persistence exists.

## Governance reassessment

- AC-019 remains PASS: durable evidence is present in the unique native `C001-*` results directory.
- AC-021 remains PASS for the pinned method precheck: workspace resolution is valid, the semantic identifier resolves unambiguously to `C001-bootstrap-p00`, and the approved coherence checkpoint is preserved.
- System state remains S0, implementation remains executing, P01 remains blocked and plan completion remains pending.

## CI

- Top-level `permissions: contents: read` was added to the existing workflow.
- No GitHub Actions run, remote, deploy or release was created.
