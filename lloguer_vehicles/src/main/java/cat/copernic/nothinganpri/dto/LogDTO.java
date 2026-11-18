package cat.copernic.nothinganpri.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa una entrada de log del sistema.
 * Contiene la fecha, el nivel, la fuente y el mensaje.
 */
public class LogDTO {

    /** Fecha y hora del log. */
    private LocalDateTime date;

    /** Nivel del log (INFO, WARNING, ERROR...). */
    private String level;

    /** Origen o componente que generó el log. */
    private String source;

    /** Mensaje del log. */
    private String message;

    /**
     * Obtiene la fecha y hora del log.
     *
     * @return fecha y hora
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * Establece la fecha y hora del log.
     *
     * @param date fecha y hora
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * Obtiene el nivel del log.
     *
     * @return nivel (INFO, WARNING, ERROR...)
     */
    public String getLevel() {
        return level;
    }

    /**
     * Establece el nivel del log.
     *
     * @param level nivel del log
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Obtiene la fuente del log.
     *
     * @return origen del log
     */
    public String getSource() {
        return source;
    }

    /**
     * Establece la fuente del log.
     *
     * @param source origen del log
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Obtiene el mensaje del log.
     *
     * @return mensaje
     */
    public String getMessage() {
        return message;
    }

    /**
     * Establece el mensaje del log.
     *
     * @param message mensaje
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
