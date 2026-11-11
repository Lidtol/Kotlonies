package com.kotlonies_api.repository;

import com.kotlonies_api.model.Usuario;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void saveAndFindById_shouldPersistUsuario() {
        Usuario usuario = new Usuario(null, "Ana", "ana@kotlonies.test", "hash", "ADMIN", true);

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findById(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("ana@kotlonies.test");
        assertThat(encontrado.get().getRol()).isEqualTo("ADMIN");
    }

    @Test
    void findByEmailIgnoreCase_shouldBeCaseInsensitive() {
        Usuario usuario = new Usuario(null, "Pedro", "pedro@kotlonies.test", "hash", "CUIDADOR", true);
        usuarioRepository.saveAndFlush(usuario);

        Optional<Usuario> encontrado = usuarioRepository.findByEmailIgnoreCase("PEDRO@KOTLONIES.TEST");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Pedro");
    }

    @Test
    void existsByEmailIgnoreCase_shouldReturnTrueForEquivalentEmails() {
        usuarioRepository.saveAndFlush(new Usuario(null, "Lola", "lola@kotlonies.test", "hash", "CONSULTA", true));

        boolean existe = usuarioRepository.existsByEmailIgnoreCase("LOLA@KOTLONIES.TEST");

        assertThat(existe).isTrue();
    }

    @Test
    void save_duplicateEmail_shouldFailBecauseOfUniqueConstraint() {
        usuarioRepository.saveAndFlush(new Usuario(null, "Marta", "marta@kotlonies.test", "hash", "ADMIN", true));

        Usuario duplicado = new Usuario(null, "Marta 2", "marta@kotlonies.test", "hash2", "CONSULTA", true);

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(duplicado))
                .isInstanceOfAny(DataIntegrityViolationException.class, PersistenceException.class);
    }

    @Test
    void delete_shouldRemoveUsuario() {
        Usuario usuario = usuarioRepository.saveAndFlush(new Usuario(null, "Berta", "berta@kotlonies.test", "hash", "CONSULTA", true));

        usuarioRepository.delete(usuario);
        usuarioRepository.flush();

        assertThat(usuarioRepository.findById(usuario.getId())).isEmpty();
    }
}
