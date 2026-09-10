package com.gloperations;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CapabilityAuthorizationService {
    private final JdbcTemplate jdbc;
    public CapabilityAuthorizationService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public boolean allows(String username, String capability, String scopeType, String scopeReference) {
        try {
            Integer count = jdbc.queryForObject("select count(*) from iam_user u join iam_user_role_grant g on g.user_id=u.id join iam_role r on r.id=g.role_id and r.enabled join iam_role_capability rc on rc.role_id=r.id where u.normalized_username=lower(trim(?)) and u.enabled and g.revoked_at is null and rc.capability_code=? and (g.scope_type='GLOBAL' or (g.scope_type=? and g.scope_reference=cast(? as uuid))) and (? <> 'GLOBAL' or g.scope_type='GLOBAL')", Integer.class, username, capability, scopeType, scopeReference, scopeType);
            return count != null && count > 0;
        } catch (RuntimeException ex) { return false; }
    }
}
