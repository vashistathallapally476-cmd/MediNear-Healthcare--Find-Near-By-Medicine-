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

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private AuthTokenFilter authTokenFilter;

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

            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(unauthorizedHandler)
            )

            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth

                // ── Authentication ───────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()

                // ── Medicine search + autocomplete ──────────────
                .requestMatchers("/api/medicines/nearby").permitAll()
                .requestMatchers("/api/medicines/autocomplete").permitAll()

                // ── OCR / Medicine Identification ───────────────
                // Frontend uploads medicine image here.
                // Spring Boot forwards it to FastAPI OCR service.
                .requestMatchers("/api/ocr/**").permitAll()

                // ── AI Assistant ─────────────────────────────────
                .requestMatchers("/api/ai/**").permitAll()

                // ── Maps / Route ─────────────────────────────────
                .requestMatchers("/api/maps/**").permitAll()

                // ── WebSocket handshake ──────────────────────────
                .requestMatchers("/ws/**").permitAll()

                // ── Everything else requires JWT ─────────────────
                .anyRequest().authenticated()
            )

            .addFilterBefore(
                authTokenFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOriginPatterns(List.of("*"));

        cfg.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
            )
        );

        cfg.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        cfg.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", cfg);

        return source;
    }
}