package com.kotlonies_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ColonyRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String description,

        @NotBlank(message = "La ubicación es obligatoria")
        @Size(max = 200, message = "La ubicación no puede superar los 200 caracteres")
        String location,

        Boolean active

) {
}