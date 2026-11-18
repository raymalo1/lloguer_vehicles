
package cat.copernic.nothinganpri.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_validacion")
public class SolicitudValidacionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClientJpaEntity cliente;

    // REGISTRO, DATOS_PERSONALES, DOCUMENTACION o TARJETA
    @Column(nullable = false, length = 40)
    private String tipo;

    // PENDIENTE, APROBADA o RECHAZADA
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    // Datos propuestos en formato JSON.
    // No guardar aquí el CVV ni el número completo de la tarjeta.
    @Lob
    @Column(name = "datos_propuestos")
    private String datosPropuestos;

    // Archivos nuevos propuestos, si los hay.
    @Lob
    @Column(name = "documento_dades")
    private byte[] documento;

    @Column(name = "documento_tipus", length = 100)
    private String documentoTipo;

    @Column(name = "documento_nom", length = 255)
    private String documentoNombre;

    @Column(name = "data_solicitud", nullable = false)
    private LocalDateTime dataSolicitud = LocalDateTime.now();

    @Column(name = "data_revisio")
    private LocalDateTime dataRevisio;

    @Column(name = "revisor_email", length = 255)
    private String revisorEmail;

    @Column(name = "motiu_rebuig", length = 1000)
    private String motiuRebuig;

    /**
     * Constructor vacío.
     */
    public SolicitudValidacionJpaEntity() {
    }

    /**
     * Obtiene id.
     *
     * @return valor de id
     */
    public Long getId() {
        return id;
    }

    /**
     * Obtiene cliente.
     *
     * @return valor de cliente
     */
    public ClientJpaEntity getCliente() {
        return cliente;
    }

    /**
     * Establece cliente.
     *
     * @param cliente nuevo valor de cliente
     */
    public void setCliente(ClientJpaEntity cliente) {
        this.cliente = cliente;
    }

    /**
     * Obtiene tipo.
     *
     * @return valor de tipo
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Establece tipo.
     *
     * @param tipo nuevo valor de tipo
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Obtiene estado.
     *
     * @return valor de estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Establece estado.
     *
     * @param estado nuevo valor de estado
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene datosPropuestos.
     *
     * @return valor de datosPropuestos
     */
    public String getDatosPropuestos() {
        return datosPropuestos;
    }

    /**
     * Establece datosPropuestos.
     *
     * @param datosPropuestos nuevo valor de datosPropuestos
     */
    public void setDatosPropuestos(String datosPropuestos) {
        this.datosPropuestos = datosPropuestos;
    }

    /**
     * Obtiene documento.
     *
     * @return valor de documento
     */
    public byte[] getDocumento() {
        return documento;
    }

    /**
     * Establece documento.
     *
     * @param documento nuevo valor de documento
     */
    public void setDocumento(byte[] documento) {
        this.documento = documento;
    }

    /**
     * Obtiene documentoTipo.
     *
     * @return valor de documentoTipo
     */
    public String getDocumentoTipo() {
        return documentoTipo;
    }

    /**
     * Establece documentoTipo.
     *
     * @param documentoTipo nuevo valor de documentoTipo
     */
    public void setDocumentoTipo(String documentoTipo) {
        this.documentoTipo = documentoTipo;
    }

    /**
     * Obtiene documentoNombre.
     *
     * @return valor de documentoNombre
     */
    public String getDocumentoNombre() {
        return documentoNombre;
    }

    /**
     * Establece documentoNombre.
     *
     * @param documentoNombre nuevo valor de documentoNombre
     */
    public void setDocumentoNombre(String documentoNombre) {
        this.documentoNombre = documentoNombre;
    }

    /**
     * Obtiene dataSolicitud.
     *
     * @return valor de dataSolicitud
     */
    public LocalDateTime getDataSolicitud() {
        return dataSolicitud;
    }

    /**
     * Establece dataSolicitud.
     *
     * @param dataSolicitud nuevo valor de dataSolicitud
     */
    public void setDataSolicitud(LocalDateTime dataSolicitud) {
        this.dataSolicitud = dataSolicitud;
    }

    /**
     * Obtiene dataRevisio.
     *
     * @return valor de dataRevisio
     */
    public LocalDateTime getDataRevisio() {
        return dataRevisio;
    }

    /**
     * Establece dataRevisio.
     *
     * @param dataRevisio nuevo valor de dataRevisio
     */
    public void setDataRevisio(LocalDateTime dataRevisio) {
        this.dataRevisio = dataRevisio;
    }

    /**
     * Obtiene revisorEmail.
     *
     * @return valor de revisorEmail
     */
    public String getRevisorEmail() {
        return revisorEmail;
    }

    /**
     * Establece revisorEmail.
     *
     * @param revisorEmail nuevo valor de revisorEmail
     */
    public void setRevisorEmail(String revisorEmail) {
        this.revisorEmail = revisorEmail;
    }

    /**
     * Obtiene motiuRebuig.
     *
     * @return valor de motiuRebuig
     */
    public String getMotiuRebuig() {
        return motiuRebuig;
    }

    /**
     * Establece motiuRebuig.
     *
     * @param motiuRebuig nuevo valor de motiuRebuig
     */
    public void setMotiuRebuig(String motiuRebuig) {
        this.motiuRebuig = motiuRebuig;
    }
}
