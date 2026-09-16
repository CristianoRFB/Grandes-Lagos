package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class IdentityMigrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"));

    private static final UUID R01 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Map<String, String> PUBLISHED_MIGRATION_SHA256 = Map.of(
            "V0001__bootstrap.sql", "4a97f977ec3287e1794bebd61e3e92d62338ed7482c991097183cef89e89bc82",
            "V0002__identity_access_schema.sql", "d328f19d6261e6265b8c2f27225cff50bacb82816ce7a76b026d3324e559f1bc",
            "V0003__identity_access_reference_data.sql", "897823c9892305dffad544b022a1c7f4e42a3062c8109f2e34bfb36f56674177");

    private static final Set<String> CAPABILITIES = Set.of(
            "identity.manage", "policy.manage", "catalog.write", "inventory.read", "inventory.reserve",
            "inventory.move", "inventory.adjust", "inventory.count", "inventory.adjust.approve",
            "requisition.manage", "purchase.request", "purchase.source", "purchase.order.issue",
            "purchase.approve", "receiving.execute", "production.plan", "production.execute",
            "maintenance.manage", "maintenance.execute", "housekeeping.manage", "housekeeping.execute",
            "integration.manage", "integration.reconcile", "analytics.read", "analytics.export", "audit.read");

    private static final Map<String, String> ROLE_NAMES = Map.ofEntries(
            Map.entry("R01", "System Administrator"), Map.entry("R02", "IT / Integration Operator"),
            Map.entry("R03", "Operations Manager"), Map.entry("R04", "Warehouse Manager"),
            Map.entry("R05", "Warehouse Operator"), Map.entry("R06", "Buyer"),
            Map.entry("R07", "Purchase Approver"), Map.entry("R08", "Receiving Operator"),
            Map.entry("R09", "Production Planner"), Map.entry("R10", "Kitchen / Production Operator"),
            Map.entry("R11", "Maintenance Manager"), Map.entry("R12", "Maintenance Technician"),
            Map.entry("R13", "Housekeeping Manager"), Map.entry("R14", "Housekeeping Operator"),
            Map.entry("R15", "Inventory Counter"), Map.entry("R16", "Inventory Adjustment Approver"),
            Map.entry("R17", "Data Analyst"), Map.entry("R18", "Auditor"),
            Map.entry("R19", "Executive Viewer"), Map.entry("R20", "Demo Viewer"));

    private static final Map<String, Set<String>> ROLE_CAPABILITIES = Map.ofEntries(
            Map.entry("R01", Set.of("analytics.read", "audit.read", "identity.manage", "policy.manage")),
            Map.entry("R02", Set.of("analytics.read", "audit.read", "integration.manage", "integration.reconcile")),
            Map.entry("R03", Set.of("analytics.export", "analytics.read", "audit.read", "housekeeping.manage", "maintenance.manage", "requisition.manage")),
            Map.entry("R04", Set.of("analytics.read", "inventory.count", "inventory.move", "inventory.read", "inventory.reserve", "requisition.manage")),
            Map.entry("R05", Set.of("inventory.move", "inventory.read", "inventory.reserve", "requisition.manage")),
            Map.entry("R06", Set.of("analytics.read", "inventory.read", "purchase.order.issue", "purchase.request", "purchase.source")),
            Map.entry("R07", Set.of("analytics.read", "audit.read", "purchase.approve")),
            Map.entry("R08", Set.of("analytics.read", "inventory.read", "receiving.execute")),
            Map.entry("R09", Set.of("analytics.read", "inventory.read", "production.plan")),
            Map.entry("R10", Set.of("inventory.read", "production.execute")),
            Map.entry("R11", Set.of("analytics.read", "inventory.read", "inventory.reserve", "maintenance.manage")),
            Map.entry("R12", Set.of("inventory.read", "inventory.reserve", "maintenance.execute")),
            Map.entry("R13", Set.of("analytics.read", "housekeeping.manage", "maintenance.manage")),
            Map.entry("R14", Set.of("housekeeping.execute")),
            Map.entry("R15", Set.of("inventory.count")),
            Map.entry("R16", Set.of("audit.read", "inventory.adjust.approve", "inventory.read")),
            Map.entry("R17", Set.of("analytics.export", "analytics.read")),
            Map.entry("R18", Set.of("analytics.export", "analytics.read", "audit.read")),
            Map.entry("R19", Set.of("analytics.read")), Map.entry("R20", Set.of("analytics.read")));

    private JdbcTemplate jdbc;
    private Flyway flyway;
    private int initialMigrationsExecuted;

    @BeforeEach
    void migrateAnIsolatedEmptySchema() {
        String schema = "identity_contract_" + UUID.randomUUID().toString().replace("-", "");
        String separator = POSTGRES.getJdbcUrl().contains("?") ? "&" : "?";
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl() + separator + "currentSchema=" + schema,
                POSTGRES.getUsername(), POSTGRES.getPassword());
        flyway = Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema).load();
        initialMigrationsExecuted = flyway.migrate().migrationsExecuted;
        jdbc = new JdbcTemplate(dataSource);
    }

    @Test
    void cleanMigrationAndRetryPreserveAllAppliedChecksums() {
        Map<String, Integer> checksums = migrationChecksums();
        assertThat(checksums).containsKeys(PUBLISHED_MIGRATION_SHA256.keySet().toArray(String[]::new));
        assertThat(initialMigrationsExecuted).isEqualTo(checksums.size()).isGreaterThanOrEqualTo(3);
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(migrationChecksums()).isEqualTo(checksums);
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(jdbc.queryForObject(
                "select count(*) from flyway_schema_history where not success", Integer.class)).isZero();
    }

    @Test
    void publishedMigrationsRemainByteForByteIntact() throws Exception {
        for (Map.Entry<String, String> migration : PUBLISHED_MIGRATION_SHA256.entrySet()) {
            try (InputStream input = getClass().getResourceAsStream("/db/migration/" + migration.getKey())) {
                assertThat(input).as(migration.getKey()).isNotNull();
                String checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()));
                assertThat(checksum).as(migration.getKey()).isEqualTo(migration.getValue());
            }
        }
    }

    @Test
    void referenceCatalogContainsExactlyTheCanonicalCapabilitiesAndRoles() {
        assertThat(jdbc.queryForList("select code from iam_capability", String.class))
                .hasSize(26).containsExactlyInAnyOrderElementsOf(CAPABILITIES);
        Map<String, String> names = jdbc.query("select code, display_name from iam_role", rs -> {
            Map<String, String> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getString(2));
            }
            return result;
        });
        assertThat(names).hasSize(20).isEqualTo(ROLE_NAMES);
        assertThat(jdbc.queryForObject(
                "select count(*) from iam_role where enabled and system_role", Integer.class)).isEqualTo(20);
    }

    @Test
    void roleCapabilityMatrixContainsExactlyAll62CanonicalEdgesAndNoAdminWildcard() {
        Set<String> expectedEdges = ROLE_CAPABILITIES.entrySet().stream()
                .flatMap(role -> role.getValue().stream().map(capability -> role.getKey() + ":" + capability))
                .collect(Collectors.toSet());
        List<String> actualEdges = jdbc.queryForList(
                "select r.code || ':' || rc.capability_code from iam_role r join iam_role_capability rc on rc.role_id = r.id",
                String.class);
        assertThat(expectedEdges).hasSize(62);
        assertThat(actualEdges).hasSize(62).containsExactlyInAnyOrderElementsOf(expectedEdges);
        assertThat(jdbc.queryForList(
                "select capability_code from iam_role_capability where role_id = ?", String.class, R01))
                .containsExactlyInAnyOrder("analytics.read", "audit.read", "identity.manage", "policy.manage");
    }

    @Test
    void migrationsCreateOnlyIdentityTablesAndNeverRuntimeUsersOrGrants() {
        assertThat(jdbc.queryForList(
                "select table_name from information_schema.tables where table_schema = current_schema() and table_type = 'BASE TABLE'",
                String.class)).containsExactlyInAnyOrder(
                        "flyway_schema_history", "iam_user", "iam_role", "iam_capability",
                        "iam_role_capability", "iam_user_role_grant");
        assertThat(jdbc.queryForObject("select count(*) from iam_user", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from iam_user_role_grant", Integer.class)).isZero();
    }

    @Test
    void allIdentityIdsAndOpaqueScopeReferencesUseUuidWithoutP02ForeignKeys() {
        List<String> uuidColumns = jdbc.queryForList(
                "select table_name || '.' || column_name from information_schema.columns where table_schema = current_schema() and data_type = 'uuid'",
                String.class);
        assertThat(uuidColumns).containsExactlyInAnyOrder(
                "iam_user.id", "iam_role.id", "iam_role_capability.role_id", "iam_user_role_grant.id",
                "iam_user_role_grant.user_id", "iam_user_role_grant.role_id", "iam_user_role_grant.scope_reference");
        assertThat(jdbc.queryForList("""
                select source.relname || '->' || target.relname
                from pg_constraint c
                join pg_class source on source.oid = c.conrelid
                join pg_class target on target.oid = c.confrelid
                join pg_namespace n on n.oid = source.relnamespace
                where c.contype = 'f' and n.nspname = current_schema()
                """, String.class)).containsExactlyInAnyOrder(
                        "iam_role_capability->iam_role", "iam_role_capability->iam_capability",
                        "iam_user_role_grant->iam_user", "iam_user_role_grant->iam_role");
    }

    @Test
    void databaseEnforcesNormalizedUsernameCaseAndWhitespaceUniqueness() {
        insertUser("Alice", "alice");
        assertConstraint("23505", () -> insertUser("  ALICE  ", "alice"));
    }

    @Test
    void databaseRejectsForgedNormalizedUsernameOnInsertAndUpdate() {
        UUID userId = insertUser("Alice", "alice");
        assertConstraint("23514", () -> insertUser("ALICE", "different-normalization"));
        assertConstraint("23514", () -> jdbc.update(
                "update iam_user set normalized_username = 'different-normalization' where id = ?", userId));
        assertConstraint("23514", () -> jdbc.update(
                "update iam_user set username = 'Bob' where id = ?", userId));
        assertThat(jdbc.queryForObject("select normalized_username from iam_user where id = ?", String.class, userId))
                .isEqualTo("alice");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLOBAL", "ORGANIZATION", "BUSINESS_UNIT", "OPERATIONAL_AREA", "WAREHOUSE", "COST_CENTER"})
    void everyCanonicalScopeIsPersistedWithAnOpaqueUuidAndGlobalHasNullReference(String scope) {
        UUID user = insertUser("scope-user", "scope-user");
        UUID reference = "GLOBAL".equals(scope) ? null : UUID.randomUUID();
        UUID grant = insertGrant(user, R01, scope, reference);
        assertThat(jdbc.queryForObject("select scope_type from iam_user_role_grant where id = ?", String.class, grant))
                .isEqualTo(scope);
        assertThat(jdbc.queryForObject("select scope_reference from iam_user_role_grant where id = ?", UUID.class, grant))
                .isEqualTo(reference);
    }

    @ParameterizedTest
    @CsvSource({"GLOBAL,true", "ORGANIZATION,false", "BUSINESS_UNIT,false", "OPERATIONAL_AREA,false",
            "WAREHOUSE,false", "COST_CENTER,false", "UNKNOWN,true", "global,false"})
    void databaseRejectsInvalidScopeTypeOrReferenceNullability(String scope, boolean withReference) {
        UUID user = insertUser("invalid-scope", "invalid-scope");
        UUID reference = withReference ? UUID.randomUUID() : null;
        assertConstraint("23514", () -> insertGrant(user, R01, scope, reference));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLOBAL", "WAREHOUSE"})
    void databaseRejectsDuplicateActiveIdenticalGrantsIncludingNullGlobalReference(String scope) {
        UUID user = insertUser("duplicate-user", "duplicate-user");
        UUID reference = "GLOBAL".equals(scope) ? null : UUID.randomUUID();
        insertGrant(user, R01, scope, reference);
        assertConstraint("23505", () -> insertGrant(user, R01, scope, reference));
        if (!"GLOBAL".equals(scope)) {
            insertGrant(user, R01, scope, UUID.randomUUID());
            insertGrant(user, R01, "COST_CENTER", reference);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLOBAL", "WAREHOUSE"})
    void revokedGrantCanBeRegrantedButCannotBeReactivatedOverAnActiveGrant(String scope) {
        UUID user = insertUser("regrant-user", "regrant-user");
        UUID reference = "GLOBAL".equals(scope) ? null : UUID.randomUUID();
        UUID original = insertGrant(user, R01, scope, reference);
        jdbc.update("update iam_user_role_grant set revoked_at = current_timestamp where id = ?", original);
        insertGrant(user, R01, scope, reference);
        assertThat(jdbc.queryForObject("select count(*) from iam_user_role_grant where user_id = ? and revoked_at is null",
                Integer.class, user)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from iam_user_role_grant where user_id = ? and revoked_at is not null",
                Integer.class, user)).isEqualTo(1);
        assertConstraint("23505", () -> jdbc.update("update iam_user_role_grant set revoked_at = null where id = ?", original));
    }

    @Test
    void roleCapabilitiesHaveUniqueAssociationsAndEnforcedForeignKeys() {
        assertConstraint("23505", () -> jdbc.update(
                "insert into iam_role_capability(role_id, capability_code) values (?, 'identity.manage')", R01));
        assertConstraint("23503", () -> jdbc.update(
                "insert into iam_role_capability(role_id, capability_code) values (?, 'identity.manage')", UUID.randomUUID()));
        assertConstraint("23503", () -> jdbc.update(
                "insert into iam_role_capability(role_id, capability_code) values (?, 'unknown.capability')", R01));
    }

    @Test
    void userRoleGrantsRejectMissingUsersAndRoles() {
        UUID user = insertUser("foreign-key-user", "foreign-key-user");
        assertConstraint("23503", () -> insertGrant(UUID.randomUUID(), R01, "GLOBAL", null));
        assertConstraint("23503", () -> insertGrant(user, UUID.randomUUID(), "GLOBAL", null));
    }

    private Map<String, Integer> migrationChecksums() {
        return jdbc.query("select script, checksum from flyway_schema_history where type = 'SQL' and success order by installed_rank", rs -> {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getInt(2));
            }
            return result;
        });
    }

    private UUID insertUser(String username, String normalizedUsername) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into iam_user(id, username, normalized_username, display_name, password_hash) values (?, ?, ?, ?, ?)",
                id, username, normalizedUsername, "Synthetic migration fixture", "{bcrypt}synthetic-unused-fixture");
        return id;
    }

    private UUID insertGrant(UUID user, UUID role, String scope, UUID reference) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into iam_user_role_grant(id, user_id, role_id, scope_type, scope_reference) values (?, ?, ?, ?, ?)",
                id, user, role, scope, reference);
        return id;
    }

    private void assertConstraint(String sqlState, Runnable action) {
        DataIntegrityViolationException failure = assertThrows(DataIntegrityViolationException.class, action::run);
        assertThat(failure.getMostSpecificCause()).isInstanceOf(SQLException.class);
        assertThat(((SQLException) failure.getMostSpecificCause()).getSQLState()).isEqualTo(sqlState);
    }
}
