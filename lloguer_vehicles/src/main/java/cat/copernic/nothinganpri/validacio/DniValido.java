
package cat.copernic.nothinganpri.validacio;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada para validar el formato y la letra de control
 * de un documento nacional de identidad (DNI).
 *
 * Se utiliza sobre campos y delega la validación en DniValidator.
 */
@Documented
@Constraint(validatedBy = DniValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DniValido {

    /**
     * Mensaje que se muestra cuando el DNI no supera la validación.
     *
     * @return mensaje de error predeterminado
     */
    String message() default "El DNI no és vàlid. Comprova els números i la lletra.";

    /**
     * Grupos de validación a los que pertenece esta restricción.
     *
     * @return grupos de validación
     */
    Class<?>[] groups() default {};

    /**
     * Información adicional asociada a la restricción.
     *
     * @return tipos de carga útil
     */
    Class<? extends Payload>[] payload() default {};
}
