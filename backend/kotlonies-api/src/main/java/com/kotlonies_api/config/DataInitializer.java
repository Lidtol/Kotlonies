
package com.kotlonies_api.config;

import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class DataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public ApplicationRunner seedUsuarios(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            Environment environment
    ) {
        return args -> {

            // Si ya existen usuarios, no creamos otro administrador.
            if (repository.count() > 0) {
                return;
            }

            String email = environment.getProperty(
                    "APP_BOOTSTRAP_ADMIN_EMAIL"
            );

            String password = environment.getProperty(
                    "APP_BOOTSTRAP_ADMIN_PASSWORD"
            );

            // Sin credenciales configuradas no creamos una cuenta insegura.
            if (!StringUtils.hasText(email)
                    || !StringUtils.hasText(password)) {

                log.warn(
                        "No se ha creado el administrador inicial. "
                        + "Configura las variables de entorno."
                );

                return;
            }

            if (password.length() < 12) {
                throw new IllegalStateException(
                        "La contraseña inicial debe tener "
                        + "al menos 12 caracteres."
                );
            }

            Usuario administrador = new Usuario(
                    null,
                    "Administrador",
                    email.trim(),
                    passwordEncoder.encode(password),
                    "ADMIN",
                    true
            );

            repository.save(administrador);

            log.info(
                    "Cuenta de administrador inicial creada correctamente."
            );
        };
    }
}
