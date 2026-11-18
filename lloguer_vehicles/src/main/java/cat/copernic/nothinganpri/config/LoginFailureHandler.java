package cat.copernic.nothinganpri.config;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.repository.SolicitudValidacionRepository;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Gestor personalitzat d'errors d'autenticació.
 *
 * Quan el compte està desactivat ({@code DisabledException}), determina el motiu
 * i el passa com a paràmetre a la pàgina de login per mostrar un missatge específic:
 * <ul>
 *   <li>{@code pendent} - El registre espera validació d'un agent</li>
 *   <li>{@code rebutjat} - L'agent ha rebutjat el registre</li>
 *   <li>{@code disabled} - Compte desactivat per altre motiu</li>
 * </ul>
 * Només es revela el motiu si la contrasenya és correcta (evita enumeració d'usuaris).
 *
 * @author Nothingan Pri Development Team
 * @since 1.0
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UsuariJpaRepository usuariRepository;
    private final SolicitudValidacionRepository solicitudRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor amb dependències.
     *
     * @param usuariRepository repositori d'usuaris
     * @param solicitudRepository repositori de sol·licituds de validació
     * @param passwordEncoder codificador de contrasenyes
     */
    public LoginFailureHandler(
            UsuariJpaRepository usuariRepository,
            SolicitudValidacionRepository solicitudRepository,
            PasswordEncoder passwordEncoder) {

        this.usuariRepository = usuariRepository;
        this.solicitudRepository = solicitudRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Gestiona l'error d'autenticació.
     *
     * @param request petició HTTP
     * @param response resposta HTTP
     * @param exception excepció d'autenticació
     * @throws IOException si hi ha error en la redirecció
     */
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        String destino = "/login?error";

        if (exception instanceof DisabledException) {
            destino = "/login?error=" + motivo(
                    request.getParameter("email"),
                    request.getParameter("password"));
        }

        response.sendRedirect(request.getContextPath() + destino);
    }

    /**
     * Determina el motiu de la desactivació del compte.
     *
     * @param email email de l'usuari
     * @param password contrasenya proporcionada
     * @return motiu: "pendent", "rebutjat" o "disabled"
     */
    private String motivo(String email, String password) {

        if (email == null || password == null) {
            return "disabled";
        }

        Optional<UsuariJpaEntity> trobat =
                usuariRepository.findByEmail(email.trim());

        if (trobat.isEmpty()
                || !passwordEncoder.matches(password,
                        trobat.get().getPassword())) {
            return "disabled";
        }

        if (trobat.get() instanceof ClientJpaEntity) {

            ClientJpaEntity client = (ClientJpaEntity) trobat.get();

            if (solicitudRepository.existsByClienteAndTipoAndEstado(
                    client, "REGISTRO", "PENDIENTE")) {
                return "pendent";
            }

            if (solicitudRepository.existsByClienteAndTipoAndEstado(
                    client, "REGISTRO", "RECHAZADA")) {
                return "rebutjat";
            }
        }

        return "disabled";
    }
}
