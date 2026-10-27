
package com.kotlonies_api.service;

import com.kotlonies_api.model.Gato;
import com.kotlonies_api.repository.ColoniaJsonRepository;
import com.kotlonies_api.repository.GatoJsonRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GatoService {

    private final GatoJsonRepository gatoRepository;
    private final ColoniaJsonRepository coloniaRepository;

    public GatoService(
            GatoJsonRepository gatoRepository,
            ColoniaJsonRepository coloniaRepository
    ) {
        this.gatoRepository = gatoRepository;
        this.coloniaRepository = coloniaRepository;
    }

    public List<Gato> findAll() {
        return gatoRepository.findAll();
    }

    public Gato findById(Long id) {
        return gatoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un gato con ID " + id
                ));
    }

    public Gato create(Gato gato) {
        validarColonia(gato.getColoniaId());

        gato.setId(null);
        return gatoRepository.save(gato);
    }

    public Gato update(Long id, Gato gato) {
        findById(id);
        validarColonia(gato.getColoniaId());

        gato.setId(id);
        return gatoRepository.save(gato);
    }

    public void delete(Long id) {
        findById(id);
        gatoRepository.deleteById(id);
    }

    private void validarColonia(Long coloniaId) {
        if (coloniaId == null) {
            return;
        }

        boolean existe = coloniaRepository.findById(coloniaId)
                .isPresent();

        if (!existe) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No existe una colonia con ID " + coloniaId
            );
        }
    }
}
