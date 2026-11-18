
package cat.copernic.nothinganpri.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_anulacion")
public class SolicitudAnulacioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private ReservaJpaEntity reserva;

    @Column(nullable = false, length = 1000)
    private String motivo;

    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaRevision;

    @Column(length = 255)
    private String revisorEmail;

    @Column(length = 1000)
    private String motivoRechazo;

    @Column(nullable = false)
    private Double porcentajeDevolucion;

    @Column(nullable = false)
    private Double importeDevolucion;

    @Column(nullable = false)
    private Double importeRetenido;

    public SolicitudAnulacioJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReservaJpaEntity getReserva() {
        return reserva;
    }

    public void setReserva(ReservaJpaEntity reserva) {
        this.reserva = reserva;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(LocalDateTime fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getRevisorEmail() {
        return revisorEmail;
    }

    public void setRevisorEmail(String revisorEmail) {
        this.revisorEmail = revisorEmail;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public Double getPorcentajeDevolucion() {
        return porcentajeDevolucion;
    }

    public void setPorcentajeDevolucion(Double porcentajeDevolucion) {
        this.porcentajeDevolucion = porcentajeDevolucion;
    }

    public Double getImporteDevolucion() {
        return importeDevolucion;
    }

    public void setImporteDevolucion(Double importeDevolucion) {
        this.importeDevolucion = importeDevolucion;
    }

    public Double getImporteRetenido() {
        return importeRetenido;
    }

    public void setImporteRetenido(Double importeRetenido) {
        this.importeRetenido = importeRetenido;
    }
}
