package com.kotlonies_api.controller;

import com.kotlonies_api.dto.ColoniaRequest;
import com.kotlonies_api.model.Colonia;
import com.kotlonies_api.service.ColoniaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colonias")
public class ColoniaController {

    private final ColoniaService service;

    public ColoniaController(ColoniaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Colonia>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Colonia> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Colonia> crear(
            @Valid @RequestBody ColoniaRequest request
    ) {
        Colonia creada = service.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Colonia> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ColoniaRequest request
    ) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id
    ) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}