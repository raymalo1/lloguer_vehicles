package cat.copernic.nothinganpri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal de l'aplicació Nothingan Pri - Lloguer de Vehicles.
 * <p>
 * Punt d'entrada de l'aplicació Spring Boot 4.1.1 amb Java 21.
 * Configura automàticament: JPA/Hibernate (MySQL), MongoDB, Spring Security,
 * Thymeleaf, validació, correu electrònic i servidors HTTPS (port 8443).
 * </p>
 *
 * @author Nothingan Pri Development Team
 * @version 0.0.1-SNAPSHOT
 * @since 1.0
 */
@SpringBootApplication
public class Main {

    /**
     * Mètode principal que inicia l'aplicació Spring Boot.
     *
     * @param args arguments de línia de comandes (no s'utilitzen)
     */
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}
