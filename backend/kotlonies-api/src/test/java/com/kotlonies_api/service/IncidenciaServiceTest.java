package com.kotlonies_api.service;

import com.kotlonies_api.dto.IncidenciaRequest;
import com.kotlonies_api.dto.IncidenciaResponse;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.EstadoIncidencia;
import com.kotlonies_api.model.Incidencia;
import com.kotlonies_api.model.PrioridadIncidencia;
import com.kotlonies_api.model.TipoIncidencia;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import com.kotlonies_api.repository.IncidenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenciaServiceTest {

    @Mock
    private IncidenciaRepository incidenciaRepository;

    @Mock
    private CatRepository catRepository;

    @Mock
    private ColonyRepository colonyRepository;

    @InjectMocks
    private IncidenciaService incidenciaService;

    @Test
    void obtenerTodasLasIncidenciasDevuelveLista() {
        Incidencia primera = new Incidencia();
        setId(primera, 1L);
        primera.setTitulo("Vacunación");
        primera.setDescripcion("Necesita revisión");
        primera.setTipo(TipoIncidencia.SALUD);
        primera.setPrioridad(PrioridadIncidencia.ALTA);
        primera.setEstado(EstadoIncidencia.ABIERTA);

        Incidencia segunda = new Incidencia();
        setId(segunda, 2L);
        segunda.setTitulo("Comida");
        segunda.setDescripcion("Falta pienso");
        segunda.setTipo(TipoIncidencia.ALIMENTACION);
        segunda.setPrioridad(PrioridadIncidencia.MEDIA);
        segunda.setEstado(EstadoIncidencia.EN_PROCESO);

        when(incidenciaRepository.findAll()).thenReturn(List.of(primera, segunda));

        List<IncidenciaResponse> resultado = incidenciaService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Vacunación", resultado.get(0).titulo());
        assertEquals("Comida", resultado.get(1).titulo());

        verify(incidenciaRepository).findAll();
    }

    @Test
    void obtenerIncidenciaPorIdExistenteDevuelveRespuesta() {
        Incidencia incidencia = new Incidencia();
        setId(incidencia, 1L);
        incidencia.setTitulo("Vacunación");
        incidencia.setDescripcion("Necesita revisión");
        incidencia.setTipo(TipoIncidencia.SALUD);
        incidencia.setPrioridad(PrioridadIncidencia.ALTA);
        incidencia.setEstado(EstadoIncidencia.ABIERTA);

        Cat cat = new Cat();
        setId(cat, 3L);
        cat.setName("Milo");
        incidencia.setCat(cat);

        Colony colony = new Colony("La Primavera", "Descripcion", "Norte", true);
        setId(colony, 5L);
        incidencia.setColony(colony);

        when(incidenciaRepository.findById(1L)).thenReturn(Optional.of(incidencia));

        IncidenciaResponse resultado = incidenciaService.findById(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Vacunación", resultado.titulo());
        assertEquals("SALUD", resultado.tipo());
        assertEquals("ALTA", resultado.prioridad());
        assertEquals("ABIERTA", resultado.estado());
        assertEquals(3L, resultado.catId());
        assertEquals("Milo", resultado.catName());
        assertEquals(5L, resultado.colonyId());
        assertEquals("La Primavera", resultado.colonyName());

        verify(incidenciaRepository).findById(1L);
    }

    @Test
    void crearIncidenciaCorrectamenteGuardaLaEntidad() {
        IncidenciaRequest request = new IncidenciaRequest(
                "Vacunación",
                "Necesita revisión",
                TipoIncidencia.SALUD,
                PrioridadIncidencia.ALTA,
                EstadoIncidencia.ABIERTA,
                null,
                null
        );

        when(incidenciaRepository.save(any(Incidencia.class)))
                .thenAnswer(invocation -> {
                    Incidencia creada = invocation.getArgument(0);
                    setId(creada, 10L);
                    return creada;
                });

        IncidenciaResponse resultado = incidenciaService.create(request);

        assertEquals(10L, resultado.id());
        assertEquals("Vacunación", resultado.titulo());
        assertEquals("Necesita revisión", resultado.descripcion());
        assertEquals("SALUD", resultado.tipo());
        assertEquals("ALTA", resultado.prioridad());
        assertEquals("ABIERTA", resultado.estado());
        assertNull(resultado.catId());
        assertNull(resultado.colonyId());

        verify(incidenciaRepository).save(any(Incidencia.class));
    }

    @Test
    void actualizarIncidenciaExistenteModificaCampos() {
        Incidencia incidencia = new Incidencia();
        setId(incidencia, 2L);
        incidencia.setTitulo("Viejo");
        incidencia.setDescripcion("Vieja descripción");
        incidencia.setTipo(TipoIncidencia.OTRO);
        incidencia.setPrioridad(PrioridadIncidencia.BAJA);
        incidencia.setEstado(EstadoIncidencia.ABIERTA);

        Cat cat = new Cat();
        setId(cat, 3L);
        cat.setName("Milo");

        Colony colony = new Colony("La Primavera", "Descripcion", "Norte", true);
        setId(colony, 5L);

        IncidenciaRequest request = new IncidenciaRequest(
                "Nuevo título",
                "Nueva descripción",
                TipoIncidencia.ALIMENTACION,
                PrioridadIncidencia.URGENTE,
                EstadoIncidencia.EN_PROCESO,
                3L,
                5L
        );

        when(incidenciaRepository.findById(2L)).thenReturn(Optional.of(incidencia));
        when(catRepository.findById(3L)).thenReturn(Optional.of(cat));
        when(colonyRepository.findById(5L)).thenReturn(Optional.of(colony));

        IncidenciaResponse resultado = incidenciaService.update(2L, request);

        assertEquals("Nuevo título", resultado.titulo());
        assertEquals("Nueva descripción", resultado.descripcion());
        assertEquals("ALIMENTACION", resultado.tipo());
        assertEquals("URGENTE", resultado.prioridad());
        assertEquals("EN_PROCESO", resultado.estado());
        assertEquals(3L, resultado.catId());
        assertEquals("Milo", resultado.catName());
        assertEquals(5L, resultado.colonyId());
        assertEquals("La Primavera", resultado.colonyName());

        verify(incidenciaRepository).findById(2L);
        verify(catRepository).findById(3L);
        verify(colonyRepository).findById(5L);
    }

    @Test
    void eliminarIncidenciaExistenteEliminaEntidad() {
        Incidencia incidencia = new Incidencia();
        setId(incidencia, 7L);
        incidencia.setTitulo("Comida");
        incidencia.setDescripcion("Falta pienso");
        incidencia.setTipo(TipoIncidencia.ALIMENTACION);
        incidencia.setPrioridad(PrioridadIncidencia.MEDIA);
        incidencia.setEstado(EstadoIncidencia.ABIERTA);

        when(incidenciaRepository.findById(7L)).thenReturn(Optional.of(incidencia));

        incidenciaService.delete(7L);

        verify(incidenciaRepository).delete(incidencia);
    }

    @Test
    void obtenerIncidenciaInexistenteLanzaNotFound() {
        when(incidenciaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> incidenciaService.findById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("99"));

        verify(incidenciaRepository).findById(99L);
    }

    @Test
    void eliminarIncidenciaInexistenteLanzaNotFound() {
        when(incidenciaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> incidenciaService.delete(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(incidenciaRepository).findById(99L);
    }

    @Test
    void crearIncidenciaSinGatoNiColoniaEsPermitido() {
        IncidenciaRequest request = new IncidenciaRequest(
                "Control",
                "Revisión general",
                TipoIncidencia.SALUD,
                PrioridadIncidencia.MEDIA,
                EstadoIncidencia.ABIERTA,
                null,
                null
        );

        when(incidenciaRepository.save(any(Incidencia.class)))
                .thenAnswer(invocation -> {
                    Incidencia creada = invocation.getArgument(0);
                    setId(creada, 20L);
                    return creada;
                });

        IncidenciaResponse resultado = incidenciaService.create(request);

        assertEquals("Control", resultado.titulo());
        assertNull(resultado.catId());
        assertNull(resultado.colonyId());
        verify(incidenciaRepository).save(any(Incidencia.class));
        verify(catRepository, never()).findById(anyLong());
        verify(colonyRepository, never()).findById(anyLong());
    }

    @Test
    void crearIncidenciaConGatoInexistenteLanzaNotFound() {
        IncidenciaRequest request = new IncidenciaRequest(
                "Vacunación",
                "Necesita revisión",
                TipoIncidencia.SALUD,
                PrioridadIncidencia.ALTA,
                EstadoIncidencia.ABIERTA,
                99L,
                null
        );

        when(catRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> incidenciaService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("99"));
        verify(catRepository).findById(99L);
        verify(incidenciaRepository, never()).save(any(Incidencia.class));
    }

    @Test
    void crearIncidenciaConColoniaInexistenteLanzaNotFound() {
        IncidenciaRequest request = new IncidenciaRequest(
                "Control",
                "Revisión general",
                TipoIncidencia.SALUD,
                PrioridadIncidencia.MEDIA,
                EstadoIncidencia.ABIERTA,
                null,
                77L
        );

        when(colonyRepository.findById(77L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> incidenciaService.create(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("77"));
        verify(colonyRepository).findById(77L);
        verify(incidenciaRepository, never()).save(any(Incidencia.class));
    }

    @Test
    void validacionDeCamposObligatoriosSeResuelveEnControladorNoEnServicio() {
        IncidenciaRequest request = new IncidenciaRequest(
                null,
                "Descripción",
                TipoIncidencia.SALUD,
                PrioridadIncidencia.MEDIA,
                EstadoIncidencia.ABIERTA,
                null,
                null
        );

        assertThrows(
                NullPointerException.class,
                () -> incidenciaService.create(request)
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
