package com.gloperations;

import static com.gloperations.IdentityDtos.*;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {
    private final CapabilityAuthorizationService authorization;
    private final IdentityRepository identities;
    IdentityController(CapabilityAuthorizationService authorization, IdentityRepository identities) {
        this.authorization = authorization;
        this.identities = identities;
    }
    @GetMapping("/api/auth/csrf")
    CsrfSummary csrf(CsrfToken token) { return new CsrfSummary(token.getHeaderName(), token.getToken()); }
    @GetMapping("/api/auth/me")
    CurrentIdentity me(Authentication authentication) {
        return authorization.currentIdentity(((IdentityPrincipal) authentication.getPrincipal()).id());
    }
    @GetMapping("/api/identity/users")
    BoundedList<UserSummary> users() { return identities.users(); }
    @GetMapping("/api/identity/roles")
    BoundedList<RoleSummary> roles() { return identities.roles(); }

    @ExceptionHandler(DataAccessException.class)
    ProblemDetail databaseUnavailable() { return IdentityProblemResponses.unavailable(); }
}
