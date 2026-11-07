package br.com.system.security;

import br.com.system.exception.ExceptionResponse;
import br.com.system.security.jwt.JwtTokenFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final List<String> allowedOrigins;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SecurityConfig(
            JwtTokenFilter jwtTokenFilter,
            @Value("${app.cors.allowed-origins:http://localhost:3000}") List<String> allowedOrigins) {
        this.jwtTokenFilter = jwtTokenFilter;
        this.allowedOrigins = allowedOrigins;
    }

    // ─── Rotas públicas ───────────────────────────────────────────────────────

    private static final String[] PUBLIC_ROUTES = {
            "/auth/login",
            "/auth/refresh-token",
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
                        .requestMatchers("/company/members/**").hasAuthority("ROLE_OWNER")

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
        config.setAllowedOrigins(allowedOrigins);
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
        return (request, response, authException) -> writeError(
                response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required!", request.getRequestURI());
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeError(
                response, HttpServletResponse.SC_FORBIDDEN,
                "Access denied! You don't have permission to access this resource.", request.getRequestURI());
    }

    private void writeError(HttpServletResponse response, int status, String message, String requestUri)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        OBJECT_MAPPER.writeValue(response.getOutputStream(),
                new ExceptionResponse(new java.util.Date(), status, message, "uri=" + requestUri));
    }
}
