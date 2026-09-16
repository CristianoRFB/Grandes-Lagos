package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import org.springframework.jdbc.core.JdbcTemplate;

class IdentityContractTest {
    private static final String REF = "10000000-0000-0000-0000-000000000001";
    private static final String OTHER = "10000000-0000-0000-0000-000000000002";
    @Test void globalGrantMatchesSpecificScopeButScopedGrantDoesNotMatchGlobal() {
        assertThat(ScopeAuthorization.matches("GLOBAL", null, "GLOBAL", null)).isTrue();
        for (String type : new String[] {"ORGANIZATION", "BUSINESS_UNIT", "OPERATIONAL_AREA", "WAREHOUSE", "COST_CENTER"}) {
            assertThat(ScopeAuthorization.matches("GLOBAL", null, type, REF)).isTrue();
            assertThat(ScopeAuthorization.matches(type, REF, type, REF)).isTrue();
            assertThat(ScopeAuthorization.matches(type, REF, type, OTHER)).isFalse();
            assertThat(ScopeAuthorization.matches(type, REF, "GLOBAL", null)).isFalse();
        }
        assertThat(ScopeAuthorization.matches("WAREHOUSE", REF, "COST_CENTER", REF)).isFalse();
    }

    @Test void invalidScopeRepresentationsFailClosedEvenWithGlobalGrant() {
        assertThat(ScopeAuthorization.matches("GLOBAL", REF, "GLOBAL", null)).isFalse();
        assertThat(ScopeAuthorization.matches("GLOBAL", null, "GLOBAL", REF)).isFalse();
        assertThat(ScopeAuthorization.matches("GLOBAL", null, "WAREHOUSE", null)).isFalse();
        assertThat(ScopeAuthorization.matches("GLOBAL", null, "WAREHOUSE", "not-a-uuid")).isFalse();
        assertThat(ScopeAuthorization.matches("GLOBAL", null, "WAREHOUSE", "1-1-1-1-1")).isFalse();
        assertThat(ScopeAuthorization.matches("GLOBAL", null, "UNKNOWN", REF)).isFalse();
        assertThat(ScopeAuthorization.matches("GLOBAL", null, null, null)).isFalse();
        assertThat(ScopeAuthorization.matches("WAREHOUSE", null, "WAREHOUSE", null)).isFalse();
    }

    @Test void invalidAuthorizationInputDoesNotReachDatabase() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        var service = new CapabilityAuthorizationService(jdbc);
        assertThat(service.allows("synthetic", "identity.manage", "INVALID", null)).isFalse();
        assertThat(service.allows("synthetic", "*", "GLOBAL", null)).isFalse();
        assertThat(service.allows("synthetic", "ALL", "GLOBAL", null)).isFalse();
        verifyNoInteractions(jdbc);
    }
}
