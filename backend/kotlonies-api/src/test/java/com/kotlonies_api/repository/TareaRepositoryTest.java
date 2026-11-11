package com.kotlonies_api.repository;

import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.EstadoTarea;
import com.kotlonies_api.model.Tarea;
import com.kotlonies_api.model.TipoTarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TareaRepositoryTest {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private ColonyRepository colonyRepository;

    @Test
    void saveAndFindById_shouldPersistTask() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Tareas", "Desc", "Murcia", true));
        Cat gato = catRepository.saveAndFlush(catConColonia(colonia, "Olivia"));
        Tarea tarea = new Tarea();
        tarea.setTitulo("Visita veterinaria");
        tarea.setDescripcion("Revisión general");
        tarea.setTipo(TipoTarea.VETERINARIO);
        tarea.setFechaHora(LocalDateTime.of(2026, 11, 20, 10, 0));
        tarea.setEstado(EstadoTarea.PENDIENTE);
        tarea.setCat(gato);
        tarea.setColony(colonia);

        Tarea guardada = tareaRepository.saveAndFlush(tarea);

        Tarea encontrada = tareaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(encontrada.getTitulo()).isEqualTo("Visita veterinaria");
        assertThat(encontrada.getColony().getName()).isEqualTo("Colonia Tareas");
    }

    @Test
    void findAllByOrderByFechaHoraAsc_shouldReturnTasksInChronologicalOrder() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Calendario", "Desc", "Granada", true));
        Cat gato = catRepository.saveAndFlush(catConColonia(colonia, "Kiko"));

        Tarea tarea2 = tareaCon(gato, colonia, "Tarea tarde", LocalDateTime.of(2026, 11, 20, 17, 0));
        Tarea tarea1 = tareaCon(gato, colonia, "Tarea mañana", LocalDateTime.of(2026, 11, 20, 9, 0));
        Tarea tarea3 = tareaCon(gato, colonia, "Tarea noche", LocalDateTime.of(2026, 11, 20, 21, 0));

        tareaRepository.saveAllAndFlush(List.of(tarea2, tarea1, tarea3));

        List<Tarea> ordenadas = tareaRepository.findAllByOrderByFechaHoraAsc();

        assertThat(ordenadas).extracting(Tarea::getTitulo)
                .containsExactly("Tarea mañana", "Tarea tarde", "Tarea noche");
    }

    @Test
    void delete_shouldRemoveTask() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Tarea Delete", "Desc", "Cuenca", true));
        Cat gato = catRepository.saveAndFlush(catConColonia(colonia, "Mango"));
        Tarea tarea = tareaRepository.saveAndFlush(tareaCon(gato, colonia, "Tarea delete", LocalDateTime.of(2026, 12, 1, 9, 0)));

        tareaRepository.delete(tarea);
        tareaRepository.flush();

        assertThat(tareaRepository.findById(tarea.getId())).isEmpty();
    }

    private Cat catConColonia(Colony colonia, String nombre) {
        Cat gato = new Cat();
        gato.setName(nombre);
        gato.setSex("MALE");
        gato.setColor("Tigre");
        gato.setStatus("EN_COLONIA");
        gato.setNeutered(true);
        gato.setIdentified(true);
        gato.setColony(colonia);
        return gato;
    }

    private Tarea tareaCon(Cat gato, Colony colonia, String titulo, LocalDateTime fechaHora) {
        Tarea tarea = new Tarea();
        tarea.setTitulo(titulo);
        tarea.setDescripcion("Descripcion");
        tarea.setTipo(TipoTarea.REVISION);
        tarea.setFechaHora(fechaHora);
        tarea.setEstado(EstadoTarea.PENDIENTE);
        tarea.setCat(gato);
        tarea.setColony(colonia);
        return tarea;
    }
}
