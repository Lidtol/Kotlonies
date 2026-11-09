
package com.kotlonies_api.config;

import com.kotlonies_api.model.Usuario;
import com.kotlonies_api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class ActiveUserFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public ActiveUserFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // Permitimos iniciar sesión o cerrar sesión normalmente.
        if (path.equals("/api/auth/login") ||
                path.equals("/api/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Solo comprobamos usuarios realmente autenticados.
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            String email = authentication.getName();

            Optional<Usuario> usuario =
                    usuarioRepository.findByEmailIgnoreCase(email);

            // Si el usuario ya no existe o está desactivado,
            // invalidamos su sesión.
            if (usuario.isEmpty() || !usuario.get().isActivo()) {

                SecurityContextHolder.clearContext();

                HttpSession session = request.getSession(false);

                if (session != null) {
                    session.invalidate();
                }

                response.sendError(
                        HttpStatus.UNAUTHORIZED.value(),
                        "La sesión ya no es válida. Inicia sesión nuevamente."
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
