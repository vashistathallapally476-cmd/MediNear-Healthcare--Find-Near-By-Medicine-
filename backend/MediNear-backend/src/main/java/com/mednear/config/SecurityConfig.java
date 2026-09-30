package com.mednear.config;

import com.mednear.security.AuthEntryPointJwt;
import com.mednear.security.AuthTokenFilter;
import com.mednear.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired private UserDetailsServiceImpl userDetailsService;
    @Autowired private AuthEntryPointJwt      unauthorizedHandler;
    @Autowired private AuthTokenFilter        authTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg)
            throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // ── Auth ──────────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()
                // ── Medicine search + autocomplete (public) ───────
                .requestMatchers("/api/medicines/nearby").permitAll()
                .requestMatchers("/api/medicines/autocomplete").permitAll()
                // ── AI Assistant (public — no login needed) ───────
                .requestMatchers("/api/ai/**").permitAll()
                // ── Maps / Route (public) ─────────────────────────
                .requestMatchers("/api/maps/**").permitAll()
<<<<<<< HEAD
=======
                // ── Prescription OCR Scan (public entry with optional JWT) ───
                .requestMatchers("/api/ocr/scan").permitAll()
                // ── Uploaded Images (public) ──────────────────────
                .requestMatchers("/uploads/**").permitAll()
>>>>>>> d5ea729 (feat: integrate full Spring Boot backend with OCR ML pipeline and web frontend)
                // ── WebSocket handshake (public) ──────────────────
                .requestMatchers("/ws/**").permitAll()
                // ── Everything else requires JWT ──────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
<<<<<<< HEAD
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
=======
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
>>>>>>> d5ea729 (feat: integrate full Spring Boot backend with OCR ML pipeline and web frontend)
        cfg.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
