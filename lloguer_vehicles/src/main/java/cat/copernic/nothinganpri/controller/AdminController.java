package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import cat.copernic.nothinganpri.service.LogQueryService;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controlador de la zona de administración.
 *
 * Gestiona la visualización de clientes y de los logs del sistema,
 * así como el borrado de los logs.
 */
@Controller
public class AdminController {

    /** Repositorio de usuarios. */
    private final UsuariJpaRepository usuariRepository;

    /** Servicio de consulta y gestión de logs. */
    private final LogQueryService logQueryService;

    /**
     * Constructor del controlador de administración.
     *
     * @param usuariRepository repositorio de usuarios
     * @param logQueryService servicio de logs
     */
    public AdminController(
            UsuariJpaRepository usuariRepository,
            LogQueryService logQueryService) {
        this.usuariRepository = usuariRepository;
        this.logQueryService = logQueryService;
    }

    /**
     * Muestra la lista de clientes registrados en el sistema.
     *
     * @param model modelo de Spring para pasar datos a la vista
     * @return nombre de la vista "admin/clients"
     */
    @GetMapping("/admin/clients")
    public String mostrarClients(Model model) {

        List<ClientJpaEntity> clients = usuariRepository.findByRol("CLIENT");

        model.addAttribute("clients", clients);

        return "admin/clients";
    }

    /**
     * Muestra los logs del sistema y sus estadísticas.
     *
     * @param model modelo de Spring para pasar datos a la vista
     * @return nombre de la vista "logs"
     * @throws IOException si se produce un error al leer los logs
     */
    @GetMapping("/admin/logs")
    public String mostrarLogs(Model model) throws IOException {
        List<String> logs = logQueryService.getLogs();

        model.addAttribute("logs", logs);
        model.addAttribute(
                "logStats",
                logQueryService.getStatistics(logs));

        return "logs";
    }

    /**
     * Borra todos los logs del sistema.
     *
     * Registra la acción en el propio sistema de logs y redirige
     * de nuevo a la página de logs.
     *
     * @return redirección a "/admin/logs"
     * @throws IOException si se produce un error al borrar los logs
     */
    @PostMapping("/admin/logs/clear")
    public String borrarLogs() throws IOException {

        try {
            logQueryService.clearLogs();

            logQueryService.registrarInfo(
                    "ADMIN",
                    "S'han esborrat els logs del sistema.");

            return "redirect:/admin/logs";

        } catch (IOException e) {
            logQueryService.registrarError(
                    "ADMIN",
                    "Error en esborrar els logs: " + e.getMessage());
            throw e;
        }
    }
}
