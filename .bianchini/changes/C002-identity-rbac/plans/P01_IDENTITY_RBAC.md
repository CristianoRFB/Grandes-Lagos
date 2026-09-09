---
{
  "id": "P01",
  "status": "planned",
  "result": "Identity/RBAC server-side verificável com revogação dinâmica.",
  "depends_on": [],
  "provides": ["identity_auth", "identity_authorization", "identity_api"],
  "consumes": ["p00_runtime", "health_readiness_contract"],
  "owns": ["identity_access"],
  "touches": ["identity_access", "postgresql_runtime"],
  "requirements": ["AC-P01-001", "AC-P01-002", "AC-P01-003", "AC-P01-004"],
  "acceptance": ["Login, proteção e logout demonstram 401/200/403 e revogação."],
  "verifications": ["Testcontainers PostgreSQL 18.6 e gate CI P01 verdes."],
  "model_delta": {"modules": {"add": [{"id": "identity_access", "owns": ["identity_access"]}]}, "interfaces": {"add": [{"id": "auth_login", "provider": "identity_access", "consumers": []}, {"id": "identity_users", "provider": "identity_access", "consumers": []}]}, "capabilities": {"add": [{"id": "identity_access_foundation", "owner": "identity_access"}]}, "contracts": {"add": [{"id": "identity_api", "provider": "identity_access", "consumers": []}]}, "ownership": {"add": [{"id": "identity_access", "owner": "identity_access"}]}, "data": {"add": [{"id": "iam_user", "owner": "identity_access", "classification": "synthetic_identity"}, {"id": "iam_role", "owner": "identity_access", "classification": "reference"}, {"id": "iam_capability", "owner": "identity_access", "classification": "reference"}]}, "invariants": {"add": [{"id": "identity_r01_no_wildcard", "statement": "R01 não possui wildcard."}, {"id": "identity_no_p02", "statement": "Nenhuma entidade P02 é criada."}]}}
}
---

# P01 — Identity/RBAC

Implementar autenticação, sessão server-side, CSRF, autorização por capability
e grants com escopo, migrations V0002/V0003, endpoints mínimos e testes reais.
P02 e closeout ficam fora desta execução.
