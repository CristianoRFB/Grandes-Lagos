package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class BootstrapIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"));

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Flyway flyway;

    @Test
    void appliesNoOpMigrationOnceWithoutChecksumDrift() {
        Integer historyRows = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where script = 'V0001__bootstrap.sql' and success",
                Integer.class);
        Integer checksumBefore = jdbc.queryForObject(
                "select checksum from flyway_schema_history where script = 'V0001__bootstrap.sql'",
                Integer.class);

        assertThat(historyRows).isEqualTo(1);
        assertThat(flyway.migrate().migrationsExecuted).isZero();

        Integer checksumAfter = jdbc.queryForObject(
                "select checksum from flyway_schema_history where script = 'V0001__bootstrap.sql'",
                Integer.class);
        assertThat(checksumAfter).isEqualTo(checksumBefore);
    }

    @Test
    void rebuildsAnIndependentCleanDatabaseFromVersionedMigration() throws Exception {
        jdbc.execute("create database gl_operations_rebuild");
        String rebuildUrl = POSTGRES.getJdbcUrl().replace(
                "/" + POSTGRES.getDatabaseName(), "/gl_operations_rebuild");
        Flyway rebuild = Flyway.configure()
                .dataSource(rebuildUrl, POSTGRES.getUsername(), POSTGRES.getPassword())
                .load();

        assertThat(rebuild.migrate().migrationsExecuted).isEqualTo(1);
        assertThat(rebuild.migrate().migrationsExecuted).isZero();

        try (Connection connection = DriverManager.getConnection(
                        rebuildUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(
                        "select count(*) from flyway_schema_history where script = 'V0001__bootstrap.sql' and success")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getInt(1)).isEqualTo(1);
        }
    }
}
