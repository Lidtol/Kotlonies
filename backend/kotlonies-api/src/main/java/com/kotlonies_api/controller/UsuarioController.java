package com.kotlonies_api.controller;

import com.kotlonies_api.dto.UsuarioRequest;
import com.kotlonies_api.dto.UsuarioUpdateRequest;
import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    /**
     * Usuario sin hash de contraseña para no exponer el secreto en las respuestas.
     */
    record UsuarioPublico(
            Long id,
            String nombre,
            String email,
            String rol,
            boolean activo
    ) {
        static UsuarioPublico from(Usuario usuario) {
            return new UsuarioPublico(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getRol(),
                    usuario.isActivo()
            );
        }
    }

    @GetMapping
    public ResponseEntity<List<UsuarioPublico>> listar() {
        return ResponseEntity.ok(
                service.listar().stream().map(UsuarioPublico::from).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioPublico> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(UsuarioPublico.from(service.obtenerPorId(id)));
    }

    @PostMapping
    public ResponseEntity<UsuarioPublico> crear(
            @Valid @RequestBody UsuarioRequest request
    ) {
        Usuario creado = service.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UsuarioPublico.from(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioPublico> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        Usuario actualizado = service.actualizar(id, request);

        return ResponseEntity.ok(UsuarioPublico.from(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
