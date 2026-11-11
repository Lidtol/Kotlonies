package com.kotlonies_api.service;

import com.kotlonies_api.dto.ColonyRequest;
import com.kotlonies_api.dto.ColonyResponse;
import com.kotlonies_api.exception.ColonyNotFoundException;
import com.kotlonies_api.exception.DuplicateColonyNameException;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.repository.ColonyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColoniaServiceTest {

    @Mock
    private ColonyRepository repository;

    @InjectMocks
    private ColonyService colonyService;

    @Test
    void listarColoniasDevuelveTodasLasColonias() {
        Colony colonia1 = new Colony("La Primavera", "Descripcion 1", "Norte", true);
        Colony colonia2 = new Colony("El Roble", "Descripcion 2", "Sur", false);

        when(repository.findAll()).thenReturn(List.of(colonia1, colonia2));

        List<ColonyResponse> resultado = colonyService.list();

        assertEquals(2, resultado.size());
        assertEquals("La Primavera", resultado.get(0).name());
        assertEquals("El Roble", resultado.get(1).name());
        assertTrue(resultado.get(0).active());
        assertFalse(resultado.get(1).active());

        verify(repository).findAll();
    }

    @Test
    void obtenerColoniaPorIdExistenteDevuelveRespuesta() {
        Colony colonia = new Colony("La Primavera", "Descripcion", "Norte", true);

        when(repository.findById(1L)).thenReturn(Optional.of(colonia));

        ColonyResponse resultado = colonyService.getById(1L);

        assertEquals("La Primavera", resultado.name());
        assertEquals("Descripcion", resultado.description());
        assertEquals("Norte", resultado.location());
        assertTrue(resultado.active());

        verify(repository).findById(1L);
    }

    @Test
    void obtenerColoniaPorIdInexistenteLanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ColonyNotFoundException.class,
                () -> colonyService.getById(99L)
        );

        verify(repository).findById(99L);
    }

    @Test
    void crearColoniaCorrectamenteGuardaLaEntidadYDevuelveLaRespuesta() {
        ColonyRequest request = new ColonyRequest(
                " La Primavera ",
                "Descripcion",
                " Norte ",
                null
        );

        when(repository.existsByNameIgnoreCase("La Primavera")).thenReturn(false);
        when(repository.save(any(Colony.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ColonyResponse resultado = colonyService.create(request);

        assertEquals("La Primavera", resultado.name());
        assertEquals("Descripcion", resultado.description());
        assertEquals("Norte", resultado.location());
        assertTrue(resultado.active());

        verify(repository).existsByNameIgnoreCase("La Primavera");
        verify(repository).save(any(Colony.class));
    }

    @Test
    void crearColoniaConNombreDuplicadoLanzaExcepcion() {
        ColonyRequest request = new ColonyRequest(
                "La Primavera",
                "Descripcion",
                "Norte",
                true
        );

        when(repository.existsByNameIgnoreCase("La Primavera")).thenReturn(true);

        assertThrows(
                DuplicateColonyNameException.class,
                () -> colonyService.create(request)
        );

        verify(repository).existsByNameIgnoreCase("La Primavera");
        verify(repository, never()).save(any(Colony.class));
    }

    @Test
    void actualizarColoniaExistenteModificaLosCamposCorrectamente() {
        Colony colonia = new Colony("Vieja", "Descripcion vieja", "Ubicacion vieja", true);
        ColonyRequest request = new ColonyRequest(
                "Nueva",
                "Descripcion nueva",
                "Ubicacion nueva",
                false
        );

        when(repository.findById(1L)).thenReturn(Optional.of(colonia));
        when(repository.existsByNameIgnoreCase("Nueva")).thenReturn(false);

        ColonyResponse resultado = colonyService.update(1L, request);

        assertEquals("Nueva", resultado.name());
        assertEquals("Descripcion nueva", resultado.description());
        assertEquals("Ubicacion nueva", resultado.location());
        assertFalse(resultado.active());

        verify(repository).findById(1L);
        verify(repository).existsByNameIgnoreCase("Nueva");
    }

    @Test
    void actualizarColoniaInexistenteLanzaExcepcion() {
        ColonyRequest request = new ColonyRequest(
                "Nueva",
                "Descripcion",
                "Ubicacion",
                true
        );

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ColonyNotFoundException.class,
                () -> colonyService.update(99L, request)
        );

        verify(repository).findById(99L);
        verify(repository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    void actualizarColoniaConNombreDuplicadoLanzaExcepcion() {
        Colony colonia = new Colony("Vieja", "Descripcion vieja", "Ubicacion vieja", true);
        ColonyRequest request = new ColonyRequest(
                "Duplicada",
                "Descripcion nueva",
                "Ubicacion nueva",
                true
        );

        when(repository.findById(1L)).thenReturn(Optional.of(colonia));
        when(repository.existsByNameIgnoreCase("Duplicada")).thenReturn(true);

        assertThrows(
                DuplicateColonyNameException.class,
                () -> colonyService.update(1L, request)
        );

        verify(repository).findById(1L);
        verify(repository).existsByNameIgnoreCase("Duplicada");
    }

    @Test
    void archivarColoniaExistenteDesactivaLaColonia() {
        Colony colonia = new Colony("La Primavera", "Descripcion", "Norte", true);

        when(repository.findById(1L)).thenReturn(Optional.of(colonia));

        ColonyResponse resultado = colonyService.archive(1L);

        assertEquals("La Primavera", resultado.name());
        assertFalse(resultado.active());
        assertFalse(colonia.isActive());

        verify(repository).findById(1L);
    }

    @Test
    void archivarColoniaInexistenteLanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ColonyNotFoundException.class,
                () -> colonyService.archive(99L)
        );

        verify(repository).findById(99L);
    }
}
