package com.kotlonies_api.service;

import com.kotlonies_api.exception.CredencialesInvalidasException;
import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validarCredenciales_conUsuarioActivoYPasswordCorrecta_devuelveUsuario() {
        Usuario usuario = new Usuario(
                1L,
                "Ana",
                "ana@example.com",
                "hash-seguro",
                "ADMIN",
                true
        );

        when(repository.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("demoPass123", "hash-seguro"))
                .thenReturn(true);

        Usuario resultado = authService.validarCredenciales("ana@example.com", "demoPass123");

        assertEquals("ana@example.com", resultado.getEmail());
        assertTrue(resultado.isActivo());
    }

    @Test
    void validarCredenciales_conPasswordIncorrecta_lanzaCredencialesInvalidasException() {
        Usuario usuario = new Usuario(
                1L,
                "Ana",
                "ana@example.com",
                "hash-seguro",
                "ADMIN",
                true
        );

        when(repository.findByEmailIgnoreCase("ana@example.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongPass", "hash-seguro"))
                .thenReturn(false);

        CredencialesInvalidasException ex = assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.validarCredenciales("ana@example.com", "wrongPass")
        );

        assertEquals("Correo o contraseña incorrectos", ex.getMessage());
    }

    @Test
    void validarCredenciales_conUsuarioInexistente_lanzaCredencialesInvalidasException() {
        when(repository.findByEmailIgnoreCase("nobody@example.com"))
                .thenReturn(Optional.empty());

        CredencialesInvalidasException ex = assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.validarCredenciales("nobody@example.com", "demoPass123")
        );

        assertEquals("Correo o contraseña incorrectos", ex.getMessage());
    }

    @Test
    void validarCredenciales_conUsuarioInactivo_lanzaCredencialesInvalidasException() {
        Usuario usuario = new Usuario(
                2L,
                "Luis",
                "luis@example.com",
                "hash-seguro",
                "CUIDADOR",
                false
        );

        when(repository.findByEmailIgnoreCase("luis@example.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("demoPass123", "hash-seguro"))
                .thenReturn(true);

        CredencialesInvalidasException ex = assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.validarCredenciales("luis@example.com", "demoPass123")
        );

        assertEquals("Esta cuenta está desactivada. Contacta con la administración.", ex.getMessage());
    }

    @Test
    void obtenerSesionActual_conAutenticacionValida_devuelveUsuario() {
        Usuario usuario = new Usuario(
                3L,
                "Marta",
                "marta@example.com",
                "hash-seguro",
                "CONSULTA",
                true
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "marta@example.com",
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_CONSULTA")
                )
        );

        when(repository.findByEmailIgnoreCase("marta@example.com"))
                .thenReturn(Optional.of(usuario));

        Usuario resultado = authService.obtenerSesionActual();

        assertEquals("marta@example.com", resultado.getEmail());
    }
}
