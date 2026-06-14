package com.banking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        // Wraps CookieCsrfTokenRepository to block saveToken(null, …) calls.
        // CsrfAuthenticationStrategy issues saveToken(null) then saveToken(newToken) on
        // every login, which produces two Set-Cookie headers in one response. Some browsers
        // and proxies process them out of order, deleting the token instead of replacing it.
        // Blocking the null-save means only the new token cookie is written — one header,
        // no ambiguity.  The deferred-token path (loadDeferredToken → delegate directly)
        // is unaffected and continues to write the cookie normally on every response.
        CookieCsrfTokenRepository delegate = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRepository csrfRepo = new CsrfTokenRepository() {
            @Override public CsrfToken generateToken(HttpServletRequest request) {
                return delegate.generateToken(request);
            }
            @Override public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
                // Block null (delete) saves only during login: CsrfAuthenticationStrategy issues
                // saveToken(null) then saveToken(new) in one response, and some browsers process
                // the two Set-Cookie headers out of order, deleting the token instead of rotating it.
                // During logout the null save is intentional — allow it so the cookie is cleared for
                // the next user, who will get a fresh token via the interceptor's 403-retry flow.
                if (token == null && !"/logout".equals(request.getServletPath())) return;
                delegate.saveToken(token, request, response);
            }
            @Override public CsrfToken loadToken(HttpServletRequest request) {
                return delegate.loadToken(request);
            }
            @Override public DeferredCsrfToken loadDeferredToken(HttpServletRequest request, HttpServletResponse response) {
                return delegate.loadDeferredToken(request, response);
            }
        };

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepo)
                .csrfTokenRequestHandler(csrfHandler)
            )
            // Forces the deferred XSRF-TOKEN cookie to be written on every response so the
            // Angular client always has a valid token before its first mutating request.
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/api/csrf").permitAll()
                .requestMatchers("/user/create", "/user/getAllUsers").hasRole("ADMIN")
                .requestMatchers("/transaction/getAllUsersTransactions").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .successHandler((req, res, authentication) ->
                        res.setStatus(HttpServletResponse.SC_OK))
                .failureHandler((req, res, exception) -> {
                        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        res.getWriter().flush();
                })
            )
            .logout(logout -> logout
                .logoutSuccessHandler((req, res, authentication) ->
                        res.setStatus(HttpServletResponse.SC_OK))
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        res.getWriter().flush();
                })
                .accessDeniedHandler((req, res, e) -> {
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.getWriter().flush();
                })
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrf != null) {
                csrf.getToken();
            }
            chain.doFilter(request, response);
        }
    }
}
