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

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
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
    public Usuario actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario existente = obtenerPorId(id);
        String email = request.email().trim();

        repository.findByEmailIgnoreCase(email)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new IllegalArgumentException(
                            "Ya existe otro usuario con el correo " + email
                    );
                });

        existente.setNombre(request.nombre().trim());
        existente.setEmail(email);
        existente.setRol(request.rol().trim().toUpperCase());

        if (request.password() != null && !request.password().isBlank()) {
            existente.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        if (request.activo() != null) {
            existente.setActivo(request.activo());
        }

        return repository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        obtenerPorId(id);
        repository.deleteById(id);
    }
}
