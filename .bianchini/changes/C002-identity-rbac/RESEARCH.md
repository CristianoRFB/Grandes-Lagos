# Pesquisa C002/P01

Baseline local: Spring Boot/Flyway/PostgreSQL/Testcontainers do C001. O desenho
aplica Spring Security com CSRF habilitado, `HttpSession` server-side e JDBC sem
JPA. Escopos: GLOBAL, ORGANIZATION, BUSINESS_UNIT, OPERATIONAL_AREA, WAREHOUSE
e COST_CENTER; sem tabelas ou FKs P02. Testes de integração reais comprovam os
contratos.
