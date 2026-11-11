package com.kotlonies_api.repository;

import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.Incidencia;
import com.kotlonies_api.model.PrioridadIncidencia;
import com.kotlonies_api.model.TipoIncidencia;
import com.kotlonies_api.model.EstadoIncidencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IncidenciaRepositoryTest {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private ColonyRepository colonyRepository;

    @Test
    void saveAndFindById_shouldPersistIncidenciaWithRelations() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Incidencias", "Desc", "Toledo", true));
        Cat gato = catRepository.saveAndFlush(catConColonia(colonia, "Kira"));
        Incidencia incidencia = new Incidencia();
        incidencia.setTitulo("Necesita revisión");
        incidencia.setDescripcion("El gato tiene fiebre");
        incidencia.setTipo(TipoIncidencia.SALUD);
        incidencia.setPrioridad(PrioridadIncidencia.ALTA);
        incidencia.setEstado(EstadoIncidencia.ABIERTA);
        incidencia.setCat(gato);
        incidencia.setColony(colonia);

        Incidencia guardada = incidenciaRepository.saveAndFlush(incidencia);

        Incidencia encontrada = incidenciaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(encontrada.getTitulo()).isEqualTo("Necesita revisión");
        assertThat(encontrada.getCat()).isNotNull();
        assertThat(encontrada.getColony()).isNotNull();
        assertThat(encontrada.getCat().getName()).isEqualTo("Kira");
    }

    @Test
    void findAll_shouldReturnIncidenciasForSpecificColony() {
        Colony coloniaA = colonyRepository.saveAndFlush(new Colony("Colonia A", "Desc", "Sevilla", true));
        Colony coloniaB = colonyRepository.saveAndFlush(new Colony("Colonia B", "Desc", "Cordoba", true));
        Cat gatoA = catRepository.saveAndFlush(catConColonia(coloniaA, "Milo"));
        Cat gatoB = catRepository.saveAndFlush(catConColonia(coloniaB, "Luna"));
        incidenciaRepository.saveAndFlush(incidenciaPara(gatoA, coloniaA, "Incidencia A"));
        incidenciaRepository.saveAndFlush(incidenciaPara(gatoB, coloniaB, "Incidencia B"));

        List<Incidencia> incidencias = incidenciaRepository.findAll().stream()
                .filter(i -> i.getColony() != null && i.getColony().getId().equals(coloniaA.getId()))
                .toList();

        assertThat(incidencias).hasSize(1);
        assertThat(incidencias.getFirst().getTitulo()).isEqualTo("Incidencia A");
    }

    @Test
    void delete_shouldRemoveIncidencia() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Delete", "Desc", "Almería", true));
        Cat gato = catRepository.saveAndFlush(catConColonia(colonia, "Pip"));
        Incidencia incidencia = incidenciaRepository.saveAndFlush(incidenciaPara(gato, colonia, "Incidencia Delete"));

        incidenciaRepository.delete(incidencia);
        incidenciaRepository.flush();

        assertThat(incidenciaRepository.findById(incidencia.getId())).isEmpty();
    }

    private Cat catConColonia(Colony colonia, String nombre) {
        Cat gato = new Cat();
        gato.setName(nombre);
        gato.setSex("MALE");
        gato.setBirthDate(LocalDate.of(2022, 5, 15));
        gato.setColor("Blanco");
        gato.setStatus("EN_COLONIA");
        gato.setNeutered(true);
        gato.setIdentified(true);
        gato.setColony(colonia);
        return gato;
    }

    private Incidencia incidenciaPara(Cat gato, Colony colonia, String titulo) {
        Incidencia i = new Incidencia();
        i.setTitulo(titulo);
        i.setDescripcion("Consulta de prueba");
        i.setTipo(TipoIncidencia.ALIMENTACION);
        i.setPrioridad(PrioridadIncidencia.MEDIA);
        i.setEstado(EstadoIncidencia.EN_PROCESO);
        i.setCat(gato);
        i.setColony(colonia);
        return i;
    }
}
