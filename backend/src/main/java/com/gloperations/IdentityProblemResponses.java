package com.gloperations;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class IdentityProblemResponses {
    private final ObjectMapper json;
    IdentityProblemResponses(ObjectMapper json) { this.json = json; }

    public void write(HttpServletResponse response, HttpStatus status, String code, String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/problem+json");
        response.setHeader("Cache-Control", "no-store");
        json.writeValue(response.getOutputStream(), Map.of("type", "about:blank", "title", status.getReasonPhrase(),
            "status", status.value(), "detail", detail, "code", code));
    }

    static ProblemDetail unavailable() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Identity service unavailable");
        problem.setProperty("code", "AUTH_SERVICE_UNAVAILABLE");
        return problem;
    }
}
