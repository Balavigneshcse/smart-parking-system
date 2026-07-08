package com.smart.parking.config;

import com.smart.parking.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) { this.jwtAuthFilter = jwtAuthFilter; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        // ── Static resources + all portal pages (no trailing comma!) ──
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico",
                                "/css/**", "/js/**",
                                "/customer/**", "/admin/**", "/staff/**",
                                "/super/**", "/state/**", "/manager/**",
                                "/actuator/health"
                        ).permitAll()
                        // ── Public API endpoints ──────────────────────────────────────
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/places", "/api/places/*/occupancy",
                                "/api/prices",          // pricing table visible without login
                                "/api/ping"
                        ).permitAll()
                        // ── Role-restricted API endpoints ─────────────────────────────
                        .requestMatchers("/api/super/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/api/state/**").hasAuthority("STATE_MANAGER")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/manager/**").hasAuthority("MANAGER")
                        .requestMatchers("/api/gate/**").hasAnyAuthority("MANAGER", "SECURITY")
                        .requestMatchers("/api/customer/**").hasAuthority("CUSTOMER")
                        .requestMatchers("/api/reservations/**").hasAuthority("CUSTOMER")
                        .requestMatchers(
                                "/api/payments/razorpay/exit-order/**",
                                "/api/payments/razorpay/verify-exit"
                        ).hasAnyAuthority("MANAGER", "SECURITY")
                        .requestMatchers("/api/payments/**").hasAuthority("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
