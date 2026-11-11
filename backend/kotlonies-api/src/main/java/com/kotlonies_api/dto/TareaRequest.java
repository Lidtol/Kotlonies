package com.kotlonies_api.dto;

import com.kotlonies_api.model.EstadoTarea;
import com.kotlonies_api.model.TipoTarea;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record TareaRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título no puede superar los 120 caracteres")
    String titulo,

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    String descripcion,

    @NotNull(message = "El tipo de tarea es obligatorio")
    TipoTarea tipo,

    @NotNull(message = "La fecha y hora es obligatoria")
    LocalDateTime fechaHora,

    EstadoTarea estado,

    @Positive(message = "El ID del gato debe ser positivo")
    Long catId,

    @Positive(message = "El ID de la colonia debe ser positivo")
    Long colonyId
) {}
