package com.kotlonies_api.service;

import com.kotlonies_api.dto.ColonyRequest;
import com.kotlonies_api.dto.ColonyResponse;
import com.kotlonies_api.exception.ColonyNotFoundException;
import com.kotlonies_api.exception.DuplicateColonyNameException;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.repository.ColonyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ColonyService {

    private final ColonyRepository repository;

    public ColonyService(ColonyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ColonyResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ColonyResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public ColonyResponse create(ColonyRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateColonyNameException(name);
        }
        boolean active = request.active() == null || request.active();
        Colony colony = new Colony(name, request.description(), request.location().trim(), active);
        return toResponse(repository.save(colony));
    }

    @Transactional
    public ColonyResponse update(Long id, ColonyRequest request) {
        Colony colony = findOrThrow(id);
        String name = request.name().trim();
        if (!colony.getName().equalsIgnoreCase(name) && repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateColonyNameException(name);
        }
        colony.setName(name);
        colony.setDescription(request.description());
        colony.setLocation(request.location().trim());
        if (request.active() != null) {
            colony.setActive(request.active());
        }
        return toResponse(colony);
    }

    @Transactional
    public ColonyResponse archive(Long id) {
        Colony colony = findOrThrow(id);
        colony.setActive(false);
        return toResponse(colony);
    }

    private Colony findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ColonyNotFoundException(id));
    }

    private ColonyResponse toResponse(Colony colony) {
        return new ColonyResponse(
                colony.getId(),
                colony.getName(),
                colony.getDescription(),
                colony.getLocation(),
                colony.isActive()
        );
    }
}