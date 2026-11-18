package cat.copernic.nothinganpri.dto;

/**
 * Una línea de cambio de una solicitud: valor actual del cliente
 * y valor que propone, para que el agente pueda validarlo o rechazarlo.
 */
public class CanviSolicitud {

    /** Nombre del campo que se quiere modificar. */
    private final String camp;

    /** Valor actual que tiene el cliente. */
    private final String actual;

    /** Valor propuesto por el cliente. */
    private final String proposat;

    /**
     * Crea un cambio de solicitud.
     *
     * @param camp nombre del campo
     * @param actual valor actual
     * @param proposat valor propuesto
     */
    public CanviSolicitud(String camp, String actual, String proposat) {
        this.camp = camp;
        this.actual = actual;
        this.proposat = proposat;
    }

    /**
     * Obtiene el nombre del campo.
     *
     * @return nombre del campo modificado
     */
    public String getCamp() {
        return camp;
    }

    /**
     * Obtiene el valor actual del campo.
     *
     * @return valor actual
     */
    public String getActual() {
        return actual;
    }

    /**
     * Obtiene el valor propuesto para el campo.
     *
     * @return valor propuesto
     */
    public String getProposat() {
        return proposat;
    }
}
