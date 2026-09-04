# Recovery precheck

Status: **PASS**

Observed at: 2026-09-01T12:59:00-03:00

- Target: `C:\Users\MICRO-10\Downloads\Svekte\77\É algo mais sério\Leads\Grandes Lagos`.
- Initial repository recovery: Git initialized on `main`; initial HEAD `fe902c5395156d4d9e655aa96f16d3b127be67b4`.
- Planning workspace: `C:\Users\MICRO-10\gl-operations-c001-planning`, branch `planning/c001-p00`.
- Execution workspace: `C:\Users\MICRO-10\gl-operations-c001-execution`, branch `bm/c001-p00`.
- Approved package commit: `39df638ded354798121c148074d539007828b494`.
- Bianchini source: `https://github.com/felipebianchini2006/bianchini-method`.
- Immutable upstream ref: `684be48b02cdd9448c52e03498fbb9a1fc0403f8`.
- External installation: `C:\Users\MICRO-10\.codex\skills`.
- `_shared/VERSION`: `0.4.0`.
- Release manifest `release_version`: `0.4.0`.
- `_shared/scripts/bm.py`: present.
- Required skills: present; `executar-plano` was discovered and used in slice mode.
- Reinstallation/update was deliberately avoided; no upstream content was vendored into the repository.
- Coherence was approved before execution with digest `d90514a70aad2088999c921a090b4faa7e5951e22146eaf6767feaa8ad84c9b7`.
- Final Bianchini 0.4 `workspace check` against the execution root returned `valid: true`.

Windows compatibility note: the pinned CLI was invoked through the temporary external shim
`C:\Users\MICRO-10\AppData\Local\Temp\bm_windows_compat.py`. The shim only neutralizes
directory `fsync`, which native Windows rejects, and does not modify the installed upstream or
the repository. `PYTHONUTF8=1` was used to preserve paths containing accented characters.

The generic `validate-state` and `status` commands in this pinned CLI target the older
`method_version: 2` project-state format and therefore reject a 0.4 state index. The applicable
0.4 validation path is `workspace check`, which passed. An initial check aimed at the planning root
reported an invalid execution branch; rerunning it against the execution root returned valid.
