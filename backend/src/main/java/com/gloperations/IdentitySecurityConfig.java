package com.gloperations;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class IdentitySecurityConfig {
    @Bean
    JdbcUserDetailsManager users(DataSource ds) {
        JdbcUserDetailsManager m = new JdbcUserDetailsManager(ds);
        m.setUsersByUsernameQuery("select username,password_hash,enabled from iam_user where username = ?");
        m.setAuthoritiesByUsernameQuery("select u.username, c.code from iam_user u join iam_user_role_grant g on g.user_id=u.id join iam_role_capability rc on rc.role_id=g.role_id join iam_capability c on c.id=rc.capability_id where u.username=? and g.scope_type='GLOBAL'");
        return m;
    }
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        http.csrf(c -> c.ignoringRequestMatchers("/actuator/**"))
            .authorizeHttpRequests(a -> a.requestMatchers("/actuator/health/**").permitAll().requestMatchers("/api/auth/login").permitAll().anyRequest().authenticated())
            .formLogin(f -> f.loginProcessingUrl("/api/auth/login").successHandler((r,s,a)->s.setStatus(200)).failureHandler((r,s,e)->s.setStatus(401)))
            .logout(l -> l.logoutUrl("/api/auth/logout").logoutSuccessHandler((r,s,a)->s.setStatus(204)))
            .sessionManagement(s -> s.sessionFixation().migrateSession());
        return http.build();
    }
}
