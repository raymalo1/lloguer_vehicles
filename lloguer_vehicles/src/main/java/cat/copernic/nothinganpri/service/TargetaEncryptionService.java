
package cat.copernic.nothinganpri.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de generar y verificar hashes de números
 * de tarjetas bancarias mediante HMAC-SHA-256.
 *
 * Los hashes son irreversibles y utilizan una clave secreta
 * para proteger los números de tarjeta.
 *
 * @author Nothinganpri Development Team
 * @since 1.0
 */
@Service
public class TargetaEncryptionService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final int KEY_SIZE = 32;

    private final SecretKeySpec secretKey;

    /**
     * Inicializa el servicio con una clave secreta de al menos 32 bytes.
     *
     * @param encodedKey clave codificada en Base64
     */
    public TargetaEncryptionService(
            @Value("${app.security.card-encryption-key}") String encodedKey) {

        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(encodedKey.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "La clave de cifrado no tiene un formato Base64 válido.", e);
        }

        if (keyBytes.length < KEY_SIZE) {
            throw new IllegalArgumentException(
                    "La clave debe tener al menos 32 bytes.");
        }

        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Genera un hash HMAC-SHA-256 del número de tarjeta.
     *
     * @param numero número de tarjeta original
     * @return hash codificado en Base64
     * @throws IllegalStateException si ocurre un error al generar el hash
     */
    public String encriptar(String numero) {

        if (numero == null) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    numero.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No se ha podido generar el hash de la tarjeta.", e);
        }
    }

    /**
     * Comprueba si un número de tarjeta coincide con un hash almacenado.
     *
     * @param numero número original introducido
     * @param hashAlmacenado hash guardado en la base de datos
     * @return true si coinciden; false en caso contrario
     */
    public boolean verificar(String numero, String hashAlmacenado) {

        if (numero == null || hashAlmacenado == null) {
            return false;
        }

        String hashCalculado = encriptar(numero);

        return MessageDigest.isEqual(
                hashCalculado.getBytes(StandardCharsets.UTF_8),
                hashAlmacenado.getBytes(StandardCharsets.UTF_8));
    }
}
