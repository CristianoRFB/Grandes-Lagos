package com.gloperations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

class HealthConfigurationTest {
    @Test
    void readinessIncludesDatabaseAndLivenessDoesNot() throws Exception {
        var configuration = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml")).getFirst();
        assertThat(configuration.getProperty("management.endpoint.health.group.readiness.include"))
                .isEqualTo("readinessState,db");
        assertThat(configuration.getProperty("management.endpoint.health.group.liveness.include"))
                .isEqualTo("livenessState");
        assertThat(configuration.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("health");
    }
}
