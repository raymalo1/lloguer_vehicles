package cat.copernic.nothinganpri.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO para la creación de una reserva.
 * Contiene las fechas, el vehículo, el cliente y los importes calculados.
 */
public class CreateReservaRequest {

    /**
     * Fecha de inicio de la reserva.
     * Formato ISO (yyyy-MM-dd) para &lt;input type="date"&gt;.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInici;

    /** Hora de recogida del vehículo. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaInici;

    /** Fecha de fin de la reserva. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFi;

    public LocalTime getHoraFi() {
        return horaFi;
    }

    public void setHoraFi(LocalTime horaFi) {
        this.horaFi = horaFi;
    }

    public Boolean getAcceptaDocumentacio() {
        return acceptaDocumentacio;
    }

    public void setAcceptaDocumentacio(Boolean acceptaDocumentacio) {
        this.acceptaDocumentacio = acceptaDocumentacio;
    }
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaFi;

    private Boolean acceptaDocumentacio;
    /** Identificador del vehículo reservado. */
    private Long vehicleId;

    /** Identificador del cliente que realiza la reserva. */
    private Long clientId;

    /** Subtotal de la reserva (sin fianza). */
    private Double subtotal;

    /** Importe de la fianza. */
    private Double fianca;

    /** Importe total (subtotal + fianza). */
    private Double total;

    /** Fianza original sin descuento (antes del 25% premium). */
    private Double fiancaOriginal;

    /** true si se ha aplicado el descuento premium del 25% a la fianza. */
    private Boolean descomptePremium;


    /** Total sin descuento (subtotal + fianza original). */
    private Double totalSenseDescompte;
    /**
     * Obtiene la fecha de inicio de la reserva.
     *
     * @return fecha de inicio
     */
    public LocalDate getDataInici() {
        return dataInici;
    }

    /**
     * Establece la fecha de inicio de la reserva.
     *
     * @param dataInici fecha de inicio
     */
    public void setDataInici(LocalDate dataInici) {
        this.dataInici = dataInici;
    }

    /**
     * Obtiene la hora de recogida del vehículo.
     *
     * @return hora de inicio
     */
    public LocalTime getHoraInici() {
        return horaInici;
    }

    /**
     * Establece la hora de recogida del vehículo.
     *
     * @param horaInici hora de inicio
     */
    public void setHoraInici(LocalTime horaInici) {
        this.horaInici = horaInici;
    }

    /**
     * Obtiene la fecha de fin de la reserva.
     *
     * @return fecha de fin
     */
    public LocalDate getDataFi() {
        return dataFi;
    }

    /**
     * Establece la fecha de fin de la reserva.
     *
     * @param dataFi fecha de fin
     */
    public void setDataFi(LocalDate dataFi) {
        this.dataFi = dataFi;
    }

    /**
     * Obtiene el identificador del vehículo.
     *
     * @return id del vehículo
     */
    public Long getVehicleId() {
        return vehicleId;
    }

    /**
     * Establece el identificador del vehículo.
     *
     * @param vehicleId id del vehículo
     */
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Obtiene el identificador del cliente.
     *
     * @return id del cliente
     */
    public Long getClientId() {
        return clientId;
    }

    /**
     * Establece el identificador del cliente.
     *
     * @param clientId id del cliente
     */
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    /**
     * Obtiene el subtotal de la reserva.
     *
     * @return subtotal
     */
    public Double getSubtotal() {
        return subtotal;
    }

    /**
     * Establece el subtotal de la reserva.
     *
     * @param subtotal subtotal
     */
    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * Obtiene el importe de la fianza.
     *
     * @return fianza
     */
    public Double getFianca() {
        return fianca;
    }

    /**
     * Establece el importe de la fianza.
     *
     * @param fianca fianza
     */
    public void setFianca(Double fianca) {
        this.fianca = fianca;
    }

    /**
     * Obtiene el importe total de la reserva.
     *
     * @return total
     */
    public Double getTotal() {
        return total;
    }

    /**
     * Establece el importe total de la reserva.
     *
     * @param total total
     */
    
    public void setTotal(Double total) {
        this.total = total;
    }
    
     /**
     * Obtiene la fianza original sin descuento.
     *
     * @return fianza original, o {@code null} si aún no se ha calculado
     */
    public Double getFiancaOriginal() {
        return fiancaOriginal;
    }

    /**
     * Establece la fianza original sin descuento.
     *
     * @param fiancaOriginal fianza estándar del vehículo antes del descuento premium
     */
    public void setFiancaOriginal(Double fiancaOriginal) {
        this.fiancaOriginal = fiancaOriginal;
    }

    /**
     * Indica si se ha aplicado el descuento premium a la fianza.
     *
     * @return {@code true} si el cliente es premium y se aplicó el 25% de descuento
     */
    public Boolean getDescomptePremium() {
        return descomptePremium;
    }

    /**
     * Marca si se ha aplicado el descuento premium.
     *
     * @param descomptePremium {@code true} si se aplicó el descuento; {@code false} en caso contrario
     */
    public void setDescomptePremium(Boolean descomptePremium) {
        this.descomptePremium = descomptePremium;
    }

    /**
     * Obtiene el total sin descuento (subtotal + fianza original).
     *
     * @return total sin descuento, o {@code null} si aún no se ha calculado
     */
    public Double getTotalSenseDescompte() {
        return totalSenseDescompte;
    }

    /**
     * Establece el total sin descuento.
     *
     * @param totalSenseDescompte subtotal más la fianza original
     */
    public void setTotalSenseDescompte(Double totalSenseDescompte) {
        this.totalSenseDescompte = totalSenseDescompte;
    }
}
