package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(properties = "identity.security.session-idle-timeout=7m")
@AutoConfigureMockMvc(print = MockMvcPrint.NONE, printOnlyOnFailure = false)
@ActiveProfiles("test")
@Testcontainers
class IdentitySecurityIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"));
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired CapabilityAuthorizationService authorization;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    // Isolated synthetic credential: request and result dumping are disabled above.
    private static final String SYNTHETIC_PASSWORD = "synthetic-fixture-only";
    private static final UUID R01 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private UUID userId;
    private String username;

    @BeforeEach void fixture() {
        userId = UUID.randomUUID();
        username = "synthetic-" + userId;
        jdbc.update("insert into iam_user(id,username,normalized_username,display_name,password_hash) values (?,?,?,?,?)",
            userId, username, username, "Synthetic Identity", encoder.encode(SYNTHETIC_PASSWORD));
        grant("GLOBAL", null);
    }

    @Test void exactSameProtectedEndpointDistinguishes401200403() throws Exception {
        mvc.perform(get("/api/identity/users")).andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.code").value("AUTH_UNAUTHENTICATED"))
            .andExpect(header().doesNotExist("Location"));
        var session = login();
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk());
        revokeGrants();
        jdbc.update("insert into iam_user_role_grant(id,user_id,role_id,scope_type) values (?,?,?,'GLOBAL')",
            UUID.randomUUID(), userId, UUID.fromString("00000000-0000-0000-0000-000000000020"));
        var noGrantSession = login();
        mvc.perform(get("/api/identity/users").session(noGrantSession)).andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.code").value("AUTH_FORBIDDEN"));
    }

    @Test void jsonLoginEstablishesFixationProtectedSessionAndAppliesConfiguredTimeout() throws Exception {
        CsrfClient client = csrf(null);
        String before = client.session().getId();
        var session = login(client, username, SYNTHETIC_PASSWORD, 200);
        assertThat(session.getId().equals(before)).isFalse();
        assertThat(session.getMaxInactiveInterval()).isEqualTo(420);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.username").value(username))
            .andExpect(jsonPath("$.displayName").value("Synthetic Identity"))
            .andExpect(jsonPath("$.roles[0].code").value("R01"))
            .andExpect(jsonPath("$.capabilities.length()").value(4));
    }

    @Test void invalidAndUnknownCredentialsHaveSameGeneric401Problem() throws Exception {
        var known = loginResult(csrf(null), username, "invalid-fixture", 401);
        var unknown = loginResult(csrf(null), "synthetic-unknown", "invalid-fixture", 401);
        assertThat(known.getResponse().getContentAsString().equals(unknown.getResponse().getContentAsString())).isTrue();
        assertThat(known.getResponse().getContentType()).startsWith("application/problem+json");
        assertThat(JSON.readTree(known.getResponse().getContentAsString()).path("code").asText()).isEqualTo("AUTH_INVALID_CREDENTIALS");
        assertThat(known.getRequest().getSession(false)).isNull();
        mvc.perform(get("/api/identity/users")).andExpect(status().isUnauthorized());
    }

    @Test void loginAndLogoutRejectMissingOrInvalidCsrf() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody(username, SYNTHETIC_PASSWORD)))
            .andExpect(status().isForbidden());
        var initial = csrf(null);
        mvc.perform(post("/api/auth/login").session(initial.session()).header(initial.headerName(), "invalid-token")
            .contentType(MediaType.APPLICATION_JSON).content(loginBody(username, SYNTHETIC_PASSWORD))).andExpect(status().isForbidden());
        var session = login();
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk());
    }

    @Test void malformedJsonLoginReturnsSafe400() throws Exception {
        var client = csrf(null);
        mvc.perform(post("/api/auth/login").session(client.session()).header(client.headerName(), client.token())
            .contentType(MediaType.APPLICATION_JSON).content("{broken"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("AUTH_INVALID_REQUEST"));
    }

    @Test void committedRevocationIsEffectiveOnVeryNextRequestOfSameSession() throws Exception {
        var session = login();
        String sessionId = session.getId();
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk());
        revokeGrants();
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isForbidden());
        assertThat(session.getId().equals(sessionId)).isTrue();
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
            .andExpect(jsonPath("$.roles").isEmpty()).andExpect(jsonPath("$.capabilities").isEmpty());
    }

    @Test void logoutClearsContextInvalidatesSessionAndOldSessionCannotAuthorize() throws Exception {
        var session = login();
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk());
        var client = csrf(session);
        mvc.perform(post("/api/auth/logout").session(session).header(client.headerName(), client.token()))
            .andExpect(status().isNoContent()).andExpect(header().doesNotExist("Location"));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isUnauthorized());
    }

    @Test void invalidatedSessionCannotReachMe() throws Exception {
        var session = login();
        session.invalidate();
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test void disabledUserCannotLoginAndDisablingSameSessionFailsClosed() throws Exception {
        var session = login();
        jdbc.update("update iam_user set enabled=false where id=?", userId);
        loginResult(csrf(null), username, SYNTHETIC_PASSWORD, 401);
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isUnauthorized());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test void disabledRoleRevokesCapabilityWithoutInvalidatingIdentity() throws Exception {
        var session = login();
        try {
            jdbc.update("update iam_role set enabled=false where id=?", R01);
            mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isEmpty()).andExpect(jsonPath("$.capabilities").isEmpty());
        } finally {
            jdbc.update("update iam_role set enabled=true where id=?", R01);
        }
    }

    @Test void identityUsesStableIdWhenLoginNameChanges() throws Exception {
        var session = login();
        String renamed = "renamed-" + userId;
        jdbc.update("update iam_user set username=?,normalized_username=? where id=?", renamed, renamed, userId);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString())).andExpect(jsonPath("$.username").value(renamed));
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk());
    }

    @Test void r01UsesRealCatalogAndNeverActsAsWildcard() throws Exception {
        var actual = jdbc.queryForList("select capability_code from iam_role_capability where role_id=? order by capability_code", String.class, R01);
        assertThat(actual).containsExactly("analytics.read", "audit.read", "identity.manage", "policy.manage");
        assertThat(authorization.allows(username, "identity.manage", "GLOBAL", null)).isTrue();
        for (String capability : new String[] {"inventory.adjust", "purchase.approve", "maintenance.execute", "*", "ALL", "nonexistent.read"}) {
            assertThat(authorization.allows(username, capability, "GLOBAL", null)).as(capability).isFalse();
        }
        mvc.perform(get("/api/identity/users").session(login())).andExpect(status().isOk());
    }

    @Test void realGrantScopeMatchingRejectsWrongOrInvalidRequirements() throws Exception {
        String ref = UUID.randomUUID().toString();
        assertThat(authorization.allows(username, "identity.manage", "WAREHOUSE", ref)).isTrue();
        assertThat(authorization.allows(username, "identity.manage", "UNKNOWN", ref)).isFalse();
        assertThat(authorization.allows(username, "identity.manage", "GLOBAL", ref)).isFalse();
        assertThat(authorization.allows(username, "identity.manage", "WAREHOUSE", "invalid")).isFalse();
        revokeGrants();
        grant("WAREHOUSE", ref);
        assertThat(authorization.allows(username, "identity.manage", "WAREHOUSE", ref)).isTrue();
        assertThat(authorization.allows(username, "identity.manage", "WAREHOUSE", UUID.randomUUID().toString())).isFalse();
        assertThat(authorization.allows(username, "identity.manage", "COST_CENTER", ref)).isFalse();
        assertThat(authorization.allows(username, "identity.manage", "GLOBAL", null)).isFalse();
        mvc.perform(get("/api/identity/users").session(login())).andExpect(status().isForbidden());
    }

    @Test void usersAndRolesContainRealBoundedSafeDtos() throws Exception {
        var session = login();
        for (String endpoint : new String[] {"/api/auth/me", "/api/identity/users", "/api/identity/roles"}) {
            var result = mvc.perform(get(endpoint).session(session)).andExpect(status().isOk()).andReturn();
            String body = result.getResponse().getContentAsString().toLowerCase();
            assertThat(body.contains("password") || body.contains("hash") || body.contains(SYNTHETIC_PASSWORD)).isFalse();
        }
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk())
            .andExpect(jsonPath("$.limit").value(100)).andExpect(jsonPath("$.items").isArray());
        mvc.perform(get("/api/identity/roles").session(session)).andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(20)).andExpect(jsonPath("$.items[0].code").value("R01"))
            .andExpect(jsonPath("$.items[0].capabilities.length()").value(4));
        String hash = jdbc.queryForObject("select password_hash from iam_user where id=?", String.class, userId);
        assertThat(hash != null && hash.startsWith("{bcrypt}") && encoder.matches(SYNTHETIC_PASSWORD, hash)).isTrue();
    }

    @Test void unknownRoutesAndUnsupportedMethodsFailClosed() throws Exception {
        var session = login();
        for (String endpoint : new String[] {"/api/unknown", "/api/identity/unknown", "/actuator/env", "/actuator/health"}) {
            mvc.perform(get(endpoint).session(session)).andExpect(status().isForbidden());
        }
        var client = csrf(session);
        mvc.perform(post("/api/identity/users").session(session).header(client.headerName(), client.token()))
            .andExpect(status().isForbidden());
    }

    @Test void authorizationDatabaseFailureNeverFallsBackToCachedGrant() throws Exception {
        var session = login();
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk());
        try {
            jdbc.execute("alter table iam_user_role_grant rename to iam_user_role_grant_unavailable");
            mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isForbidden());
            mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AUTH_SERVICE_UNAVAILABLE"));
        } finally {
            jdbc.execute("alter table iam_user_role_grant_unavailable rename to iam_user_role_grant");
        }
    }

    private void grant(String type, String reference) {
        jdbc.update("insert into iam_user_role_grant(id,user_id,role_id,scope_type,scope_reference) values (?,?,?,?,?)",
            UUID.randomUUID(), userId, R01, type, reference == null ? null : UUID.fromString(reference));
    }

    private void revokeGrants() {
        new TransactionTemplate(transactions).executeWithoutResult(status -> jdbc.update(
            "update iam_user_role_grant set revoked_at=current_timestamp where user_id=? and revoked_at is null", userId));
    }

    private MockHttpSession login() throws Exception { return login(csrf(null), username, SYNTHETIC_PASSWORD, 200); }

    private MockHttpSession login(CsrfClient client, String name, String password, int expected) throws Exception {
        return (MockHttpSession) loginResult(client, name, password, expected).getRequest().getSession(false);
    }

    private MvcResult loginResult(CsrfClient client, String name, String password, int expected) throws Exception {
        return mvc.perform(post("/api/auth/login").session(client.session()).header(client.headerName(), client.token())
            .contentType(MediaType.APPLICATION_JSON).content(loginBody(name, password)))
            .andExpect(status().is(expected)).andExpect(header().doesNotExist("Location")).andReturn();
    }

    private static String loginBody(String name, String password) {
        return JSON.writeValueAsString(java.util.Map.of("username", name, "password", password));
    }

    private CsrfClient csrf(MockHttpSession session) throws Exception {
        var request = get("/api/auth/csrf");
        if (session != null && !session.isInvalid()) request.session(session);
        MvcResult result = mvc.perform(request).andExpect(status().isOk()).andReturn();
        JsonNode body = JSON.readTree(result.getResponse().getContentAsString());
        return new CsrfClient((MockHttpSession) result.getRequest().getSession(false), body.path("headerName").asText(), body.path("token").asText());
    }

    private record CsrfClient(MockHttpSession session, String headerName, String token) {}
}
