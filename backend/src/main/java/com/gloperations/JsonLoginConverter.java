package com.gloperations;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** JSON transport adapter only; Spring's authentication provider verifies credentials. */
final class JsonLoginConverter implements AuthenticationConverter {
    private static final int MAX_BODY_BYTES = 8192;
    private final ObjectMapper json;
    JsonLoginConverter(ObjectMapper json) { this.json = json; }

    @Override
    public Authentication convert(HttpServletRequest request) {
        try {
            if (request.getContentType() == null || !MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(request.getContentType()))) {
                throw new InvalidLoginRequest();
            }
            byte[] body = request.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
            if (body.length > MAX_BODY_BYTES) throw new InvalidLoginRequest();
            JsonNode document = json.readTree(body);
            if (document == null || !document.isObject() || document.size() != 2
                    || !document.path("username").isString() || !document.path("password").isString()) {
                throw new InvalidLoginRequest();
            }
            String username = document.path("username").asText();
            String password = document.path("password").asText();
            if (username.isBlank() || username.length() > 120 || password.isEmpty() || password.length() > 1024) {
                throw new InvalidLoginRequest();
            }
            return UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        } catch (IOException | RuntimeException malformed) {
            // Never retain a JSON parsing exception, since its message can contain credential input.
            throw new InvalidLoginRequest();
        }
    }

    static final class InvalidLoginRequest extends AuthenticationException {
        private static final long serialVersionUID = 1L;
        InvalidLoginRequest() { super("Invalid login request"); }
    }
}
