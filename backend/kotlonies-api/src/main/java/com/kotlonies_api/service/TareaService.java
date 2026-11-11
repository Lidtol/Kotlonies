package com.kotlonies_api.service;

import com.kotlonies_api.dto.TareaRequest;
import com.kotlonies_api.dto.TareaResponse;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.EstadoTarea;
import com.kotlonies_api.model.Tarea;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import com.kotlonies_api.repository.TareaRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;
    private final CatRepository catRepository;
    private final ColonyRepository colonyRepository;

    public TareaService(
        TareaRepository tareaRepository,
        CatRepository catRepository,
        ColonyRepository colonyRepository
    ) {
        this.tareaRepository = tareaRepository;
        this.catRepository = catRepository;
        this.colonyRepository = colonyRepository;
    }

    @Transactional
    public List<TareaResponse> findAll() {
        return tareaRepository.findAllByOrderByFechaHoraAsc()
            .stream()
            .map(TareaResponse::from)
            .toList();
    }

    @Transactional
    public TareaResponse findById(Long id) {
        return TareaResponse.from(findOrThrow(id));
    }

    @Transactional
    public TareaResponse create(TareaRequest request) {
        Tarea tarea = new Tarea();
        apply(tarea, request);
        return TareaResponse.from(tareaRepository.save(tarea));
    }

    @Transactional
    public TareaResponse update(Long id, TareaRequest request) {
        Tarea tarea = findOrThrow(id);
        apply(tarea, request);
        return TareaResponse.from(tarea);
    }

    @Transactional
    public TareaResponse completar(Long id, Boolean completada) {
        Tarea tarea = findOrThrow(id);
        tarea.setEstado(Boolean.TRUE.equals(completada) ? EstadoTarea.COMPLETADA : EstadoTarea.PENDIENTE);
        return TareaResponse.from(tarea);
    }

    @Transactional
    public void delete(Long id) {
        tareaRepository.delete(findOrThrow(id));
    }

    private Tarea findOrThrow(Long id) {
        return tareaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se encontró ninguna tarea con ID " + id
            ));
    }

    private void apply(Tarea tarea, TareaRequest request) {
        tarea.setTitulo(request.titulo().trim());
        tarea.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        tarea.setTipo(request.tipo());
        tarea.setFechaHora(request.fechaHora());
        tarea.setEstado(request.estado() == null ? EstadoTarea.PENDIENTE : request.estado());

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

        tarea.setCat(cat);
        tarea.setColony(colony);
    }
}
