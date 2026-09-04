# Pesquisa aplicada — modo repo_only

As versões e decisões foram aprovadas no C001/P00 e não foram reabertas:

- Java 21; Maven Wrapper 3.9.16; Spring Boot 4.1.1.
- Flyway 12.4.0, PostgreSQL JDBC 42.7.13 e Testcontainers 2.0.5 gerenciados pelo Spring Boot.
- Node 24.20.0; npm 11.19.0; Vite 8.1.0; React + TypeScript.
- PostgreSQL/Docker image `postgres:18.6`.
- GitHub Actions em `ubuntu-24.04`, checkout v4, setup-java v5 e setup-node v4.

O host inicialmente apresenta Java 8, Node 24.11.1, npm 11.6.4 e Docker
indisponível. Essas diferenças bloqueiam gates dependentes, não a criação dos
artefatos. Nenhuma fonte mutável foi usada para alterar os pins aprovados.

