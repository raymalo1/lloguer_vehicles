package cat.copernic.nothinganpri.repository;

import cat.copernic.nothinganpri.document.HistoricReservaDocument;
import java.util.Collection;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Repositori de MongoDB (l'altra base de dades; les reserves "vives" continuen a MySQL). */
public interface HistoricReservaRepository extends MongoRepository<HistoricReservaDocument, String> {

    List<HistoricReservaDocument> findAllByOrderByDataEventDesc();

    List<HistoricReservaDocument> findByCodiReservaOrderByDataEventAsc(String codiReserva);

    List<HistoricReservaDocument> findByClientIdOrderByDataEventDesc(Long clientId);

    List<HistoricReservaDocument> findByVehicleIdOrderByDataEventDesc(Long vehicleId);

    /** Reserves creades d'un client (per a l'informe RF91). */
    List<HistoricReservaDocument> findByClientIdAndAccioOrderByDataIniciDesc(Long clientId, String accio);

    /** Reserves creades de diversos clients. */
    List<HistoricReservaDocument> findByClientIdInAndAccioOrderByClientNomAscDataIniciDesc(
            Collection<Long> clientIds, String accio);

    /** Totes les reserves creades (informe de tots els clients). */
    List<HistoricReservaDocument> findByAccioOrderByClientNomAscDataIniciDesc(String accio);

    long countByClientIdAndAccio(Long clientId, String accio);

    long countByClientIdInAndAccio(Collection<Long> clientIds, String accio);

    long countByAccio(String accio);
}