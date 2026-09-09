---
{
  "approval": {
    "approved_at": "2026-09-09T21:18:40+00:00",
    "approved_by": "00 — ROUTER / ORQUESTRADOR",
    "digest": "7500d455472e283934892670336cd43e661c99ffdd5fcdfaea596b3643f8d011"
  },
  "change": "C002-identity-rbac",
  "digest": "7500d455472e283934892670336cd43e661c99ffdd5fcdfaea596b3643f8d011",
  "findings": [],
  "impact": null,
  "model": {
    "current": "5f117ffe83b10ef3e6ff1e9f397a62c43684aaefbfe7f893921e9f7a9c286247",
    "expected": "8672153488bd5eccbec883cdf07d8f3ef3e43f355414feefc5863592106625d0"
  },
  "plans": [
    {
      "acceptance": [
        "Login, proteção e logout demonstram 401/200/403 e revogação."
      ],
      "consumes": [
        "p00_runtime",
        "health_readiness_contract"
      ],
      "depends_on": [],
      "external_effects": [],
      "future_constraints": [],
      "id": "P01",
      "migrations": [],
      "model_delta": {
        "capabilities": {
          "add": [
            {
              "id": "identity_access_foundation",
              "owner": "identity_access"
            }
          ]
        },
        "contracts": {
          "add": [
            {
              "consumers": [],
              "id": "identity_api",
              "provider": "identity_access"
            }
          ]
        },
        "data": {
          "add": [
            {
              "classification": "synthetic_identity",
              "id": "iam_user",
              "owner": "identity_access"
            },
            {
              "classification": "reference",
              "id": "iam_role",
              "owner": "identity_access"
            },
            {
              "classification": "reference",
              "id": "iam_capability",
              "owner": "identity_access"
            }
          ]
        },
        "interfaces": {
          "add": [
            {
              "consumers": [],
              "id": "auth_login",
              "provider": "identity_access"
            },
            {
              "consumers": [],
              "id": "identity_users",
              "provider": "identity_access"
            }
          ]
        },
        "invariants": {
          "add": [
            {
              "id": "identity_r01_no_wildcard",
              "statement": "R01 não possui wildcard."
            },
            {
              "id": "identity_no_p02",
              "statement": "Nenhuma entidade P02 é criada."
            }
          ]
        },
        "modules": {
          "add": [
            {
              "id": "identity_access",
              "owns": [
                "identity_access"
              ]
            }
          ]
        },
        "ownership": {
          "add": [
            {
              "id": "identity_access",
              "owner": "identity_access"
            }
          ]
        }
      },
      "owns": [
        "identity_access"
      ],
      "provides": [
        "identity_auth",
        "identity_authorization",
        "identity_api"
      ],
      "requirements": [
        "AC-P01-001",
        "AC-P01-002",
        "AC-P01-003",
        "AC-P01-004"
      ],
      "touches": [
        "identity_access",
        "postgresql_runtime"
      ],
      "verifications": [
        "Testcontainers PostgreSQL 18.6 e gate CI P01 verdes."
      ]
    }
  ],
  "schema_version": 1,
  "semantic": {
    "available": true,
    "findings": [],
    "input_digest": "b7bd6fdcdea8b3073a49964ab4d417eae280a1ec3ebc3a0b14b6d46fbcd2110b",
    "prompt_digest": "28091a4a5dad910c7e23811387da778d487a5317716e4f8909a332cb49b13de8",
    "sources_digest": "0816fd09ef439e4948fa6496d4015b6712efec656b12244a4f0491780788edc9"
  },
  "stale_plans": [],
  "status": "approved",
  "structural_only": false,
  "updated_at": "2026-09-09T21:18:40+00:00"
}
---

# Coerência

Status: approved.

## Impact Radius

Ainda não calculado para uma mudança executada.
