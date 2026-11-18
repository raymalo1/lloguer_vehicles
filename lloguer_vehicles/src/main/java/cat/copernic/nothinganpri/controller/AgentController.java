
package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.dto.AgentForm;
import cat.copernic.nothinganpri.dto.CanviSolicitud;
import cat.copernic.nothinganpri.entity.AgentJpaEntity;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.SolicitudAnulacioJpaEntity;
import cat.copernic.nothinganpri.entity.SolicitudValidacionJpaEntity;
import cat.copernic.nothinganpri.service.AgentService;
import cat.copernic.nothinganpri.service.ClientService;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.SolicitudAnulacioService;
import cat.copernic.nothinganpri.service.SolicitudValidacionService;
import cat.copernic.nothinganpri.validacio.ProvinciaValidator;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controlador encargado de gestionar las operaciones de los agentes.
 *
 * Permite administrar clientes, gestionar solicitudes de validación
 * y anulación, y realizar operaciones de administración de agentes.
 */
@Controller
public class AgentController {

    private final AgentService agentService;
    private final ClientService clientService;
    private final SolicitudValidacionService solicitudService;
    private final SolicitudAnulacioService solicitudAnulacioService;
    private final LogQueryService logQueryService;

    /**
     * Constructor del controlador que inicializa los servicios necesarios
     * para gestionar agentes, clientes, solicitudes y registros.
     *
     * @param agentService servicio encargado de gestionar los agentes
     * @param clientService servicio encargado de gestionar los clientes
     * @param solicitudService servicio encargado de gestionar las solicitudes de validación
     * @param solicitudAnulacioService servicio encargado de gestionar las solicitudes de anulación
     * @param logQueryService servicio encargado de registrar avisos y errores
     */
    public AgentController(
            AgentService agentService,
            ClientService clientService,
            SolicitudValidacionService solicitudService,
            SolicitudAnulacioService solicitudAnulacioService,
            LogQueryService logQueryService) {

        this.agentService = agentService;
        this.clientService = clientService;
        this.solicitudService = solicitudService;
        this.solicitudAnulacioService = solicitudAnulacioService;
        this.logQueryService = logQueryService;
    }

    /**
     * Carga la lista de provincias disponibles y la añade al modelo.
     *
     * @param model modelo donde se almacenarán las provincias
     */
    private void carregarProvincies(Model model) {
        model.addAttribute(
                "provincies",
                ProvinciaValidator.obtenirProvincies()
                        .stream()
                        .sorted()
                        .toList());
    }

    /**
     * Carga los roles disponibles para la gestión de agentes.
     *
     * @param model modelo donde se almacenarán los roles
     */
    private void carregarRols(Model model) {
        model.addAttribute("rols", List.of("AGENT", "ADMINISTRADOR"));
    }

    /**
     * Muestra el listado completo de clientes.
     *
     * Recupera los clientes sin aplicar filtros, carga las provincias
     * disponibles y prepara las solicitudes pendientes para mostrarlas
     * en la vista.
     *
     * @param model modelo donde se almacenarán los datos de la vista
     * @return nombre de la vista del listado de clientes
     */
    @GetMapping("/agent/clients")
    public String listarClients(Model model) {

        List<ClientJpaEntity> clients = clientService.cercarFiltres(
                null, null, null, null, null, null, null);

        model.addAttribute(
                "clients",
                clients != null ? clients : Collections.emptyList());

        carregarProvincies(model);

        model.addAttribute("nacionalitat", null);
        model.addAttribute("provincia", null);
        model.addAttribute("dni", null);
        model.addAttribute("cognoms", null);
        model.addAttribute("telefon", null);
        model.addAttribute("email", null);
        model.addAttribute("poblacio", null);

        carregarSolicitudes(model);

        return "llistat-clients";
    }

