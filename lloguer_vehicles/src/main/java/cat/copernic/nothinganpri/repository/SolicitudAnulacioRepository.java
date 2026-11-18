
package cat.copernic.nothinganpri.repository;

import cat.copernic.nothinganpri.entity.SolicitudAnulacioJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudAnulacioRepository extends JpaRepository<SolicitudAnulacioJpaEntity, Long> {

    @Query("""
        SELECT s
        FROM SolicitudAnulacioJpaEntity s
        JOIN FETCH s.reserva r
        JOIN FETCH r.client
        WHERE s.estado = :estado
        ORDER BY s.fechaSolicitud ASC
    """)
    List<SolicitudAnulacioJpaEntity> findByEstadoOrderByFechaSolicitudAsc(
            @Param("estado") String estado);

    boolean existsByReserva_IdAndEstado(Long reservaId, String estado);

    List<SolicitudAnulacioJpaEntity> findByReserva_IdOrderByFechaSolicitudDesc(Long reservaId);
}
