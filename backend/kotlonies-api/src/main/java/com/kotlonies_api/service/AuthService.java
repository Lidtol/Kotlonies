package com.kotlonies_api.service;

import com.kotlonies_api.exception.CredencialesInvalidasException;
import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Valida las credenciales contra la tabla de usuarios (ORM + BCrypt). */
    @Transactional(readOnly = true)
    public Usuario validarCredenciales(String email, String password) {
        Usuario usuario = repository.findByEmailIgnoreCase(email)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() ->
                        new CredencialesInvalidasException(
                                "Correo o contraseña incorrectos"
                        )
                );

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException(
                    "Esta cuenta está desactivada. Contacta con la administración."
            );
        }

        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new CredencialesInvalidasException("La sesión no es válida")
                );
    }

    @Transactional(readOnly = true)
    public Usuario obtenerSesionActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new CredencialesInvalidasException("La sesión no es válida");
        }

        return repository.findByEmailIgnoreCase(autenticacion.getName())
                .orElseThrow(() ->
                        new CredencialesInvalidasException("La sesión ya no es válida")
                );
    }
}

