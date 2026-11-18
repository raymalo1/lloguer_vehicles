package cat.copernic.nothinganpri.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador encargado de mostrar la página de inicio de sesión.
 */
@Controller
public class UsuariController {

    /**
     * Muestra la vista de login.
     *
     * @return nombre de la vista "login"
     */
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }
}
