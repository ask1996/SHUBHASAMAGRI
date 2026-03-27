package com.shubhasamagri.config;

import com.shubhasamagri.security.JwtAuthenticationFilter;
import com.shubhasamagri.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration.
 *
 * Security strategy:
 * - Stateless JWT-based authentication (no sessions)
 * - Public endpoints: auth, occasions, kits, swagger, h2-console
 * - Protected endpoints: cart, orders (require valid JWT)
 * - CSRF disabled (JWT inherently protects against CSRF for stateless APIs)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http))  // uses CorsConfig bean
            .authorizeHttpRequests(auth -> auth
                // Public: authentication endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Public: browsing occasions and kits (read-only)
                .requestMatchers(HttpMethod.GET, "/api/occasions/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/kits/**").permitAll()
                // Public: Swagger UI and H2 console (dev only)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // Protected: cart and orders require authentication
                .requestMatchers("/api/chatbot/chat").permitAll()
                .requestMatchers("/api/chatbot/health").permitAll()
                .requestMatchers("/api/cart/**").authenticated()
                .requestMatchers("/api/orders/**").authenticated()
                // Admin-only: creating/updating occasions and kits
                .requestMatchers(HttpMethod.POST, "/api/occasions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/occasions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/occasions/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/kits/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // Allow H2 console frames in dev
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
