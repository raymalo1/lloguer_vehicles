package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.dto.FitxerPujat;
import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.TipusCombustible;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.repository.VehicleJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lògica de negoci dels vehicles (RF40 alta, RF41 baixa, RF42 modificació, RF46 activar, RF47 desactivar).
 * Normalitza i valida les dades abans de desar-les a la base de dades (RN20).
 */
@Service
public class VehicleService {

    /** Matrícula española nueva: 4 dígitos + 3 consonantes (sin vocales ni Ñ/Q). */
    private static final Pattern PATRO_MATRICULA = Pattern.compile(
            "^[0-9]{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");

    private final VehicleJpaRepository repo;
    private final ReservaJpaRepository reservaRepo;
    private final LogQueryService logQueryService;

    public VehicleService(
            VehicleJpaRepository repo,
            ReservaJpaRepository reservaRepo,
            LogQueryService logQueryService) {
        this.repo = repo;
        this.reservaRepo = reservaRepo;
        this.logQueryService = logQueryService;
    }

    /** Desa el vehicle sense canviar la seva foto. */
    @Transactional
    public VehicleJpaEntity guardar(VehicleJpaEntity v) {
        return guardar(v, null);
    }

    /**
     * Desa el vehicle. Si s'indica una foto nova, es guarda al mateix vehicle (a la base de dades)
     * i substitueix l'anterior; si no, el vehicle conserva la foto que ja tenia.
     */
    @Transactional
    public VehicleJpaEntity guardar(VehicleJpaEntity v, MultipartFile fitxerFoto) {
        if (v != null && v.getMatricula() != null) {
            v.setMatricula(normalitzarMatricula(v.getMatricula()));
        }
        validar(v);

        Optional<VehicleJpaEntity> existent = repo.findByMatricula(v.getMatricula());
        if (existent.isPresent() && !existent.get().getId().equals(v.getId())) {
            logQueryService.registrarWarning("VEHICLE", "Intent de guardar un vehicle amb una matrícula ja existent.");
            throw new IllegalArgumentException("Ja existeix un vehicle amb la matrícula " + v.getMatricula());
        }

        if (fitxerFoto != null && !fitxerFoto.isEmpty()) {
            try {
                FitxerPujat foto = ImatgeUtil.llegir(fitxerFoto);
                v.setFoto(foto.getDades());
                v.setFotoContentType(foto.getContentType());
            } catch (RuntimeException e) {
                logQueryService.registrarError("VEHICLE", "Error en llegir la fotografia del vehicle: " + e.getMessage());
                throw e;
            }
        } else if (v.getId() != null) {
            repo.findById(v.getId()).ifPresent(actual -> {
                v.setFoto(actual.getFoto());
                v.setFotoContentType(actual.getFotoContentType());
            });
        }

        try {
            VehicleJpaEntity vehicleGuardat = repo.save(v);
            logQueryService.registrarInfo("VEHICLE", "Vehicle guardat correctament.");
            return vehicleGuardat;
        } catch (RuntimeException e) {
            logQueryService.registrarError("VEHICLE", "Error en guardar el vehicle: " + e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<VehicleJpaEntity> filtrar(String matricula, String marca, String model, Integer any,
                                          LocalDate dataInici, LocalDate dataFi) {
        return filtrar(matricula, marca, model, any, dataInici, dataFi, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<VehicleJpaEntity> filtrar(String matricula, String marca, String model, Integer any,
                                          LocalDate dataInici, LocalDate dataFi,
                                          List<CategoriaVehicle> categories,
                                          List<TipusCombustible> combustibles,
                                          BigDecimal preuMin, BigDecimal preuMax) {

        exigir(preuMin == null || preuMax == null || preuMin.compareTo(preuMax) <= 0,
                "El preu mínim no pot ser superior al preu màxim");

        boolean ambDates = dataInici != null || dataFi != null;
        if (ambDates) {
            exigir(dataInici != null && dataFi != null, "Cal indicar la data d'inici i la data final");
            exigir(!dataInici.isAfter(dataFi), "La data d'inici no pot ser posterior a la data final");
        }

        return repo.findAll().stream()
                .filter(VehicleJpaEntity::isActiu)
                .filter(v -> conte(v.getMatricula(), matricula))
                .filter(v -> conte(v.getMarca(), marca))
                .filter(v -> conte(v.getModel(), model))
                .filter(v -> any == null || any.equals(v.getAnyMatriculacio()))
                .filter(v -> categories == null || categories.isEmpty() || categories.contains(v.getCategoria()))
                .filter(v -> combustibles == null || combustibles.isEmpty() || combustibles.contains(v.getCombustible()))
                .filter(v -> preuMin == null || (v.getPreuHora() != null && v.getPreuHora().compareTo(preuMin) >= 0))
                .filter(v -> preuMax == null || (v.getPreuHora() != null && v.getPreuHora().compareTo(preuMax) <= 0))
                .filter(v -> !ambDates
                        || reservaRepo.countConflictingReserves(v.getId(), dataInici, dataFi) == 0)
                .toList();
    }

    private boolean conte(String camp, String filtre) {
        if (esBuit(filtre)) {
            return true;
        }
        return camp != null && camp.toLowerCase().contains(filtre.trim().toLowerCase());
    }

    @Transactional
    public VehicleJpaEntity activar(Long id, String motiu, BigDecimal importActivacio) {
        VehicleJpaEntity vehicle = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle no trobat amb ID: " + id));

        if (vehicle.isActiu()) {
            logQueryService.registrarWarning("VEHICLE", "Intent d'activar un vehicle que ja estava actiu.");
            throw new IllegalStateException("El vehicle ja està actiu");
        }

        vehicle.setActiu(true);
        vehicle.setDataFiDesactivacio(null);
        vehicle.setDataFiDesactivacioPrevista(null);

        // SOLO campos específicos (RF46) - NO comentariPrivat
        vehicle.setMotiuUltimaActivacio(motiu);
        vehicle.setDataUltimaActivacio(LocalDate.now());
        vehicle.setImportUltimaActivacio(importActivacio);

        try {
            VehicleJpaEntity vehicleGuardat = repo.save(vehicle);
            logQueryService.registrarInfo("VEHICLE", "Vehicle activat correctament.");
            return vehicleGuardat;
        } catch (RuntimeException e) {
            logQueryService.registrarError("VEHICLE", "Error en activar el vehicle: " + e.getMessage());
            throw e;
        }
    }


    @Transactional
    public boolean donarDeBaixa(Long id) {
        VehicleJpaEntity vehicle = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El vehicle no existeix"));

        if (!reservaRepo.existsByVehicleId(id)) {
            try {
                repo.delete(vehicle);
                logQueryService.registrarInfo("VEHICLE", "Vehicle eliminat definitivament.");
                return true;
            } catch (RuntimeException e) {
                logQueryService.registrarError("VEHICLE", "Error en eliminar definitivament el vehicle: " + e.getMessage());
                throw e;
            }
        }

        boolean teReservesBloquejadores = reservaRepo.existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(
                id,
                List.of(EstatReserva.ACTIVA, EstatReserva.PENDENT, EstatReserva.EN_CURS),
                LocalDate.now()
        );

        if (teReservesBloquejadores) {
            logQueryService.registrarWarning("VEHICLE", "Baixa de vehicle bloquejada perquè té reserves actives, pendents o en curs.");
            throw new IllegalArgumentException(
                "No es pot donar de baixa el vehicle " + vehicle.getMatricula() +
                " perquè té reserves en curs, actives o pendents. " +
                "Les reserves finalitzades o anul·lades NO bloquegen la baixa."
            );
        }

        vehicle.setActiu(false);
        vehicle.setDataFiDesactivacio(LocalDate.now());

        try {
            repo.save(vehicle);
            logQueryService.registrarInfo("VEHICLE", "Vehicle donat de baixa lògicament.");
            return false;
        } catch (RuntimeException e) {
            logQueryService.registrarError("VEHICLE", "Error en donar de baixa el vehicle: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public VehicleJpaEntity desactivar(Long id, String motiu, LocalDate dataFiDesactivacio) {
        VehicleJpaEntity vehicle = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle no trobat"));

        if (!vehicle.isActiu()) {
            logQueryService.registrarWarning("VEHICLE", "Intent de desactivar un vehicle que ja estava inactiu.");
            throw new IllegalStateException("El vehicle ja està inactiu");
        }

        LocalDate iniciDesactivacio = LocalDate.now();
        List<EstatReserva> estatsBloquejadors =
                List.of(EstatReserva.ACTIVA, EstatReserva.PENDENT, EstatReserva.EN_CURS);

        boolean teReservesConflicte;
        if (dataFiDesactivacio != null) {
            // Desactivació amb data de fi: bloqueja només les reserves que se solapen amb el període
            teReservesConflicte = reservaRepo
                    .existsByVehicleIdAndEstatInAndDataIniciLessThanEqualAndDataFiGreaterThanEqual(
                            id, estatsBloquejadors, dataFiDesactivacio, iniciDesactivacio);
        } else {
            // Desactivació indefinida: bloqueja qualsevol reserva que encara no hagi acabat
            teReservesConflicte = reservaRepo
                    .existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(
                            id, estatsBloquejadors, iniciDesactivacio);
        }

        if (teReservesConflicte) {
            logQueryService.registrarWarning("VEHICLE", "Desactivació bloquejada: vehicle amb reserves en el període");
            throw new IllegalArgumentException(
                    "No es pot desactivar el vehicle " + vehicle.getMatricula() +
                    " perquè té reserves actives, pendents o en curs durant el període de desactivació."
            );
        }

        vehicle.setActiu(false);
        vehicle.setDataFiDesactivacio(dataFiDesactivacio);
        vehicle.setDataFiDesactivacioPrevista(dataFiDesactivacio);

        // SOLO campos específicos (RF47) - NO comentariPrivat
        vehicle.setMotiuUltimaDesactivacio(motiu);
        vehicle.setDataUltimaDesactivacio(LocalDate.now());

        try {
            VehicleJpaEntity vehicleGuardat = repo.save(vehicle);
            logQueryService.registrarInfo("VEHICLE", "Vehicle desactivat temporalment.");
            return vehicleGuardat;
        } catch (RuntimeException e) {
            logQueryService.registrarError("VEHICLE", "Error en desactivar el vehicle: " + e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public java.util.Set<Long> idsVehiclesAmbReserves() {
        return reservaRepo.findIdsVehiclesAmbReserves();
    }

    @Transactional(readOnly = true)
    public List<VehicleJpaEntity> llistar() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<VehicleJpaEntity> buscarPerId(Long id) {
        return repo.findById(id);
    }

    private String normalitzarMatricula(String matricula) {
        return matricula.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private void validar(VehicleJpaEntity v) {
        exigir(v != null, "El vehicle no pot ser buit");

        exigir(!esBuit(v.getMatricula()), "La matrícula és obligatòria");
        exigir(PATRO_MATRICULA.matcher(v.getMatricula()).matches(),
                "La matrícula ha de ser del format espanyol nou: 4 xifres i 3 consonants "
                        + "(ex. 1234BCD). Sense vocals ni Ñ/Q.");

        exigir(!esBuit(v.getMarca()), "La marca és obligatòria");
        exigir(v.getMarca().length() <= 50, "La marca pot tenir com a màxim 50 caràcters");
        exigir(!esBuit(v.getModel()), "El model és obligatori");
        exigir(v.getModel().length() <= 50, "El model pot tenir com a màxim 50 caràcters");

        int anyMaxim = Year.now().getValue() + 1;
        exigir(v.getAnyMatriculacio() != null, "L'any de matriculació és obligatori");
        exigir(v.getAnyMatriculacio() >= 1900 && v.getAnyMatriculacio() <= anyMaxim,
                "L'any de matriculació ha d'estar entre 1900 i " + anyMaxim);

        exigir(v.getCategoria() != null, "La categoria és obligatòria");
        exigir(v.getCombustible() != null, "Cal triar un tipus de combustible");
        exigir(v.getColor() == null || v.getColor().length() <= 30,
                "El color pot tenir com a màxim 30 caràcters");
        exigir(v.getPotenciaCv() == null || v.getPotenciaCv() >= 1,
                "La potència ha de ser major que zero");
        exigir(v.getPotenciaCv() == null || v.getPotenciaCv() <= 2000,
                "La potència no pot superar els 2000 CV");

        exigir(v.getPlaces() != null, "El nombre de places és obligatori");
        exigir(v.getPlaces() >= 1, "El nombre de places ha de ser com a mínim 1");
        exigir(v.getPlaces() <= 9, "El nombre de places no pot superar 9");

        exigir(v.getPreuHora() != null && v.getPreuHora().compareTo(BigDecimal.ZERO) > 0,
                "El preu per hora ha de ser major que zero");
        exigir(v.getFiancaEstandard() != null, "La fiança és obligatòria");
        exigir(v.getFiancaEstandard().compareTo(BigDecimal.ZERO) >= 0, "La fiança no pot ser negativa");

        exigir(v.getMinDies() != null, "Els dies mínims de lloguer són obligatoris");
        exigir(v.getMinDies() >= 1, "El mínim de dies de lloguer és 1");
        exigir(v.getMaxDies() == null || v.getMaxDies() >= v.getMinDies(),
                "El màxim de dies no pot ser menor que el mínim");
        exigir(v.getLimitQuilometres() == null || v.getLimitQuilometres() >= 1,
                "El límit de quilòmetres ha de ser major que zero");

        // ===== RF40: FECHA DE FIN DE DESACTIVACIÓN =====
        if (v.isActiu()) {
            v.setDataFiDesactivacio(null);
        } else if (v.getDataFiDesactivacio() != null) {
            exigir(!v.getDataFiDesactivacio().isBefore(LocalDate.now()),
                    "La data de fi de desactivació no pot ser anterior a avui");
        }
        // ===============================================

        exigir(v.getComentariPrivat() == null || v.getComentariPrivat().length() <= 2000,
                "El comentari privat pot tenir com a màxim 2000 caràcters");
    }

    private void exigir(boolean condicio, String missatge) {
        if (!condicio) {
            logQueryService.registrarWarning("VEHICLE", "Validació de vehicle no superada.");
            throw new IllegalArgumentException(missatge);
        }
    }

    private boolean esBuit(String s) {
        return s == null || s.trim().isEmpty();
    }
}