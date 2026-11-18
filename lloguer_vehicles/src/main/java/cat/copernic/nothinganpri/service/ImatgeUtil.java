package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.dto.FitxerPujat;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utilidad para leer y validar imágenes subidas por el usuario.
 *
 * Comprueba el tamaño máximo, el tipo real del fichero (mediante la
 * firma de bytes) y genera un objeto {@link FitxerPujat} listo para
 * ser persistido. Lanza {@link IllegalArgumentException} si la imagen
 * no es válida.
 *
 * RF40: solo se aceptan JPG y PNG.
 */
public final class ImatgeUtil {

    /** Tamaño máximo permitido para una imagen (5 MB). */
    public static final long MIDA_MAXIMA = 5L * 1024 * 1024; // 5 MB

    /**
     * Constructor privado para evitar la instanciación de la clase de utilidad.
     */
    private ImatgeUtil() {
    }

    /**
     * Lee y valida un fichero multiparte que debe contener una imagen.
     *
     * Verifica que el fichero no esté vacío, que no supere el tamaño máximo
     * y que el contenido sea realmente una imagen JPG o PNG
     * (detectado por la firma de bytes, no por el Content-Type del navegador).
     *
     * @param fitxer fichero multiparte subido por el usuario
     * @return objeto {@link FitxerPujat} con el nombre limpio, el tipo MIME
     *         detectado y los bytes de la imagen
     * @throws IllegalArgumentException si el fichero es nulo, vacío, demasiado
     *         grande o no es una imagen JPG/PNG
     */
    public static FitxerPujat llegir(MultipartFile fitxer) {
        if (fitxer == null || fitxer.isEmpty()) {
            throw new IllegalArgumentException("Cal adjuntar una imatge");
        }
        if (fitxer.getSize() > MIDA_MAXIMA) {
            throw new IllegalArgumentException("La imatge no pot superar els 5 MB");
        }

        byte[] dades;
        try {
            dades = fitxer.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("No s'ha pogut llegir la imatge", e);
        }

        // El tipus es detecta pels primers bytes: no ens fiem del Content-Type del navegador
        // RF40: només JPG i PNG
        String tipus = detectarTipus(dades);
        if (tipus == null) {
            throw new IllegalArgumentException("El fitxer ha de ser una imatge JPG o PNG");
        }
        return new FitxerPujat(netejarNom(fitxer.getOriginalFilename()), tipus, dades);
    }

    /**
     * Detecta el tipo MIME de una imagen a partir de su firma de bytes.
     * Solo acepta JPG y PNG (RF40).
     *
     * @param d array de bytes del fichero
     * @return tipo MIME ("image/jpeg" o "image/png") o {@code null} si no se reconoce
     */
    private static String detectarTipus(byte[] d) {
        // JPEG: FF D8 FF
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // PNG: 89 50 4E 47 ...
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "image/png";
        }
        // GIF y WEBP ya no se aceptan (RF40)
        return null;
    }

    /**
     * Limpia el nombre original del fichero, eliminando rutas y limitando
     * la longitud a 255 caracteres.
     *
     * @param original nombre original del fichero (puede contener rutas)
     * @return nombre limpio y seguro, o "imatge" si el original es nulo o vacío
     */
    private static String netejarNom(String original) {
        if (original == null || original.isBlank()) {
            return "imatge";
        }
        String nom = original.substring(Math.max(original.lastIndexOf('/'), original.lastIndexOf('\\')) + 1).trim();
        if (nom.isEmpty()) {
            return "imatge";
        }
        return nom.length() > 255 ? nom.substring(0, 255) : nom;
    }
}