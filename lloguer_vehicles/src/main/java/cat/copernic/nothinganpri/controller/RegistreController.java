package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.dto.DocumentacioForm;
import cat.copernic.nothinganpri.dto.RegistreForm;
import cat.copernic.nothinganpri.service.UsuariService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes({"registre", "documentacio"})
public class RegistreController {

    private final UsuariService usuariService;

    /**
     * Constructor del controlador de registro.
     *
     * @param usuariService servicio de usuarios
     */
    public RegistreController(UsuariService usuariService) {
        this.usuariService = usuariService;
    }

    // CONFIGURACIÓ DEL FORMULARI DE DOCUMENTACIÓ
    @InitBinder("documentacio")
    /**
     * Init binder.
     *
     * @param binder binder
     */
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(
                "documentIdentitat",
                "llicenciaConduir"
        );
    }

    // CREAR FORMULARI DE REGISTRE
    @ModelAttribute("registre")
    /**
     * Registre form.
     * @return resultado
     */
    public RegistreForm registreForm() {
        return new RegistreForm();
    }

    // CREAR FORMULARI DE DOCUMENTACIÓ
    @ModelAttribute("documentacio")
    /**
     * Documentacio form.
     * @return resultado
     */
    public DocumentacioForm documentacioForm() {
        return new DocumentacioForm();
    }

    // MOSTRAR REGISTRE
    @GetMapping("/registre")
    /**
     * Mostrar registre.
     * @return resultado
     */
    public String mostrarRegistre() {
        return "registre";
    }

    // PROCESSAR REGISTRE
    @PostMapping("/registre")
    public String processarRegistre(
            @Valid @ModelAttribute("registre") RegistreForm registre,
            BindingResult result) {

        // COMPROVAR ERRORS DEL FORMULARI
        if (result.hasErrors()) {
            return "registre";
        }

        // COMPROVAR SI EL CORREU JA EXISTEIX
        if (usuariService.emailExisteix(registre.getEmail())) {

            result.rejectValue(
                    "email",
                    "email.duplicat",
                    "Aquest correu electrònic ja està registrat"
            );

            return "registre";
        }

        // ANAR A LA DOCUMENTACIÓ
        return "redirect:/documentacio";
    }

    // MOSTRAR DOCUMENTACIÓ
    @GetMapping("/documentacio")
    /**
     * Mostrar documentacio.
     * @return resultado
     */
    public String mostrarDocumentacio() {
        return "documentacio";
    }

    // PROCESSAR DOCUMENTACIÓ
    @PostMapping("/documentacio")
    public String processarDocumentacio(
            @Valid @ModelAttribute("documentacio") DocumentacioForm documentacio,
            BindingResult result) {

        if (result.hasErrors()) {
            return "documentacio";
        }

        // ANAR A LA REVISIÓ
        return "redirect:/revisio";
    }

    // MOSTRAR REVISIÓ DE LES DADES
    @GetMapping("/revisio")
    /**
     * Mostrar revisio.
     * @return resultado
     */
    public String mostrarRevisio() {
        return "revisio";
    }

    // CONFIRMAR I FINALITZAR REGISTRE
    @PostMapping("/revisio")
    public String confirmarRegistre(
            @RequestParam(
                    value = "confirmacioDades",
                    required = false
            )
            Boolean confirmacioDades,

            @ModelAttribute("registre") RegistreForm registre,

            @ModelAttribute("documentacio") DocumentacioForm documentacio,

            SessionStatus sessionStatus,
            Model model) {

        // COMPROVAR LA CONFIRMACIÓ
        if (!Boolean.TRUE.equals(confirmacioDades)) {

            model.addAttribute(
                    "error",
                    "Has de confirmar les dades."
            );

            return "revisio";
        }

        try {

            // REGISTRAR EL CLIENT
            usuariService.registrarClient(registre, documentacio);

            // NETEJAR LES DADES DE LA SESSIÓ
            sessionStatus.setComplete();

            // ANAR A LA PÀGINA DE CONFIRMACIÓ
            return "redirect:/confirmacio";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            return "revisio";
        }
    }

    // MOSTRAR CONFIRMACIÓ
    @GetMapping("/confirmacio")
    /**
     * Mostrar confirmacio.
     * @return resultado
     */
    public String mostrarConfirmacio() {
        return "confirmacio";
    }

    // MOSTRAR POLÍTICA DE PRIVACITAT
    @GetMapping("/privacitat")
    /**
     * Mostrar privacitat.
     * @return resultado
     */
    public String mostrarPrivacitat() {
        return "privacitat";
    }
}