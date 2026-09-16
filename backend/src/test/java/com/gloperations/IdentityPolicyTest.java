package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class IdentityPolicyTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner().withUserConfiguration(Policies.class);

    @Test void canonicalPolicyDefaultsAreThirtyMinutesAndReauthTrue() {
        context.run(application -> {
            var policy = application.getBean(IdentitySecurityProperties.class);
            assertThat(policy.sessionIdleTimeout()).isEqualTo(Duration.ofMinutes(30));
            assertThat(policy.sessionIdleTimeoutSeconds()).isEqualTo(1800);
            assertThat(policy.sensitiveActionReauth()).isTrue();
        });
    }

    @Test void typedPolicyOverridesBindWithoutScatteredConstants() {
        context.withPropertyValues("identity.security.session-idle-timeout=9m", "identity.security.sensitive-action-reauth=false")
            .run(application -> {
                var policy = application.getBean(IdentitySecurityProperties.class);
                assertThat(policy.sessionIdleTimeoutSeconds()).isEqualTo(540);
                assertThat(policy.sensitiveActionReauth()).isFalse();
            });
    }

    @Test void invalidSessionPoliciesFailStartup() {
        for (String invalid : new String[] {"0s", "-1m", "500ms", "2147483648s"}) {
            context.withPropertyValues("identity.security.session-idle-timeout=" + invalid)
                .run(application -> assertThat(application).hasFailed());
        }
    }

    @Test void deploymentCookieDefaultsAndContainerTimeoutShareIdentityPolicy() {
        context.withInitializer(new ConfigDataApplicationContextInitializer())
            .withPropertyValues("identity.security.session-idle-timeout=9m")
            .run(application -> {
                var environment = application.getEnvironment();
                assertThat(environment.getProperty("server.servlet.session.timeout")).isEqualTo("9m");
                assertThat(environment.getProperty("server.servlet.session.cookie.http-only", Boolean.class)).isTrue();
                assertThat(environment.getProperty("server.servlet.session.cookie.secure", Boolean.class)).isTrue();
                assertThat(environment.getProperty("server.servlet.session.cookie.same-site")).isEqualTo("lax");
            });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(IdentitySecurityProperties.class)
    static class Policies {}
}
