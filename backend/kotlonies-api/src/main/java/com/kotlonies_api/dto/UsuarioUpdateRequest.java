package com.kotlonies_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 200, message = "El correo electrónico no puede superar los 200 caracteres")
        String email,

        // Opcional en las ediciones: si viene vacía, se mantiene la contraseña actual.
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(
                regexp = "ADMIN|CUIDADOR|CONSULTA",
                message = "El rol debe ser ADMIN, CUIDADOR o CONSULTA"
        )
        String rol,

        Boolean activo

) {
}
