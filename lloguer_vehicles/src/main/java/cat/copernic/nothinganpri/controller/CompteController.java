package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import cat.copernic.nothinganpri.service.SolicitudValidacionService;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.validacio.ProvinciaValidator;

import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class CompteController {

    private final UsuariJpaRepository usuariRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolicitudValidacionService solicitudService;

    public CompteController(
            UsuariJpaRepository usuariRepository,
            PasswordEncoder passwordEncoder,
            SolicitudValidacionService solicitudService) {

        this.usuariRepository = usuariRepository;
        this.passwordEncoder = passwordEncoder;
        this.solicitudService = solicitudService;
    }

    // =====================================================
    // PERFIL
    // =====================================================

    @GetMapping({"", "/compte"})
    public String perfil(Model model, Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);
        model.addAttribute("client", obtenerCliente(usuari));

        return "perfil";
    }

    // =====================================================
    // EDITAR DATOS PERSONALES
    // =====================================================

    @GetMapping("/editar-dades")
    public String editar(Model model, Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);
        model.addAttribute("client", obtenerCliente(usuari));
        model.addAttribute(
                "provincias",
                ProvinciaValidator.obtenirProvincies()
                        .stream()
                        .sorted()
                        .toList()
        );

        return "editar-dades";
    }

    @PostMapping("/editar-dades")
    public String guardar(
            Principal principal,
            @RequestParam String nom,
            @RequestParam String cognoms,
            @RequestParam(required = false) String telefon,
            @RequestParam(required = false) String nacionalitat,
            @RequestParam(required = false) String adreca,
            @RequestParam(required = false) String provincia,
            @RequestParam(required = false) String DNI,
            @RequestParam(required = false) String cadDNI,
            @RequestParam(required = false) String Lliconduir,
            @RequestParam(required = false) String cdLLiconduir,
            RedirectAttributes redirect) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null) {
            redirect.addFlashAttribute(
                    "error",
                    "Aquesta operació només està disponible per als clients.");
            return "redirect:/perfil";
        }

        try {

            solicitudService.solicitarDatosPersonales(
                    client,
                    nom,
                    cognoms,
                    telefon,
                    nacionalitat,
                    adreca,
                    provincia
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "El teu canvi s'ha enviat correctament. "
                    + "Un agent validarà les dades abans que s'apliquin."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Motiu clar: sense canvis, o ja hi ha una sol·licitud pendent, etc.
            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "No s'han pogut enviar els canvis."
            );
        } catch (Exception e) {
            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null && !e.getMessage().isBlank()
                    ? e.getMessage()
                    : "No s'han pogut enviar els canvis."
            );
        }

        return "redirect:/perfil";
    }

    // =====================================================
    // CAMBIAR CONTRASEÑA
    // =====================================================

    @GetMapping("/contrasenya")
    public String contrasenya(Model model, Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);

        return "contrasenya";
    }

    @PostMapping("/cambiar_contrasenya")
    public String cambiarContrasenya(
            Principal principal,
            @RequestParam String contrasenyaActual,
            @RequestParam String novaContrasenya,
            @RequestParam String confirmarContrasenya,
            RedirectAttributes redirect) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(
                contrasenyaActual, usuari.getPassword())) {

            redirect.addFlashAttribute(
                    "error",
                    "La contrasenya actual no és correcta.");

            return "redirect:/perfil/contrasenya";
        }

        if (novaContrasenya == null || novaContrasenya.length() < 8) {

            redirect.addFlashAttribute(
                    "error",
                    "La contrasenya ha de tenir almenys 8 caràcters.");

            return "redirect:/perfil/contrasenya";
        }

        if (!novaContrasenya.equals(confirmarContrasenya)) {

            redirect.addFlashAttribute(
                    "error",
                    "Les contrasenyes no coincideixen.");

            return "redirect:/perfil/contrasenya";
        }

        usuari.setPassword(passwordEncoder.encode(novaContrasenya));
        usuariRepository.save(usuari);

        redirect.addFlashAttribute(
                "missatge",
                "La contrasenya s'ha canviat correctament.");

        return "redirect:/perfil/contrasenya";
    }

    // =====================================================
    // RESERVAS
    // =====================================================

    @GetMapping("/reserves")
    public String mostrarReserves(
            Model model,
            Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);

        return "meves-reserves";
    }

    // =====================================================
    // DOCUMENTACIÓN - GET
    // =====================================================

    @GetMapping("/documentacio")
    public String mostrarFormulariDocumentacio(
            Model model,
            Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null) {
            return "redirect:/perfil";
        }

        model.addAttribute("usuari", usuari);
        model.addAttribute("client", client);

        return "edita-documentacio";
    }

    // =====================================================
    // DOCUMENTACIÓN - POST
    // =====================================================

    @PostMapping("/documentacio")
    public String guardarDocumentacio(
            Principal principal,
            @RequestParam("numeroDocument") String numeroDocument,
            @RequestParam("dataCaducitat") String dataCaducitat,
            @RequestParam("tipusLlicencia") String tipusLlicencia,
            @RequestParam("dataCaducitatLlicencia") String dataCaducitatLlicencia,
            @RequestParam(value = "documentIdentitat", required = false) MultipartFile documentIdentitat,
            @RequestParam(value = "llicenciaConduir", required = false) MultipartFile llicenciaConduir,
            RedirectAttributes redirect) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null) {
            return "redirect:/perfil";
        }

        try {

            boolean guardadoDirecto =
                    solicitudService.procesarDocumentacion(
                            client,
                            numeroDocument,
                            dataCaducitat,
                            tipusLlicencia,
                            dataCaducitatLlicencia,
                            documentIdentitat,
                            llicenciaConduir
                    );

            if (guardadoDirecto) {

                redirect.addFlashAttribute(
                        "mensaje",
                        "La documentació s'ha guardat correctament.");

            } else {

                redirect.addFlashAttribute(
                        "mensaje",
                        "La teva documentació s'ha enviat correctament. "
                        + "Un agent validarà els canvis abans que s'apliquin.");
            }

            // Tornar a la mateixa pàgina per veure les fotos
            return "redirect:/perfil/documentacio";

        } catch (Exception e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                    ? e.getMessage()
                    : "No s'ha pogut processar la documentació.");

            return "redirect:/perfil/documentacio";
        }
    }

    // =====================================================
    // MOSTRAR DOCUMENTO DNI
    // =====================================================

    @GetMapping("/documentacio/dni")
    public ResponseEntity<byte[]> mostrarDni(Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return ResponseEntity.status(401).build();
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null
                || client.getFotoDniAnverso() == null
                || client.getFotoDniAnversoTipus() == null) {

            return ResponseEntity.notFound().build();
        }

        try {

            MediaType tipus = MediaType.parseMediaType(
                    client.getFotoDniAnversoTipus());

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline")
                    .contentType(tipus)
                    .body(client.getFotoDniAnverso());

        } catch (IllegalArgumentException e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // MOSTRAR DOCUMENTO PERMISO
    // =====================================================

    @GetMapping("/documentacio/permis")
    public ResponseEntity<byte[]> mostrarPermis(Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return ResponseEntity.status(401).build();
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null
                || client.getFotoPermiso() == null
                || client.getFotoPermisoTipus() == null) {

            return ResponseEntity.notFound().build();
        }

        try {

            MediaType tipus = MediaType.parseMediaType(
                    client.getFotoPermisoTipus());

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline")
                    .contentType(tipus)
                    .body(client.getFotoPermiso());

        } catch (IllegalArgumentException e) {

            return ResponseEntity.internalServerError().build();
        }
    }

    // =====================================================
    // TARJETA - GET
    // =====================================================

    @GetMapping("/tarjeta")
    public String mostrarFormulariPremium(
            Model model,
            Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null) {
            return "redirect:/perfil";
        }

        model.addAttribute("usuari", usuari);
        model.addAttribute("client", client);

        return "tarjeta";
    }

    // =====================================================
    // TARJETA - POST
    // =====================================================

    @PostMapping("/tarjeta")
    public String guardarTargeta(
            @RequestParam("titular") String titular,
            @RequestParam("numtarjetacredit") String numeroTarjeta,
            @RequestParam("caducitatTargeta") String caducitat,
            Principal principal,
            RedirectAttributes redirect) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null) {
            return "redirect:/perfil";
        }

        try {

            boolean guardadoDirecto =
                    solicitudService.procesarTarjeta(
                            client,
                            titular,
                            numeroTarjeta,
                            caducitat
                    );

            if (guardadoDirecto) {

                redirect.addFlashAttribute(
                        "mensaje",
                        "La targeta simulada s'ha configurat correctament.");

            } else {

                redirect.addFlashAttribute(
                        "mensaje",
                        "La teva sol·licitud de canvi de targeta s'ha enviat. "
                        + "Un agent validarà els canvis abans que s'apliquin.");
            }

            return "redirect:/perfil";

        } catch (Exception e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                    ? e.getMessage()
                    : "No s'ha pogut processar la targeta.");

            return "redirect:/perfil/tarjeta";
        }
    }

    // =====================================================
    // SEGURIDAD
    // =====================================================

    @GetMapping("/seguretat")
    public String mostrarSeguretat(
            Model model,
            Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);

        return "seguretat";
    }

    // =====================================================
    // CONFIGURACIÓN - GET
    // =====================================================

    @GetMapping("/configuracio")
    public String mostrarConfiguracio(
            Model model,
            Principal principal) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuari", usuari);
        model.addAttribute("client", obtenerCliente(usuari));

        return "configuracio";
    }

    // =====================================================
    // CONFIGURACIÓN - POST
    // =====================================================

    @PostMapping("/configuracio")
    public String guardarConfiguracio(
            Principal principal,
            @RequestParam String nom,
            @RequestParam String cognoms,
            @RequestParam(required = false) String telefon,
            RedirectAttributes redirect) {

        UsuariJpaEntity usuari = obtenerUsuario(principal);

        if (usuari == null) {
            return "redirect:/login";
        }

        ClientJpaEntity client = obtenerCliente(usuari);

        if (client == null) {

            redirect.addFlashAttribute(
                    "error",
                    "Aquesta operació només està disponible per als clients.");

            return "redirect:/perfil/configuracio";
        }

        try {

            solicitudService.solicitarDatosPersonales(
                    client,
                    nom,
                    cognoms,
                    telefon,
                    null,
                    null,
                    null
            );

            redirect.addFlashAttribute(
                    "mensaje",
                    "La teva sol·licitud de canvi s'ha enviat correctament. "
                    + "Un agent validarà els canvis abans que s'apliquin."
            );

        } catch (Exception e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                    ? e.getMessage()
                    : "No s'han pogut enviar els canvis.");
        }

        return "redirect:/perfil/configuracio";
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private UsuariJpaEntity obtenerUsuario(Principal principal) {

        if (principal == null) {
            return null;
        }

        return usuariRepository
                .findByEmail(principal.getName())
                .orElse(null);
    }

    private ClientJpaEntity obtenerCliente(UsuariJpaEntity usuari) {

        if (usuari instanceof ClientJpaEntity) {
            return (ClientJpaEntity) usuari;
        }

        return null;
    }
}