    /**
     * Busca clientes aplicando los filtros recibidos desde el formulario.
     *
     * Los resultados y los valores de los filtros se añaden al modelo
     * para mantener la información en la vista.
     *
     * @param nacionalitat nacionalidad por la que se desea filtrar
     * @param provincia provincia por la que se desea filtrar
     * @param dni documento de identidad por el que se desea filtrar
     * @param cognoms apellidos por los que se desea filtrar
     * @param telefon teléfono por el que se desea filtrar
     * @param email correo electrónico por el que se desea filtrar
     * @param poblacio población por la que se desea filtrar
     * @param model modelo donde se almacenarán los resultados
     * @return nombre de la vista del listado de clientes
     */
    @PostMapping("/agent/clients")
    public String cercarClients(
            @RequestParam(required = false) String nacionalitat,
            @RequestParam(required = false) String provincia,
            @RequestParam(required = false) String dni,
            @RequestParam(required = false) String cognoms,
            @RequestParam(required = false) String telefon,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String poblacio,
            Model model) {

        List<ClientJpaEntity> clients = clientService.cercarFiltres(
                nacionalitat,
                provincia,
                dni,
                cognoms,
                telefon,
                email,
                poblacio);

        model.addAttribute(
                "clients",
                clients != null ? clients : Collections.emptyList());

        carregarProvincies(model);

        model.addAttribute("nacionalitat", nacionalitat);
        model.addAttribute("provincia", provincia);
        model.addAttribute("dni", dni);
        model.addAttribute("cognoms", cognoms);
        model.addAttribute("telefon", telefon);
        model.addAttribute("email", email);
        model.addAttribute("poblacio", poblacio);

        carregarSolicitudes(model);

        return "llistat-clients";
    }

    /**
     * Recupera las solicitudes pendientes y las organiza según su tipo.
     *
     * Clasifica las solicitudes de registro, datos personales,
     * documentación y tarjeta. También carga las solicitudes de anulación
     * y las comparativas de cambios correspondientes.
     *
     * @param model modelo donde se almacenarán las solicitudes y comparativas
     */
    private void carregarSolicitudes(Model model) {

        List<SolicitudValidacionJpaEntity> pendents =
                solicitudService.obtenerPendientes();

        if (pendents == null) {
            pendents = Collections.emptyList();
        }

        List<SolicitudValidacionJpaEntity> registresInicials =
                pendents.stream()
                        .filter(s -> esTipus(s, "REGISTRO"))
                        .collect(Collectors.toList());

        List<SolicitudValidacionJpaEntity> solicitudesDatos =
                pendents.stream()
                        .filter(s -> esTipus(s, "DATOS_PERSONALES"))
                        .collect(Collectors.toList());

        List<SolicitudValidacionJpaEntity> solicitudesDocumentacion =
                pendents.stream()
                        .filter(s -> esTipus(s, "DOCUMENTACION"))
                        .collect(Collectors.toList());

        List<SolicitudValidacionJpaEntity> solicitudesTarjeta =
                pendents.stream()
                        .filter(s -> esTipus(s, "TARJETA"))
                        .collect(Collectors.toList());

        model.addAttribute("solicitudPorCliente", registresInicials);
        model.addAttribute("solicitudesDatos", solicitudesDatos);
        model.addAttribute("solicitudesDocumentacion", solicitudesDocumentacion);
        model.addAttribute("solicitudesTarjeta", solicitudesTarjeta);

        List<SolicitudAnulacioJpaEntity> solicitudesAnulacion =
                solicitudAnulacioService.obtenerPendientes();

        model.addAttribute(
                "solicitudAnulacions",
                solicitudesAnulacion != null
                        ? solicitudesAnulacion
                        : Collections.emptyList());

        Map<Long, List<CanviSolicitud>> canvisPerSolicitud = new HashMap<>();

        for (SolicitudValidacionJpaEntity solicitud : pendents) {

            if (solicitud == null || solicitud.getId() == null) {
                continue;
            }

            canvisPerSolicitud.put(
                    solicitud.getId(),
                    solicitudService.obtenerComparativa(solicitud));
        }

        model.addAttribute("canvisPerSolicitud", canvisPerSolicitud);
    }

