package com.kotlonies_api.controller;

import com.kotlonies_api.dto.LoginRequest;
import com.kotlonies_api.dto.LoginResponse;
import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        // 1. Comprobamos email y contraseña contra la base de datos (JPA + BCrypt).
        Usuario usuario = authService.validarCredenciales(
                request.email().trim(), request.password()
        );

        // 2. Creamos la autenticación con el rol del usuario (ROLE_ADMIN, ROLE_CUIDADOR...).
        var autenticacion = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(),
                null,
                AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol())
        );

        // 3. Creamos o recuperamos la sesión y renovamos su identificador
        // para evitar ataques de Session Fixation.
        HttpSession session = httpRequest.getSession(true);
        httpRequest.changeSessionId();

        // 4. Guardamos el SecurityContext en la sesión: en las siguientes peticiones
        // Spring Security lo recupera a partir de la cookie JSESSIONID.
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                contexto
        );

        return ResponseEntity.ok(LoginResponse.from(usuario));
    }

    @GetMapping("/sesion")
    public ResponseEntity<LoginResponse> sesion() {
        return ResponseEntity.ok(LoginResponse.from(authService.obtenerSesionActual()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }
}
