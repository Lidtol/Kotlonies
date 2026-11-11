
package com.kotlonies_api.service;

import com.kotlonies_api.dto.UsuarioRequest;
import com.kotlonies_api.dto.UsuarioUpdateRequest;
import com.kotlonies_api.exception.RecursoNoEncontradoException;
import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(
                1L,
                "Lidia",
                "lidia@example.com",
                "hash-antiguo",
                "ADMIN",
                true
        );
    }

    @Test
    void crearUsuarioCorrectamente() {
        UsuarioRequest request = new UsuarioRequest(
                " Ana ",
                " ANA@example.com ",
                "password123",
                "cuidador",
                null
        );

        when(repository.existsByEmailIgnoreCase("ANA@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hash-nuevo");

        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.crear(request);

        assertEquals("Ana", resultado.getNombre());
        assertEquals("ANA@example.com", resultado.getEmail());
        assertEquals("hash-nuevo", resultado.getPasswordHash());
        assertEquals("CUIDADOR", resultado.getRol());
        assertTrue(resultado.isActivo());

        verify(repository).save(any(Usuario.class));
    }

    @Test
    void noDebeCrearUsuarioConEmailDuplicado() {
        UsuarioRequest request = new UsuarioRequest(
                "Ana",
                "lidia@example.com",
                "password123",
                "CUIDADOR",
                true
        );

        when(repository.existsByEmailIgnoreCase("lidia@example.com"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.crear(request)
        );

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuarioSinCambiarPassword() {
        Usuario usuarioCuidador = new Usuario(
                2L,
                "Marta",
                "marta@example.com",
                "hash-cuidador",
                "CUIDADOR",
                true
        );

        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Nuevo nombre",
                "nuevo@example.com",
                null,
                "CONSULTA",
                false
        );

        when(repository.findById(2L))
                .thenReturn(Optional.of(usuarioCuidador));

        when(repository.findByEmailIgnoreCase("nuevo@example.com"))
                .thenReturn(Optional.empty());

        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.actualizar(
                2L,
                request,
                "admin@example.com"
        );

        assertEquals("Nuevo nombre", resultado.getNombre());
        assertEquals("nuevo@example.com", resultado.getEmail());
        assertEquals("CONSULTA", resultado.getRol());
        assertFalse(resultado.isActivo());

        // La contraseña anterior debe conservarse.
        assertEquals("hash-cuidador", resultado.getPasswordHash());

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void actualizarUsuarioCambiandoPassword() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Lidia",
                "lidia@example.com",
                "password-nueva",
                "ADMIN",
                true
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.findByEmailIgnoreCase("lidia@example.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode("password-nueva"))
                .thenReturn("hash-nuevo");

        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.actualizar(
                1L,
                request,
                "admin@example.com"
        );

        assertEquals("hash-nuevo", resultado.getPasswordHash());

        verify(passwordEncoder).encode("password-nueva");
    }

    @Test
    void noDebeEliminarUsuarioInexistente() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> usuarioService.eliminar(99L, "admin@example.com")
        );

        verify(repository, never()).deleteById(any());
    }

    @Test
    void eliminarUsuarioExistente() {
        Usuario usuarioCuidador = new Usuario(
                2L,
                "Marta",
                "marta@example.com",
                "hash-cuidador",
                "CUIDADOR",
                true
        );

        when(repository.findById(2L))
                .thenReturn(Optional.of(usuarioCuidador));

        usuarioService.eliminar(2L, "admin@example.com");

        verify(repository).delete(usuarioCuidador);
    }

    @Test
    void unAdministradorNoPuedeEliminarSuPropiaCuenta() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.eliminar(1L, "lidia@example.com")
        );

        verify(repository, never()).delete(any(Usuario.class));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void unAdministradorNoPuedeQuitarseSuPropioRolAdmin() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Lidia",
                "lidia@example.com",
                null,
                "CUIDADOR",
                true
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.actualizar(1L, request, "lidia@example.com")
        );

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void unAdministradorNoPuedeDesactivarSuPropiaCuenta() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Lidia",
                "lidia@example.com",
                null,
                "ADMIN",
                false
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.actualizar(1L, request, "lidia@example.com")
        );

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void noSePuedeEliminarAlUltimoAdministradorActivo() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.countByRolAndActivoTrue("ADMIN"))
                .thenReturn(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.eliminar(1L, "otro@example.com")
        );

        verify(repository, never()).delete(any(Usuario.class));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void noSePuedeDesactivarAlUltimoAdministradorActivo() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Lidia",
                "lidia@example.com",
                null,
                "ADMIN",
                false
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.findByEmailIgnoreCase("lidia@example.com"))
                .thenReturn(Optional.of(usuario));

        when(repository.countByRolAndActivoTrue("ADMIN"))
                .thenReturn(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.actualizar(1L, request, "otro@example.com")
        );

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void noSePuedeCambiarRolDelUltimoAdministradorActivo() {
        UsuarioUpdateRequest requestCuidadores = new UsuarioUpdateRequest(
                "Lidia",
                "lidia@example.com",
                null,
                "CUIDADOR",
                true
        );
        UsuarioUpdateRequest requestConsultas = new UsuarioUpdateRequest(
                "Lidia",
                "lidia@example.com",
                null,
                "CONSULTA",
                true
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.findByEmailIgnoreCase("lidia@example.com"))
                .thenReturn(Optional.of(usuario));

        when(repository.countByRolAndActivoTrue("ADMIN"))
                .thenReturn(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.actualizar(1L, requestCuidadores, "otro@example.com")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.actualizar(1L, requestConsultas, "otro@example.com")
        );

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void sePermiteEliminarUnAdministradorCuandoExisteOtroAdministradorActivo() {
        Usuario otroAdministrador = new Usuario(
                2L,
                "Ana",
                "ana@example.com",
                "hash-ana",
                "ADMIN",
                true
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.countByRolAndActivoTrue("ADMIN"))
                .thenReturn(2L);

        usuarioService.eliminar(1L, "ana@example.com");

        verify(repository).delete(usuario);
    }

    @Test
    void sePermiteDesactivarUnAdministradorCuandoExisteOtroAdministradorActivo() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Lidia",
                "lidia@example.com",
                null,
                "ADMIN",
                false
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(repository.findByEmailIgnoreCase("lidia@example.com"))
                .thenReturn(Optional.of(usuario));

        when(repository.countByRolAndActivoTrue("ADMIN"))
                .thenReturn(2L);

        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.actualizar(
                1L,
                request,
                "ana@example.com"
        );

        assertFalse(resultado.isActivo());
        assertEquals("ADMIN", resultado.getRol());
        verify(repository).save(any(Usuario.class));
    }
}
