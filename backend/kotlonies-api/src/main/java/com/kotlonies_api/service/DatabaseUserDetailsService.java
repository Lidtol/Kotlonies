package com.kotlonies_api.service;

import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Indica a Spring Security de dónde salen los usuarios (tabla "usuarios").
 *
 * El login de la API no pasa por aquí: lo valida AuthService para poder
 * devolver nuestros propios mensajes de error. Aun así, este bean es
 * necesario: si no existe ningún UserDetailsService, Spring Boot crea un
 * usuario en memoria "user" con una contraseña aleatoria que aparece en el log.
 */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public DatabaseUserDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCase(email)
                .map(usuario -> User.withUsername(usuario.getEmail())
                        .password(usuario.getPasswordHash())
                        .roles(usuario.getRol())
                        .disabled(!usuario.isActivo())
                        .build())
                .orElseThrow(() ->
                        new UsernameNotFoundException("No existe el usuario " + email)
                );
    }
}
