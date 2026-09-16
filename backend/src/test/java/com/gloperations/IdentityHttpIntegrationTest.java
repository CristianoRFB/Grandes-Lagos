package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Real embedded servlet container and cookie transport; no MockHttpSession expiration simulation. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "identity.security.session-idle-timeout=2s", "server.servlet.session.cookie.secure=false"
})
@ActiveProfiles("test")
@Testcontainers
class IdentityHttpIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"));
    private static final String SYNTHETIC_PASSWORD = "synthetic-http-fixture-only";
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NEVER).build();
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager transactions;
    private UUID userId;
    private String username;

    @BeforeEach void fixture() {
        userId = UUID.randomUUID();
        username = "synthetic-http-" + userId;
        jdbc.update("insert into iam_user(id,username,normalized_username,display_name,password_hash) values (?,?,?,?,?)",
            userId, username, username, "Synthetic HTTP Identity", encoder.encode(SYNTHETIC_PASSWORD));
        grantRole(1);
    }

    @Test void realHttpCookieJourneyProves401200403RevocationAndLogout() throws Exception {
        assertStatus(send("GET", "/api/identity/users", null, null, null), 401);
        Client bootstrap = csrf(null);
        var success = login(bootstrap, username, SYNTHETIC_PASSWORD);
        assertStatus(success, 200);
        String sessionCookie = cookie(success);
        assertThat(sessionCookie.equals(bootstrap.cookie())).isFalse();
        String setCookie = success.headers().firstValue("set-cookie").orElseThrow().toLowerCase();
        // This test is explicitly HTTP-only. Deployment's Secure=true is checked by IdentityPolicyTest.
        assertThat(setCookie.contains("httponly")).isTrue();
        assertThat(setCookie.contains("samesite=lax")).isTrue();
        assertThat(setCookie.contains("secure")).isFalse();
        assertStatus(send("GET", "/api/identity/users", sessionCookie, null, null), 200);

        new TransactionTemplate(transactions).executeWithoutResult(status -> jdbc.update(
            "update iam_user_role_grant set revoked_at=current_timestamp where user_id=? and revoked_at is null", userId));
        // Exact same immutable cookie string; no reauthentication or new session after commit.
        assertStatus(send("GET", "/api/identity/users", sessionCookie, null, null), 403);
        var me = send("GET", "/api/auth/me", sessionCookie, null, null);
        assertStatus(me, 200);
        assertThat(JSON.readTree(me.body()).path("capabilities").size()).isZero();

        Client logout = csrf(sessionCookie);
        assertStatus(send("POST", "/api/auth/logout", sessionCookie, logout, ""), 204);
        assertStatus(send("GET", "/api/identity/users", sessionCookie, null, null), 401);
    }

    @Test void actualServletIdleExpiryRejectsPriorCookie() throws Exception {
        var authenticated = login(csrf(null), username, SYNTHETIC_PASSWORD);
        assertStatus(authenticated, 200);
        String sessionCookie = cookie(authenticated);
        assertStatus(send("GET", "/api/identity/users", sessionCookie, null, null), 200);
        // Tomcat checks idle expiry when resolving a session on the subsequent request.
        Thread.sleep(2400);
        assertStatus(send("GET", "/api/identity/users", sessionCookie, null, null), 401);
    }

    @Test void realHttpR20LoginCannotManageIdentity() throws Exception {
        jdbc.update("delete from iam_user_role_grant where user_id=?", userId);
        grantRole(20);
        var authenticated = login(csrf(null), username, SYNTHETIC_PASSWORD);
        assertStatus(authenticated, 200);
        String sessionCookie = cookie(authenticated);
        assertStatus(send("GET", "/api/identity/users", sessionCookie, null, null), 403);
        var me = send("GET", "/api/auth/me", sessionCookie, null, null);
        assertStatus(me, 200);
        assertThat(JSON.readTree(me.body()).path("roles").get(0).path("code").asText()).isEqualTo("R20");
    }

    @Test void realCsrfBootstrapProtectsLoginAndRotatesTokenAfterAuthentication() throws Exception {
        String body = JSON.writeValueAsString(Map.of("username", username, "password", SYNTHETIC_PASSWORD));
        assertStatus(send("POST", "/api/auth/login", null, null, body), 403);
        Client before = csrf(null);
        var authenticated = login(before, username, SYNTHETIC_PASSWORD);
        assertStatus(authenticated, 200);
        String sessionCookie = cookie(authenticated);
        assertStatus(send("POST", "/api/auth/logout", sessionCookie, null, ""), 403);
        assertStatus(send("POST", "/api/auth/logout", sessionCookie, before, ""), 403);
        Client refreshed = csrf(sessionCookie);
        assertStatus(send("POST", "/api/auth/logout", sessionCookie, refreshed, ""), 204);
    }

    @Test void invalidCredentialsAndForgedCookieNeverAuthenticate() throws Exception {
        var failed = login(csrf(null), username, "invalid-fixture");
        assertStatus(failed, 401);
        assertThat(JSON.readTree(failed.body()).path("code").asText()).isEqualTo("AUTH_INVALID_CREDENTIALS");
        assertThat(failed.headers().firstValue("location").isEmpty()).isTrue();
        assertStatus(send("GET", "/api/identity/users", "JSESSIONID=synthetic-invalid-cookie", null, null), 401);
    }

    private void grantRole(int number) {
        UUID roleId = new UUID(0, number == 20 ? 0x20 : number);
        jdbc.update("insert into iam_user_role_grant(id,user_id,role_id,scope_type) values (?,?,?,'GLOBAL')", UUID.randomUUID(), userId, roleId);
    }

    private Client csrf(String sessionCookie) throws Exception {
        var response = send("GET", "/api/auth/csrf", sessionCookie, null, null);
        assertStatus(response, 200);
        JsonNode value = JSON.readTree(response.body());
        return new Client(sessionCookie == null ? cookie(response) : sessionCookie,
            value.path("headerName").asText(), value.path("token").asText());
    }

    private HttpResponse<String> login(Client client, String name, String password) throws Exception {
        return send("POST", "/api/auth/login", client.cookie(), client, JSON.writeValueAsString(Map.of("username", name, "password", password)));
    }

    private HttpResponse<String> send(String method, String path, String sessionCookie, Client csrf, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).timeout(Duration.ofSeconds(15));
        if (sessionCookie != null) request.header("Cookie", sessionCookie);
        if (csrf != null) request.header(csrf.headerName(), csrf.token());
        if (body != null) request.header("Content-Type", "application/json");
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String cookie(HttpResponse<String> response) {
        return response.headers().allValues("set-cookie").stream().filter(value -> value.startsWith("JSESSIONID="))
            .findFirst().orElseThrow().split(";", 2)[0];
    }

    private static void assertStatus(HttpResponse<String> response, int status) {
        // Only numeric status is printed on failure, never headers, cookies, tokens, or request bodies.
        assertThat(response.statusCode()).isEqualTo(status);
    }

    private record Client(String cookie, String headerName, String token) {}
}
