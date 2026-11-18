package cat.copernic.nothinganpri.enums;

/**
 * Estados posibles de una reserva.
 */
public enum EstatReserva {
    /** Reserva pendiente de confirmación o inicio. */
    PENDENT("Pendent"),
    /** Reserva activa (confirmada). */
    ACTIVA("Activa"),
    /** Reserva en curso (vehículo entregado). */
    EN_CURS("En curs"),
    /** Reserva finalizada correctamente. */
    FINALITZADA("Finalitzada"),
    /** Reserva anulada. */
    ANUL_LADA("Anul·lada");

    /** Nombre formateado para mostrar en la interfaz. */
    private final String nomFormatejat;

    /**
     * Constructor del enumerado.
     *
     * @param nomFormatejat nombre legible del estado
     */
    EstatReserva(String nomFormatejat) {
        this.nomFormatejat = nomFormatejat;
    }

    /**
     * Obtiene el nombre formateado del estado.
     *
     * @return nombre legible del estado
     */
    public String getNomFormatejat() {
        return nomFormatejat;
    }
}
