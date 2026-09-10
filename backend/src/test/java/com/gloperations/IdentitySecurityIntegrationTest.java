package com.gloperations;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class IdentitySecurityIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18.6"));
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    private String username;
    @BeforeEach void fixture() {
        username = "test-" + UUID.randomUUID();
        UUID user = UUID.randomUUID();
        jdbc.update("insert into iam_user(id,username,normalized_username,display_name,password_hash) values (?,?,?,?,?)", user, username, username, "Synthetic", encoder.encode("secret"));
        jdbc.update("insert into iam_user_role_grant(id,user_id,role_id,scope_type) values (?,?,?,'GLOBAL')", UUID.randomUUID(), user, UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
    @Test void unauthenticatedIs401() throws Exception { mvc.perform(get("/api/identity/users")).andExpect(status().isUnauthorized()); }
    @Test void authorizedSessionIs200ThenRevocationIs403() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login").param("username", username).param("password", "secret").with(csrf())).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isOk());
        jdbc.update("update iam_user_role_grant set revoked_at=current_timestamp where user_id=(select id from iam_user where username=?)", username);
        mvc.perform(get("/api/identity/users").session(session)).andExpect(status().isForbidden());
    }
    @Test void invalidLoginIs401() throws Exception { mvc.perform(post("/api/auth/login").param("username", username).param("password", "wrong").with(csrf())).andExpect(status().isUnauthorized()); }
}
