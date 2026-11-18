
package cat.copernic.nothinganpri.validacio;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador encargado de comprobar si un DNI tiene un formato válido
 * y si su letra de control corresponde al número indicado.
 */
public class DniValidator implements ConstraintValidator<DniValido, String> {

    private static final String LETRAS = "TRWAGMYFPDXBNJZSQVHLCKE";

    /**
     * Comprueba si el DNI recibido es válido.
     *
     * Si el valor es nulo o está vacío, permite la validación.
     * Elimina los espacios y convierte el DNI a mayúsculas antes
     * de comprobar su formato y calcular la letra de control.
     *
     * @param dni DNI que se desea validar
     * @param context contexto de validación de Jakarta
     * @return true si el DNI es válido o está vacío; false en caso contrario
     */
    @Override
    public boolean isValid(String dni, ConstraintValidatorContext context) {

        if (dni == null || dni.isBlank()) {
            return true;
        }

        dni = dni.trim().toUpperCase();

        if (!dni.matches("^[0-9]{8}[A-Z]$")) {
            return false;
        }

        try {
            int numero = Integer.parseInt(dni.substring(0, 8));

            char letraCalculada = LETRAS.charAt(numero % 23);

            return dni.charAt(8) == letraCalculada;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}
