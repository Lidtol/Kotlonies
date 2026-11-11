package com.kotlonies_api.controller;

import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        usuarioRepository.deleteAll();
    }

    @Test
    void loginCorrecto_creaSesionAutenticada() throws Exception {
        Usuario usuario = guardarUsuario("admin-auth-ok@example.com", "ADMIN", true, "demoPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + usuario.getEmail() + "\",\"password\":\"demoPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(request().sessionAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        notNullValue()
                ));
    }

    @Test
    void loginConPasswordIncorrecta_devuelve401() throws Exception {
        Usuario usuario = guardarUsuario("admin-auth-bad-pass@example.com", "ADMIN", true, "demoPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + usuario.getEmail() + "\",\"password\":\"passwordIncorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Autenticación fallida"))
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void loginConUsuarioInexistente_devuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"noexiste@example.com\",\"password\":\"demoPassword123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void loginConUsuarioInactivo_devuelve401() throws Exception {
        Usuario usuario = guardarUsuario("inactive-auth@example.com", "CUIDADOR", false, "demoPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + usuario.getEmail() + "\",\"password\":\"demoPassword123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Esta cuenta está desactivada. Contacta con la administración."));
    }

    @Test
    void sesionAutenticada_devuelveDatosDelUsuarioActual() throws Exception {
        Usuario usuario = guardarUsuario("session-user@example.com", "CONSULTA", true, "demoPassword123");

        MockHttpSession session = login(usuario.getEmail(), "demoPassword123");

        mockMvc.perform(get("/api/auth/sesion").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.rol").value("CONSULTA"));
    }

    @Test
    void cerrarSesion_invalidaLaSesionActual() throws Exception {
        Usuario usuario = guardarUsuario("logout-user@example.com", "ADMIN", true, "demoPassword123");
        MockHttpSession session = login(usuario.getEmail(), "demoPassword123");

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isNoContent());

        assertTrue(session.isInvalid());
    }

    @Test
    void endpointProtegido_sinSesion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegido_conSesionAdmin_autorizado() throws Exception {
        Usuario usuario = guardarUsuario("admin-session@example.com", "ADMIN", true, "demoPassword123");
        MockHttpSession session = login(usuario.getEmail(), "demoPassword123");

        mockMvc.perform(get("/api/usuarios").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(usuario.getEmail()));
    }

    @Test
    void activeUserFilter_invalidaSesionCuandoUsuarioDejaDeEstarActivo() throws Exception {
        Usuario usuario = guardarUsuario("inactive-filter@example.com", "ADMIN", true, "demoPassword123");
        MockHttpSession session = login(usuario.getEmail(), "demoPassword123");

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        mockMvc.perform(get("/api/usuarios").session(session))
                .andExpect(status().isUnauthorized());

        assertTrue(session.isInvalid());
    }

    private Usuario guardarUsuario(String email, String rol, boolean activo, String password) {
        Usuario usuario = new Usuario(
                null,
                "Usuario de prueba",
                email,
                passwordEncoder.encode(password),
                rol,
                activo
        );
        return usuarioRepository.save(usuario);
    }

    private MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
