package com.kotlonies_api.service;

import com.kotlonies_api.dto.ColoniaRequest;
import com.kotlonies_api.exception.RecursoNoEncontradoException;
import com.kotlonies_api.model.Colonia;
import com.kotlonies_api.repository.ColoniaJsonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColoniaService {

    private final ColoniaJsonRepository repository;

    public ColoniaService(ColoniaJsonRepository repository) {
        this.repository = repository;
    }

    public List<Colonia> listar() {
        return repository.findAll();
    }

    public Colonia obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe la colonia con ID " + id
                        )
                );
    }

    public Colonia crear(ColoniaRequest request) {
        Colonia colonia = new Colonia(
                null,
                request.nombre().trim(),
                request.descripcion() == null
                        ? ""
                        : request.descripcion().trim(),
                request.ubicacion().trim(),
                request.activa() == null || request.activa()
        );

        return repository.save(colonia);
    }

    public Colonia actualizar(Long id, ColoniaRequest request) {
        Colonia existente = obtenerPorId(id);

        existente.setNombre(request.nombre().trim());
        existente.setDescripcion(
                request.descripcion() == null
                        ? ""
                        : request.descripcion().trim()
        );
        existente.setUbicacion(request.ubicacion().trim());

        if (request.activa() != null) {
            existente.setActiva(request.activa());
        }

        return repository.save(existente);
    }

    public void eliminar(Long id) {
        obtenerPorId(id);
        repository.deleteById(id);
    }
}