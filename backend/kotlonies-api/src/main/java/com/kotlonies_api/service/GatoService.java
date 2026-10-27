
package com.kotlonies_api.service;

import com.kotlonies_api.model.Gato;
import com.kotlonies_api.repository.GatoJsonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GatoService {

    private final GatoJsonRepository repository;

    public GatoService(GatoJsonRepository repository) {
        this.repository = repository;
    }

    public List<Gato> findAll() {
        return repository.findAll();
    }

    public Gato findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un gato con ID " + id
                ));
    }

    public Gato create(Gato gato) {
        gato.setId(null);
        return repository.save(gato);
    }

    public Gato update(Long id, Gato gato) {
        findById(id);
        gato.setId(id);
        return repository.save(gato);
    }

    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }
}
