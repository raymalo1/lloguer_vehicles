
package cat.copernic.nothinganpri.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.nothinganpri.dto.AgentEditarForm;
import cat.copernic.nothinganpri.dto.DocumentacioForm;
import cat.copernic.nothinganpri.dto.TargetaForm;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.service.ClientService;
import cat.copernic.nothinganpri.service.ReservaService;
import cat.copernic.nothinganpri.validacio.ProvinciaValidator;

import jakarta.validation.Valid;

/**
 * Controlador encargado de gestionar las operaciones de administración
 * relacionadas con los clientes.
 * 
 * Permite consultar y modificar los datos personales, la documentación,
 * la licencia de conducir, los datos de la tarjeta y la contraseña
 * de los clientes.
 *
 * El acceso está restringido a los usuarios con roles ADMIN,
 * ADMINISTRADOR o AGENT.
 */
@Controller
@RequestMapping("/agent/clients")
@PreAuthorize("hasAnyRole('ADMIN', 'ADMINISTRADOR', 'AGENT')")
public class AdminClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ReservaService reservaService;

    /**
     * Configura los campos que no pueden modificarse desde el formulario
     * de edición de los datos personales del cliente.
     *
     * @param binder objeto encargado de controlar la vinculación de datos
     *               del formulario con la entidad del cliente
     */
    @InitBinder("client")
    public void configurarBinder(WebDataBinder binder) {

        binder.setDisallowedFields(
                "password",
                "rol",
                "activo",
                "DNI",
                "dni",
                "fotoDniAnverso",
                "fotoDniAnversoTipus",
                "fotoPermiso",
                "fotoPermisoTipus",
                "fotoDniReverso",
                "titularTargeta",
                "numtarjetacredit",
                "caducitatTargeta"
        );
    }

    /**
     * Prepara los datos necesarios para mostrar la vista de edición
     * del perfil de un cliente.
     *
     * Carga la documentación actual, los datos de la tarjeta, las
     * provincias disponibles y el historial de reservas.
     *
     * @param model objeto que contiene los atributos que se enviarán a la vista
     * @param client entidad del cliente cuyos datos se mostrarán
     * @param form formulario con los datos personales del cliente
     * @param documentacioForm formulario con los datos de documentación
     * @param targetaForm formulario con los datos de la tarjeta
     */
    private void prepararVista(
            Model model,
            ClientJpaEntity client,
            AgentEditarForm form,
            DocumentacioForm documentacioForm,
            TargetaForm targetaForm) {

        if (documentacioForm == null) {
            documentacioForm = new DocumentacioForm();
        }

        /**
         * Carga el número del documento de identidad actual del cliente.
         */
        documentacioForm.setNumeroDocument(client.getDNI());

        /**
         * Recupera la fecha de caducidad del DNI y la convierte
         * al formato LocalDate.
         */
        if (client.getCadDNI() != null
                && !client.getCadDNI().isBlank()) {
            try {
                documentacioForm.setDataCaducitat(
                        LocalDate.parse(client.getCadDNI())
                );
            } catch (Exception e) {
                documentacioForm.setDataCaducitat(null);
            }
        }

        /**
         * Carga el tipo de licencia de conducir registrado para el cliente.
         */
        documentacioForm.setTipusLlicencia(
                client.getLliconduir()
        );

        /**
         * Recupera y convierte la fecha de caducidad de la licencia
         * de conducir.
         */
        if (client.getCdLLiconduir() != null
                && !client.getCdLLiconduir().isBlank()) {
            try {
                documentacioForm.setDataCaducitatLlicencia(
                        LocalDate.parse(client.getCdLLiconduir())
                );
            } catch (Exception e) {
                documentacioForm.setDataCaducitatLlicencia(null);
            }
        }

        model.addAttribute("dni", client.getDNI());
        model.addAttribute("client", form);
        model.addAttribute("documentacioForm", documentacioForm);
        model.addAttribute("premium", client.isPremium());

        model.addAttribute(
                "provincias",
                ProvinciaValidator.obtenirProvincies()
                        .stream()
                        .sorted()
                        .toList()
        );

        /**
         * Comprueba si el cliente tiene una tarjeta registrada.
         * El hash de la tarjeta nunca se envía al formulario.
         */
        boolean targetaGuardada =
                client.getNumtarjetacredit() != null
                && !client.getNumtarjetacredit().isBlank();

        model.addAttribute("targetaGuardada", targetaGuardada);

        /**
         * Prepara los datos visibles de la tarjeta.
         * Si no existe un formulario, crea uno nuevo y carga únicamente
         * los datos que pueden mostrarse de forma segura.
         */
        if (targetaForm == null) {

            targetaForm = new TargetaForm();

            targetaForm.setTitularTargeta(
                    client.getTitularTargeta()
            );

            /**
             * No se carga el número de tarjeta porque en la base de datos
             * únicamente se almacena su hash irreversible.
             */

            targetaForm.setCaducitatTargeta(
                    client.getCaducitatTargeta()
            );
        }

        model.addAttribute("targetaForm", targetaForm);

        model.addAttribute(
                "reserves",
                reservaService.obtenirHistoricClient(client.getId())
        );
    }

    /**
     * Crea un formulario de edición personal utilizando los datos
     * actuales de la entidad del cliente.
     *
     * @param client entidad del cliente de la que se obtendrán los datos
     * @return formulario rellenado con la información personal del cliente
     */
    private AgentEditarForm crearFormPersonal(
            ClientJpaEntity client) {

        AgentEditarForm form = new AgentEditarForm();

        form.setNom(client.getNom());
        form.setCognoms(client.getCognoms());
        form.setTelefon(client.getTelefon());
        form.setEmail(client.getEmail());
        form.setAdreca(client.getAdreca());
        form.setPoblacio(client.getPoblacio());
        form.setNacionalitat(client.getNacionalitat());
        form.setProvincia(client.getProvincia());
        form.setPremium(client.isPremium());

        return form;
    }

    /**
     * Muestra el perfil de un cliente identificado mediante su DNI.
     *
     * Si el cliente no existe, redirige al listado de clientes
     * mostrando un mensaje de error.
     *
     * @param dni documento de identidad del cliente
     * @param model objeto que contiene los datos que se mostrarán en la vista
     * @param redirectAttributes atributos que se conservarán durante la redirección
     * @return nombre de la vista del perfil o ruta de redirección
     */
    @GetMapping("/{dni}/perfil")
    public String mostrarPerfil(
            @PathVariable String dni,
            Model model,
            RedirectAttributes redirectAttributes) {

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client."
            );

            return "redirect:/agent/clients";
        }

        prepararVista(
                model,
                client,
                crearFormPersonal(client),
                new DocumentacioForm(),
                null
        );

        return "edit-user";
    }

    /**
     * Guarda exclusivamente los datos personales del cliente.
     *
     * Valida el formulario antes de actualizar la información.
     * En caso de error de validación o de actualización, vuelve a mostrar
     * el formulario con los datos correspondientes.
     *
     * @param dni documento de identidad del cliente
     * @param dades formulario con los nuevos datos personales
     * @param bindingResult resultado de la validación del formulario
     * @param model objeto que contiene los datos que se mostrarán en la vista
     * @param redirectAttributes atributos que se conservarán durante la redirección
     * @return nombre de la vista del perfil o ruta de redirección
     */
    @PostMapping("/{dni}/guardar")
    public String guardarDatos(
            @PathVariable String dni,

            @Valid @ModelAttribute("client") AgentEditarForm dades,
            BindingResult bindingResult,

            Model model,
            RedirectAttributes redirectAttributes) {

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client."
            );

            return "redirect:/agent/clients";
        }

        if (bindingResult.hasErrors()) {

            prepararVista(
                    model,
                    client,
                    dades,
                    new DocumentacioForm(),
                    null
            );

            return "edit-user";
        }

        try {
            client.setNom(dades.getNom());
            client.setCognoms(dades.getCognoms());
            client.setTelefon(dades.getTelefon());
            client.setEmail(dades.getEmail());
            client.setAdreca(dades.getAdreca());
            client.setPoblacio(dades.getPoblacio());
            client.setNacionalitat(dades.getNacionalitat());
            client.setProvincia(dades.getProvincia());
            client.setPremium(dades.isPremium());

            clientService.actualizarClientAdmin(
                    dni,
                    client,
                    null,
                    null
            );

            redirectAttributes.addFlashAttribute(
                    "missatge",
                    "Les dades personals s'han actualitzat correctament."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";

        } catch (IllegalArgumentException | IOException e) {

            model.addAttribute("error", e.getMessage());

            prepararVista(
                    model,
                    client,
                    dades,
                    new DocumentacioForm(),
                    null
            );

            return "edit-user";
        }
    }

    /**
     * Actualiza exclusivamente la documentación del documento de identidad
     * del cliente.
     *
     * Permite modificar los datos documentales y adjuntar una nueva imagen
     * del DNI. Si ocurre un error, vuelve a mostrar el formulario.
     *
     * @param dni documento de identidad del cliente
     * @param documentacioForm formulario con los datos del documento
     * @param fotoDni archivo de imagen del DNI, si se proporciona
     * @param model objeto que contiene los datos que se mostrarán en la vista
     * @param redirectAttributes atributos que se conservarán durante la redirección
     * @return nombre de la vista del perfil o ruta de redirección
     */
    @PostMapping("/{dni}/guardar-documentacio")
    public String guardarDocumentacio(
            @PathVariable String dni,

            @ModelAttribute("documentacioForm")
            DocumentacioForm documentacioForm,

            @RequestParam(value = "fotoDniFile", required = false)
            MultipartFile fotoDni,

            Model model,
            RedirectAttributes redirectAttributes) {

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client."
            );

            return "redirect:/agent/clients";
        }

        try {
            clientService.actualizarDocumentacio(
                    dni,
                    documentacioForm,
                    fotoDni
            );

            redirectAttributes.addFlashAttribute(
                    "missatge",
                    "La documentació del DNI s'ha actualitzat correctament."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            prepararVista(
                    model,
                    client,
                    crearFormPersonal(client),
                    documentacioForm,
                    null
            );

            return "edit-user";

        } catch (IOException e) {

            model.addAttribute(
                    "error",
                    "No s'ha pogut actualitzar la documentació del DNI."
            );

            prepararVista(
                    model,
                    client,
                    crearFormPersonal(client),
                    documentacioForm,
                    null
            );

            return "edit-user";
        }
    }

    /**
     * Actualiza exclusivamente los datos de la licencia de conducir
     * del cliente.
     *
     * Permite modificar la información de la licencia y adjuntar
     * una nueva imagen del permiso de conducir.
     *
     * @param dni documento de identidad del cliente
     * @param documentacioForm formulario con los datos de la licencia
     * @param fotoPermiso archivo de imagen de la licencia, si se proporciona
     * @param model objeto que contiene los datos que se mostrarán en la vista
     * @param redirectAttributes atributos que se conservarán durante la redirección
     * @return nombre de la vista del perfil o ruta de redirección
     */
    @PostMapping("/{dni}/guardar-llicencia")
    public String guardarLlicencia(
            @PathVariable String dni,

            @ModelAttribute("documentacioForm")
            DocumentacioForm documentacioForm,

            @RequestParam(value = "fotoPermisoFile", required = false)
            MultipartFile fotoPermiso,

            Model model,
            RedirectAttributes redirectAttributes) {

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client."
            );

            return "redirect:/agent/clients";
        }

        try {
            clientService.actualizarLlicencia(
                    dni,
                    documentacioForm,
                    fotoPermiso
            );

            redirectAttributes.addFlashAttribute(
                    "missatge",
                    "La llicència s'ha actualitzat correctament."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            prepararVista(
                    model,
                    client,
                    crearFormPersonal(client),
                    documentacioForm,
                    null
            );

            return "edit-user";

        } catch (IOException e) {

            model.addAttribute(
                    "error",
                    "No s'ha pogut actualitzar la llicència."
            );

            prepararVista(
                    model,
                    client,
                    crearFormPersonal(client),
                    documentacioForm,
                    null
            );

            return "edit-user";
        }
    }

    /**
     * Actualiza exclusivamente los datos de la tarjeta bancaria
     * del cliente.
     *
     * Valida los datos recibidos antes de enviarlos al servicio.
     * Si se produce algún error, vuelve a mostrar el formulario
     * conservando los datos introducidos.
     *
     * @param dni documento de identidad del cliente
     * @param targeta formulario con los datos de la tarjeta
     * @param bindingResult resultado de la validación del formulario
     * @param model objeto que contiene los datos que se mostrarán en la vista
     * @param redirectAttributes atributos que se conservarán durante la redirección
     * @return nombre de la vista del perfil o ruta de redirección
     */
    @PostMapping("/{dni}/guardar-targeta")
    public String guardarTargeta(
            @PathVariable String dni,

            @Valid @ModelAttribute("targetaForm") TargetaForm targeta,
            BindingResult bindingResult,

            Model model,
            RedirectAttributes redirectAttributes) {

        ClientJpaEntity client = clientService.findByDNI(dni)
                .orElse(null);

        if (client == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha trobat el client."
            );

            return "redirect:/agent/clients";
        }

        if (bindingResult.hasErrors()) {

            prepararVista(
                    model,
                    client,
                    crearFormPersonal(client),
                    new DocumentacioForm(),
                    targeta
            );

            return "edit-user";
        }

        try {
            clientService.actualizarTargeta(
                    dni,
                    targeta.getTitularTargeta(),
                    targeta.getNumtarjetacredit(),
                    targeta.getCaducitatTargeta()
            );

            redirectAttributes.addFlashAttribute(
                    "missatge",
                    "Les dades de la targeta s'han actualitzat correctament."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            prepararVista(
                    model,
                    client,
                    crearFormPersonal(client),
                    new DocumentacioForm(),
                    targeta
            );

            return "edit-user";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "No s'han pogut actualitzar les dades de la targeta."
            );

            prepararVista(
                    model,
                    client,
                    crearFormPersonal(client),
                    new DocumentacioForm(),
                    targeta
            );

            return "edit-user";
        }
    }

    /**
     * Cambia la contraseña de un cliente.
     *
     * Comprueba los datos de la nueva contraseña mediante el servicio
     * correspondiente y muestra un mensaje indicando si la operación
     * se ha realizado correctamente.
     *
     * @param dni documento de identidad del cliente
     * @param novaContrasenya nueva contraseña que se desea establecer
     * @param confirmarContrasenya confirmación de la nueva contraseña
     * @param redirectAttributes atributos que se conservarán durante la redirección
     * @return ruta de redirección al perfil del cliente
     */
    @PostMapping("/{dni}/guardar-contrasenya")
    public String guardarContrasenya(
            @PathVariable String dni,

            @RequestParam String novaContrasenya,
            @RequestParam String confirmarContrasenya,

            RedirectAttributes redirectAttributes) {

        try {
            clientService.canviarContrasenya(
                    dni,
                    novaContrasenya,
                    confirmarContrasenya
            );

            redirectAttributes.addFlashAttribute(
                    "missatge",
                    "La contrasenya s'ha canviat correctament."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/agent/clients/" + dni + "/perfil";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha pogut canviar la contrasenya."
            );

            return "redirect:/agent/clients/" + dni + "/perfil";
        }
    }
}
