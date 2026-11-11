package com.kotlonies_api.controller;

import com.kotlonies_api.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerIntegrationTest extends ControllerIntegrationTestSupport {

    @Test
    void listarComoAdmin_retorna200() throws Exception {
        Usuario admin = crearUsuario("admin-lista@kotlonies.test", "ADMIN", true);

        mockMvc.perform(get("/api/usuarios")
                        .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(admin.getEmail()));
    }

    @Test
    void crearUsuarioValido_retorna201() throws Exception {
        Usuario admin = crearUsuario("admin-create@kotlonies.test", "ADMIN", true);

        mockMvc.perform(post("/api/usuarios")
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"Ana\",\"email\":\"ana@kotlonies.test\",\"password\":\"Password123!\",\"rol\":\"CUIDADOR\",\"activo\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@kotlonies.test"))
                .andExpect(jsonPath("$.rol").value("CUIDADOR"));
    }

    @Test
    void crearUsuarioInvalido_retorna400() throws Exception {
        Usuario admin = crearUsuario("admin-invalid@kotlonies.test", "ADMIN", true);

        mockMvc.perform(post("/api/usuarios")
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"\",\"email\":\"email-malo\",\"password\":\"123\",\"rol\":\"X\",\"activo\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos no válidos"));
    }

    @Test
    void actualizarUsuarioExistente_retorna200() throws Exception {
        Usuario admin = crearUsuario("admin-update@kotlonies.test", "ADMIN", true);
        Usuario usuario = crearUsuario("target-update@kotlonies.test", "CONSULTA", true);

        mockMvc.perform(put("/api/usuarios/{id}", usuario.getId())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"nombre\":\"Actualizado\",\"email\":\"target-updated@kotlonies.test\",\"password\":\"Password123!\",\"rol\":\"CUIDADOR\",\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("target-updated@kotlonies.test"))
                .andExpect(jsonPath("$.rol").value("CUIDADOR"));
    }

    @Test
    void eliminarUsuarioExistente_retorna204() throws Exception {
        Usuario admin = crearUsuario("admin-delete@kotlonies.test", "ADMIN", true);
        Usuario usuario = crearUsuario("user-delete@kotlonies.test", "CONSULTA", true);

        mockMvc.perform(delete("/api/usuarios/{id}", usuario.getId())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void usuarioInexistente_retorna404() throws Exception {
        Usuario admin = crearUsuario("admin-404@kotlonies.test", "ADMIN", true);

        mockMvc.perform(get("/api/usuarios/{id}", 9999L)
                        .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinAutenticacion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinPermisos_devuelve403() throws Exception {
        Usuario consulta = crearUsuario("consulta-user@kotlonies.test", "CONSULTA", true);

        mockMvc.perform(get("/api/usuarios")
                        .with(user(consulta.getEmail()).roles("CONSULTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void postSinCsrf_rechaza() throws Exception {
        Usuario admin = crearUsuario("admin-csrf@kotlonies.test", "ADMIN", true);

        mockMvc.perform(post("/api/usuarios")
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .contentType("application/json")
                        .content("{\"nombre\":\"Ana\",\"email\":\"ana-csrf@kotlonies.test\",\"password\":\"Password123!\",\"rol\":\"CUIDADOR\",\"activo\":true}"))
                .andExpect(status().isForbidden());
    }
}
