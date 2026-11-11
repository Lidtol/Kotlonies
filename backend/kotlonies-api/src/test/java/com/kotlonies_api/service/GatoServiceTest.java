package com.kotlonies_api.service;

import com.kotlonies_api.dto.CatRequest;
import com.kotlonies_api.dto.CatResponse;
import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatoServiceTest {

    @Mock
    private CatRepository catRepository;

    @Mock
    private ColonyRepository colonyRepository;

    @InjectMocks
    private CatService catService;

    @Test
    void obtenerTodosLosGatosDevuelveListado() {
        Cat gato1 = new Cat();
        gato1.setId(1L);
        gato1.setName("Milo");
        gato1.setStatus("EN_SEGUIMIENTO");

        Cat gato2 = new Cat();
        gato2.setId(2L);
        gato2.setName("Luna");
        gato2.setStatus("EN_ADOPCION");

        when(catRepository.findAll()).thenReturn(List.of(gato1, gato2));

        List<CatResponse> resultado = catService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Milo", resultado.get(0).name());
        assertEquals("Luna", resultado.get(1).name());

        verify(catRepository).findAll();
    }

    @Test
    void obtenerUnGatoPorIdExistenteDevuelveRespuesta() {
        Cat gato = new Cat();
        gato.setId(1L);
        gato.setName("Milo");
        gato.setSex("MALE");
        gato.setBirthDate(LocalDate.of(2022, 4, 12));
        gato.setColor("Naranja");
        gato.setStatus("EN_SEGUIMIENTO");
        gato.setNeutered(true);
        gato.setIdentified(false);
        gato.setObservations("Cuidado con el ojo izquierdo");

        when(catRepository.findById(1L)).thenReturn(Optional.of(gato));

        CatResponse resultado = catService.findById(1L);

        assertEquals(1L, resultado.id());
        assertEquals("Milo", resultado.name());
        assertEquals("MALE", resultado.sex());
        assertEquals("EN_SEGUIMIENTO", resultado.status());
        assertNull(resultado.colonyId());

        verify(catRepository).findById(1L);
    }

    @Test
    void operacionesSobreGatoInexistenteLanzanRespuestaNotFound() {
        when(catRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exFind = assertThrows(
                ResponseStatusException.class,
                () -> catService.findById(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exFind.getStatusCode());

        ResponseStatusException exDelete = assertThrows(
                ResponseStatusException.class,
                () -> catService.delete(99L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exDelete.getStatusCode());

        verify(catRepository, times(2)).findById(99L);
    }

    @Test
    void crearGatoCorrectamenteGuardaElEntidadYDevuelveRespuesta() {
        CatRequest request = new CatRequest();
        request.setName("Milo");
        request.setSex("MALE");
        request.setBirthDate("2023-05-10");
        request.setColor("Naranja");
        request.setStatus("EN_SEGUIMIENTO");
        request.setNeutered(true);
        request.setIdentified(false);
        request.setObservations("Observación");
        request.setColonyId(null);

        when(catRepository.save(any(Cat.class))).thenAnswer(invocation -> {
            Cat saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        CatResponse resultado = catService.create(request);

        assertEquals("Milo", resultado.name());
        assertEquals("MALE", resultado.sex());
        assertEquals("EN_SEGUIMIENTO", resultado.status());
        assertNull(resultado.colonyId());
        assertEquals("Observación", resultado.observations());

        verify(catRepository).save(any(Cat.class));
    }

    @Test
    void actualizarGatoExistenteModificaCamposYColonia() {
        Cat gato = new Cat();
        gato.setId(1L);
        gato.setName("Viejo");
        gato.setStatus("EN_SEGUIMIENTO");
        gato.setColony(null);

        Colony colonia = new Colony("La Primavera", "Colonia central", "Norte", true);
        setId(colonia, 7L);

        CatRequest request = new CatRequest();
        request.setName("Nuevo");
        request.setSex("FEMALE");
        request.setBirthDate("2021-08-15");
        request.setColor("Blanco");
        request.setStatus("EN_COLONIA");
        request.setNeutered(false);
        request.setIdentified(true);
        request.setObservations("Actualizada");
        request.setColonyId(7L);

        when(catRepository.findById(1L)).thenReturn(Optional.of(gato));
        when(colonyRepository.findById(7L)).thenReturn(Optional.of(colonia));

        CatResponse resultado = catService.update(1L, request);

        assertEquals("Nuevo", resultado.name());
        assertEquals("FEMALE", resultado.sex());
        assertEquals("EN_COLONIA", resultado.status());
        assertEquals(7L, resultado.colonyId());
        assertEquals("La Primavera", resultado.colonyName());

        verify(catRepository).findById(1L);
        verify(colonyRepository).findById(7L);
    }

    @Test
    void eliminarGatoExistenteEliminaEntidad() {
        Cat gato = new Cat();
        gato.setId(1L);
        gato.setName("Milo");
        gato.setStatus("EN_SEGUIMIENTO");

        when(catRepository.findById(1L)).thenReturn(Optional.of(gato));

        catService.delete(1L);

        verify(catRepository).delete(gato);
    }

    @Test
    void crearGatoSinStatusRechazaOperacion() {
        CatRequest request = new CatRequest();
        request.setName("Milo");
        request.setStatus(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> catService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Cat status is required"));
        verify(catRepository, never()).save(any(Cat.class));
    }

    @Test
    void crearGatoEnColoniaSinColoniaAsociadaRechazaOperacion() {
        CatRequest request = new CatRequest();
        request.setName("Milo");
        request.setStatus("EN_COLONIA");
        request.setColonyId(null);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> catService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("assigned colony"));
        verify(catRepository, never()).save(any(Cat.class));
    }

    @Test
    void crearGatoEnSeguimientoSinColoniaEsPermitido() {
        CatRequest request = new CatRequest();
        request.setName("Milo");
        request.setStatus("EN_SEGUIMIENTO");
        request.setColonyId(null);

        when(catRepository.save(any(Cat.class))).thenAnswer(invocation -> {
            Cat saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        CatResponse resultado = catService.create(request);

        assertEquals("EN_SEGUIMIENTO", resultado.status());
        assertNull(resultado.colonyId());
        verify(catRepository).save(any(Cat.class));
    }

    @Test
    void crearGatoEnAdopcionSinColoniaEsPermitido() {
        CatRequest request = new CatRequest();
        request.setName("Luna");
        request.setStatus("EN_ADOPCION");
        request.setColonyId(null);

        when(catRepository.save(any(Cat.class))).thenAnswer(invocation -> {
            Cat saved = invocation.getArgument(0);
            saved.setId(13L);
            return saved;
        });

        CatResponse resultado = catService.create(request);

        assertEquals("EN_ADOPCION", resultado.status());
        assertNull(resultado.colonyId());
        verify(catRepository).save(any(Cat.class));
    }

    @Test
    void crearGatoConColoniaInexistenteLanzaBadRequest() {
        CatRequest request = new CatRequest();
        request.setName("Milo");
        request.setStatus("EN_COLONIA");
        request.setColonyId(99L);

        when(colonyRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> catService.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No colony found with ID 99"));
        verify(colonyRepository).findById(99L);
        verify(catRepository, never()).save(any(Cat.class));
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
