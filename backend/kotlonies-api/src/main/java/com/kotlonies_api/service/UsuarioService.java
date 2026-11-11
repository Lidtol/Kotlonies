
package com.kotlonies_api.service;

import com.kotlonies_api.dto.UsuarioRequest;
import com.kotlonies_api.dto.UsuarioUpdateRequest;
import com.kotlonies_api.exception.RecursoNoEncontradoException;
import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe el usuario con ID " + id
                        )
                );
    }

    @Transactional
    public Usuario crear(UsuarioRequest request) {
        String email = request.email().trim();

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el correo " + email
            );
        }

        Usuario usuario = new Usuario(
                null,
                request.nombre().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.rol().trim().toUpperCase(),
                request.activo() == null || request.activo()
        );

        return repository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(
            Long id,
            UsuarioUpdateRequest request,
            String emailAdministrador
    ) {
        Usuario existente = obtenerPorId(id);

        String email = request.email().trim();
        String nuevoRol = request.rol().trim().toUpperCase();

        boolean esSuPropiaCuenta =
                existente.getEmail().equalsIgnoreCase(emailAdministrador);

        boolean vaADejarDeSerAdministrador =
                !nuevoRol.equals("ADMIN")
                || Boolean.FALSE.equals(request.activo());

        // No permitimos que un administrador se quite sus propios permisos.
        if (esSuPropiaCuenta && vaADejarDeSerAdministrador) {
            throw new IllegalArgumentException(
                    "No puedes quitarte tus propios permisos de administrador."
            );
        }

        // Comprobamos que no se repita el correo.
        repository.findByEmailIgnoreCase(email)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException(
                            "Ya existe otro usuario con el correo " + email
                    );
                });

        boolean eraAdministradorActivo =
                existente.getRol().equals("ADMIN")
                && existente.isActivo();

        boolean vaADejarDeSerAdministradorActivo =
                !nuevoRol.equals("ADMIN")
                || Boolean.FALSE.equals(request.activo());

        // Protegemos al último administrador activo.
        if (eraAdministradorActivo
                && vaADejarDeSerAdministradorActivo
                && repository.countByRolAndActivoTrue("ADMIN") <= 1) {

            throw new IllegalArgumentException(
                    "No puedes quitar o desactivar al último administrador activo."
            );
        }

        existente.setNombre(request.nombre().trim());
        existente.setEmail(email);
        existente.setRol(nuevoRol);

        if (request.password() != null
                && !request.password().isBlank()) {
            existente.setPasswordHash(
                    passwordEncoder.encode(request.password())
            );
        }

        if (request.activo() != null) {
            existente.setActivo(request.activo());
        }

        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id, String emailAdministrador) {
        Usuario usuario = obtenerPorId(id);

        // No permitimos eliminar la propia cuenta.
        if (usuario.getEmail().equalsIgnoreCase(emailAdministrador)) {
            throw new IllegalArgumentException(
                    "No puedes eliminar tu propia cuenta."
            );
        }

        boolean esAdministradorActivo =
                usuario.getRol().equals("ADMIN")
                && usuario.isActivo();

        // No permitimos eliminar al último administrador activo.
        if (esAdministradorActivo
                && repository.countByRolAndActivoTrue("ADMIN") <= 1) {

            throw new IllegalArgumentException(
                    "No puedes eliminar al último administrador activo."
            );
        }

        repository.delete(usuario);
    }
}
