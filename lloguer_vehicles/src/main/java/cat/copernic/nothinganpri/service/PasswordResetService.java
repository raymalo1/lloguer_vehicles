package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.entity.PasswordResetToken;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.repository.PasswordResetTokenRepository;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar la recuperación y restablecimiento
 * de contraseñas de los usuarios.
 *
 * Genera tokens temporales, envía correos de recuperación y permite
 * cambiar la contraseña de forma segura mediante un enlace de un solo uso.
 */
@Service
public class PasswordResetService {

    /** Repositorio de usuarios. */
    private final UsuariJpaRepository usuariRepository;

    /** Repositorio de tokens de restablecimiento de contraseña. */
    private final PasswordResetTokenRepository tokenRepository;

    /** Servicio de envío de correos electrónicos. */
    private final EmailService emailService;

    /** Codificador de contraseñas. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor del servicio de restablecimiento de contraseña.
     *
     * @param usuariRepository repositorio de usuarios
     * @param tokenRepository repositorio de tokens de restablecimiento
     * @param emailService servicio de envío de correos
     * @param passwordEncoder codificador de contraseñas
     */
    public PasswordResetService(
            UsuariJpaRepository usuariRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {

        this.usuariRepository = usuariRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Solicita la recuperación de contraseña para el correo indicado.
     *
     * Si el usuario existe, genera un token temporal de 15 minutos,
     * lo guarda en base de datos y envía un correo con el enlace de
     * restablecimiento. Si el usuario no existe, no se realiza ninguna acción
     * (para no revelar si el correo está registrado).
     *
     * @param email correo electrónico del usuario que solicita la recuperación
     */
    @Transactional
    public void solicitarRecuperacion(String email) {

        var usuario = usuariRepository.findByEmail(email);

        if (usuario.isEmpty()) {
            return;
        }

        UsuariJpaEntity usuari = usuario.get();

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                usuari,
                LocalDateTime.now().plusMinutes(15)
        );

        tokenRepository.save(resetToken);

        String enlace = "https://localhost:8443/reset-password?token=" + token;

        String asunto = "Recuperación de contraseña";

        String contenido = "Hola " + usuari.getNom() + ",\n\n"
                + "Has solicitado recuperar tu contraseña.\n\n"
                + "Pulsa el siguiente enlace para continuar:\n"
                + enlace + "\n\n"
                + "Este enlace caduca en 15 minutos.\n\n"
                + "Si no has solicitado este cambio, ignora este correo.";

        emailService.enviarCorreo(email, asunto, contenido);
    }

    /**
     * Comprueba si el token de restablecimiento es válido.
     *
     * Un token es válido cuando existe, no ha sido utilizado previamente
     * y su fecha de caducidad aún no ha pasado.
     *
     * @param token token de restablecimiento a validar
     * @return {@code true} si el token es válido; {@code false} en caso contrario
     */
    public boolean esTokenValido(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        var resultado = tokenRepository.findByToken(token);

        if (resultado.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = resultado.get();

        return !resetToken.isUtilitzat()
                && resetToken.getDataCaducitat().isAfter(LocalDateTime.now());
    }

    /**
     * Cambia la contraseña del usuario asociado al token indicado.
     *
     * Valida el token, cifra la nueva contraseña, la guarda en el usuario
     * y marca el token como utilizado para que no pueda reutilizarse.
     *
     * @param token token de restablecimiento válido
     * @param nuevaPassword nueva contraseña en texto plano
     * @return {@code true} si el cambio se realizó correctamente;
     *         {@code false} si el token no es válido
     */
    @Transactional
    public boolean cambiarPassword(String token, String nuevaPassword) {

        if (!esTokenValido(token)) {
            return false;
        }

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token).orElseThrow();

        UsuariJpaEntity usuari = resetToken.getUsuari();

        usuari.setPassword(passwordEncoder.encode(nuevaPassword));

        resetToken.setUtilitzat(true);

        tokenRepository.save(resetToken);

        return true;
    }
}
