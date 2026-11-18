package cat.copernic.nothinganpri.dto;

/**
 * Representa un documento de un cliente (DNI o permiso de conducir)
 * con sus datos binarios y el tipo MIME.
 */
public class DocumentClient {

    /** Contenido binario del documento. */
    private final byte[] dades;

    /** Tipo MIME del documento (por ejemplo image/jpeg). */
    private final String tipus;

    /**
     * Crea un documento de cliente.
     *
     * @param dades bytes del documento
     * @param tipus tipo MIME del documento
     */
    public DocumentClient(byte[] dades, String tipus) {
        this.dades = dades;
        this.tipus = tipus;
    }

    /**
     * Obtiene los bytes del documento.
     *
     * @return contenido binario
     */
    public byte[] getDades() {
        return dades;
    }

    /**
     * Obtiene el tipo MIME del documento.
     *
     * @return tipo MIME
     */
    public String getTipus() {
        return tipus;
    }
}
