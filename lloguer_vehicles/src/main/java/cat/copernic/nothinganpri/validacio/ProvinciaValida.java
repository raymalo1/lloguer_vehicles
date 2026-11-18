
package cat.copernic.nothinganpri.validacio;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotación personalizada para validar que una provincia sea válida.
 *
 * Se aplica a campos y delega la comprobación en ProvinciaValidator.
 */
@Documented
@Constraint(validatedBy = ProvinciaValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProvinciaValida {

    /**
     * Mensaje que se muestra cuando la provincia no supera la validación.
     *
     * @return mensaje de error predeterminado
     */
    String message() default "La província no és vàlida";

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
