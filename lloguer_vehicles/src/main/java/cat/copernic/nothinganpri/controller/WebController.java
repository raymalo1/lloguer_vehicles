package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.TipusCombustible;
import java.math.BigDecimal;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import cat.copernic.nothinganpri.service.VehicleService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador principal de las páginas públicas de la aplicación.
 *
 * Gestiona la página de inicio (index) y el catálogo de vehículos
 * con filtros de búsqueda.
 */
@Controller
public class WebController {

    /** Servicio de gestión de vehículos. */
    @Autowired
    private VehicleService vehicleService;

    /** Repositorio de usuarios. */
    @Autowired
    private UsuariJpaRepository usuariRepository;

    /**
     * Muestra la página de inicio con la lista de vehículos disponibles.
     *
     * Si el usuario está autenticado, también añade su nombre y correo
     * al modelo para mostrar el menú personalizado.
     *
     * @param model modelo de Spring para pasar datos a la vista
     * @param authentication información de autenticación del usuario actual
     * @return nombre de la vista "index"
     */
    @GetMapping({"/", "/index"})
    public String mostrarIndex(Model model, Authentication authentication) {
        List<VehicleJpaEntity> llistaVehicles = vehicleService.llistar();
        model.addAttribute("vehicles", llistaVehicles);

        // Si hay sesión, pasar datos del usuario (para el avatar y el menú)
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            String email = authentication.getName();

            usuariRepository.findByEmail(email).ifPresent(usuari -> {
                String nomComplet = usuari.getNom();
                if (usuari.getCognoms() != null && !usuari.getCognoms().isBlank()) {
                    nomComplet += " " + usuari.getCognoms();
                }
                model.addAttribute("nomUsuari", nomComplet);
                model.addAttribute("emailUsuari", usuari.getEmail());
            });
        }

        return "index";
    }

    /**
     * Muestra el catálogo de vehículos aplicando los filtros recibidos.
     *
     * Los filtros permiten buscar por matrícula, marca, modelo, año,
     * rango de fechas de disponibilidad, categorías, tipos de combustible
     * y rango de precios. Los valores de los filtros se devuelven a la vista
     * para que el formulario no se vacíe.
     *
     * @param matricula filtro por matrícula (opcional)
     * @param marca filtro por marca (opcional)
     * @param modelVehicle filtro por modelo (opcional)
     * @param any filtro por año de fabricación (opcional)
     * @param dataInici fecha de inicio del periodo de disponibilidad (opcional)
     * @param dataFi fecha de fin del periodo de disponibilidad (opcional)
     * @param categoriesSel lista de categorías seleccionadas (opcional)
     * @param combustiblesSel lista de tipos de combustible seleccionados (opcional)
     * @param preuMin precio mínimo por hora (opcional)
     * @param preuMax precio máximo por hora (opcional)
     * @param model modelo de Spring para pasar datos a la vista
     * @param authentication información de autenticación del usuario actual
     * @return nombre de la vista "cataleg"
     */
    @GetMapping("/cataleg")
    public String mostrarCataleg(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String marca,
            @RequestParam(name = "model", required = false) String modelVehicle,
            @RequestParam(required = false) Integer any,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
            @RequestParam(name = "categoria", required = false) List<CategoriaVehicle> categoriesSel,
            @RequestParam(name = "combustible", required = false) List<TipusCombustible> combustiblesSel,
            @RequestParam(required = false) BigDecimal preuMin,
            @RequestParam(required = false) BigDecimal preuMax,
            Model model,
            Authentication authentication) {

        // Devolver los filtros a la vista (para que el formulario no se vacíe)
        model.addAttribute("matricula", matricula);
        model.addAttribute("marca", marca);
        model.addAttribute("modelVehicle", modelVehicle);
        model.addAttribute("any", any);
        model.addAttribute("dataInici", dataInici);
        model.addAttribute("dataFi", dataFi);
        model.addAttribute("categoriesSel", categoriesSel);
        model.addAttribute("combustiblesSel", combustiblesSel);
        model.addAttribute("preuMin", preuMin);
        model.addAttribute("preuMax", preuMax);
        // Opcions dels checkboxes del panell lateral
        model.addAttribute("categories", CategoriaVehicle.values());
        model.addAttribute("combustibles", TipusCombustible.values());

        // Filtrar vehículos
        try {
            model.addAttribute("vehicles",
                    vehicleService.filtrar(matricula, marca, modelVehicle, any, dataInici, dataFi,
                            categoriesSel, combustiblesSel, preuMin, preuMax));
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegoci", e.getMessage());
            model.addAttribute("vehicles", vehicleService.llistar());
        }

        // Datos del usuario logueado
        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            String email = authentication.getName();

            usuariRepository.findByEmail(email).ifPresent(usuari -> {
                String nomComplet = usuari.getNom();

                if (usuari.getCognoms() != null
                        && !usuari.getCognoms().isBlank()) {
                    nomComplet += " " + usuari.getCognoms();
                }

                model.addAttribute("nomUsuari", nomComplet);
                model.addAttribute("emailUsuari", usuari.getEmail());
            });
        }

        return "cataleg";
    }
}
