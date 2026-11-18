package cat.copernic.nothinganpri.document;

import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Esdeveniment de l'històric d'una reserva (col·lecció "historic_reserves" de MongoDB).
 *
 * Cada vegada que una reserva es crea o s'anul·la, es guarda un document NOU amb una
 * "foto" de les dades en aquell moment. Les dades del vehicle i del client es copien
 * (no hi ha claus foranes), així l'històric es conserva encara que després es modifiqui
 * o s'esborri el vehicle o el client a MySQL.
 */
@Document(collection = "historic_reserves")
public class HistoricReservaDocument {

    @Id
    private String id;

    // Què ha passat: CREADA o ANUL·LADA
    private String accio;

    private Instant dataEvent;

    // Reserva
    @Indexed
    private Long reservaId;

    @Indexed
    private String codiReserva;

    private LocalDate dataInici;
    private LocalDate dataFi;
    private Double importTotal;
    private String estat;

    // Còpia de les dades del vehicle
    @Indexed
    private Long vehicleId;
    private String matricula;
    private String marca;
    private String model;

    // Còpia de les dades del client
    @Indexed
    private Long clientId;
    private String clientNom;
    private String clientEmail;

    /**
     * Constructor vacío.
     */
    public HistoricReservaDocument() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAccio() { return accio; }
    public void setAccio(String accio) { this.accio = accio; }

    public Instant getDataEvent() { return dataEvent; }
    public void setDataEvent(Instant dataEvent) { this.dataEvent = dataEvent; }

    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }

    public String getCodiReserva() { return codiReserva; }
    public void setCodiReserva(String codiReserva) { this.codiReserva = codiReserva; }

    public LocalDate getDataInici() { return dataInici; }
    public void setDataInici(LocalDate dataInici) { this.dataInici = dataInici; }

    public LocalDate getDataFi() { return dataFi; }
    public void setDataFi(LocalDate dataFi) { this.dataFi = dataFi; }

    public Double getImportTotal() { return importTotal; }
    public void setImportTotal(Double importTotal) { this.importTotal = importTotal; }

    public String getEstat() { return estat; }
    public void setEstat(String estat) { this.estat = estat; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
}