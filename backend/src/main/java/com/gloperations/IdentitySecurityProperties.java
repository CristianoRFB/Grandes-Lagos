package com.gloperations;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Narrow Identity policy seam. Sensitive-action reauthentication has no P01 consumer. */
@ConfigurationProperties("identity.security")
public record IdentitySecurityProperties(
        @DefaultValue("30m") Duration sessionIdleTimeout,
        @DefaultValue("true") boolean sensitiveActionReauth) {
    public IdentitySecurityProperties {
        if (sessionIdleTimeout == null || sessionIdleTimeout.getSeconds() < 1
                || sessionIdleTimeout.getSeconds() > Integer.MAX_VALUE || sessionIdleTimeout.getNano() != 0) {
            throw new IllegalArgumentException("Identity session timeout must be a positive whole number of seconds");
        }
    }

    public int sessionIdleTimeoutSeconds() {
        return Math.toIntExact(sessionIdleTimeout.getSeconds());
    }
}
