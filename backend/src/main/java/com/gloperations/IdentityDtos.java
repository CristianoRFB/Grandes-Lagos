package com.gloperations;

import java.util.List;
import java.util.UUID;

/** Explicit public projections: never serialize persistence credentials or Authentication. */
public final class IdentityDtos {
    private IdentityDtos() {}
    public record UserSummary(UUID id, String username, String displayName, boolean enabled) {}
    public record RoleSummary(UUID id, String code, String displayName, boolean enabled, List<String> capabilities) {}
    public record RoleAccess(UUID id, String code, String displayName, String scopeType, String scopeReference) {}
    public record CapabilityAccess(String code, String scopeType, String scopeReference) {}
    public record CurrentIdentity(UUID id, String username, String displayName,
            List<RoleAccess> roles, List<CapabilityAccess> capabilities) {}
    public record BoundedList<T>(List<T> items, int limit, boolean hasMore) {}
    public record CsrfSummary(String headerName, String token) {}
}
