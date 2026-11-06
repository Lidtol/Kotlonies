package com.kotlonies_api.dto;

import com.kotlonies_api.model.EstadoIncidencia;
import com.kotlonies_api.model.PrioridadIncidencia;
import com.kotlonies_api.model.TipoIncidencia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IncidenciaRequest(

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título no puede superar los 120 caracteres")
    String titulo,

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    String descripcion,

    @NotNull(message = "El tipo de incidencia es obligatorio")
    TipoIncidencia tipo,

    @NotNull(message = "La prioridad es obligatoria")
    PrioridadIncidencia prioridad,

    @NotNull(message = "El estado es obligatorio")
    EstadoIncidencia estado,

    @Positive(message = "El ID del gato debe ser positivo")
    Long catId,

    @Positive(message = "El ID de la colonia debe ser positivo")
    Long colonyId

) {
}