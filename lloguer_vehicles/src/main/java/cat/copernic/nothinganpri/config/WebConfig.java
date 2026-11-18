package cat.copernic.nothinganpri.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuració de recursos estàtics per a l'aplicació.
 * <p>
 * Configura el servei de fitxers pujats (uploads) sota la ruta /uploads/**.
 * El directori es configura mitjançant la propietat {@code app.upload-dir}
 * (per defecte: directori "uploads" al directori de treball).
 * </p>
 *
 * @author Nothingan Pri Development Team
 * @since 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * Registra el gestor de recursos per als fitxers pujats.
     *
     * @param registry registre de gestors de recursos de Spring MVC
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("No es pot crear " + dir, e);
        }
        System.out.println(">>> Servint /uploads/** des de: " + dir);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(dir.toUri().toString());
    }
}
