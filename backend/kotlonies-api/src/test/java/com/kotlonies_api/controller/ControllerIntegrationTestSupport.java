package com.kotlonies_api.controller;

import com.kotlonies_api.model.Cat;
import com.kotlonies_api.model.Colony;
import com.kotlonies_api.model.Incidencia;
import com.kotlonies_api.model.Tarea;
import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.CatRepository;
import com.kotlonies_api.repository.ColonyRepository;
import com.kotlonies_api.repository.IncidenciaRepository;
import com.kotlonies_api.repository.TareaRepository;
import com.kotlonies_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ActiveProfiles("test")
public abstract class ControllerIntegrationTestSupport {

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected ColonyRepository colonyRepository;

    @Autowired
    protected CatRepository catRepository;

    @Autowired
    protected IncidenciaRepository incidenciaRepository;

    @Autowired
    protected TareaRepository tareaRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void configurarMockMvcYLimpiarBaseDeDatos() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        incidenciaRepository.deleteAll();
        tareaRepository.deleteAll();
        catRepository.deleteAll();
        colonyRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    protected Usuario crearUsuario(String email, String rol, boolean activo) {
        Usuario usuario = new Usuario(
                null,
                "Usuario de prueba",
                email,
                passwordEncoder.encode("Password123!"),
                rol,
                activo
        );
        return usuarioRepository.save(usuario);
    }

    protected MockHttpSession login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    protected Colony crearColonia(String name) {
        Colony colonia = new Colony(name, "Descripción de prueba", "Ubicación test", true);
        return colonyRepository.save(colonia);
    }

    protected Cat crearGato(Colony colonia, String nombre, String status) {
        Cat gato = new Cat();
        gato.setName(nombre);
        gato.setSex("MALE");
        gato.setColor("Gris");
        gato.setStatus(status);
        gato.setNeutered(true);
        gato.setIdentified(true);
        gato.setColony(colonia);
        return catRepository.save(gato);
    }

    protected Incidencia crearIncidencia(Colony colonia, Cat gato, String titulo) {
        Incidencia incidencia = new Incidencia();
        incidencia.setTitulo(titulo);
        incidencia.setDescripcion("Descripción de prueba");
        incidencia.setTipo(com.kotlonies_api.model.TipoIncidencia.SALUD);
        incidencia.setPrioridad(com.kotlonies_api.model.PrioridadIncidencia.MEDIA);
        incidencia.setEstado(com.kotlonies_api.model.EstadoIncidencia.ABIERTA);
        incidencia.setCat(gato);
        incidencia.setColony(colonia);
        return incidenciaRepository.save(incidencia);
    }

    protected Tarea crearTarea(Colony colonia, Cat gato, String titulo) {
        Tarea tarea = new Tarea();
        tarea.setTitulo(titulo);
        tarea.setDescripcion("Descripción de la tarea");
        tarea.setTipo(com.kotlonies_api.model.TipoTarea.REVISION);
        tarea.setEstado(com.kotlonies_api.model.EstadoTarea.PENDIENTE);
        tarea.setFechaHora(java.time.LocalDateTime.now().plusDays(1));
        tarea.setCat(gato);
        tarea.setColony(colonia);
        return tareaRepository.save(tarea);
    }
}
