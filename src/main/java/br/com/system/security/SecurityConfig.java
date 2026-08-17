package br.com.system.security;

import br.com.system.security.jwt.JwtTokenFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Date;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenFilter jwtTokenFilter;

    // ─── Rotas públicas ───────────────────────────────────────────────────────

    private static final String[] PUBLIC_ROUTES = {
            "/auth/login",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**"
    };

    // ─── Regras de acesso ─────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // rotas públicas
                        .requestMatchers(PUBLIC_ROUTES).permitAll()

                        // rotas apenas do OWNER
                        .requestMatchers("/administrators/**").hasAuthority("ROLE_OWNER")

                        // rotas do OWNER e ADMIN
                        .requestMatchers("/analytics/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/suppliers/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/categories/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/brands/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/users/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")

                        // rotas do OWNER, ADMIN e OPERATOR
                        .requestMatchers("/products/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/sales/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/clients/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/stock-movements/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                        .requestMatchers("/alerts/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")

                        // qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // ─── CORS ─────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ─── Password Encoder ─────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── Authentication Manager ───────────────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─── Handlers de erro de segurança ───────────────────────────────────────────

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"timestamp\": \"" + new Date() + "\", " +
                            "\"status\": 401, " +
                            "\"message\": \"Authentication required!\", " +
                            "\"details\": \"uri=" + request.getRequestURI() + "\"}"
            );
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"timestamp\": \"" + new Date() + "\", " +
                            "\"status\": 403, " +
                            "\"message\": \"Access denied! You don't have permission to access this resource.\", " +
                            "\"details\": \"uri=" + request.getRequestURI() + "\"}"
            );
        };
    }
}