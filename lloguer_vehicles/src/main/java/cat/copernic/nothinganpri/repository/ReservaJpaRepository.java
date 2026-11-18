
package cat.copernic.nothinganpri.repository;

import cat.copernic.nothinganpri.entity.ReservaJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ReservaJpaRepository extends JpaRepository<ReservaJpaEntity, Long> {

    @Query("SELECT COUNT(r) FROM ReservaJpaEntity r WHERE r.vehicle.id = :vehicleId " +
           "AND r.estat = 'ACTIVA' " +
           "AND (r.dataInici <= :dataFi AND r.dataFi >= :dataInici)")
    long countConflictingReserves(
            @Param("vehicleId") Long vehicleId,
            @Param("dataInici") LocalDate dataInici,
            @Param("dataFi") LocalDate dataFi
    );

    boolean existsByVehicleIdAndDataFiGreaterThanEqual(
            Long vehicleId,
            LocalDate avui
    );

    boolean existsByVehicleId(Long vehicleId);

    @Query("SELECT DISTINCT r.vehicle.id FROM ReservaJpaEntity r")
    Set<Long> findIdsVehiclesAmbReserves();

    // --- RF54: filtres combinables (codi, data, matrícula, email, DNI) ---
    @Query("SELECT r FROM ReservaJpaEntity r WHERE " +
           "(:codi IS NULL OR r.codiReserva = :codi) AND " +
           "(:data IS NULL OR r.dataInici = :data) AND " +
           "(:matricula IS NULL OR r.vehicle.matricula = :matricula) AND " +
           "(:email IS NULL OR r.client.email = :email) AND " +
           "(:dni IS NULL OR TREAT(r.client AS cat.copernic.nothinganpri.entity.ClientJpaEntity).DNI = :dni)")
    List<ReservaJpaEntity> cercarAmbFiltres(
            @Param("codi") String codi,
            @Param("data") LocalDate data,
            @Param("matricula") String matricula,
            @Param("email") String email,
            @Param("dni") String dni
    );

    long countByClientId(Long clientId);

    List<ReservaJpaEntity> findByClientId(Long clientId);

    boolean existsByClient_IdAndEstatIn(
            Long clientId,
            List<EstatReserva> estats
    );

    boolean existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(
            Long vehicleId,
            List<EstatReserva> estats,
            LocalDate dataFi
    );

    // Conservat de HEAD
    boolean existsByClientId(Long id);
    

    // Conservat de origin/develop
    boolean existsByVehicleIdAndEstatInAndDataIniciLessThanEqualAndDataFiGreaterThanEqual(
            Long id,
            List<EstatReserva> estats,
            LocalDate fiDesactivacio,
            LocalDate iniciDesactivacio
    );
}
