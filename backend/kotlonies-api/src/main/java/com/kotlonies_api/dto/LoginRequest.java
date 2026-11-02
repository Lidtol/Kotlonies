package com.kotlonies_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Size(max = 200, message = "El correo electrónico no puede superar los 200 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 100, message = "La contraseña no puede superar los 100 caracteres")
        String password

) {
}
