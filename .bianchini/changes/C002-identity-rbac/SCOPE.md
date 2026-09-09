# Escopo C002/P01 — Identity/RBAC

Implementar autenticação e autorização server-side no backend Spring Boot, com
sessões HTTP JDBC, CSRF ativo, capabilities/grants com escopo, migrations
V0002/V0003 e endpoints mínimos de login, logout, sessão, usuários e papéis.

Aceite: fluxos 401/200/403, revogação dinâmica na mesma sessão, R01 sem
wildcard, timeout, regressão P00 e gate Testcontainers PostgreSQL 18.6.

Fora: frontend novo, P02/domínio de negócio, JPA/Hibernate, OAuth/OIDC, dados
reais e fechamento Bianchini (`plan complete`/`cycle-close`).
