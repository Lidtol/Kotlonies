package com.kotlonies_api.config;

import com.kotlonies_api.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * Configuración de seguridad de la API.
 *
 * - Autenticación con sesión (cookie JSESSIONID). El login lo hace
 *   AuthController + AuthService, que guardan el SecurityContext en la sesión.
 * - CSRF con cookie XSRF-TOKEN legible desde Angular, que la devuelve
 *   en la cabecera X-XSRF-TOKEN.
 * - Permisos por rol con @PreAuthorize en los controladores.
 * - ActiveUserFilter invalida la sesión si el usuario se desactiva o se borra.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        CookieCsrfTokenRepository csrfRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        csrfRepository.setCookiePath("/");

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, ex) ->
                                response.sendError(HttpStatus.UNAUTHORIZED.value())
                        )
                        .accessDeniedHandler((request, response, ex) ->
                                response.sendError(HttpStatus.FORBIDDEN.value())
                        )
                )
                .addFilterBefore(
                        new ActiveUserFilter(usuarioRepository),
                        CsrfFilter.class
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Manejador de CSRF para una SPA: genera la cookie en cada respuesta y
     * acepta el token tal cual cuando llega en la cabecera X-XSRF-TOKEN.
     */
    private static final class SpaCsrfTokenRequestHandler
            implements CsrfTokenRequestHandler {

        private final CsrfTokenRequestHandler plain =
                new CsrfTokenRequestAttributeHandler();

        private final CsrfTokenRequestHandler xor =
                new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(
                HttpServletRequest request,
                HttpServletResponse response,
                Supplier<CsrfToken> csrfToken
        ) {
            this.xor.handle(request, response, csrfToken);

            // Fuerza la generación de la cookie CSRF.
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(
                HttpServletRequest request,
                CsrfToken csrfToken
        ) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());

            return StringUtils.hasText(headerValue)
                    ? this.plain.resolveCsrfTokenValue(request, csrfToken)
                    : this.xor.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
