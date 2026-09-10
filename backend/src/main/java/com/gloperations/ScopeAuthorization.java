package com.gloperations;

public final class ScopeAuthorization {
    private ScopeAuthorization() {}
    public static boolean matches(String grantType, String grantRef, String requiredType, String requiredRef) {
        if ("GLOBAL".equals(grantType)) return true;
        return grantType != null && grantType.equals(requiredType) && grantRef != null && grantRef.equals(requiredRef) && !"GLOBAL".equals(requiredType);
    }
}
