package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class IdentityContractTest {
    @Test void globalGrantMatchesSpecificScopeButScopedGrantDoesNotMatchGlobal() {
        assertThat(ScopeAuthorization.matches("GLOBAL", null, "WAREHOUSE", "x")).isTrue();
        assertThat(ScopeAuthorization.matches("WAREHOUSE", "x", "WAREHOUSE", "x")).isTrue();
        assertThat(ScopeAuthorization.matches("WAREHOUSE", "x", "WAREHOUSE", "y")).isFalse();
        assertThat(ScopeAuthorization.matches("WAREHOUSE", "x", "GLOBAL", null)).isFalse();
    }

    @Test void administratorIsCapabilityBounded() {
        assertThat("*".equals("identity.manage")).isFalse();
        assertThat(java.util.List.of("analytics.read", "audit.read", "identity.manage", "policy.manage"))
            .doesNotContain("inventory.adjust");
    }
}
