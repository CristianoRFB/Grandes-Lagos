---
{
  "id": "P00",
  "status": "planned",
  "result": "Ambiente técnico P00 reproduzível e verificável.",
  "depends_on": [],
  "provides": ["p00_runtime", "health_liveness_contract", "health_readiness_contract", "frontend_http_contract", "migration_chain"],
  "consumes": [],
  "owns": ["runtime_bootstrap"],
  "touches": ["platform_operations", "postgresql_runtime", "technical_frontend_shell"],
  "requirements": ["AC-001", "AC-002", "AC-003", "AC-004", "AC-005", "AC-006", "AC-007", "AC-008", "AC-009", "AC-010", "AC-011", "AC-012", "AC-013", "AC-014", "AC-015", "AC-016", "AC-017", "AC-018", "AC-019", "AC-020", "AC-021", "AC-022"],
  "acceptance": [
    "Backend verifica com Java 21 e Maven Wrapper 3.9.16.",
    "Frontend instala e compila com Node 24.20.0, npm 11.19.0 e Vite 8.1.0.",
    "PostgreSQL 18.6 e Flyway aplicam V0001 no-op sem drift.",
    "Liveness independe do DB e readiness depende do DB/migrations.",
    "Docker Compose e scripts passam smoke limpo e repetível.",
    "Job GitHub Actions p00 executa verde.",
    "Nenhum secret, dado real, P01+ ou área fora do escopo é introduzido."
  ],
  "verifications": [
    "backend/mvnw.cmd -B verify",
    "npm ci",
    "npm run build",
    "docker compose --env-file infra/env/.env.example -f infra/compose.yml config",
    "scripts/p00-up.sh && scripts/p00-verify.sh && scripts/p00-down.sh",
    "GitHub Actions job p00 success"
  ],
  "model_delta": {
    "modules": {"add": [{"id": "platform_operations", "owns": ["runtime_bootstrap"]}]},
    "interfaces": {"add": [
      {"id": "health_liveness", "provider": "platform_operations", "consumers": []},
      {"id": "health_readiness", "provider": "platform_operations", "consumers": []},
      {"id": "p00_scripts", "provider": "platform_operations", "consumers": []},
      {"id": "frontend_http", "provider": "platform_operations", "consumers": []}
    ]},
    "capabilities": {"add": [
      {"id": "reproducible_bootstrap", "owner": "platform_operations"},
      {"id": "technical_frontend_shell", "owner": "platform_operations"}
    ]},
    "contracts": {"add": [
      {"id": "p00_runtime", "provider": "platform_operations", "consumers": []},
      {"id": "health_liveness_contract", "provider": "platform_operations", "consumers": []},
      {"id": "health_readiness_contract", "provider": "platform_operations", "consumers": []},
      {"id": "frontend_http_contract", "provider": "platform_operations", "consumers": []},
      {"id": "migration_chain", "provider": "platform_operations", "consumers": []}
    ]},
    "ownership": {"add": [{"id": "runtime_bootstrap", "owner": "platform_operations"}]},
    "data": {"add": [
      {"id": "postgresql_runtime", "owner": "platform_operations", "classification": "synthetic_technical"},
      {"id": "flyway_schema_history", "owner": "flyway", "classification": "framework_owned"}
    ]},
    "journeys": {"add": [{"id": "technical_bootstrap_journey", "path": ["platform_operations", "reproducible_bootstrap", "postgresql_runtime", "health_readiness", "technical_frontend_shell", "frontend_http"]}]},
    "invariants": {"add": [
      {"id": "liveness_db_independent", "statement": "Liveness não depende do PostgreSQL."},
      {"id": "readiness_db_dependent", "statement": "Readiness depende do PostgreSQL e migrations válidas."},
      {"id": "no_business_domain_p00", "statement": "P00 não contém domínio, RBAC ou dados reais."}
    ]},
    "effects": {"add": [{"id": "ephemeral_local_stack", "owner": "platform_operations", "external": false}]}
  },
  "migrations": [
    {"id": "V0001__bootstrap", "after": [], "destructive": false, "compatibility": "comment-only/no-op; forward-only"}
  ],
  "external_effects": [],
  "future_constraints": [
    "P01 permanece fora deste change.",
    "Migration publicada é corrigida somente por forward-fix.",
    "S1 exige AC-001..AC-022 todos PASS."
  ],
  "execution": "slice",
  "review": "per_slice"
}
---

# P00 — Bootstrap reproduzível

## Implementação

1. Materializar o workspace Bianchini e manter STATE em S0 durante a execução.
2. Criar backend Spring Boot mínimo com Actuator, JDBC, Flyway e testes PostgreSQL 18.6.
3. Criar shell frontend React/TypeScript/Vite mínimo.
4. Criar Dockerfiles, Compose, env sintético e scripts canônicos.
5. Criar job CI sequencial e executar todos os gates disponíveis.
6. Registrar evidências reais, SUMMARY e acceptance AC-001..AC-024.

## Rollback e recovery

Teardown remove somente containers e volumes efêmeros do projeto P00. Artefatos
de código são revertidos por Git; migrations publicadas recebem forward-fix.

## Done

O plano só conclui quando a implementação e evidência correspondem ao delta
aprovado. CI local não substitui execução GitHub Actions real.

