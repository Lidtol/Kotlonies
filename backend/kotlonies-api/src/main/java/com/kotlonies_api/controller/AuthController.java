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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        // Validación de credenciales contra la base de datos (JPA + BCrypt).
        Usuario usuario = authService.validarCredenciales(
                request.email().trim(), request.password()
        );

        // Guardamos la autenticación en la HttpSession para que Spring
        // Security la recupere en las peticiones siguientes (cookie JSESSIONID).
        var autenticacion = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(),
                null,
                AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol())
        );

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);

        HttpSession session = httpRequest.getSession(true);
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
