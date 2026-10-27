
package com.kotlonies_api.controller;

import com.kotlonies_api.model.Gato;
import com.kotlonies_api.service.GatoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gatos")
public class GatoController {

    private final GatoService service;

    public GatoController(GatoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Gato>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gato> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<Gato> create(@RequestBody Gato gato) {
        Gato creado = service.create(gato);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gato> update(
            @PathVariable Long id,
            @RequestBody Gato gato
    ) {
        return ResponseEntity.ok(service.update(id, gato));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
