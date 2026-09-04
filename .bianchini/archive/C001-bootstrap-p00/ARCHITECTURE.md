# Arquitetura global — C001/P00

## Decisão

Preservar o Modular Monolith e introduzir somente a fundação técnica transversal
de M23 — Platform Operations: uma aplicação Spring Boot, um shell React/Vite,
PostgreSQL canônico, migrations Flyway, containers locais e CI sequencial.

## Seams públicos

- `GET /actuator/health/liveness`: processo vivo, independente do banco.
- `GET /actuator/health/readiness`: pronto somente com PostgreSQL e migrations.
- `scripts/p00-up.sh`, `p00-verify.sh` e `p00-down.sh`.
- HTTP do shell frontend técnico.

## Dados e migrations

O único objeto persistente permitido é `flyway_schema_history`, pertencente ao
framework. `V0001__bootstrap.sql` é comment-only/no-op e forward-only. Não há
schema, tabela, seed ou entidade de negócio.

## Recuperação

Ambientes P00 contêm apenas dados sintéticos e podem ser derrubados e recriados.
Migration publicada nunca é reescrita; correções futuras usam forward-fix.

## Alternativas rejeitadas

Microservices, JPA/ORM, brokers, reverse proxy arquitetural, Kubernetes, stack de
observabilidade, autenticação e qualquer fundação P01+ foram rejeitados por
estarem fora do escopo aprovado.

