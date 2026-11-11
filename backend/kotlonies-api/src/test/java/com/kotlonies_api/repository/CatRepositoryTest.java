package com.kotlonies_api.repository;

import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
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
class CatRepositoryTest {

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private ColonyRepository colonyRepository;

    @Test
    void saveAndFindById_shouldPersistCatWithColonyRelation() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Gatos", "Desc", "Madrid", true));
        Cat gato = new Cat();
        gato.setName("Milo");
        gato.setSex("MALE");
        gato.setBirthDate(LocalDate.of(2021, 3, 12));
        gato.setColor("Negro");
        gato.setStatus("EN_COLONIA");
        gato.setNeutered(true);
        gato.setIdentified(true);
        gato.setColony(colonia);

        Cat guardado = catRepository.saveAndFlush(gato);

        Cat encontrado = catRepository.findById(guardado.getId()).orElseThrow();
        assertThat(encontrado.getName()).isEqualTo("Milo");
        assertThat(encontrado.getColony()).isNotNull();
        assertThat(encontrado.getColony().getName()).isEqualTo("Colonia Gatos");
    }

    @Test
    void findAll_shouldReturnCatsFilteredByStatus() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Estado", "Desc", "Barcelona", true));
        catRepository.saveAndFlush(gatoConEstado(colonia, "Nala", "EN_COLONIA"));
        catRepository.saveAndFlush(gatoConEstado(colonia, "Mochi", "EN_ADOPCION"));
        catRepository.saveAndFlush(gatoConEstado(colonia, "Pipa", "EN_COLONIA"));

        List<Cat> gatosEnColonia = catRepository.findAll().stream()
                .filter(cat -> "EN_COLONIA".equals(cat.getStatus()))
                .toList();

        assertThat(gatosEnColonia).hasSize(2);
        assertThat(gatosEnColonia).extracting(Cat::getName).containsExactlyInAnyOrder("Nala", "Pipa");
    }

    @Test
    void delete_shouldRemoveCat() {
        Colony colonia = colonyRepository.saveAndFlush(new Colony("Colonia Delete", "Desc", "Valencia", true));
        Cat gato = catRepository.saveAndFlush(gatoConEstado(colonia, "Luna", "EN_COLONIA"));

        catRepository.delete(gato);
        catRepository.flush();

        assertThat(catRepository.findById(gato.getId())).isEmpty();
    }

    private Cat gatoConEstado(Colony colonia, String nombre, String status) {
        Cat gato = new Cat();
        gato.setName(nombre);
        gato.setSex("FEMALE");
        gato.setColor("Blanco");
        gato.setStatus(status);
        gato.setNeutered(false);
        gato.setIdentified(true);
        gato.setColony(colonia);
        return gato;
    }
}
