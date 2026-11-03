package com.kotlonies_api.controller;

import com.kotlonies_api.dto.CatRequest;
import com.kotlonies_api.dto.CatResponse;
import com.kotlonies_api.service.CatService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cats")
public class CatController {

    private final CatService catService;

    public CatController(CatService catService) {
        this.catService = catService;
    }

    @GetMapping
    public ResponseEntity<List<CatResponse>> findAll() {
        return ResponseEntity.ok(catService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(catService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CatResponse> create(
            @Valid @RequestBody CatRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(catService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CatRequest request
    ) {
        return ResponseEntity.ok(catService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catService.delete(id);
        return ResponseEntity.noContent().build();
    }
}