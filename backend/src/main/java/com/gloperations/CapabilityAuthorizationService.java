package com.gloperations;

import static com.gloperations.IdentityDtos.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Service;

@Service
public class CapabilityAuthorizationService {
    private static final Logger LOG = LoggerFactory.getLogger(CapabilityAuthorizationService.class);
    private static final Pattern CAPABILITY = Pattern.compile("[a-z]+(?:[._][a-z]+)+");
    private final JdbcTemplate jdbc;
    public CapabilityAuthorizationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public AuthorizationManager<RequestAuthorizationContext> require(String capability, String type, String reference) {
        return (authentication, context) -> {
            var current = authentication.get();
            boolean allowed = current != null && current.isAuthenticated()
                && current.getPrincipal() instanceof IdentityPrincipal principal
                && allows(principal.id(), capability, type, reference);
            if (!allowed) LOG.info("action=capability_check outcome=denied capability={}", capability);
            return new AuthorizationDecision(allowed);
        };
    }

    public boolean allows(UUID userId, String capability, String scopeType, String scopeReference) {
        if (userId == null) return false;
        return allowsByIdentity("u.id=?", userId, capability, scopeType, scopeReference);
    }

    /** Compatibility seam for callers that have not yet resolved a stable principal. */
    public boolean allows(String username, String capability, String scopeType, String scopeReference) {
        if (username == null || username.isBlank()) return false;
        return allowsByIdentity("u.normalized_username=lower(btrim(?))", username, capability, scopeType, scopeReference);
    }

    private boolean allowsByIdentity(String identityPredicate, Object identity, String capability, String type, String reference) {
        if (!validCapability(capability) || !ScopeAuthorization.valid(type, reference)) return false;
        try {
            // Only the fixed internal identity predicate is composed. All caller data is parameterized.
            return jdbc.query("""
                select g.scope_type,g.scope_reference from iam_user u
                join iam_user_role_grant g on g.user_id=u.id and g.revoked_at is null
                join iam_role r on r.id=g.role_id and r.enabled
                join iam_role_capability rc on rc.role_id=r.id
                where %s and u.enabled and rc.capability_code=?
                and ((g.scope_type='GLOBAL' and g.scope_reference is null)
                    or (g.scope_type=? and g.scope_reference=cast(? as uuid))) limit 1
                """.formatted(identityPredicate), (rs, row) -> ScopeAuthorization.matches(rs.getString("scope_type"), rs.getString("scope_reference"), type, reference),
                identity, capability, type, reference).stream().anyMatch(Boolean.TRUE::equals);
        } catch (DataAccessException unavailable) {
            LOG.warn("action=capability_check outcome=unavailable");
            return false;
        }
    }

    public CurrentIdentity currentIdentity(UUID id) {
        // A single statement produces one committed snapshot. No cached authorities or N+1 grant reads.
        var identities = new ArrayList<UserSummary>(1);
        var roles = new LinkedHashSet<RoleAccess>();
        var capabilities = new LinkedHashSet<CapabilityAccess>();
        jdbc.query("""
            select u.id,u.username,u.display_name,r.id as role_id,r.code as role_code,r.display_name as role_name,
                g.scope_type,g.scope_reference,rc.capability_code
            from iam_user u
            left join iam_user_role_grant g on g.user_id=u.id and g.revoked_at is null
            left join iam_role r on r.id=g.role_id and r.enabled
            left join iam_role_capability rc on rc.role_id=r.id
            where u.id=? and u.enabled order by r.code,g.scope_type,g.scope_reference,rc.capability_code
            """, rs -> {
                if (identities.isEmpty()) identities.add(new UserSummary(rs.getObject("id", UUID.class),
                    rs.getString("username"), rs.getString("display_name"), true));
                UUID role = rs.getObject("role_id", UUID.class);
                String type = rs.getString("scope_type");
                String reference = rs.getString("scope_reference");
                if (role != null && ScopeAuthorization.valid(type, reference)) {
                    roles.add(new RoleAccess(role, rs.getString("role_code"), rs.getString("role_name"), type, reference));
                    String capability = rs.getString("capability_code");
                    if (validCapability(capability)) capabilities.add(new CapabilityAccess(capability, type, reference));
                }
            }, id);
        if (identities.isEmpty()) throw new AccessDeniedException("Identity unavailable");
        UserSummary user = identities.getFirst();
        return new CurrentIdentity(user.id(), user.username(), user.displayName(), List.copyOf(roles), List.copyOf(capabilities));
    }

    private static boolean validCapability(String capability) {
        return capability != null && capability.length() <= 80 && CAPABILITY.matcher(capability).matches();
    }
}
