package com.gloperations;

import java.util.Set;
import java.util.UUID;

public final class ScopeAuthorization {
    private static final Set<String> TYPES = Set.of("GLOBAL", "ORGANIZATION", "BUSINESS_UNIT", "OPERATIONAL_AREA", "WAREHOUSE", "COST_CENTER");
    private ScopeAuthorization() {}
    public static boolean valid(String type, String reference) {
        if (type == null || !TYPES.contains(type)) return false;
        if ("GLOBAL".equals(type)) return reference == null;
        if (reference == null) return false;
        try {
            return UUID.fromString(reference).toString().equalsIgnoreCase(reference);
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    public static boolean matches(String grantType, String grantRef, String requiredType, String requiredRef) {
        if (!valid(grantType, grantRef) || !valid(requiredType, requiredRef)) return false;
        if ("GLOBAL".equals(grantType)) return true;
        return grantType.equals(requiredType) && !"GLOBAL".equals(requiredType)
            && UUID.fromString(grantRef).equals(UUID.fromString(requiredRef));
    }
}
