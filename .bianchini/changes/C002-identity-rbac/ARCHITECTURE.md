# Arquitetura global C002/P01

Spring Security usa `SecurityFilterChain` com CSRF e sessão server-side; grants
são consultados por JDBC a cada requisição protegida, permitindo revogação sem
novo login. PostgreSQL/Flyway é a fonte de verdade; V0001 fica imutável e
V0002/V0003 são aditivos.

JWT stateless, JPA/Hibernate, wildcard R01, `/api/test/**` e credenciais fixas
são rejeitados. O custo de consultas frescas é aceito em favor da revogação.
