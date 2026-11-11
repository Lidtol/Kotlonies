package com.kotlonies_api.repository;

import com.kotlonies_api.model.Colony;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ColonyRepositoryTest {

    @Autowired
    private ColonyRepository colonyRepository;

    @Test
    void saveAndFindById_shouldPersistColony() {
        Colony colonia = new Colony("Colonia Norte", "Descripcion", "Sevilla", true);

        Colony guardada = colonyRepository.saveAndFlush(colonia);
        Optional<Colony> encontrada = colonyRepository.findById(guardada.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getName()).isEqualTo("Colonia Norte");
        assertThat(encontrada.get().getLocation()).isEqualTo("Sevilla");
    }

    @Test
    void existsByNameIgnoreCase_shouldIgnoreCase() {
        colonyRepository.saveAndFlush(new Colony("Colonia Sur", "Desc", "Valencia", true));

        boolean existe = colonyRepository.existsByNameIgnoreCase("COLONIA SUR");

        assertThat(existe).isTrue();
    }

    @Test
    void delete_shouldRemoveColony() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Borrar", "Desc", "Zaragoza", true));

        colonyRepository.delete(colonia);
        colonyRepository.flush();

        assertThat(colonyRepository.findById(colonia.getId())).isEmpty();
    }
}
