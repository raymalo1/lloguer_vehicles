package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.TipusCombustible;
import cat.copernic.nothinganpri.service.VehicleService;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private static final String VISTA_FITXA = "P13-fitxa-vehicle";
    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @InitBinder
    void trimStrings(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute
    void desplegables(Model model) {
        model.addAttribute("categories", CategoriaVehicle.values());
        model.addAttribute("combustibles", TipusCombustible.values());
    }

    @GetMapping
    public String llistat(Model model) {
        model.addAttribute("vehicles", service.llistar());
        model.addAttribute("idsAmbReserves", service.idsVehiclesAmbReserves());
        return "llistat-vehicles";
    }

    @GetMapping("/nou")
    public String nou(Model model) {
        model.addAttribute("vehicle", new VehicleJpaEntity());
        return VISTA_FITXA;
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        VehicleJpaEntity vehicle = service.buscarPerId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle no trobat"));
        model.addAttribute("vehicle", vehicle);
        return VISTA_FITXA;
    }

    @PostMapping("/desa")
    public String desa(@ModelAttribute("vehicle") VehicleJpaEntity vehicle,
                       BindingResult errors,
                       @RequestParam(value = "fileFoto", required = false) MultipartFile fileFoto,
                       Model model,
                       RedirectAttributes flash) {

        if (!errors.hasErrors()) {
            try {
                service.guardar(vehicle, fileFoto);
                flash.addFlashAttribute("missatge", "Vehicle desat correctament");
                return "redirect:/vehicles";
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorNegoci", e.getMessage());
            }
        }
        return VISTA_FITXA;
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        return service.buscarPerId(id)
                .filter(v -> v.getFoto() != null)
                .map(v -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(v.getFotoContentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
                        .header("X-Content-Type-Options", "nosniff")
                        .body(v.getFoto()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/baixa")
    public String baixa(@PathVariable Long id, RedirectAttributes flash) {
        try {
            boolean eliminat = service.donarDeBaixa(id);
            flash.addFlashAttribute("missatge", eliminat
                    ? "Vehicle donat de baixa definitivament (eliminat, no tenia reserves)"
                    : "Vehicle desactivat (té historial de reserves, es conserva a la base de dades)");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("errorNegoci", e.getMessage());
        }
        return "redirect:/vehicles";
    }

    @GetMapping("/{id}")
    public String detall(@PathVariable Long id, Model model) {
        VehicleJpaEntity vehicle = service.buscarPerId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle no trobat"));
        model.addAttribute("vehicle", vehicle);
        return "detall-vehicle";
    }
    
    @GetMapping("/filtrar")
    public String mostrarFiltre(@RequestParam(required = false) String matricula,
                                @RequestParam(required = false) String marca,
                                @RequestParam(required = false) String model,
                                @RequestParam(required = false) Integer any,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi,
                                Model modelView) {

        boolean hayFiltros = (matricula != null && !matricula.isBlank()) ||
                             (marca != null && !marca.isBlank()) ||
                             (model != null && !model.isBlank()) ||
                             (any != null) ||
                             (dataInici != null) ||
                             (dataFi != null);

        if (!hayFiltros) {
            return "redirect:/vehicles";
        }

        try {
            List<VehicleJpaEntity> vehicles = service.filtrar(matricula, marca, model, any, dataInici, dataFi);
            modelView.addAttribute("vehicles", vehicles);
        } catch (IllegalArgumentException e) {
            modelView.addAttribute("errorNegoci", e.getMessage());
            modelView.addAttribute("vehicles", List.of());
        }

        modelView.addAttribute("idsAmbReserves", service.idsVehiclesAmbReserves());
        modelView.addAttribute("matricula", matricula);
        modelView.addAttribute("marca", marca);
        modelView.addAttribute("model", model);
        modelView.addAttribute("any", any);
        modelView.addAttribute("dataInici", dataInici);
        modelView.addAttribute("dataFi", dataFi);

        return "llistat-vehicles";
    }

    @PostMapping("/filtrar")
    public String procesarFiltre(@RequestParam(required = false) String matricula,
                                 @RequestParam(required = false) String marca,
                                 @RequestParam(required = false) String model,
                                 @RequestParam(required = false) Integer any,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInici,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFi) {

        boolean hayFiltros = (matricula != null && !matricula.isBlank()) ||
                             (marca != null && !marca.isBlank()) ||
                             (model != null && !model.isBlank()) ||
                             (any != null) ||
                             (dataInici != null) ||
                             (dataFi != null);

        if (!hayFiltros) {
            return "redirect:/vehicles";
        }

        String url = "/vehicles/filtrar?" +
                (matricula != null && !matricula.isBlank() ? "&matricula=" + matricula : "") +
                (marca != null && !marca.isBlank() ? "&marca=" + marca : "") +
                (model != null && !model.isBlank() ? "&model=" + model : "") +
                (any != null ? "&any=" + any : "") +
                (dataInici != null ? "&dataInici=" + dataInici : "") +
                (dataFi != null ? "&dataFi=" + dataFi : "");

        return "redirect:" + url;
    }

    @GetMapping("/{id}/activar")
    public String mostrarActivar(@PathVariable Long id, Model model) {
        VehicleJpaEntity vehicle = service.buscarPerId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle no trobat"));
        model.addAttribute("vehicle", vehicle);
        return "activar-vehicle";
    }

    @GetMapping("/{id}/desactivar")
    public String mostrarDesactivar(@PathVariable Long id, Model model) {
        VehicleJpaEntity vehicle = service.buscarPerId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle no trobat"));
        model.addAttribute("vehicle", vehicle);
        return "desactivar-vehicle";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id,
                             @RequestParam(required = false) String motiu,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFiDesactivacio,
                             RedirectAttributes flash) {
        try {
            service.desactivar(id, motiu, dataFiDesactivacio);
            flash.addFlashAttribute("missatge", "Vehicle desactivat temporalment. Pots activar-lo quan vulguis.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("errorNegoci", e.getMessage());
        }
        return "redirect:/vehicles";
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id,
                          @RequestParam(required = false) String motiu,
                          @RequestParam(required = false) BigDecimal importActivacio,
                          RedirectAttributes flash) {
        try {
            service.activar(id, motiu, importActivacio);
            flash.addFlashAttribute("missatge", "Vehicle activat correctament");
        } catch (IllegalArgumentException | IllegalStateException e) {
            flash.addFlashAttribute("errorNegoci", e.getMessage());
        }
        return "redirect:/vehicles";
    }
}