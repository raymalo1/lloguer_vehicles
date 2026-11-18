package cat.copernic.nothinganpri.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de páginas de error personalizadas.
 *
 * Muestra una vista de error genérica cuando se accede a la ruta
 * de error personalizado.
 */
@Controller
public class ErrorController {

    /**
     * Muestra la página de error personalizada.
     *
     * @return nombre de la vista "error"
     */
    @GetMapping("/error-personalitzat")
    public String mostrarError() {
        return "error";
    }
}
