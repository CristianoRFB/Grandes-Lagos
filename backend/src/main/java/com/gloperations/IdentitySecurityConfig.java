package com.gloperations;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

@Configuration
public class IdentitySecurityConfig {
    @Bean
    JdbcUserDetailsManager users(DataSource ds) {
        JdbcUserDetailsManager m = new JdbcUserDetailsManager(ds);
        m.setUsersByUsernameQuery("select username,password_hash,enabled from iam_user where normalized_username=lower(trim(?))");
        m.setAuthoritiesByUsernameQuery("select username, 'IDENTITY_USER' from iam_user where normalized_username=lower(trim(?))");
        return m;
    }
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(c -> {})
            .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health/**").permitAll().requestMatchers("/api/auth/login").permitAll().anyRequest().authenticated())
            .formLogin(f -> f.loginProcessingUrl("/api/auth/login").successHandler((r,s,a)->s.setStatus(200)).failureHandler((r,s,e)->s.setStatus(401)))
            .logout(l -> l.logoutUrl("/api/auth/logout").logoutSuccessHandler((r,s,a)->s.setStatus(204)))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .sessionManagement(s -> s.sessionFixation().migrateSession());
        return http.build();
    }
}
