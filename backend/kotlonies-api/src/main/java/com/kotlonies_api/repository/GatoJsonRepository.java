
package com.kotlonies_api.repository;

import com.kotlonies_api.model.Gato;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GatoJsonRepository {

    private final ObjectMapper objectMapper;
    private final Path archivo;

    public GatoJsonRepository(
            ObjectMapper objectMapper,
            @Value("${kotlonies.storage.gatos-file:data/gatos.json}")
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
                        new ArrayList<Gato>()
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo inicializar el archivo de gatos",
                    e
            );
        }
    }

    private List<Gato> leerTodos() {
        try {
            return objectMapper.readValue(
                    archivo.toFile(),
                    new TypeReference<List<Gato>>() {}
            );
        } catch (tools.jackson.core.JacksonException e) {
            throw new IllegalStateException(
                    "No se pudieron leer los gatos del archivo JSON",
                    e
            );
        }
    }

    private void escribirTodos(List<Gato> gatos) {
        try {
            objectMapper.writeValue(archivo.toFile(), gatos);
        } catch (tools.jackson.core.JacksonException e) {
            throw new IllegalStateException(
                    "No se pudieron guardar los gatos en el archivo JSON",
                    e
            );
        }
    }

    public synchronized List<Gato> findAll() {
        return new ArrayList<>(leerTodos());
    }

    public synchronized Optional<Gato> findById(Long id) {
        return leerTodos()
                .stream()
                .filter(gato -> id.equals(gato.getId()))
                .findFirst();
    }

    public synchronized Gato save(Gato gato) {
        List<Gato> gatos = leerTodos();

        if (gato.getId() == null) {
            long siguienteId = gatos.stream()
                    .map(Gato::getId)
                    .filter(id -> id != null)
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L) + 1;

            gato.setId(siguienteId);
            gatos.add(gato);
        } else {
            int indice = -1;

            for (int i = 0; i < gatos.size(); i++) {
                if (gato.getId().equals(gatos.get(i).getId())) {
                    indice = i;
                    break;
                }
            }

            if (indice == -1) {
                throw new IllegalArgumentException(
                        "No existe un gato con ID " + gato.getId()
                );
            }

            gatos.set(indice, gato);
        }

        escribirTodos(gatos);
        return gato;
    }

    public synchronized void deleteById(Long id) {
        List<Gato> gatos = leerTodos();

        boolean eliminado = gatos.removeIf(
                gato -> id.equals(gato.getId())
        );

        if (eliminado) {
            escribirTodos(gatos);
        }
    }
}
