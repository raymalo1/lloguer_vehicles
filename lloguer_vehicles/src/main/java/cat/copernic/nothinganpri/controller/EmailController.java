package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.service.EmailService;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el envío de correos electrónicos.
 *
 * Expone un endpoint que permite enviar mensajes de texto plano
 * a través del servicio {@link EmailService}.
 */
@RestController
@RequestMapping("/api/email")
public class EmailController {

    /** Servicio de envío de correos. */
    private final EmailService emailService;

    /**
     * Constructor del controlador de correo.
     *
     * @param emailService servicio de envío de correos
     */
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Envía un correo electrónico de texto plano.
     *
     * @param destinatario dirección de correo del destinatario
     * @param asunto asunto del mensaje
     * @param contenido cuerpo del mensaje
     * @return mensaje de confirmación "Correo enviado correctamente"
     */
    @PostMapping("/enviar")
    public String enviarCorreo(
            @RequestParam String destinatario,
            @RequestParam String asunto,
            @RequestParam String contenido) {

        emailService.enviarCorreo(
                destinatario,
                asunto,
                contenido
        );

        return "Correo enviado correctamente";
    }
}
