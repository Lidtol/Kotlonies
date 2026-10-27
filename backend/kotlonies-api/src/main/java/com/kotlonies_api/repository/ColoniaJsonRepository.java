package com.kotlonies_api.repository;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.kotlonies_api.model.Colonia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ColoniaJsonRepository {

    private final ObjectMapper objectMapper;
    private final Path archivo;

    public ColoniaJsonRepository(
            ObjectMapper objectMapper,
            @Value("${kotlonies.storage.file:data/colonias.json}")
            String rutaArchivo
    ) {
        this.objectMapper = objectMapper;
        this.archivo = Path.of(rutaArchivo);

        inicializarArchivo();
    }

    private void inicializarArchivo() {
        try {
            Path directorio = archivo.getParent();

            if (directorio != null) {
                Files.createDirectories(directorio);
            }

            if (Files.notExists(archivo)) {
                objectMapper.writeValue(
                        archivo.toFile(),
                        new ArrayList<Colonia>()
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo inicializar el archivo de colonias",
                    e
            );
        }
    }

private synchronized List<Colonia> leerTodos() {
    try {
        return objectMapper.readValue(
                archivo.toFile(),
                new TypeReference<List<Colonia>>() {}
        );
    } catch (tools.jackson.core.JacksonException e) {
        throw new IllegalStateException(
                "No se pudieron leer las colonias del archivo JSON",
                e
        );
    }
}
private synchronized void escribirTodos(List<Colonia> colonias) {
    try {
        objectMapper.writeValue(archivo.toFile(), colonias);
    } catch (tools.jackson.core.JacksonException e) {
        throw new IllegalStateException(
                "No se pudieron guardar las colonias en el archivo JSON",
                e
        );
    }
}

    public synchronized List<Colonia> findAll() {
        return new ArrayList<>(leerTodos());
    }

    public synchronized Optional<Colonia> findById(Long id) {
        return leerTodos()
                .stream()
                .filter(colonia -> id.equals(colonia.getId()))
                .findFirst();
    }

    public synchronized Colonia save(Colonia colonia) {
        List<Colonia> colonias = leerTodos();

        if (colonia.getId() == null) {
            long siguienteId = colonias.stream()
                    .map(Colonia::getId)
                    .filter(id -> id != null)
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L) + 1;

            colonia.setId(siguienteId);
            colonias.add(colonia);
        } else {
            int indice = -1;

            for (int i = 0; i < colonias.size(); i++) {
                if (colonia.getId().equals(colonias.get(i).getId())) {
                    indice = i;
                    break;
                }
            }

            if (indice == -1) {
                throw new IllegalArgumentException(
                        "No existe una colonia con ID " + colonia.getId()
                );
            }

            colonias.set(indice, colonia);
        }

        escribirTodos(colonias);
        return colonia;
    }

    public synchronized void deleteById(Long id) {
        List<Colonia> colonias = leerTodos();

        boolean eliminado = colonias.removeIf(
                colonia -> id.equals(colonia.getId())
        );

        if (eliminado) {
            escribirTodos(colonias);
        }
    }
}