    /**
     * Comprueba si una solicitud corresponde al tipo indicado.
     *
     * La comparación no distingue entre mayúsculas y minúsculas
     * y comprueba previamente que la solicitud y su tipo no sean nulos.
     *
     * @param solicitud solicitud que se desea comprobar
     * @param tipus tipo de solicitud que se quiere comparar
     * @return true si la solicitud coincide con el tipo indicado,
     *         false en caso contrario
     */
    private boolean esTipus(
            SolicitudValidacionJpaEntity solicitud,
            String tipus) {

        return solicitud != null
                && solicitud.getTipo() != null
                && tipus.equalsIgnoreCase(solicitud.getTipo());
    }

    /**
     * Muestra las solicitudes de validación pendientes.
     *
     * Recupera las solicitudes mediante el servicio y las añade al modelo.
     *
     * @param model modelo donde se almacenarán las solicitudes
     * @return nombre de la vista de solicitudes del agente
     */
    @GetMapping("/agent/solicitudes")
    public String mostrarSolicitudes(Model model) {

        List<SolicitudValidacionJpaEntity> solicitudes =
                solicitudService.obtenerPendientes();

        model.addAttribute(
                "solicitudes",
                solicitudes != null
                        ? solicitudes
                        : Collections.emptyList());

        return "agent/solicitudes";
    }

    /**
     * Aprueba una solicitud de registro de un cliente.
     *
     * Identifica al agente que realiza la operación mediante el usuario
     * autenticado y muestra un mensaje con el resultado.
     *
     * @param solicitudId identificador de la solicitud que se desea aprobar
     * @param principal información del usuario autenticado
     * @param redirect atributos que se conservarán durante la redirección
     * @return ruta de redirección al listado de clientes o al inicio de sesión
     */
    @PostMapping("/agent/solicitudes/aprobar")
    public String aprobarRegistro(
            @RequestParam("id") Long solicitudId,
            Principal principal,
            RedirectAttributes redirect) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            solicitudService.aprobarRegistro(
                    solicitudId,
                    principal.getName());

