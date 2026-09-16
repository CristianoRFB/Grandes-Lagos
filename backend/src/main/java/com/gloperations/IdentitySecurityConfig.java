package com.gloperations;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(IdentitySecurityProperties.class)
public class IdentitySecurityConfig {
    private static final Logger LOG = LoggerFactory.getLogger(IdentitySecurityConfig.class);
    @Bean
    PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
    @Bean
    SecurityFilterChain security(HttpSecurity http, IdentityRepository identities,
            CapabilityAuthorizationService authorization, IdentitySecurityProperties policy,
            PasswordEncoder encoder, ObjectMapper json, IdentityProblemResponses problems) throws Exception {
        var provider = new DaoAuthenticationProvider(identities);
        provider.setPasswordEncoder(encoder);
        var manager = new ProviderManager(provider);
        var contexts = new HttpSessionSecurityContextRepository();
        var csrfTokens = new HttpSessionCsrfTokenRepository();
        var login = new AbstractAuthenticationProcessingFilter(
            PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/login"), manager) {};
        login.setAuthenticationConverter(new JsonLoginConverter(json));
        login.setSecurityContextRepository(contexts);
        login.setSessionAuthenticationStrategy(new CompositeSessionAuthenticationStrategy(List.of(
            new ChangeSessionIdAuthenticationStrategy(),
            new CsrfAuthenticationStrategy(csrfTokens),
            (authentication, request, response) -> request.getSession().setMaxInactiveInterval(policy.sessionIdleTimeoutSeconds()))));
        login.setAuthenticationSuccessHandler((request, response, authentication) -> {
            var principal = (IdentityPrincipal) authentication.getPrincipal();
            LOG.info("action=login outcome=success user_id={}", principal.id());
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            json.writeValue(response.getOutputStream(), Map.of("id", principal.id()));
        });
        login.setAuthenticationFailureHandler((request, response, failure) -> {
            new SecurityContextLogoutHandler().logout(request, response, null);
            LOG.info("action=login outcome=denied");
            if (failure instanceof JsonLoginConverter.InvalidLoginRequest) {
                problems.write(response, HttpStatus.BAD_REQUEST, "AUTH_INVALID_REQUEST", "Invalid login request");
            } else if (failure instanceof AuthenticationServiceException) {
                problems.write(response, HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SERVICE_UNAVAILABLE", "Identity service unavailable");
            } else {
                problems.write(response, HttpStatus.UNAUTHORIZED, "AUTH_INVALID_CREDENTIALS", "Invalid credentials");
            }
        });

        http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokens))
            .securityContext(context -> context.securityContextRepository(contexts).requireExplicitSave(true))
            .requestCache(cache -> cache.disable())
            .authorizeHttpRequests(rules -> rules
                .requestMatchers(HttpMethod.GET, "/actuator/health/liveness", "/actuator/health/readiness", "/api/auth/csrf").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/identity/users", "/api/identity/roles")
                    .access(authorization.require("identity.manage", "GLOBAL", null))
                .anyRequest().denyAll())
            .logout(logout -> logout.logoutRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/logout"))
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    LOG.info("action=logout outcome=success");
                    response.setStatus(HttpStatus.NO_CONTENT.value());
                }))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) ->
                    problems.write(response, HttpStatus.UNAUTHORIZED, "AUTH_UNAUTHENTICATED", "Authentication required"))
                .accessDeniedHandler((request, response, exception) -> {
                    LOG.info("action=authorization outcome=denied");
                    problems.write(response, HttpStatus.FORBIDDEN, "AUTH_FORBIDDEN", "Access denied");
                }))
            .sessionManagement(session -> session.sessionFixation().changeSessionId())
            .addFilterAt(login, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new ActiveIdentityFilter(identities, problems), AuthorizationFilter.class);
        return http.build();
    }
}
