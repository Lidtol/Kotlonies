package com.kotlonies_api.service;

import com.kotlonies_api.dto.TareaRequest;
import com.kotlonies_api.dto.TareaResponse;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.EstadoTarea;
import com.kotlonies_api.model.Tarea;
import com.kotlonies_api.model.TipoTarea;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import com.kotlonies_api.repository.TareaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TareaServiceTest {

    @Mock
    private TareaRepository tareaRepository;

    @Mock
    private CatRepository catRepository;

    @Mock
    private ColonyRepository colonyRepository;

    @InjectMocks
    private TareaService tareaService;

    @Test
    void obtenerTodasLasTareasDevuelveListadoOrdenadoPorFechaHora() {
        LocalDateTime primera = LocalDateTime.of(2026, 11, 11, 9, 0);
        LocalDateTime segunda = LocalDateTime.of(2026, 11, 12, 10, 30);

        Tarea tarea1 = new Tarea();
        setId(tarea1, 1L);
        tarea1.setTitulo("Revisión");
        tarea1.setDescripcion("Revisión general");
        tarea1.setTipo(TipoTarea.REVISION);
        tarea1.setFechaHora(primera);
        tarea1.setEstado(EstadoTarea.PENDIENTE);

        Tarea tarea2 = new Tarea();
        setId(tarea2, 2L);
        tarea2.setTitulo("Visita");
        tarea2.setDescripcion("Visita a colonia");
        tarea2.setTipo(TipoTarea.VISITA);
        tarea2.setFechaHora(segunda);
        tarea2.setEstado(EstadoTarea.COMPLETADA);

        when(tareaRepository.findAllByOrderByFechaHoraAsc()).thenReturn(List.of(tarea1, tarea2));

        List<TareaResponse> resultado = tareaService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Revisión", resultado.get(0).titulo());
        assertEquals("Visita", resultado.get(1).titulo());
        assertEquals(primera, resultado.get(0).fechaHora());

        verify(tareaRepository).findAllByOrderByFechaHoraAsc();
    }

    @Test
    void obtenerTareaPorIdExistenteDevuelveRespuesta() {
        Tarea tarea = new Tarea();
        setId(tarea, 1L);
        tarea.setTitulo("Revisión");
        tarea.setDescripcion("Revisión general");
        tarea.setTipo(TipoTarea.REVISION);
        tarea.setFechaHora(LocalDateTime.of(2026, 11, 12, 14, 30));
        tarea.setEstado(EstadoTarea.PENDIENTE);

        Cat cat = new Cat();
        setId(cat, 3L);
        cat.setName("Milo");
        tarea.setCat(cat);

        Colony colony = new Colony("La Primavera", "Descripcion", "Norte", true);
        setId(colony, 5L);
        tarea.setColony(colony);

        when(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea));

        TareaResponse resultado = tareaService.findById(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Revisión", resultado.titulo());
        assertEquals("REVISION", resultado.tipo());
        assertEquals("PENDIENTE", resultado.estado());
        assertEquals(3L, resultado.catId());
        assertEquals("Milo", resultado.catName());
        assertEquals(5L, resultado.colonyId());
        assertEquals("La Primavera", resultado.colonyName());

        verify(tareaRepository).findById(1L);
    }

    @Test
    void crearTareaCorrectamenteGuardaLaEntidad() {
        TareaRequest request = new TareaRequest(
                "Revisión",
                "Revisión general",
                TipoTarea.REVISION,
                LocalDateTime.of(2026, 11, 12, 15, 0),
                EstadoTarea.PENDIENTE,
                null,
                null
        );

        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> {
            Tarea guardada = invocation.getArgument(0);
            setId(guardada, 10L);
            return guardada;
        });

        TareaResponse resultado = tareaService.create(request);

        assertEquals(10L, resultado.id());
        assertEquals("Revisión", resultado.titulo());
        assertEquals("REVISION", resultado.tipo());
        assertEquals("PENDIENTE", resultado.estado());
        assertNull(resultado.catId());
        assertNull(resultado.colonyId());

        verify(tareaRepository).save(any(Tarea.class));
    }

    @Test
    void actualizarTareaExistenteModificaCampos() {
        Tarea tarea = new Tarea();
        setId(tarea, 2L);
        tarea.setTitulo("Viejo");
        tarea.setDescripcion("Vieja");
        tarea.setTipo(TipoTarea.ALIMENTACION);
        tarea.setFechaHora(LocalDateTime.of(2026, 10, 1, 9, 0));
        tarea.setEstado(EstadoTarea.PENDIENTE);

        Cat cat = new Cat();
        setId(cat, 3L);
        cat.setName("Milo");

        Colony colony = new Colony("La Primavera", "Desc", "Norte", true);
        setId(colony, 5L);

        TareaRequest request = new TareaRequest(
                "Nuevo título",
                "Nueva descripción",
                TipoTarea.VISITA,
                LocalDateTime.of(2026, 11, 15, 16, 30),
                EstadoTarea.COMPLETADA,
                3L,
                5L
        );

        when(tareaRepository.findById(2L)).thenReturn(Optional.of(tarea));
        when(catRepository.findById(3L)).thenReturn(Optional.of(cat));
        when(colonyRepository.findById(5L)).thenReturn(Optional.of(colony));

        TareaResponse resultado = tareaService.update(2L, request);

        assertEquals("Nuevo título", resultado.titulo());
        assertEquals("Nueva descripción", resultado.descripcion());
        assertEquals("VISITA", resultado.tipo());
        assertEquals("COMPLETADA", resultado.estado());
        assertEquals(3L, resultado.catId());
        assertEquals("Milo", resultado.catName());
        assertEquals(5L, resultado.colonyId());
        assertEquals("La Primavera", resultado.colonyName());

        verify(tareaRepository).findById(2L);
        verify(catRepository).findById(3L);
        verify(colonyRepository).findById(5L);
    }

    @Test
    void completarTareaCambiaEstadoSegunValor() {
        Tarea tarea = new Tarea();
        setId(tarea, 4L);
        tarea.setTitulo("Visita");
        tarea.setDescripcion("Desc");
        tarea.setTipo(TipoTarea.VISITA);
        tarea.setFechaHora(LocalDateTime.of(2026, 11, 12, 10, 0));
        tarea.setEstado(EstadoTarea.PENDIENTE);

        when(tareaRepository.findById(4L)).thenReturn(Optional.of(tarea));

        TareaResponse completada = tareaService.completar(4L, true);
        assertEquals("COMPLETADA", completada.estado());

        TareaResponse pendiente = tareaService.completar(4L, false);
        assertEquals("PENDIENTE", pendiente.estado());

        verify(tareaRepository, times(2)).findById(4L);
    }

    @Test
    void eliminarTareaExistenteEliminaEntidad() {
        Tarea tarea = new Tarea();
        setId(tarea, 7L);
        tarea.setTitulo("Revisión");
        tarea.setDescripcion("Desc");
        tarea.setTipo(TipoTarea.REVISION);
        tarea.setFechaHora(LocalDateTime.of(2026, 11, 20, 11, 0));
        tarea.setEstado(EstadoTarea.PENDIENTE);

        when(tareaRepository.findById(7L)).thenReturn(Optional.of(tarea));

        tareaService.delete(7L);

        verify(tareaRepository).delete(tarea);
    }

    @Test
    void operacionesSobreTareaInexistenteLanzanNotFound() {
        when(tareaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exFind = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.findById(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exFind.getStatusCode());

        ResponseStatusException exDelete = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.delete(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exDelete.getStatusCode());

        verify(tareaRepository, times(2)).findById(99L);
    }

    @Test
    void crearTareaSinGatoNiColoniaEsPermitido() {
        TareaRequest request = new TareaRequest(
                "Revisión",
                "Descripción",
                TipoTarea.REVISION,
                LocalDateTime.of(2026, 11, 12, 9, 0),
                EstadoTarea.PENDIENTE,
                null,
                null
        );

        when(tareaRepository.save(any(Tarea.class))).thenAnswer(invocation -> {
            Tarea guardada = invocation.getArgument(0);
            setId(guardada, 11L);
            return guardada;
        });

        TareaResponse resultado = tareaService.create(request);

        assertEquals("Revisión", resultado.titulo());
        assertNull(resultado.catId());
        assertNull(resultado.colonyId());
        verify(tareaRepository).save(any(Tarea.class));
        verify(catRepository, never()).findById(anyLong());
        verify(colonyRepository, never()).findById(anyLong());
    }

    @Test
    void crearTareaConGatoInexistenteLanzaNotFound() {
        TareaRequest request = new TareaRequest(
                "Revisión",
                "Descripción",
                TipoTarea.REVISION,
                LocalDateTime.of(2026, 11, 12, 9, 0),
                EstadoTarea.PENDIENTE,
                77L,
                null
        );

        when(catRepository.findById(77L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("77"));
        verify(catRepository).findById(77L);
        verify(tareaRepository, never()).save(any(Tarea.class));
    }

    @Test
    void crearTareaConColoniaInexistenteLanzaNotFound() {
        TareaRequest request = new TareaRequest(
                "Revisión",
                "Descripción",
                TipoTarea.REVISION,
                LocalDateTime.of(2026, 11, 12, 9, 0),
                EstadoTarea.PENDIENTE,
                null,
                88L
        );

        when(colonyRepository.findById(88L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tareaService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("88"));
        verify(colonyRepository).findById(88L);
        verify(tareaRepository, never()).save(any(Tarea.class));
    }

    @Test
    void crearTareaConTituloNuloReproduceComportamientoRealDelServicio() {
        TareaRequest request = new TareaRequest(
                null,
                "Descripción",
                TipoTarea.REVISION,
                LocalDateTime.of(2026, 11, 12, 9, 0),
                EstadoTarea.PENDIENTE,
                null,
                null
        );

        assertThrows(
                NullPointerException.class,
                () -> tareaService.create(request)
        );
    }

    private void setId(Object target, Long value) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
