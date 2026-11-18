package cat.copernic.nothinganpri.dto;

import java.io.Serializable;

/**
 * Fichero ya leído y validado en memoria.
 * Se utiliza para conservar en sesión las imágenes del registro
 * hasta que el cliente confirma la solicitud
 * (un MultipartFile no sobrevive a la petición que lo ha recibido).
 */
public class FitxerPujat implements Serializable {

    /** Nombre original del fichero. */
    private final String nom;

    /** Tipo MIME del fichero. */
    private final String contentType;

    /** Contenido binario del fichero. */
    private final byte[] dades;

    /**
     * Crea un fichero subido validado.
     *
     * @param nom nombre del fichero
     * @param contentType tipo MIME
     * @param dades contenido binario
     */
    public FitxerPujat(String nom, String contentType, byte[] dades) {
        this.nom = nom;
        this.contentType = contentType;
        this.dades = dades;
    }

    /**
     * Obtiene el nombre del fichero.
     *
     * @return nombre del fichero
     */
    public String getNom() {
        return nom;
    }

    /**
     * Obtiene el tipo MIME del fichero.
     *
     * @return tipo MIME
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Obtiene el contenido binario del fichero.
     *
     * @return bytes del fichero
     */
    public byte[] getDades() {
        return dades;
    }
}
