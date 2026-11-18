package cat.copernic.nothinganpri.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració de l'ObjectMapper de Jackson per a serialització JSON.
 * <p>
 * Proporciona un bean d'ObjectMapper configurat per defecte.
 * Es pot estendre per afegir mòduls personalitzats, configuració de dates, etc.
 * </p>
 *
 * @author Nothingan Pri Development Team
 * @since 1.0
 */
@Configuration
public class JacksonConfig {

    /**
     * Crea i retorna un ObjectMapper de Jackson.
     *
     * @return ObjectMapper nova instància
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
