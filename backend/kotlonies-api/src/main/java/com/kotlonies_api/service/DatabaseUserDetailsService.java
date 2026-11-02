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
 * Carga los usuarios desde la base de datos (JPA) para Spring Security,
 * de modo que los roles (ROLE_ADMIN, ROLE_CUIDADOR, ROLE_CONSULTA) se
 * lean de la tabla "usuarios" gestionada por el ORM.
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
