
package com.kotlonies_api.controller;

import com.kotlonies_api.dto.ColonyRequest;
import com.kotlonies_api.dto.ColonyResponse;
import com.kotlonies_api.service.ColonyService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colonies")
public class ColonyController {

    private final ColonyService service;

    public ColonyController(ColonyService service) {
        this.service = service;
    }

    // ADMIN, CUIDADOR y CONSULTA pueden consultar colonias.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<List<ColonyResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<ColonyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ADMIN y CUIDADOR pueden crear colonias.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<ColonyResponse> create(
            @Valid @RequestBody ColonyRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    // ADMIN y CUIDADOR pueden modificar colonias.
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<ColonyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ColonyRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // Solo ADMIN puede archivar colonias.
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ColonyResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }
}
