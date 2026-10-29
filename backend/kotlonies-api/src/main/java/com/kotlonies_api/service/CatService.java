package com.kotlonies_api.service;

import com.kotlonies_api.model.Cat;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CatService {

    private final CatRepository catRepository;
    private final ColonyRepository colonyRepository;

    public CatService(
            CatRepository catRepository,
            ColonyRepository colonyRepository
    ) {
        this.catRepository = catRepository;
        this.colonyRepository = colonyRepository;
    }

    public List<Cat> findAll() {
        return catRepository.findAll();
    }

    public Cat findById(Long id) {
        return catRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No cat found with ID " + id
                ));
    }

    public Cat create(Cat cat) {
        validateColony(cat.getColonyId());

        cat.setId(null);
        return catRepository.save(cat);
    }

    public Cat update(Long id, Cat cat) {
        findById(id);
        validateColony(cat.getColonyId());

        cat.setId(id);
        return catRepository.save(cat);
    }

    public void delete(Long id) {
        findById(id);
        catRepository.deleteById(id);
    }

    private void validateColony(Long colonyId) {
        if (colonyId == null) {
            return;
        }

        boolean exists = colonyRepository.findById(colonyId)
                .isPresent();

        if (!exists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No colony found with ID " + colonyId
            );
        }
    }
}
