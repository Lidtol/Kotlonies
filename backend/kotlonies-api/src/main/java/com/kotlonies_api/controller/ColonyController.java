package com.kotlonies_api.controller;

import com.kotlonies_api.dto.ColonyRequest;
import com.kotlonies_api.dto.ColonyResponse;
import com.kotlonies_api.service.ColonyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/colonies")
public class ColonyController {

    private final ColonyService service;

    public ColonyController(ColonyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ColonyResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColonyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ColonyResponse> create(@Valid @RequestBody ColonyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColonyResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ColonyRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ColonyResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(service.archive(id));
    }
}