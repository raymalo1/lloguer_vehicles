package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.service.VehicleService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de consulta de disponibilidad de vehículos.
 *
 * RF45: Filtro de vehículos. Se realiza con GET porque solo se consultan
 * datos (no se modifican).
 */
@Controller
public class DisponibilitatController {

    /** Servicio de vehículos. */
    private final VehicleService service;

    /**
     * Constructor del controlador de disponibilidad.
     *
     * @param service servicio de vehículos
     */
    public DisponibilitatController(VehicleService service) {
        this.service = service;
    }

    /**
     * Filtra los vehículos según los criterios recibidos y muestra
     * el resultado en la vista de consulta de disponibilidad.
     *
     * Los filtros se devuelven a la vista para que el formulario no se vacíe.
     *
     * @param matricula filtro por matrícula (opcional)
     * @param marca filtro por marca (opcional)
     * @param modelVehicle filtro por modelo (opcional)
     * @param any filtro por año (opcional)
     * @param dataInici fecha de inicio del periodo (opcional)
     * @param dataFi fecha de fin del periodo (opcional)
     * @param model modelo de Spring para pasar datos a la vista
     * @return nombre de la vista "consulta-disponibilitat"
     */
    @GetMapping("/vehicles/disponibilitat")
    public String filtrar(
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String marca,
            @RequestParam(name = "model", required = false) String modelVehicle,
            @RequestParam(required = false) Integer any,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
            Model model) {

        // Tornem els filtres a la vista perquè el formulari no s'esborri en cercar
        model.addAttribute("matricula", matricula);
        model.addAttribute("marca", marca);
        model.addAttribute("modelVehicle", modelVehicle);
        model.addAttribute("any", any);
        model.addAttribute("dataInici", dataInici);
        model.addAttribute("dataFi", dataFi);

        try {
            model.addAttribute("vehicles",
                    service.filtrar(matricula, marca, modelVehicle, any, dataInici, dataFi));
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorNegoci", e.getMessage());
        }

        return "consulta-disponibilitat";
    }
}
