package com.kotlonies_api.controller;

import com.kotlonies_api.dto.TareaRequest;
import com.kotlonies_api.dto.TareaResponse;
import com.kotlonies_api.service.TareaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tareas")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<List<TareaResponse>> findAll() {
        return ResponseEntity.ok(tareaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR', 'CONSULTA')")
    public ResponseEntity<TareaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tareaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<TareaResponse> create(@Valid @RequestBody TareaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<TareaResponse> update(@PathVariable Long id, @Valid @RequestBody TareaRequest request) {
        return ResponseEntity.ok(tareaService.update(id, request));
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<TareaResponse> completar(
        @PathVariable Long id,
        @RequestBody(required = false) Map<String, Boolean> body
    ) {
        Boolean completada = body == null ? Boolean.TRUE : body.getOrDefault("completada", Boolean.TRUE);
        return ResponseEntity.ok(tareaService.completar(id, completada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUIDADOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tareaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
