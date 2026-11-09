
package com.kotlonies_api.controller;

import com.kotlonies_api.dto.CatRequest;
import com.kotlonies_api.dto.CatResponse;
import com.kotlonies_api.service.CatService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cats")
public class CatController {

    private final CatService catService;

    public CatController(CatService catService) {
        this.catService = catService;
    }

    // ADMIN, CUIDADOR y CONSULTA pueden consultar gatos.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<List<CatResponse>> findAll() {
        return ResponseEntity.ok(catService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<CatResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(catService.findById(id));
    }

    // ADMIN y CUIDADOR pueden crear gatos.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<CatResponse> create(
            @Valid @RequestBody CatRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(catService.create(request));
    }

    // ADMIN y CUIDADOR pueden modificar gatos.
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<CatResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CatRequest request
    ) {
        return ResponseEntity.ok(catService.update(id, request));
    }

    // Solo ADMIN puede eliminar gatos.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
