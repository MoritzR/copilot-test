package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.username:admin}")
    private String username;

    @Value("${app.security.password:password}")
    private String password;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Disable CSRF for H2 Console
                .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
            .headers(headers -> headers
                // Allow frames for H2 Console
                .frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // Allow H2 Console access without authentication
                    .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                    // Require authentication for all other requests (including root path)
                    .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder().encode(password))
            .roles("USER", "ADMIN")
            .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}