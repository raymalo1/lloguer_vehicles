package cat.copernic.nothinganpri.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado del envío de correos electrónicos.
 *
 * Soporta tanto mensajes de texto plano como mensajes con contenido HTML.
 */
@Service
public class EmailService {

    /** Componente de Spring encargado de enviar los correos. */
    private final JavaMailSender mailSender;

    /** Dirección de correo del remitente (configurada en application.properties). */
    @Value("${spring.mail.username}")
    private String remitente;

    /**
     * Constructor del servicio de correo.
     *
     * @param mailSender componente JavaMailSender inyectado por Spring
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un correo electrónico de texto plano.
     *
     * @param destinatario dirección de correo del destinatario
     * @param asunto asunto del mensaje
     * @param contenido cuerpo del mensaje en texto plano
     */
    public void enviarCorreo(
            String destinatario,
            String asunto,
            String contenido) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(contenido);

        mailSender.send(mensaje);
    }

    /**
     * Envía un correo electrónico con contenido HTML.
     *
     * El mensaje se envía con codificación UTF-8 y admite tanto una
     * versión de texto plano como la versión HTML.
     *
     * @param destinatario dirección de correo del destinatario
     * @param asunto asunto del mensaje
     * @param contenidoHtml cuerpo del mensaje en formato HTML
     * @throws RuntimeException si se produce un error al preparar el mensaje MIME
     */
    public void enviarCorreoHtml(
            String destinatario,
            String asunto,
            String contenidoHtml) {

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    "UTF-8"
            );

            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            helper.setText(
                    "Tu solicitud de cancelación ha sido aceptada. "
                    + "Consulta este correo en formato HTML para ver el detalle.",
                    contenidoHtml
            );

            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al preparar el correo HTML.", e);
        }
    }
}
