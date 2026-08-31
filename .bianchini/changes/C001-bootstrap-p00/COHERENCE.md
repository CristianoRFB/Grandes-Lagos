---
{
  "approval": {
    "approved_at": "2026-08-31T23:22:16+00:00",
    "approved_by": "00 — ROUTER / ORQUESTRADOR",
    "digest": "d90514a70aad2088999c921a090b4faa7e5951e22146eaf6767feaa8ad84c9b7"
  },
  "change": "C001-bootstrap-p00",
  "digest": "d90514a70aad2088999c921a090b4faa7e5951e22146eaf6767feaa8ad84c9b7",
  "findings": [],
  "impact": null,
  "model": {
    "current": "d51ceb25e1ba2ab66e5ea1b297f5cea13629e187d4c1edc2b1ce9c174825334a",
    "expected": "5f117ffe83b10ef3e6ff1e9f397a62c43684aaefbfe7f893921e9f7a9c286247"
  },
  "plans": [
    {
      "acceptance": [
        "Backend verifica com Java 21 e Maven Wrapper 3.9.16.",
        "Frontend instala e compila com Node 24.20.0, npm 11.19.0 e Vite 8.1.0.",
        "PostgreSQL 18.6 e Flyway aplicam V0001 no-op sem drift.",
        "Liveness independe do DB e readiness depende do DB/migrations.",
        "Docker Compose e scripts passam smoke limpo e repetível.",
        "Job GitHub Actions p00 executa verde.",
        "Nenhum secret, dado real, P01+ ou área fora do escopo é introduzido."
      ],
      "consumes": [],
      "depends_on": [],
      "external_effects": [],
      "future_constraints": [
        "P01 permanece fora deste change.",
        "Migration publicada é corrigida somente por forward-fix.",
        "S1 exige AC-001..AC-022 todos PASS."
      ],
      "id": "P00",
      "migrations": [
        {
          "after": [],
          "compatibility": "comment-only/no-op; forward-only",
          "destructive": false,
          "id": "V0001__bootstrap"
        }
      ],
      "model_delta": {
        "capabilities": {
          "add": [
            {
              "id": "reproducible_bootstrap",
              "owner": "platform_operations"
            },
            {
              "id": "technical_frontend_shell",
              "owner": "platform_operations"
            }
          ]
        },
        "contracts": {
          "add": [
            {
              "consumers": [],
              "id": "p00_runtime",
              "provider": "platform_operations"
            },
            {
              "consumers": [],
              "id": "health_liveness_contract",
              "provider": "platform_operations"
            },
            {
              "consumers": [],
              "id": "health_readiness_contract",
              "provider": "platform_operations"
            },
            {
              "consumers": [],
              "id": "frontend_http_contract",
              "provider": "platform_operations"
            },
            {
              "consumers": [],
              "id": "migration_chain",
              "provider": "platform_operations"
            }
          ]
        },
        "data": {
          "add": [
            {
              "classification": "synthetic_technical",
              "id": "postgresql_runtime",
              "owner": "platform_operations"
            },
            {
              "classification": "framework_owned",
              "id": "flyway_schema_history",
              "owner": "flyway"
            }
          ]
        },
        "effects": {
          "add": [
            {
              "external": false,
              "id": "ephemeral_local_stack",
              "owner": "platform_operations"
            }
          ]
        },
        "interfaces": {
          "add": [
            {
              "consumers": [],
              "id": "health_liveness",
              "provider": "platform_operations"
            },
            {
              "consumers": [],
              "id": "health_readiness",
              "provider": "platform_operations"
            },
            {
              "consumers": [],
              "id": "p00_scripts",
              "provider": "platform_operations"
            },
            {
              "consumers": [],
              "id": "frontend_http",
              "provider": "platform_operations"
            }
          ]
        },
        "invariants": {
          "add": [
            {
              "id": "liveness_db_independent",
              "statement": "Liveness não depende do PostgreSQL."
            },
            {
              "id": "readiness_db_dependent",
              "statement": "Readiness depende do PostgreSQL e migrations válidas."
            },
            {
              "id": "no_business_domain_p00",
              "statement": "P00 não contém domínio, RBAC ou dados reais."
            }
          ]
        },
        "journeys": {
          "add": [
            {
              "id": "technical_bootstrap_journey",
              "path": [
                "platform_operations",
                "reproducible_bootstrap",
                "postgresql_runtime",
                "health_readiness",
                "technical_frontend_shell",
                "frontend_http"
              ]
            }
          ]
        },
        "modules": {
          "add": [
            {
              "id": "platform_operations",
              "owns": [
                "runtime_bootstrap"
              ]
            }
          ]
        },
        "ownership": {
          "add": [
            {
              "id": "runtime_bootstrap",
              "owner": "platform_operations"
            }
          ]
        }
      },
      "owns": [
        "runtime_bootstrap"
      ],
      "provides": [
        "p00_runtime",
        "health_liveness_contract",
        "health_readiness_contract",
        "frontend_http_contract",
        "migration_chain"
      ],
      "requirements": [
        "AC-001",
        "AC-002",
        "AC-003",
        "AC-004",
        "AC-005",
        "AC-006",
        "AC-007",
        "AC-008",
        "AC-009",
        "AC-010",
        "AC-011",
        "AC-012",
        "AC-013",
        "AC-014",
        "AC-015",
        "AC-016",
        "AC-017",
        "AC-018",
        "AC-019",
        "AC-020",
        "AC-021",
        "AC-022"
      ],
      "touches": [
        "platform_operations",
        "postgresql_runtime",
        "technical_frontend_shell"
      ],
      "verifications": [
        "backend/mvnw.cmd -B verify",
        "npm ci",
        "npm run build",
        "docker compose --env-file infra/env/.env.example -f infra/compose.yml config",
        "scripts/p00-up.sh && scripts/p00-verify.sh && scripts/p00-down.sh",
        "GitHub Actions job p00 success"
      ]
    }
  ],
  "schema_version": 1,
  "semantic": {
    "available": true,
    "findings": [],
    "input_digest": "4673fe5eb4839c76cf8027829484d82936f77ba35cfe6834083a87e4bd8e041f",
    "prompt_digest": "5e9ca5821cfaa277ad388b043e06b09ec8b4e3ba59081e52f25b959f9c676b71",
    "sources_digest": "dda77090f9052f696caadcce4ed66b6b0b4a7b13cbdd78b64ba5c1681b149b52"
  },
  "stale_plans": [],
  "status": "approved",
  "structural_only": false,
  "updated_at": "2026-08-31T23:22:16+00:00"
}
---

# Coerência

Status: approved.

## Impact Radius

Ainda não calculado para uma mudança executada.
