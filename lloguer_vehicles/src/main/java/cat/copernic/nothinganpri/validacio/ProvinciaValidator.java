
package cat.copernic.nothinganpri.validacio;

import java.text.Normalizer;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador encargado de comprobar si una provincia pertenece
 * al conjunto de provincias permitidas.
 *
 * Permite validar los nombres sin distinguir entre mayúsculas,
 * minúsculas ni acentos.
 */
public class ProvinciaValidator implements ConstraintValidator<ProvinciaValida, String> {

    /**
     * Conjunto de provincias en catalán con la primera letra en mayúscula.
     */
    private static final Set<String> PROVINCIES_VISIBLES = Stream.of(
            "Araba",
            "Albacete",
            "Alacant",
            "Almería",
            "Astúries",
            "Àvila",
            "Badajoz",
            "Barcelona",
            "Burgos",
            "Càceres",
            "Cadis",
            "Cantàbria",
            "Castelló",
            "Ciudad Real",
            "Còrdova",
            "A Corunya",
            "Conca",
            "Girona",
            "Granada",
            "Guadalajara",
            "Guipúscoa",
            "Huelva",
            "Osca",
            "Illes Balears",
            "Jaén",
            "Lleó",
            "Lleida",
            "Lugo",
            "Madrid",
            "Màlaga",
            "Múrcia",
            "Navarra",
            "Pontevedra",
            "La Rioja",
            "Salamanca",
            "Santa Cruz de Tenerife",
            "Segòvia",
            "Sevilla",
            "Sòria",
            "Tarragona",
            "Terol",
            "Toledo",
            "València",
            "Valladolid",
            "Biscaia",
            "Zamora",
            "Saragossa",
            "Ceuta",
            "Melilla",
            "Las Palmas"
    ).collect(Collectors.toSet());

    /**
     * Conjunto de provincias normalizadas para validar sin distinguir acentos,
     * mayúsculas ni minúsculas.
     */
    private static final Set<String> PROVINCIES_NORMALITZADES =
            PROVINCIES_VISIBLES.stream()
                    .map(ProvinciaValidator::normalitzar)
                    .collect(Collectors.toSet());

    /**
     * Normaliza un texto eliminando acentos, espacios sobrantes
     * y diferencias entre mayúsculas y minúsculas.
     *
     * @param s texto que se desea normalizar
     * @return texto normalizado
     */
    private static String normalitzar(String s) {
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Comprueba si el valor recibido corresponde a una provincia válida.
     *
     * Si el valor es nulo o está vacío, permite la validación.
     *
     * @param valor nombre de la provincia que se desea validar
     * @param ctx contexto de validación
     * @return true si la provincia es válida o el valor está vacío;
     *         false en caso contrario
     */
    @Override
    public boolean isValid(String valor, ConstraintValidatorContext ctx) {
        if (valor == null || valor.isBlank()) {
            return true;
        }

        return PROVINCIES_NORMALITZADES.contains(normalitzar(valor));
    }

    /**
     * Obtiene el conjunto de nombres de provincias en su formato visible.
     *
     * @return conjunto de provincias con su escritura original
     */
    public static Set<String> obtenirProvincies() {
        return PROVINCIES_VISIBLES;
    }

    /**
     * Comprueba si un nombre de provincia es válido.
     *
     * A diferencia de isValid, devuelve false cuando el valor es nulo
     * o está vacío.
     *
     * @param valor nombre de la provincia que se desea comprobar
     * @return true si corresponde a una provincia válida; false en caso contrario
     */
    public static boolean esProvinciaValida(String valor) {
    if (valor == null || valor.isBlank()) {
        return false;
    }

    return PROVINCIES_NORMALITZADES.contains(normalitzar(valor));
    }
}
