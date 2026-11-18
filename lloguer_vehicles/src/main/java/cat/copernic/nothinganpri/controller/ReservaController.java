package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.dto.CreateReservaRequest;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.ReservaJpaEntity;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.document.HistoricReservaDocument;
import cat.copernic.nothinganpri.entity.EntregaJpaEntity;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import cat.copernic.nothinganpri.repository.VehicleJpaRepository;
import cat.copernic.nothinganpri.service.ReservaService;
import cat.copernic.nothinganpri.service.SolicitudAnulacioService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequestMapping("/reserva")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private SolicitudAnulacioService solicitudAnulacioService;

    @Autowired
    private ReservaJpaRepository reservaRepository;

    @Autowired
    private VehicleJpaRepository vehicleRepository;

    @Autowired
    private UsuariJpaRepository usuariRepository;

    // ---------- Nova reserva ----------
    @GetMapping("/nova")
    public String mostrarFormulari(@RequestParam(required = false) Long vehicleId,
                                   Authentication authentication, Model model) {
        CreateReservaRequest dto = new CreateReservaRequest();

        if (vehicleId != null) {
            dto.setVehicleId(vehicleId);
        }

        if (esNomesClient(authentication)) {
            if (!preparaVistaClient(dto, authentication, model)) {
                return "redirect:/cataleg";
            }

            model.addAttribute("reservaDto", dto);
            return "reserva-form";
        }

        model.addAttribute("esClient", false);
        model.addAttribute("reservaDto", dto);
        model.addAttribute("vehicles", vehicleRepository.findAll());
        model.addAttribute("clients", usuariRepository.findAll());

        return "reserva-form";
    }

    @PostMapping("/calcular")
    public String calcularReserva(@ModelAttribute("reservaDto") CreateReservaRequest dto,
                                  Authentication authentication, Model model) {

        boolean esClient = esNomesClient(authentication);

        if (esClient && !preparaVistaClient(dto, authentication, model)) {
            return "redirect:/cataleg";
        }

        try {
            long hores = calcularImport(dto, esClient);
            model.addAttribute("hores", hores);
            model.addAttribute("missatge",
                    "Preu calculat correctament. Pots confirmar la reserva.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("reservaDto", dto);

        if (!esClient) {
            model.addAttribute("esClient", false);
            model.addAttribute("vehicles", vehicleRepository.findAll());
            model.addAttribute("clients", usuariRepository.findAll());
        }

        return "reserva-form";
    }

    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute("reservaDto") CreateReservaRequest dto,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttrs) {

        boolean esClient = esNomesClient(authentication);

        try {
            if (esClient) {
                UsuariJpaEntity autenticat = usuariAutenticat(authentication);

                if (autenticat == null) {
                    throw new Exception("No s'ha pogut identificar el client.");
                }

                dto.setClientId(autenticat.getId());
            }

            if (dto.getClientId() == null) {
                throw new Exception("Has de seleccionar un client.");
            }

            if (dto.getVehicleId() == null) {
                throw new Exception("Has de seleccionar un vehicle.");
            }

            if (dto.getDataInici() == null || dto.getDataFi() == null) {
                throw new Exception("Has d'indicar les dates d'inici i final.");
            }

            if (dto.getHoraInici() == null) {
                throw new Exception("Has d'indicar l'hora de recollida del vehicle.");
            }

            if (dto.getHoraFi() == null) {
                throw new Exception("Has d'indicar l'hora de retorn del vehicle.");
            }

            if (dto.getDataInici().equals(LocalDate.now())
                    && dto.getHoraInici().isBefore(LocalTime.now())) {
                throw new Exception("L'hora de recollida no pot ser anterior a l'hora actual.");
            }

            if (dto.getDataFi().equals(dto.getDataInici())
                    && dto.getHoraFi().isBefore(dto.getHoraInici())) {
                throw new Exception("L'hora de retorn no pot ser anterior a l'hora de recollida el mateix dia.");
            }

            if (esClient && Boolean.FALSE.equals(dto.getAcceptaDocumentacio())) {
                throw new Exception("Has d'acceptar les condicions de documentació.");
            }

            VehicleJpaEntity vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new Exception("Vehicle no trobat"));

            UsuariJpaEntity client = usuariRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new Exception("Client no trobat"));

            calcularImport(dto, esClient);

            ReservaJpaEntity reserva = new ReservaJpaEntity();

            reserva.setDataInici(dto.getDataInici());
            reserva.setHoraInici(dto.getHoraInici());
            reserva.setDataFi(dto.getDataFi());
            reserva.setHoraFi(dto.getHoraFi());
            reserva.setAcceptaDocumentacio(
                    dto.getAcceptaDocumentacio() != null && dto.getAcceptaDocumentacio());
            reserva.setImportTotal(dto.getTotal());
            reserva.setVehicle(vehicle);
            reserva.setClient(client);

            ReservaJpaEntity creada;
            if (esClient) {
                creada = reservaService.crearReservaClient(reserva);
            } else {
                creada = reservaService.crearReserva(reserva);
            }

            // RF53: després de crear, anar al detall
            return "redirect:/reserva/confirmacio/" + creada.getId();

        } catch (Exception e) {

            if (esClient) {
                if (!preparaVistaClient(dto, authentication, model)) {
                    return "redirect:/cataleg";
                }

                try {
                    calcularImport(dto, true);
                } catch (Exception ignorat) {
                }

                model.addAttribute("error", e.getMessage());
                model.addAttribute("senseTargeta",
                        ReservaService.MISSATGE_SENSE_TARGETA.equals(e.getMessage()));
                model.addAttribute("reservaDto", dto);

                return "reserva-form";
            }

            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/nova";
        }
    }

    // ---------- RF53: Detall d'una reserva (Més detalls / després de crear) ----------
    @GetMapping("/confirmacio/{id}")
    public String confirmacioReserva(@PathVariable Long id,
                                     Model model,
                                     RedirectAttributes redirectAttrs) {
        try {
            ReservaJpaEntity reserva = reservaService.buscarPerId(id);

            if (reserva.getVehicle() != null) {
                reserva.getVehicle().getMatricula();
            }
            if (reserva.getClient() != null) {
                reserva.getClient().getEmail();
            }

            long dies = 0;
            if (reserva.getDataInici() != null && reserva.getDataFi() != null) {
                dies = ChronoUnit.DAYS.between(reserva.getDataInici(), reserva.getDataFi());
                if (dies < 1) {
                    dies = 1;
                }
            }

            model.addAttribute("reserva", reserva);
            model.addAttribute("dies", dies);
            return "reserva-confirmada";

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/meves";
        }
    }

    // ---------- Ajudes ----------
    private long calcularImport(CreateReservaRequest dto, boolean esClient) throws Exception {

        dto.setSubtotal(null);
        dto.setFianca(null);
        dto.setFiancaOriginal(null);
        dto.setDescomptePremium(false);
        dto.setTotal(null);
        dto.setTotalSenseDescompte(null);

        if (dto.getDataInici() == null || dto.getDataFi() == null) {
            throw new Exception("Introdueix les dates d'inici i final per calcular el preu.");
        }

        if (dto.getHoraInici() == null) {
            throw new Exception("Introdueix l'hora de recollida del vehicle.");
        }

        if (dto.getHoraFi() == null) {
            throw new Exception("Introdueix l'hora de retorn del vehicle.");
        }

        if (esClient && dto.getDataInici().isBefore(LocalDate.now())) {
            throw new Exception("La data d'inici no pot ser anterior a avui.");
        }

        if (dto.getDataInici().equals(LocalDate.now())
                && dto.getHoraInici().isBefore(LocalTime.now())) {
            throw new Exception("L'hora de recollida no pot ser anterior a l'hora actual.");
        }

        LocalDateTime inici = LocalDateTime.of(dto.getDataInici(), dto.getHoraInici());
        LocalDateTime fi = LocalDateTime.of(dto.getDataFi(), dto.getHoraFi());

        long hores = ChronoUnit.HOURS.between(inici, fi);

        if (hores <= 0) {
            throw new Exception("L'hora de retorn ha de ser posterior a la de recollida.");
        }

        double preuHora = 2.0;
        double fiancaOriginal = 150.0;

        if (dto.getVehicleId() != null) {
            VehicleJpaEntity v = vehicleRepository.findById(dto.getVehicleId()).orElse(null);

            if (v != null) {
                if (v.getPreuHora() != null) {
                    preuHora = v.getPreuHora().doubleValue();
                }
                if (v.getFiancaEstandard() != null) {
                    fiancaOriginal = v.getFiancaEstandard().doubleValue();
                }
            }
        }

        double fianca = fiancaOriginal;
        boolean descomptePremium = false;

        // Mode premium: 25% de descompte a la fiança
        if (dto.getClientId() != null) {
            UsuariJpaEntity u = usuariRepository.findById(dto.getClientId()).orElse(null);
            if (u instanceof ClientJpaEntity client && client.isPremium()) {
                fianca = fiancaOriginal * 0.75;
                descomptePremium = true;
            }
        }

        double subtotal = hores * preuHora;

        dto.setSubtotal(subtotal);
        dto.setFiancaOriginal(fiancaOriginal);
        dto.setFianca(fianca);
        dto.setDescomptePremium(descomptePremium);
        dto.setTotalSenseDescompte(subtotal + fiancaOriginal);
        dto.setTotal(subtotal + fianca);

        return hores;
    }

    private boolean preparaVistaClient(CreateReservaRequest dto,
                                       Authentication authentication,
                                       Model model) {

        UsuariJpaEntity usuari = usuariAutenticat(authentication);

        if (usuari == null || dto.getVehicleId() == null) {
            return false;
        }

        VehicleJpaEntity vehicle = vehicleRepository.findById(dto.getVehicleId()).orElse(null);

        if (vehicle == null || !vehicle.isActiu()) {
            return false;
        }

        dto.setClientId(usuari.getId());

        String nom = usuari.getNom() != null ? usuari.getNom() : "";

        if (usuari.getCognoms() != null && !usuari.getCognoms().isBlank()) {
            nom += " " + usuari.getCognoms();
        }

        model.addAttribute("esClient", true);
        model.addAttribute("nomClient", nom);
        model.addAttribute("emailClient", usuari.getEmail());
        model.addAttribute("vehicleSeleccionat", vehicle);
        model.addAttribute("avui", LocalDate.now());

        return true;
    }

    private UsuariJpaEntity usuariAutenticat(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return usuariRepository.findByEmail(authentication.getName()).orElse(null);
    }

    // ---------- Anul·lació: ACCEPTAR / ANUL·LAR (AGENT / ADMIN) ----------
    @PostMapping("/anullar/{id}")
    public String anullarReserva(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttrs) {
        try {
            if (!esPersonal(authentication)) {
                throw new Exception("Només un agent o administrador pot aprovar una anul·lació.");
            }

            solicitudAnulacioService.aprobarSolicitud(
                    id,
                    authentication.getName()
            );

            redirectAttrs.addFlashAttribute("missatge",
                    "Sol·licitud aprovada. La reserva ha estat anul·lada i s'ha enviat el correu al client.");

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/reserva/meves";
    }

    // ---------- Anul·lació: REBUTJAR (AGENT / ADMIN) ----------
    @PostMapping("/rebutjar-anulacio/{id}")
    public String rebutjarAnulacio(@PathVariable Long id,
                                   @RequestParam(required = false) String motivoRechazo,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttrs) {
        try {
            if (!esPersonal(authentication)) {
                throw new Exception("Només un agent o administrador pot rebutjar una anul·lació.");
            }

            solicitudAnulacioService.rechazarSolicitud(
                    id,
                    authentication.getName(),
                    motivoRechazo
            );

            redirectAttrs.addFlashAttribute("missatge",
                    "Sol·licitud rebutjada. La reserva continua activa i s'ha informat el client per correu.");

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/reserva/meves";
    }

    // ---------- Sol·licitud d'anul·lació (CLIENT) ----------
    @PostMapping("/solicitar-anulacio/{id}")
    public String solicitarAnulacio(@PathVariable Long id,
                                    @RequestParam("motivo") String motivo,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttrs) {
        try {
            if (!esNomesClient(authentication)) {
                throw new Exception("Només el client pot sol·licitar l'anul·lació; l'agent decideix.");
            }

            solicitudAnulacioService.crearSolicitud(
                    id,
                    authentication.getName(),
                    motivo
            );

            redirectAttrs.addFlashAttribute("missatge",
                    "La sol·licitud d'anul·lació s'ha registrat correctament i està pendent de revisió.");

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/reserva/meves";
    }

    // ---------- RF49 Lliurar ----------
    @GetMapping("/lliurar/{id}")
    public String mostrarFormulariLliurament(@PathVariable Long id,
                                             Model model,
                                             RedirectAttributes flash) {
        try {
            ReservaJpaEntity reserva = reservaService.buscarPerId(id);
            model.addAttribute("reserva", reserva);
            return "lliurar-vehicle";

        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/meves";
        }
    }

    @PostMapping("/lliurar/{id}")
    public String processarLliurament(@PathVariable Long id,
                                      @RequestParam(required = false) Integer kmsInicials,
                                      @RequestParam(required = false) String observacions,
                                      @RequestParam(required = false) MultipartFile foto1,
                                      @RequestParam(required = false) MultipartFile foto2,
                                      @RequestParam(required = false) MultipartFile foto3,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                      LocalDateTime dataHoraLliurament,
                                      RedirectAttributes flash) {
        try {
            reservaService.lliurarVehicle(id, kmsInicials, observacions,
                    foto1, foto2, foto3, dataHoraLliurament);

            flash.addFlashAttribute("missatge",
                    "Vehicle lliurat correctament! Estat: EN_CURS");

        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/lliurar/" + id;
        }

        return "redirect:/reserva/meves";
    }

    // ---------- RF48 Retornar ----------
    @GetMapping("/retornar/{id}")
    public String mostrarFormulariRetorn(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes flash) {
        try {
            ReservaJpaEntity reserva = reservaService.buscarPerId(id);

            if (reserva.getEstat() != EstatReserva.EN_CURS
                    || reserva.getEntrega() == null
                    || reserva.getEntrega().isRetornat()) {

                flash.addFlashAttribute("error",
                        "Aquesta reserva no es pot retornar (estat: " + reserva.getEstat() + ")");

                return "redirect:/reserva/meves";
            }

            LocalDateTime dataHoraFiPrevista =
                    LocalDateTime.of(reserva.getDataFi(), reserva.getHoraInici());

            model.addAttribute("reserva", reserva);
            model.addAttribute("kmsInicials", reserva.getEntrega().getQuilometresInicials());
            model.addAttribute("dataHoraFiPrevista", dataHoraFiPrevista);
            model.addAttribute("fiancaEstandard",
                    reserva.getVehicle() != null ? reserva.getVehicle().getFiancaEstandard() : null);

            return "retornar-vehicle";

        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/meves";
        }
    }

    @PostMapping("/retornar/{id}")
    public String processarRetorn(@PathVariable Long id,
                                  @RequestParam Integer quilometresFinals,
                                  @RequestParam(required = false) String observacionsRetorn,
                                  @RequestParam("foto1") MultipartFile foto1,
                                  @RequestParam(required = false) MultipartFile foto2,
                                  @RequestParam(required = false) MultipartFile foto3,
                                  @RequestParam("dataHoraRetorn")
                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                  LocalDateTime dataHoraRetorn,
                                  @RequestParam("danysImputablesClient") Boolean danysImputablesClient,
                                  RedirectAttributes flash) {
        try {
            reservaService.retornarVehicle(id, quilometresFinals,
                    observacionsRetorn, foto1, foto2, foto3,
                    dataHoraRetorn, danysImputablesClient);

            EntregaJpaEntity entrega = reservaRepository.findById(id)
                    .map(ReservaJpaEntity::getEntrega).orElse(null);

            String missatge = "Vehicle retornat correctament! Estat: FINALITZADA";

            if (entrega != null) {
                if (entrega.getHoresTarda() != null && entrega.getHoresTarda() > 0) {
                    missatge += " | Hores de tarda: " + entrega.getHoresTarda();
                }
                if (entrega.getImportFiancaRetinguda() != null
                        && entrega.getImportFiancaRetinguda().compareTo(BigDecimal.ZERO) > 0) {
                    missatge += " | Fiança retesa: " + entrega.getImportFiancaRetinguda() + " €";
                }
            }

            flash.addFlashAttribute("missatge", missatge);

        } catch (Exception e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/retornar/" + id;
        }

        return "redirect:/reserva/meves";
    }

    // ---------- RF54 Llistat / filtres ----------
    @GetMapping("/meves")
    public String llistarReserves(
            @RequestParam(required = false) String codi,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String dni,
            Authentication authentication,
            Model model) {

        if (codi != null && codi.isBlank()) {
            codi = null;
        }
        if (matricula != null && matricula.isBlank()) {
            matricula = null;
        }
        if (email != null && email.isBlank()) {
            email = null;
        }
        if (dni != null && dni.isBlank()) {
            dni = null;
        }

        if (esNomesClient(authentication)) {
            email = authentication.getName();
            dni = null;
        }

        boolean senseFiltres = codi == null && data == null
                && matricula == null && email == null && dni == null;

        List<ReservaJpaEntity> llista;

        if (senseFiltres) {
            llista = reservaService.obtenirTotes();
        } else {
            llista = reservaService.filtrarReserves(codi, data, matricula, email, dni);
        }

        model.addAttribute("reserves",
                llista.stream().filter(r -> r != null).toList());
        model.addAttribute("codi", codi);
        model.addAttribute("data", data);
        model.addAttribute("matricula", matricula);
        model.addAttribute("email", email);
        model.addAttribute("dni", dni);

        if (esPersonal(authentication)) {
            model.addAttribute(
                    "solicitudAnulacions",
                    solicitudAnulacioService.obtenerPendientes()
            );
        }

        return "meves-reserves";
    }

    private boolean esNomesClient(Authentication authentication) {
        if (authentication == null || esPersonal(authentication)) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_CLIENT".equals(authority.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    private boolean esPersonal(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String rol = authority.getAuthority();

            if ("ROLE_AGENT".equals(rol)
                    || "ROLE_ADMIN".equals(rol)
                    || "ROLE_ADMINISTRADOR".equals(rol)) {
                return true;
            }
        }

        return false;
    }

    // ---------- RF91 Informe de reserves per client (històric MongoDB) ----------

    @GetMapping("/informe")
    public String seleccionarClientsInforme(Model model) {
        model.addAttribute("clients", usuariRepository.findByRol("CLIENT"));
        return "informe-seleccio";
    }

    @GetMapping("/informe/resultat")
    public String resultatInforme(
            @RequestParam(value = "clientIds", required = false) List<Long> clientIds,
            @RequestParam(value = "tots", required = false) Boolean tots,
            Model model,
            RedirectAttributes redirectAttrs) {

        try {
            boolean totsClients = Boolean.TRUE.equals(tots)
                    || clientIds == null
                    || clientIds.isEmpty();

            List<HistoricReservaDocument> reserves = totsClients
                    ? reservaService.obtenirInformeHistoric(null)
                    : reservaService.obtenirInformeHistoric(clientIds);

            long totalReserves = totsClients
                    ? reservaService.comptarReservesHistoric(null)
                    : reservaService.comptarReservesHistoric(clientIds);

            double preuTotal = totsClients
                    ? reservaService.totalImportHistoric(null)
                    : reservaService.totalImportHistoric(clientIds);

            model.addAttribute("reserves", reserves);
            model.addAttribute("totalReserves", totalReserves);
            model.addAttribute("preuTotal", preuTotal);
            model.addAttribute("totsClients", totsClients);
            if (!totsClients) {
                model.addAttribute("clientIdsSeleccionats", clientIds);
            }
            return "informe-client";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/informe";
        }
    }

    @GetMapping("/informe-client/{clientId}")
    public String veureInformeClient(@PathVariable Long clientId,
                                     Model model,
                                     RedirectAttributes redirectAttrs) {
        try {
            UsuariJpaEntity client = usuariRepository.findById(clientId)
                    .orElseThrow(() -> new Exception("Client no trobat."));

            List<HistoricReservaDocument> reserves = reservaService.obtenirHistoricClient(clientId);

            model.addAttribute("client", client);
            model.addAttribute("reserves", reserves);
            model.addAttribute("historicReserves", reserves);
            model.addAttribute("totalReserves", reservaService.comptarReservesClient(clientId));
            model.addAttribute("preuTotal", reservaService.totalImportHistoric(List.of(clientId)));
            model.addAttribute("totsClients", false);
            model.addAttribute("clientIdsSeleccionats", List.of(clientId));

            return "informe-client";

        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/reserva/meves";
        }
    }

    // ---------- ENDPOINTS FOTOS ----------
    @GetMapping("/entrega/{id}/foto/{num}")
    public ResponseEntity<byte[]> fotoLliurament(@PathVariable Long id,
                                                  @PathVariable int num) {
        return reservaRepository.findById(id)
                .map(ReservaJpaEntity::getEntrega)
                .filter(e -> e != null)
                .<ResponseEntity<byte[]>>map((EntregaJpaEntity e) -> {
                    byte[] dades = switch (num) {
                        case 1 -> e.getFotoLliurament1();
                        case 2 -> e.getFotoLliurament2();
                        case 3 -> e.getFotoLliurament3();
                        default -> null;
                    };
                    String tipus = switch (num) {
                        case 1 -> e.getFotoLliurament1Tipus();
                        case 2 -> e.getFotoLliurament2Tipus();
                        case 3 -> e.getFotoLliurament3Tipus();
                        default -> null;
                    };
                    return (dades != null && tipus != null)
                            ? ResponseEntity.ok()
                                    .contentType(MediaType.parseMediaType(tipus))
                                    .body(dades)
                            : ResponseEntity.<byte[]>notFound().build();
                })
                .orElseGet(() -> ResponseEntity.<byte[]>notFound().build());
    }

    @GetMapping("/entrega/{id}/foto-retorn/{num}")
    public ResponseEntity<byte[]> fotoRetorn(@PathVariable Long id,
                                              @PathVariable int num) {
        return reservaRepository.findById(id)
                .map(ReservaJpaEntity::getEntrega)
                .filter(e -> e != null && e.isRetornat())
                .<ResponseEntity<byte[]>>map((EntregaJpaEntity e) -> {
                    byte[] dades = switch (num) {
                        case 1 -> e.getFotoRetorn1();
                        case 2 -> e.getFotoRetorn2();
                        case 3 -> e.getFotoRetorn3();
                        default -> null;
                    };
                    String tipus = switch (num) {
                        case 1 -> e.getFotoRetorn1Tipus();
                        case 2 -> e.getFotoRetorn2Tipus();
                        case 3 -> e.getFotoRetorn3Tipus();
                        default -> null;
                    };
                    return (dades != null && tipus != null)
                            ? ResponseEntity.ok()
                                    .contentType(MediaType.parseMediaType(tipus))
                                    .body(dades)
                            : ResponseEntity.<byte[]>notFound().build();
                })
                .orElseGet(() -> ResponseEntity.<byte[]>notFound().build());
    }
}