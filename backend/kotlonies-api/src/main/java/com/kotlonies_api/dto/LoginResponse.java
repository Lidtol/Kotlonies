package com.kotlonies_api.dto;

import com.kotlonies_api.model.Usuario;

public record LoginResponse(
        Long id,
        String nombre,
        String email,
        String rol
) {

    public static LoginResponse from(Usuario usuario) {
        return new LoginResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }
}
