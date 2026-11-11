package com.kotlonies_api.controller;

import com.kotlonies_api.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends ControllerIntegrationTestSupport {

    @Test
    void loginCorrecto_creaSesionAutenticada() throws Exception {
        Usuario usuario = crearUsuario("admin-login@example.com", "ADMIN", true);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + usuario.getEmail() + "\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(request().sessionAttribute("SPRING_SECURITY_CONTEXT", notNullValue()));
    }

    @Test
    void loginSinCsrf_rechazaPeticion() throws Exception {
        Usuario usuario = crearUsuario("csrf-login@example.com", "ADMIN", true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + usuario.getEmail() + "\",\"password\":\"Password123!\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sesionSinAutenticacion_retorna401() throws Exception {
        mockMvc.perform(get("/api/auth/sesion"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSesionConUsuarioAutenticado_devuelveDatos() throws Exception {
        Usuario usuario = crearUsuario("session-user@example.com", "CONSULTA", true);
        MockHttpSession session = login(usuario.getEmail(), "Password123!");

        mockMvc.perform(get("/api/auth/sesion").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.rol").value("CONSULTA"));
    }

    @Test
    void logoutConTokenCsrf_validaCerrarSesion() throws Exception {
        Usuario usuario = crearUsuario("logout@example.com", "ADMIN", true);
        MockHttpSession session = login(usuario.getEmail(), "Password123!");

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    void usuarioSinPermiso_obtiene403EnEndpointProtegido() throws Exception {
        crearUsuario("consulta@example.com", "CONSULTA", true);

        mockMvc.perform(get("/api/usuarios")
                        .with(user("consulta@example.com").roles("CONSULTA")))
                .andExpect(status().isForbidden());
    }
}
