
package com.kotlonies_api.service;

import com.kotlonies_api.dto.CatRequest;
import com.kotlonies_api.dto.CatResponse;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class CatService {

    private final CatRepository catRepository;
    private final ColonyRepository colonyRepository;

    private static final Set<String> VALID_STATUSES = Set.of(
            "EN_COLONIA",
            "EN_SEGUIMIENTO",
            "EN_ADOPCION"
    );

    public CatService(CatRepository catRepository, ColonyRepository colonyRepository) {
        this.catRepository = catRepository;
        this.colonyRepository = colonyRepository;
    }

    @Transactional(readOnly = true)
    public List<CatResponse> findAll() {
        return catRepository.findAll().stream().map(CatResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CatResponse findById(Long id) {
        return CatResponse.from(findOrThrow(id));
    }

    @Transactional
    public CatResponse create(CatRequest request) {
        Cat cat = new Cat();
        apply(cat, request);
        return CatResponse.from(catRepository.save(cat));
    }

    @Transactional
    public CatResponse update(Long id, CatRequest request) {
        Cat cat = findOrThrow(id);
        apply(cat, request);
        return CatResponse.from(cat);
    }

    @Transactional
    public void delete(Long id) {
        catRepository.delete(findOrThrow(id));
    }

    private Cat findOrThrow(Long id) {
        return catRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No cat found with ID " + id
                ));
    }

    private void apply(Cat cat, CatRequest request) {

        String status = normalizeStatus(request.getStatus());

        validateBusinessRules(status, request.getColonyId());

        cat.setName(request.getName());
        cat.setSex(request.getSex() == null
                ? null
                : request.getSex().toUpperCase(Locale.ROOT));
        cat.setBirthDate(parseDate(request.getBirthDate()));
        cat.setColor(request.getColor());
        cat.setStatus(status);
        cat.setNeutered(request.getNeutered());
        cat.setIdentified(request.getIdentified());
        cat.setObservations(request.getObservations());

        if (request.getColonyId() == null) {
            cat.setColony(null);
        } else {
            Colony colony = colonyRepository.findById(request.getColonyId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "No colony found with ID " + request.getColonyId()
                    ));

            cat.setColony(colony);
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cat status is required"
            );
        }

        return status.trim().toUpperCase(Locale.ROOT);
    }

    private void validateBusinessRules(String status, Long colonyId) {

        if (!VALID_STATUSES.contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid cat status: " + status
            );
        }

        if ("EN_COLONIA".equals(status) && colonyId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A cat in a colony must have an assigned colony"
            );
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid birth date"
            );
        }
    }
}
