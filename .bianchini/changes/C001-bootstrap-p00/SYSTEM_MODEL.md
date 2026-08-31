---
{
  "schema_version": 1,
  "modules": [
    {"id": "platform_operations", "owns": ["runtime_bootstrap"]}
  ],
  "interfaces": [
    {"id": "health_liveness", "provider": "platform_operations", "consumers": []},
    {"id": "health_readiness", "provider": "platform_operations", "consumers": []},
    {"id": "p00_scripts", "provider": "platform_operations", "consumers": []},
    {"id": "frontend_http", "provider": "platform_operations", "consumers": []}
  ],
  "capabilities": [
    {"id": "reproducible_bootstrap", "owner": "platform_operations"},
    {"id": "technical_frontend_shell", "owner": "platform_operations"}
  ],
  "contracts": [
    {"id": "p00_runtime", "provider": "platform_operations", "consumers": []},
    {"id": "health_liveness_contract", "provider": "platform_operations", "consumers": []},
    {"id": "health_readiness_contract", "provider": "platform_operations", "consumers": []},
    {"id": "frontend_http_contract", "provider": "platform_operations", "consumers": []},
    {"id": "migration_chain", "provider": "platform_operations", "consumers": []}
  ],
  "ownership": [
    {"id": "runtime_bootstrap", "owner": "platform_operations"}
  ],
  "data": [
    {"id": "postgresql_runtime", "owner": "platform_operations", "classification": "synthetic_technical"},
    {"id": "flyway_schema_history", "owner": "flyway", "classification": "framework_owned"}
  ],
  "integrations": [],
  "journeys": [
    {"id": "technical_bootstrap_journey", "path": ["platform_operations", "reproducible_bootstrap", "postgresql_runtime", "health_readiness", "technical_frontend_shell", "frontend_http"]}
  ],
  "invariants": [
    {"id": "liveness_db_independent", "statement": "Liveness não depende do PostgreSQL."},
    {"id": "readiness_db_dependent", "statement": "Readiness depende do PostgreSQL e migrations válidas."},
    {"id": "no_business_domain_p00", "statement": "P00 não contém domínio, RBAC ou dados reais."}
  ],
  "effects": [
    {"id": "ephemeral_local_stack", "owner": "platform_operations", "external": false}
  ]
}
---

# Modelo final esperado após P00

O delta é exclusivamente técnico e pertence a M23 — Platform Operations. O
modelo de domínio da baseline não é duplicado e nenhuma capacidade P01+ é ativada.

