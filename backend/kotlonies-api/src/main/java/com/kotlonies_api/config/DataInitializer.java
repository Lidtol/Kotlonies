package com.kotlonies_api.config;

import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Datos iniciales de demostración.
 *
 * Crea una cuenta de administración (lidia@example.com) la primera vez que
 * arranca la aplicación, si todavía no existe ningún usuario.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public ApplicationRunner seedUsuarios(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            repository.save(new Usuario(
                    null,
                    "Lidia",
                    "lidia@example.com",
                    passwordEncoder.encode("kotlonies2026"),
                    "ADMIN",
                    true
            ));

            log.info("Usuario de demostración creado: lidia@example.com");
        };
    }
}
