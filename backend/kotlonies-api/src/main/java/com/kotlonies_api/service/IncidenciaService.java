package com.kotlonies_api.service;

import com.kotlonies_api.dto.IncidenciaRequest;
import com.kotlonies_api.dto.IncidenciaResponse;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.Incidencia;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import com.kotlonies_api.repository.IncidenciaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final CatRepository catRepository;
    private final ColonyRepository colonyRepository;

    public IncidenciaService(
        IncidenciaRepository incidenciaRepository,
        CatRepository catRepository,
        ColonyRepository colonyRepository
    ) {
        this.incidenciaRepository = incidenciaRepository;
        this.catRepository = catRepository;
        this.colonyRepository = colonyRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> findAll() {
        return incidenciaRepository.findAll()
            .stream()
            .map(IncidenciaResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public IncidenciaResponse findById(Long id) {
        return IncidenciaResponse.from(findOrThrow(id));
    }

    @Transactional
    public IncidenciaResponse create(IncidenciaRequest request) {
        Incidencia incidencia = new Incidencia();

        apply(incidencia, request);

        return IncidenciaResponse.from(
            incidenciaRepository.save(incidencia)
        );
    }

    @Transactional
    public IncidenciaResponse update(Long id, IncidenciaRequest request) {
        Incidencia incidencia = findOrThrow(id);

        apply(incidencia, request);

        return IncidenciaResponse.from(incidencia);
    }

    @Transactional
    public void delete(Long id) {
        incidenciaRepository.delete(findOrThrow(id));
    }

    private Incidencia findOrThrow(Long id) {
        return incidenciaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se encontró ninguna incidencia con ID " + id
            ));
    }

    private void apply(
        Incidencia incidencia,
        IncidenciaRequest request
    ) {
        incidencia.setTitulo(request.titulo().trim());
        incidencia.setDescripcion(request.descripcion().trim());
        incidencia.setTipo(request.tipo());
        incidencia.setPrioridad(request.prioridad());
        incidencia.setEstado(request.estado());

        Cat cat = request.catId() == null
            ? null
            : catRepository.findById(request.catId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró ningún gato con ID " + request.catId()
                ));

        Colony colony = request.colonyId() == null
            ? null
            : colonyRepository.findById(request.colonyId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontró ninguna colonia con ID " + request.colonyId()
                ));

        incidencia.setCat(cat);
        incidencia.setColony(colony);
    }
}