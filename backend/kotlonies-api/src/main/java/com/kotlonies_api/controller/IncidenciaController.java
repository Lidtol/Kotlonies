package com.kotlonies_api.controller;

import com.kotlonies_api.dto.IncidenciaRequest;
import com.kotlonies_api.dto.IncidenciaResponse;
import com.kotlonies_api.service.IncidenciaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<List<IncidenciaResponse>> findAll() {
        return ResponseEntity.ok(incidenciaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<IncidenciaResponse> findById(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(incidenciaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<IncidenciaResponse> create(
        @Valid @RequestBody IncidenciaRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(incidenciaService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<IncidenciaResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody IncidenciaRequest request
    ) {
        return ResponseEntity.ok(
            incidenciaService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<Void> delete(
        @PathVariable Long id
    ) {
        incidenciaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}