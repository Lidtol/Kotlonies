package com.kotlonies_api.controller;

import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GatoControllerIntegrationTest extends ControllerIntegrationTestSupport {

    @Test
    void listarComoConsulta_retorna200() throws Exception {
        Usuario consulta = crearUsuario("consulta-gato@kotlonies.test", "CONSULTA", true);
        Colony colonia = crearColonia("Colonia Gato");
        crearGato(colonia, "Nala", "EN_COLONIA");

        mockMvc.perform(get("/api/cats")
                        .with(user(consulta.getEmail()).roles("CONSULTA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Nala"));
    }

    @Test
    void crearGatoComoCuidador_retorna201() throws Exception {
        Usuario cuidador = crearUsuario("cuidador-gato@kotlonies.test", "CUIDADOR", true);
        Colony colonia = crearColonia("Colonia Gatos");

        mockMvc.perform(post("/api/cats")
                        .with(user(cuidador.getEmail()).roles("CUIDADOR"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Milo\",\"sex\":\"MALE\",\"birthDate\":\"2021-05-20\",\"color\":\"negro\",\"status\":\"EN_COLONIA\",\"neutered\":true,\"identified\":true,\"colonyId\":" + colonia.getId() + ",\"observations\":\"Gato de prueba\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Milo"));
    }

    @Test
    void actualizarGatoExistente_retorna200() throws Exception {
        Usuario admin = crearUsuario("admin-gato-update@kotlonies.test", "ADMIN", true);
        Colony colonia = crearColonia("Colonia Update");
        Cat gato = crearGato(colonia, "Mochi", "EN_COLONIA");

        mockMvc.perform(put("/api/cats/{id}", gato.getId())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"Mochi Actualizado\",\"sex\":\"FEMALE\",\"birthDate\":\"2020-02-02\",\"color\":\"blanco\",\"status\":\"EN_SEGUIMIENTO\",\"neutered\":false,\"identified\":true,\"colonyId\":" + colonia.getId() + ",\"observations\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mochi Actualizado"))
                .andExpect(jsonPath("$.status").value("EN_SEGUIMIENTO"));
    }

    @Test
    void eliminarGatoComoAdmin_retorna204() throws Exception {
        Usuario admin = crearUsuario("admin-gato-delete@kotlonies.test", "ADMIN", true);
        Colony colonia = crearColonia("Colonia Delete");
        Cat gato = crearGato(colonia, "Pipa", "EN_COLONIA");

        mockMvc.perform(delete("/api/cats/{id}", gato.getId())
                        .with(user(admin.getEmail()).roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void gatoInexistente_retorna404() throws Exception {
        Usuario consulta = crearUsuario("consulta-gato-404@kotlonies.test", "CONSULTA", true);

        mockMvc.perform(get("/api/cats/{id}", 9999L)
                        .with(user(consulta.getEmail()).roles("CONSULTA")))
                .andExpect(status().isNotFound());
    }

    @Test
    void datoInvalido_retorna400() throws Exception {
        Usuario cuidador = crearUsuario("cuidador-gato-invalid@kotlonies.test", "CUIDADOR", true);

        mockMvc.perform(post("/api/cats")
                        .with(user(cuidador.getEmail()).roles("CUIDADOR"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"sex\":\"INVALID\",\"birthDate\":\"2021-xx-xx\",\"color\":\"gris\",\"status\":\"INVALID\",\"neutered\":true,\"identified\":true,\"colonyId\":1,\"observations\":\"desc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultaSinPermisoParaBorrar_retorna403() throws Exception {
        Usuario consulta = crearUsuario("consulta-gato-no-delete@kotlonies.test", "CONSULTA", true);
        Colony colonia = crearColonia("Colonia No Delete");
        Cat gato = crearGato(colonia, "Gus", "EN_COLONIA");

        mockMvc.perform(delete("/api/cats/{id}", gato.getId())
                        .with(user(consulta.getEmail()).roles("CONSULTA"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
