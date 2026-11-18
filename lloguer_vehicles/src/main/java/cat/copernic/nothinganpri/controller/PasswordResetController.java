package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de la recuperación y restablecimiento de contraseñas.
 *
 * Gestiona el formulario de solicitud de recuperación, la validación
 * del token y el cambio de la contraseña.
 */
@Controller
public class PasswordResetController {

    /** Servicio de restablecimiento de contraseña. */
    private final PasswordResetService passwordResetService;

    /**
     * Constructor del controlador de restablecimiento de contraseña.
     *
     * @param passwordResetService servicio de restablecimiento
     */
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Muestra el formulario de solicitud de recuperación de contraseña.
     *
     * @return nombre de la vista "recuperar-contrasenya"
     */
    @GetMapping("/recuperar-contrasenya")
    public String mostrarRecuperacion() {
        return "recuperar-contrasenya";
    }

    /**
     * Procesa la solicitud de recuperación de contraseña.
     *
     * Envía un correo con el enlace de restablecimiento si el correo
     * está registrado. Siempre muestra un mensaje genérico para no
     * revelar si el correo existe o no.
     *
     * @param email correo electrónico del usuario
     * @param redirectAttributes atributos flash para mensajes
     * @return redirección a "/recuperar-contrasenya"
     */
    @PostMapping("/recuperar-contrasenya")
    public String recuperarPassword(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {

        passwordResetService.solicitarRecuperacion(email);

        redirectAttributes.addFlashAttribute(
                "notice",
                "Si el correo está registrado, recibirás un mensaje."
        );

        return "redirect:/recuperar-contrasenya";
    }

    /**
     * Muestra el formulario para introducir la nueva contraseña.
     *
     * Valida el token recibido. Si no es válido o ha caducado,
     * muestra un mensaje de error.
     *
     * @param token token de restablecimiento (puede ser nulo)
     * @param model modelo de Spring para pasar datos a la vista
     * @return nombre de la vista "reset-password"
     */
    @GetMapping("/reset-password")
    public String mostrarFormularioReset(
            @RequestParam(required = false) String token,
            Model model) {

        if (!passwordResetService.esTokenValido(token)) {
            model.addAttribute(
                    "error",
                    "El enlace no es válido o ha caducado."
            );
            return "reset-password";
        }

        model.addAttribute("token", token);

        return "reset-password";
    }

    /**
     * Procesa el formulario de nueva contraseña.
     *
     * Comprueba que las contraseñas coincidan y que la nueva tenga
     * al menos 8 caracteres. Si todo es correcto, cambia la contraseña
     * y redirige al login.
     *
     * @param token token de restablecimiento
     * @param password nueva contraseña
     * @param confirmarPassword confirmación de la nueva contraseña
     * @param redirectAttributes atributos flash para mensajes
     * @return redirección a "/login" o de vuelta al formulario en caso de error
     */
    @PostMapping("/reset-password")
    public String cambiarPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Las contraseñas no coinciden."
            );
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }

        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "La contraseña debe tener al menos 8 caracteres."
            );
            redirectAttributes.addAttribute("token", token);
            return "redirect:/reset-password";
        }

        boolean cambiado = passwordResetService.cambiarPassword(
                token,
                password
        );

        if (!cambiado) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "El enlace no es válido o ha caducado."
            );
            return "redirect:/reset-password";
        }

        redirectAttributes.addFlashAttribute(
                "notice",
                "Tu contraseña se ha cambiado correctamente. Ya puedes iniciar sesión."
        );

        return "redirect:/login";
    }
}