            redirect.addFlashAttribute(
                    "missatge",
                    "El registre s'ha aprovat correctament.");

        } catch (Exception e) {
            registrarErrorControlador("aprovar el registre", e);

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "No s'ha pogut aprovar el registre.");
        }

        return "redirect:/agent/clients";
    }

    /**
     * Aprueba los cambios propuestos en una solicitud de validación.
     *
     * Registra la operación mediante el servicio correspondiente
     * y muestra el resultado al usuario.
     *
     * @param solicitudId identificador de la solicitud que se desea aprobar
     * @param principal información del usuario autenticado
     * @param redirect atributos que se conservarán durante la redirección
     * @return ruta de redirección al listado de clientes o al inicio de sesión
     */
    @PostMapping("/agent/solicitudes/aprobar-cambios")
    public String aprobarCambios(
            @RequestParam("id") Long solicitudId,
            Principal principal,
            RedirectAttributes redirect) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            solicitudService.aprobarSolicitud(
                    solicitudId,
                    principal.getName());

            redirect.addFlashAttribute(
                    "missatge",
                    "Els canvis s'han aprovat correctament.");

        } catch (Exception e) {
            registrarErrorControlador("aprovar els canvis", e);

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "No s'han pogut aprovar els canvis.");
        }

        return "redirect:/agent/clients";
    }

    /**
     * Muestra el formulario para rechazar una solicitud pendiente.
     *
     * Comprueba que la solicitud exista y que todavía no haya sido revisada.
     * También prepara los datos legibles y las imágenes propuestas cuando
     * la solicitud corresponde a documentación.
     *
     * @param solicitudId identificador de la solicitud
     * @param model modelo donde se almacenarán los datos de la solicitud
     * @param redirect atributos que se conservarán durante la redirección
     * @return nombre de la vista del formulario de rechazo o ruta de redirección
     */
    @GetMapping("/agent/solicitudes/rebutjar")
    public String mostrarFormulariRebuig(
            @RequestParam("id") Long solicitudId,
            Model model,
            RedirectAttributes redirect) {

        List<SolicitudValidacionJpaEntity> pendents =
                solicitudService.obtenerPendientes();

        if (pendents == null) {
            pendents = Collections.emptyList();
        }

        SolicitudValidacionJpaEntity solicitud = pendents.stream()
                .filter(s -> s != null && s.getId() != null)
                .filter(s -> solicitudId.equals(s.getId()))
                .findFirst()
                .orElse(null);

        if (solicitud == null) {
            logQueryService.registrarWarning(
                    "AGENT",
                    "Intent de consultar un formulari de rebuig d'una sol·licitud inexistent o ja revisada.");

            redirect.addFlashAttribute(
                    "error",
                    "La sol·licitud no existeix o ja ha estat revisada.");

            return "redirect:/agent/clients";
        }

        model.addAttribute("solicitud", solicitud);

        Map<String, String> datosLegibles = Collections.emptyMap();

        String json = solicitud.getDatosPropuestos();

        if (json != null && !json.isBlank()) {
            Map<String, String> resultado =
                    solicitudService.obtenerDatosLegibles(json);

            if (resultado != null) {
                datosLegibles = resultado;
            }
        }

        model.addAttribute("datosLegibles", datosLegibles);
        model.addAttribute("fotoDniProposada", null);
        model.addAttribute("fotoPermisProposat", null);

        if (esTipus(solicitud, "DOCUMENTACION")) {

            model.addAttribute(
                    "fotoDniProposada",
                    solicitudService.obtenerFotoDni(json));

            model.addAttribute(
                    "fotoPermisProposat",
                    solicitudService.obtenerFotoPermiso(json));
        }

        return "rebutjar-registre";
    }

    /**
     * Rechaza una solicitud de validación indicando el motivo correspondiente.
     *
     * Identifica al agente autenticado que realiza el rechazo y muestra
     * un mensaje con el resultado de la operación.
     *
     * @param solicitudId identificador de la solicitud que se desea rechazar
     * @param motivo motivo por el que se rechaza la solicitud
     * @param principal información del usuario autenticado
     * @param redirect atributos que se conservarán durante la redirección
     * @return ruta de redirección al listado de clientes o al inicio de sesión
     */
    @PostMapping("/agent/solicitudes/rechazar")
    public String rechazarSolicitud(
            @RequestParam("id") Long solicitudId,
            @RequestParam("motivo") String motivo,
            Principal principal,
            RedirectAttributes redirect) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            solicitudService.rechazarSolicitud(
                    solicitudId,
                    principal.getName(),
                    motivo);

            redirect.addFlashAttribute(
                    "missatge",
                    "La sol·licitud s'ha rebutjat correctament.");

        } catch (Exception e) {
            registrarErrorControlador("rebutjar la sol·licitud", e);

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "No s'ha pogut rebutjar la sol·licitud.");
        }

        return "redirect:/agent/clients";
    }

    /**
     * Muestra el listado de agentes activos.
     *
     * Recupera los agentes mediante el servicio y prepara el estado
     * del filtro para la vista de gestión.
     *
     * @param model modelo donde se almacenarán los agentes
     * @return nombre de la vista de gestión de agentes
     */
    @GetMapping("/admin/agents")
    public String llistarAgents(Model model) {

        List<AgentJpaEntity> agents =
                agentService.cercarFiltres(null, true);

        model.addAttribute(
                "agents",
                agents != null ? agents : Collections.emptyList());

        model.addAttribute("estat", "true");

        return "gestio-agents";
    }

    /**
     * Muestra el formulario para registrar un nuevo agente.
     *
     * Inicializa la fecha del contrato con la fecha actual y carga
     * las provincias y los roles disponibles.
     *
     * @param model modelo donde se almacenará el formulario
     * @return nombre de la vista de creación de agentes
     */
    @GetMapping("/admin/agents/nou")
    public String mostrarFormulariAgent(Model model) {

        AgentForm agent = new AgentForm();

        /**
         * Establece inicialmente la fecha del contrato con la fecha actual.
         */
        agent.setDataContracte(LocalDate.now());

        model.addAttribute("agent", agent);

        carregarProvincies(model);
        carregarRols(model);

        return "nou-agent";
    }

    /**
     * Guarda un nuevo agente en el sistema.
     *
     * Valida los datos recibidos, comprueba que la contraseña y la fecha
     * del contrato sean obligatorias y crea la entidad correspondiente.
     * Si se produce un error, lo registra y muestra los mensajes adecuados.
     *
     * @param agent formulario con los datos del nuevo agente
     * @param bindingResult resultado de la validación del formulario
     * @param model modelo donde se almacenarán los datos de la vista
     * @return nombre de la vista de creación o ruta de redirección al listado
     */
    @PostMapping("/admin/agents/guardar")
    public String guardarAgent(
            @Valid @ModelAttribute("agent") AgentForm agent,
            BindingResult bindingResult,
            Model model) {

        carregarProvincies(model);
        carregarRols(model);

        /**
         * Comprueba que la contraseña sea obligatoria durante la creación
         * de un agente.
         */
        if (agent.getPassword() == null
                || agent.getPassword().isBlank()) {

            bindingResult.rejectValue(
                    "password",
                    "NotBlank",
                    "La contrasenya és obligatòria");
        }

        /**
         * Comprueba que la fecha del contrato sea obligatoria
         * durante la creación de un agente.
         */
        if (agent.getDataContracte() == null) {

            bindingResult.rejectValue(
                    "dataContracte",
                    "NotNull",
                    "La data de contracte és obligatòria");
        }

        if (bindingResult.hasErrors()) {
            return "nou-agent";
        }

        try {
            AgentJpaEntity nuevoAgent = new AgentJpaEntity();

            nuevoAgent.setDNI(agent.getDNI());
            nuevoAgent.setNom(agent.getNom());
            nuevoAgent.setCognoms(agent.getCognoms());
            nuevoAgent.setProvincia(agent.getProvincia());
            nuevoAgent.setTelefon(agent.getTelefon());
            nuevoAgent.setEmail(agent.getEmail());
            nuevoAgent.setPassword(agent.getPassword());
            nuevoAgent.setDataContracte(agent.getDataContracte());
            nuevoAgent.setRol(agent.getRol());

            agentService.guardar(nuevoAgent);

            return "redirect:/admin/agents";

        } catch (IllegalArgumentException e) {

            registrarErrorControlador("guardar l'agent", e);

            String missatge = e.getMessage();

            if (missatge != null) {

                String normalitzat = missatge.toLowerCase(Locale.ROOT);

                if (normalitzat.contains("dni")) {
                    bindingResult.rejectValue(
                            "DNI", "agent.DNI", missatge);

                } else if (normalitzat.contains("email")
                        || normalitzat.contains("correu")) {

                    bindingResult.rejectValue(
                            "email", "agent.email", missatge);

                } else if (normalitzat.contains("telèfon")
                        || normalitzat.contains("telefon")) {

                    bindingResult.rejectValue(
                            "telefon", "agent.telefon", missatge);

                } else if (normalitzat.contains("província")
                        || normalitzat.contains("provincia")) {

                    bindingResult.rejectValue(
                            "provincia", "agent.provincia", missatge);

                } else if (normalitzat.contains("rol")) {

                    bindingResult.rejectValue(
                            "rol", "agent.rol", missatge);

                } else if (normalitzat.contains("contrasenya")) {

                    bindingResult.rejectValue(
                            "password", "agent.password", missatge);

                } else if (normalitzat.contains("data de contracte")) {

                    bindingResult.rejectValue(
                            "dataContracte", "agent.dataContracte", missatge);

                } else {
                    model.addAttribute("error", missatge);
                }

            } else {
                model.addAttribute(
                        "error",
                        "Les dades introduïdes no són vàlides.");
            }

            return "nou-agent";

        } catch (Exception e) {

            registrarErrorControlador("guardar l'agent", e);

            model.addAttribute(
                    "error",
                    "No s'ha pogut registrar l'agent. Comprova les dades i torna-ho a provar.");

            return "nou-agent";
        }
    }

    /**
     * Filtra los agentes según el DNI y el estado de actividad indicados.
     *
     * Conserva los valores de los filtros en el modelo para mostrarlos
     * en la vista de resultados.
     *
     * @param dni documento de identidad utilizado como filtro
     * @param activo estado de actividad utilizado como filtro
     * @param model modelo donde se almacenarán los resultados
     * @return nombre de la vista de gestión de agentes
     */
    @PostMapping("/admin/agents/filtrar")
    public String filtrarAgents(
            @RequestParam(value = "dni", required = false) String dni,
            @RequestParam(value = "activo", required = false) Boolean activo,
            Model model) {

        List<AgentJpaEntity> agents =
                agentService.cercarFiltres(dni, activo);

        model.addAttribute(
                "agents",
                agents != null ? agents : Collections.emptyList());

        model.addAttribute("dni", dni);
        model.addAttribute(
                "estat",
                activo == null ? "" : activo.toString());

        return "gestio-agents";
    }

    /**
     * Muestra la ficha informativa de un agente identificado por su DNI.
     *
     * Si el agente no existe, registra un aviso y redirige al listado.
     *
     * @param dni documento de identidad del agente
     * @param model modelo donde se almacenarán los datos del agente
     * @param redirect atributos que se conservarán durante la redirección
     * @return nombre de la vista informativa o ruta de redirección
     */
    @GetMapping("/admin/agents/dni/{dni}")
    public String veureFitxaAgent(
            @PathVariable String dni,
            Model model,
            RedirectAttributes redirect) {

        AgentJpaEntity agent =
                agentService.buscarAgentPerDNI(dni);

        if (agent == null) {
            logQueryService.registrarWarning(
                    "AGENT",
                    "Intent de consultar la fitxa d'un agent inexistent.");

            redirect.addFlashAttribute(
                    "error",
                    "No s'ha trobat l'agent.");

            return "redirect:/admin/agents";
        }

        model.addAttribute("agent", agent);

        return "informacio-agents";
    }

    /**
     * Muestra el formulario de edición de un agente.
     *
     * Recupera los datos del agente mediante su identificador y los copia
     * al formulario. Si el agente no existe, registra un aviso y redirige
     * al listado de agentes.
     *
     * @param id identificador del agente que se desea editar
     * @param model modelo donde se almacenará el formulario
     * @param redirect atributos que se conservarán durante la redirección
     * @return nombre de la vista de edición o ruta de redirección
     */
    @GetMapping("/admin/agents/editar/{id}")
    public String mostrarEditarAgent(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirect) {

        AgentJpaEntity agent = agentService.buscarPerId(id);

        if (agent == null) {
            logQueryService.registrarWarning(
                    "AGENT",
                    "Intent de modificar un agent inexistent.");

            redirect.addFlashAttribute(
                    "error",
                    "No s'ha trobat l'agent.");

            return "redirect:/admin/agents";
        }

        AgentForm form = new AgentForm();

        form.setId(agent.getId());
        form.setDNI(agent.getDNI());
        form.setNom(agent.getNom());
        form.setCognoms(agent.getCognoms());
        form.setProvincia(agent.getProvincia());
        form.setTelefon(agent.getTelefon());
        form.setEmail(agent.getEmail());
        form.setDataContracte(agent.getDataContracte());
        form.setRol(agent.getRol());

        model.addAttribute("agent", form);

        carregarProvincies(model);
        carregarRols(model);

        return "editar-agent";
    }

    /**
     * Actualiza los datos de un agente existente.
     *
     * Valida el formulario y delega la actualización al servicio.
     * Si ocurre algún error, lo registra y vuelve a mostrar el formulario
     * con los datos necesarios.
     *
     * @param id identificador del agente que se desea actualizar
     * @param form formulario con los nuevos datos del agente
     * @param bindingResult resultado de la validación del formulario
     * @param model modelo donde se almacenarán los datos de la vista
     * @param redirect atributos que se conservarán durante la redirección
     * @return nombre de la vista de edición o ruta de redirección
     */
    @PostMapping("/admin/agents/editar/{id}")
    public String actualizarAgent(
            @PathVariable Long id,
            @Valid @ModelAttribute("agent") AgentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirect) {

        form.setId(id);

        if (bindingResult.hasErrors()) {
            carregarProvincies(model);
            carregarRols(model);
            return "editar-agent";
        }

        try {
            /**
             * El servicio conserva la contraseña original del agente
             * y también mantiene la fecha del contrato si recibe null.
             */
            agentService.actualitzar(id, form);

            redirect.addFlashAttribute(
                    "missatge",
                    "L'agent s'ha actualitzat correctament.");

            return "redirect:/admin/agents";

        } catch (IllegalArgumentException e) {

            registrarErrorControlador("actualitzar l'agent", e);

            model.addAttribute("error", e.getMessage());

            carregarProvincies(model);
            carregarRols(model);

            return "editar-agent";

        } catch (Exception e) {

            registrarErrorControlador("actualitzar l'agent", e);

            model.addAttribute(
                    "error",
                    "No s'ha pogut actualitzar l'agent.");

            carregarProvincies(model);
            carregarRols(model);

            return "editar-agent";
        }
    }

    /**
     * Elimina un agente mediante su identificador.
     *
     * Registra los posibles errores y muestra un mensaje indicando
     * el resultado de la operación.
     *
     * @param id identificador del agente que se desea eliminar
     * @param redirect atributos que se conservarán durante la redirección
     * @return ruta de redirección al listado de agentes
     */
    @PostMapping("/admin/agents/eliminar/{id}")
    public String eliminarAgent(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        try {
            agentService.eliminar(id);

            redirect.addFlashAttribute(
                    "missatge",
                    "L'agent s'ha eliminat correctament.");

        } catch (IllegalArgumentException e) {

            registrarErrorControlador("eliminar l'agent", e);

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage());

        } catch (Exception e) {

            registrarErrorControlador("eliminar l'agent", e);

            redirect.addFlashAttribute(
                    "error",
                    "No s'ha pogut eliminar l'agent. Pot tenir registres relacionats.");
        }

        return "redirect:/admin/agents";
    }

    /**
     * Registra los errores producidos durante las operaciones del controlador.
     *
     * Las excepciones de tipo IllegalArgumentException e
     * IllegalStateException se registran como advertencias.
     * El resto de excepciones se registran como errores.
     *
     * @param operacion descripción de la operación que ha fallado
     * @param e excepción producida durante la operación
     */
    private void registrarErrorControlador(
            String operacion,
            Exception e) {

        if (e instanceof IllegalArgumentException
                || e instanceof IllegalStateException) {

            logQueryService.registrarWarning(
                    "AGENT",
                    "No s'ha pogut " + operacion + ": " + e.getMessage());

        } else {
            logQueryService.registrarError(
                    "AGENT",
                    "Error inesperat en intentar " + operacion + ": " + e.getMessage());
        }
    }
}
