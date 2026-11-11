package com.kotlonies_api.controller;

import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ColoniaControllerIntegrationTest extends ControllerIntegrationTestSupport {

    @Test
    void listarComoConsultor_retorna200() throws Exception {
        Usuario consulta = crearUsuario("consulta-colonia@kotlonies.test", "CONSULTA", true);
        crearColonia("Colonia Norte");

        mockMvc.perform(get("/api/colonies")
                        .with(user(consulta.getEmail()).roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Colonia Norte"));
    }

    @Test
    void crearColoniaComoCuidador_retorna201() throws Exception {
        Usuario cuidador = crearUsuario("cuidador-colonia@kotlonies.test", "CUIDADOR", true);

        mockMvc.perform(post("/api/colonies")
                        .with(user(cuidador.getEmail()).roles("CUIDADOR"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Colonia Sur\",\"description\":\"Colonia para pruebas\",\"location\":\"Sevilla\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Colonia Sur"));
    }

    @Test
    void actualizarColoniaExistente_retorna200() throws Exception {
        Usuario admin = crearUsuario("admin-colonia-update@kotlonies.test", "ADMIN", true);
        Colony colonia = crearColonia("Colonia Actualizar");

        mockMvc.perform(put("/api/colonies/{id}", colonia.getId())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Colonia Actualizada\",\"description\":\"Nueva desc\",\"location\":\"Málaga\",\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Colonia Actualizada"));
    }

    @Test
    void archivarColoniaComoAdmin_retorna200() throws Exception {
        Usuario admin = crearUsuario("admin-archive@kotlonies.test", "ADMIN", true);
        Colony colonia = crearColonia("Colonia Archivar");

        mockMvc.perform(patch("/api/colonies/{id}/archive", colonia.getId())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void crearColoniaInvalida_retorna400() throws Exception {
        Usuario cuidador = crearUsuario("cuidador-invalid@kotlonies.test", "CUIDADOR", true);

        mockMvc.perform(post("/api/colonies")
                        .with(user(cuidador.getEmail()).roles("CUIDADOR"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"description\":\"desc\",\"location\":\"\",\"active\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Datos no válidos"));
    }

    @Test
    void coloniaInexistente_retorna404() throws Exception {
        Usuario consulta = crearUsuario("consulta-colonia-404@kotlonies.test", "CONSULTA", true);

        mockMvc.perform(get("/api/colonies/{id}", 9999L)
                        .with(user(consulta.getEmail()).roles("CONSULTA")))
                .andExpect(status().isNotFound());
    }

    @Test
    void sinAutenticacion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/colonies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultaSinPermisosParaCrear_retorna403() throws Exception {
        Usuario consulta = crearUsuario("consulta-no-create@kotlonies.test", "CONSULTA", true);

        mockMvc.perform(post("/api/colonies")
                        .with(user(consulta.getEmail()).roles("CONSULTA"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Colonia Prohibida\",\"description\":\"desc\",\"location\":\"Sevilla\",\"active\":true}"))
                .andExpect(status().isForbidden());
    }
}
