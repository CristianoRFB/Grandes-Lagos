package com.gloperations;

import static com.gloperations.IdentityDtos.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class IdentityRepository implements UserDetailsService {
    static final int LIST_LIMIT = 100;
    private final JdbcTemplate jdbc;

    IdentityRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return jdbc.query("""
            select id, username, password_hash, enabled from iam_user
            where normalized_username=lower(btrim(?))
            """, (rs, row) -> new IdentityPrincipal(rs.getObject("id", UUID.class), rs.getString("username"),
                rs.getString("password_hash"), rs.getBoolean("enabled")), username)
            .stream().findFirst().orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    public boolean isActive(UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
            "select exists(select 1 from iam_user where id=? and enabled)", Boolean.class, id));
    }

    public BoundedList<UserSummary> users() {
        List<UserSummary> rows = jdbc.query("""
            select id,username,display_name,enabled from iam_user order by normalized_username,id limit ?
            """, (rs, row) -> new UserSummary(rs.getObject("id", UUID.class), rs.getString("username"),
                rs.getString("display_name"), rs.getBoolean("enabled")), LIST_LIMIT + 1);
        return new BoundedList<>(List.copyOf(rows.subList(0, Math.min(rows.size(), LIST_LIMIT))), LIST_LIMIT, rows.size() > LIST_LIMIT);
    }

    public BoundedList<RoleSummary> roles() {
        // Limit roles before the capability join; no per-role queries or truncated capability lists.
        var roles = new LinkedHashMap<UUID, RoleSummary>();
        jdbc.query("""
            with bounded_roles as (select id,code,display_name,enabled from iam_role order by code,id limit ?)
            select r.id,r.code,r.display_name,r.enabled,rc.capability_code from bounded_roles r
            left join iam_role_capability rc on rc.role_id=r.id order by r.code,r.id,rc.capability_code
            """, rs -> {
                UUID id = rs.getObject("id", UUID.class);
                RoleSummary role = roles.get(id);
                if (role == null) {
                    role = new RoleSummary(id, rs.getString("code"), rs.getString("display_name"), rs.getBoolean("enabled"), new ArrayList<>());
                    roles.put(id, role);
                }
                String capability = rs.getString("capability_code");
                if (capability != null) role.capabilities().add(capability);
            }, LIST_LIMIT + 1);
        List<RoleSummary> all = new ArrayList<>(roles.values());
        List<RoleSummary> bounded = all.subList(0, Math.min(all.size(), LIST_LIMIT)).stream()
            .map(r -> new RoleSummary(r.id(), r.code(), r.displayName(), r.enabled(), List.copyOf(r.capabilities()))).toList();
        return new BoundedList<>(bounded, LIST_LIMIT, all.size() > LIST_LIMIT);
    }
}
