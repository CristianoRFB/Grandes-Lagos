package com.gloperations;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

/** Revalidate account status on every protected API request; probes and logout remain independent. */
final class ActiveIdentityFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveIdentityFilter.class);
    private final IdentityRepository identities;
    private final IdentityProblemResponses problems;

    ActiveIdentityFilter(IdentityRepository identities, IdentityProblemResponses problems) {
        this.identities = identities;
        this.problems = problems;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api/") || path.equals("/api/auth/login")
            || path.equals("/api/auth/csrf") || path.equals("/api/auth/logout");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            try {
                if (!(authentication.getPrincipal() instanceof IdentityPrincipal principal) || !identities.isActive(principal.id())) {
                    new SecurityContextLogoutHandler().logout(request, response, authentication);
                    LOG.info("action=identity_revalidate outcome=denied");
                    problems.write(response, HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHENTICATED", "Authentication required");
                    return;
                }
            } catch (DataAccessException unavailable) {
                LOG.warn("action=identity_revalidate outcome=unavailable");
                problems.write(response, HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SERVICE_UNAVAILABLE", "Identity service unavailable");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
