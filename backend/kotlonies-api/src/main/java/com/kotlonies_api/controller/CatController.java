package com.kotlonies_api.controller;

import com.kotlonies_api.dto.CatRequest;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.service.CatService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cats")
public class CatController {

    private final CatService catService;

    public CatController(CatService catService) {
        this.catService = catService;
    }

    @GetMapping
    public ResponseEntity<List<Cat>> findAll() {
        return ResponseEntity.ok(catService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cat> findById(@PathVariable Long id) {
        return ResponseEntity.ok(catService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Cat> create(
            @Valid @RequestBody CatRequest request) {

        Cat cat = convertToCat(request);
        Cat createdCat = catService.create(cat);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCat);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cat> update(
            @PathVariable Long id,
            @Valid @RequestBody CatRequest request) {

        Cat cat = convertToCat(request);

        return ResponseEntity.ok(catService.update(id, cat));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Cat convertToCat(CatRequest request) {
        Cat cat = new Cat();
        cat.setName(request.getName());
        cat.setSex(request.getSex());
        cat.setBirthDate(request.getBirthDate());
        cat.setColor(request.getColor());
        cat.setStatus(request.getStatus());
        cat.setNeutered(request.getNeutered());
        cat.setIdentified(request.getIdentified());
        cat.setColonyId(request.getColonyId());
        cat.setObservations(request.getObservations());

        return cat;
    }
}
