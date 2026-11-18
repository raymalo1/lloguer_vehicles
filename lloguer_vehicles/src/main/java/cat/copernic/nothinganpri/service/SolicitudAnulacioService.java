
package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.entity.ReservaJpaEntity;
import cat.copernic.nothinganpri.entity.SolicitudAnulacioJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.repository.SolicitudAnulacioRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitudAnulacioService {

    private static final ZoneId ZONA_ESPANYA = ZoneId.of("Europe/Madrid");

    @Autowired
    private SolicitudAnulacioRepository solicitudRepository;

    @Autowired
    private ReservaJpaRepository reservaRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public SolicitudAnulacioJpaEntity crearSolicitud(
            Long reservaId,
            String emailClient,
            String motivo) throws Exception {

        if (motivo == null || motivo.isBlank()) {
            throw new Exception("El motivo de la anulación es obligatorio.");
        }

        if (motivo.trim().length() > 1000) {
            throw new Exception("El motivo no puede superar los 1000 caracteres.");
        }

        ReservaJpaEntity reserva = reservaService.buscarPerId(reservaId);

        if (reserva.getClient() == null
                || reserva.getClient().getEmail() == null
                || !reserva.getClient().getEmail().equalsIgnoreCase(emailClient)) {
            throw new Exception("Solo puedes solicitar la anulación de tus propias reservas.");
        }

        if (reserva.getEstat() != EstatReserva.ACTIVA
                && reserva.getEstat() != EstatReserva.PENDENT) {
            throw new Exception("Esta reserva ya no se puede anular.");
        }

        if (reserva.isAnulacioSolicitada()
                || solicitudRepository.existsByReserva_IdAndEstado(
                        reservaId, "PENDIENTE")) {
            throw new Exception("Ya existe una solicitud de anulación pendiente para esta reserva.");
        }

        if (reserva.getDataInici() == null || reserva.getHoraInici() == null) {
            throw new Exception("No se puede calcular la devolución porque falta la fecha o la hora de recogida.");
        }

        if (reserva.getImportTotal() == null || reserva.getImportTotal() < 0) {
            throw new Exception("El importe total de la reserva no es válido.");
        }

        if (emailClient == null || emailClient.isBlank()) {
            throw new Exception("No se ha podido identificar el correo del cliente.");
        }

        LocalDateTime fechaHoraRecogida = LocalDateTime.of(
                reserva.getDataInici(),
                reserva.getHoraInici()
        );

        LocalDateTime ahora = LocalDateTime.now(ZONA_ESPANYA);

        Duration tiempoRestante = Duration.between(
                ahora,
                fechaHoraRecogida
        );

        double porcentajeDevolucion =
                tiempoRestante.compareTo(Duration.ofHours(24)) >= 0
                        ? 100.0 : 0.0;

        double importeTotal = reserva.getImportTotal();

        double importeDevolucion = Math.round(
                importeTotal * porcentajeDevolucion / 100.0 * 100.0
        ) / 100.0;

        double importeRetenido = Math.round(
                (importeTotal - importeDevolucion) * 100.0
        ) / 100.0;

        SolicitudAnulacioJpaEntity solicitud =
                new SolicitudAnulacioJpaEntity();

        solicitud.setReserva(reserva);
        solicitud.setMotivo(motivo.trim());
        solicitud.setEstado("PENDIENTE");
        solicitud.setFechaSolicitud(ahora);
        solicitud.setPorcentajeDevolucion(porcentajeDevolucion);
        solicitud.setImporteDevolucion(importeDevolucion);
        solicitud.setImporteRetenido(importeRetenido);

        SolicitudAnulacioJpaEntity solicitudGuardada =
                solicitudRepository.save(solicitud);

        reserva.setAnulacioSolicitada(true);
        reservaRepository.save(reserva);

        return solicitudGuardada;
    }

    @Transactional(readOnly = true)
    public List<SolicitudAnulacioJpaEntity> obtenerPendientes() {
        return solicitudRepository.findByEstadoOrderByFechaSolicitudAsc(
                "PENDIENTE"
        );
    }

    @Transactional(readOnly = true)
    public List<SolicitudAnulacioJpaEntity> obtenerPorReserva(Long reservaId) {
        return solicitudRepository.findByReserva_IdOrderByFechaSolicitudDesc(
                reservaId
        );
    }

    @Transactional
    public void aprobarSolicitud(
            Long solicitudId,
            String revisorEmail) throws Exception {

        SolicitudAnulacioJpaEntity solicitud = solicitudRepository.findById(
                solicitudId
        ).orElseThrow(() -> new Exception(
                "No se ha encontrado la solicitud de anulación."
        ));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new Exception("Esta solicitud ya ha sido revisada.");
        }

        ReservaJpaEntity reserva = solicitud.getReserva();

        reservaService.confirmarAnulacioSolicitud(
                reserva.getId(),
                solicitud.getPorcentajeDevolucion(),
                solicitud.getImporteDevolucion(),
                solicitud.getImporteRetenido()
        );

        solicitud.setEstado("APROBADA");
        solicitud.setFechaRevision(LocalDateTime.now(ZONA_ESPANYA));
        solicitud.setRevisorEmail(revisorEmail);

        solicitudRepository.save(solicitud);
    }

    @Transactional
    public void rechazarSolicitud(
            Long solicitudId,
            String revisorEmail,
            String motivoRechazo) throws Exception {

        SolicitudAnulacioJpaEntity solicitud = solicitudRepository.findById(
                solicitudId
        ).orElseThrow(() -> new Exception(
                "No se ha encontrado la solicitud de anulación."
        ));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new Exception("Esta solicitud ya ha sido revisada.");
        }

        ReservaJpaEntity reserva = solicitud.getReserva();

        if (reserva.getClient() == null
                || reserva.getClient().getEmail() == null
                || reserva.getClient().getEmail().isBlank()) {
            throw new Exception("La reserva no tiene un correo electrónico de cliente válido.");
        }

        solicitud.setEstado("RECHAZADA");
        solicitud.setFechaRevision(LocalDateTime.now(ZONA_ESPANYA));
        solicitud.setRevisorEmail(revisorEmail);
        solicitud.setMotivoRechazo(
                motivoRechazo == null || motivoRechazo.isBlank()
                        ? null : motivoRechazo.trim()
        );

        solicitudRepository.save(solicitud);

        reserva.setAnulacioSolicitada(false);
        reservaRepository.save(reserva);

        String asunto = "Solicitud de anulación rechazada - Reserva "
                + reserva.getCodiReserva();

        String contenido = "Hola,\n\n"
                + "Te informamos de que tu solicitud de anulación de la reserva "
                + reserva.getCodiReserva()
                + " ha sido rechazada.\n\n"
                + "La reserva continúa activa.\n\n"
                + (solicitud.getMotivoRechazo() != null
                        ? "Motivo del rechazo: " + solicitud.getMotivoRechazo() + "\n\n"
                        : "")
                + "Puedes volver a solicitar la anulación si lo necesitas.\n\n"
                + "Gracias.";

        emailService.enviarCorreo(
                reserva.getClient().getEmail(),
                asunto,
                contenido
        );
    }
}
