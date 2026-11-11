package com.kotlonies_api;

import com.kotlonies_api.dto.CatRequest;
import com.kotlonies_api.dto.ColonyRequest;
import com.kotlonies_api.dto.IncidenciaRequest;
import com.kotlonies_api.dto.TareaRequest;
import com.kotlonies_api.dto.UsuarioRequest;
import com.kotlonies_api.dto.UsuarioUpdateRequest;
import com.kotlonies_api.exception.DuplicateColonyNameException;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.EstadoIncidencia;
import com.kotlonies_api.model.EstadoTarea;
import com.kotlonies_api.model.Incidencia;
import com.kotlonies_api.model.PrioridadIncidencia;
import com.kotlonies_api.model.Tarea;
import com.kotlonies_api.model.TipoIncidencia;
import com.kotlonies_api.model.TipoTarea;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import com.kotlonies_api.repository.IncidenciaRepository;
import com.kotlonies_api.repository.TareaRepository;
import com.kotlonies_api.repository.UsuarioRepository;
import com.kotlonies_api.service.CatService;
import com.kotlonies_api.service.ColonyService;
import com.kotlonies_api.service.IncidenciaService;
import com.kotlonies_api.service.TareaService;
import com.kotlonies_api.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessFlowIntegrationTest {

    @Autowired
    private ColonyService colonyService;

    @Autowired
    private CatService catService;

    @Autowired
    private IncidenciaService incidenciaService;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ColonyRepository colonyRepository;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void shouldCreateColonyAndAssociateCatsWithDifferentStatuses() {
        var colonia = colonyService.create(new ColonyRequest(
                "Colonia Norte",
                "Colonia principal",
                "Madrid",
                true
        ));

        var gatoEnColonia = catService.create(catRequest(
                "Milo",
                "MALE",
                "2021-03-12",
                "Negro",
                "EN_COLONIA",
                true,
                true,
                colonia.id()
        ));

        var gatoEnSeguimiento = catService.create(catRequest(
                "Luna",
                "FEMALE",
                "2022-05-04",
                "Blanca",
                "EN_SEGUIMIENTO",
                false,
                true,
                null
        ));

        var gatoEnAdopcion = catService.create(catRequest(
                "Nico",
                "MALE",
                "2020-11-22",
                "Atigrado",
                "EN_ADOPCION",
                false,
                false,
                colonia.id()
        ));

        assertThat(colonia.id()).isNotNull();
        assertThat(gatoEnColonia.colonyId()).isEqualTo(colonia.id());
        assertThat(gatoEnSeguimiento.colonyId()).isNull();
        assertThat(gatoEnAdopcion.colonyId()).isEqualTo(colonia.id());

        var gatos = catRepository.findAll();
        assertThat(gatos).hasSize(3);
        assertThat(gatos).extracting(Cat::getStatus)
                .containsExactlyInAnyOrder("EN_COLONIA", "EN_SEGUIMIENTO", "EN_ADOPCION");

        assertThat(catRepository.findById(gatoEnColonia.id()).orElseThrow().getColony().getName())
                .isEqualTo("Colonia Norte");
        assertThat(catRepository.findById(gatoEnSeguimiento.id()).orElseThrow().getColony()).isNull();
    }

    @Test
    void shouldPersistIncidenciasWithAndWithoutAssociations() {
        var colonia = colonyService.create(new ColonyRequest(
                "Colonia Sur",
                "Colonia de incidencias",
                "Sevilla",
                true
        ));

        var gato = catService.create(catRequest(
                "Pipa",
                "FEMALE",
                "2021-02-10",
                "Blanco",
                "EN_COLONIA",
                true,
                true,
                colonia.id()
        ));

        var incidenciaConRelaciones = incidenciaService.create(new IncidenciaRequest(
                "Vacunación",
                "Necesita revisión anual",
                TipoIncidencia.SALUD,
                PrioridadIncidencia.ALTA,
                EstadoIncidencia.ABIERTA,
                gato.id(),
                colonia.id()
        ));

        var incidenciaSinRelaciones = incidenciaService.create(new IncidenciaRequest(
                "Control general",
                "Sin asociación directa",
                TipoIncidencia.OTRO,
                PrioridadIncidencia.MEDIA,
                EstadoIncidencia.ABIERTA,
                null,
                null
        ));

        assertThat(incidenciaConRelaciones.catId()).isEqualTo(gato.id());
        assertThat(incidenciaConRelaciones.colonyId()).isEqualTo(colonia.id());
        assertThat(incidenciaSinRelaciones.catId()).isNull();
        assertThat(incidenciaSinRelaciones.colonyId()).isNull();

        List<Incidencia> persistidas = incidenciaRepository.findAll();
        assertThat(persistidas).hasSize(2);
        assertThat(persistidas).extracting(Incidencia::getTitulo)
                .containsExactlyInAnyOrder("Vacunación", "Control general");

        var incidenciaGuardada = incidenciaRepository.findById(incidenciaConRelaciones.id()).orElseThrow();
        assertThat(incidenciaGuardada.getCat().getId()).isEqualTo(gato.id());
        assertThat(incidenciaGuardada.getColony().getId()).isEqualTo(colonia.id());
    }

    @Test
    void shouldCreateCalendarEventsAndKeepThemOrderedAndCompleted() {
        var colonia = colonyService.create(new ColonyRequest(
                "Colonia Calendario",
                "Agenda de tareas",
                "Valencia",
                true
        ));

        var gato = catService.create(catRequest(
                "Mochi",
                "MALE",
                "2022-10-18",
                "Gris",
                "EN_SEGUIMIENTO",
                false,
                false,
                null
        ));

        var tarea1 = tareaService.create(new TareaRequest(
                "Revisión inicial",
                "Comprobar estado general",
                TipoTarea.REVISION,
                LocalDateTime.of(2026, 11, 20, 9, 0),
                EstadoTarea.PENDIENTE,
                gato.id(),
                colonia.id()
        ));

        var tarea2 = tareaService.create(new TareaRequest(
                "Visita de seguimiento",
                "Revisión en la colonia",
                TipoTarea.VISITA,
                LocalDateTime.of(2026, 11, 20, 8, 30),
                EstadoTarea.PENDIENTE,
                gato.id(),
                colonia.id()
        ));

        var ordenadas = tareaService.findAll();
        assertThat(ordenadas).extracting(item -> item.titulo())
                .containsExactly("Visita de seguimiento", "Revisión inicial");

        var completada = tareaService.completar(tarea1.id(), true);
        assertThat(completada.estado()).isEqualTo("COMPLETADA");

        var tareaPersistida = tareaRepository.findById(tarea1.id()).orElseThrow();
        assertThat(tareaPersistida.getEstado()).isEqualTo(EstadoTarea.COMPLETADA);

        tareaService.delete(tarea2.id());
        assertThat(tareaRepository.findById(tarea2.id())).isEmpty();
    }

    @Test
    void shouldCreateUsersWithDifferentRolesAndRespectAdminRules() {
        var admin = usuarioService.crear(new UsuarioRequest(
                "Admin 1",
                "admin1@kotlonies.test",
                "Password123",
                "ADMIN",
                true
        ));

        var cuidador = usuarioService.crear(new UsuarioRequest(
                "Cuidador",
                "cuidador@kotlonies.test",
                "Password123",
                "CUIDADOR",
                true
        ));

        var consulta = usuarioService.crear(new UsuarioRequest(
                "Consulta",
                "consulta@kotlonies.test",
                "Password123",
                "CONSULTA",
                true
        ));

        assertThat(usuarioRepository.findAll()).hasSize(3);
        assertThat(usuarioRepository.findById(admin.getId()).orElseThrow().getRol()).isEqualTo("ADMIN");
        assertThat(usuarioRepository.findById(cuidador.getId()).orElseThrow().getRol()).isEqualTo("CUIDADOR");
        assertThat(usuarioRepository.findById(consulta.getId()).orElseThrow().getRol()).isEqualTo("CONSULTA");

        var admin2 = usuarioService.crear(new UsuarioRequest(
                "Admin 2",
                "admin2@kotlonies.test",
                "Password123",
                "ADMIN",
                false
        ));

        assertThatThrownBy(() -> usuarioService.actualizar(
                admin.getId(),
                new UsuarioUpdateRequest(
                        "Admin 1",
                        "admin1@kotlonies.test",
                        null,
                        "ADMIN",
                        false
                ),
                admin2.getEmail()
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("último administrador activo");

        usuarioService.actualizar(
                admin2.getId(),
                new UsuarioUpdateRequest(
                        "Admin 2",
                        "admin2@kotlonies.test",
                        null,
                        "ADMIN",
                        true
                ),
                admin.getEmail()
        );

        usuarioService.actualizar(
                admin.getId(),
                new UsuarioUpdateRequest(
                        "Admin 1",
                        "admin1@kotlonies.test",
                        null,
                        "ADMIN",
                        false
                ),
                admin2.getEmail()
        );

        assertThat(usuarioRepository.findById(admin.getId()).orElseThrow().isActivo()).isFalse();
    }

    @Test
    void shouldRejectInvalidOperationsWithoutLeavingPartialChanges() {
        var colonia = colonyService.create(new ColonyRequest(
                "Colonia Inválida",
                "Para comprobar rollback",
                "Bilbao",
                true
        ));

        long colonyCountBeforeDuplicate = colonyRepository.count();
        assertThatThrownBy(() -> colonyService.create(new ColonyRequest(
                "Colonia Inválida",
                "Duplicada",
                "Sevilla",
                true
        ))).isInstanceOf(DuplicateColonyNameException.class);
        assertThat(colonyRepository.count()).isEqualTo(colonyCountBeforeDuplicate);

        long catCountBeforeInvalid = catRepository.count();
        assertThatThrownBy(() -> catService.create(catRequest(
                "Gato inválido",
                "MALE",
                "2023-07-05",
                "Naranja",
                "EN_COLONIA",
                false,
                true,
                null
        ))).isInstanceOf(ResponseStatusException.class);
        assertThat(catRepository.count()).isEqualTo(catCountBeforeInvalid);

        long incidenciaCountBeforeInvalid = incidenciaRepository.count();
        assertThatThrownBy(() -> incidenciaService.create(new IncidenciaRequest(
                "Fallo",
                "No debería persistir",
                TipoIncidencia.OTRO,
                PrioridadIncidencia.MEDIA,
                EstadoIncidencia.ABIERTA,
                999L,
                colonia.id()
        ))).isInstanceOf(ResponseStatusException.class);
        assertThat(incidenciaRepository.count()).isEqualTo(incidenciaCountBeforeInvalid);
    }

    private CatRequest catRequest(
            String name,
            String sex,
            String birthDate,
            String color,
            String status,
            boolean neutered,
            boolean identified,
            Long colonyId
    ) {
        CatRequest request = new CatRequest();
        request.setName(name);
        request.setSex(sex);
        request.setBirthDate(birthDate);
        request.setColor(color);
        request.setStatus(status);
        request.setNeutered(neutered);
        request.setIdentified(identified);
        request.setColonyId(colonyId);
        return request;
    }
}
