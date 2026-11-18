package cat.copernic.nothinganpri.repository;

import cat.copernic.nothinganpri.entity.SolicitudValidacionJpaEntity;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudValidacionRepository
        extends JpaRepository<SolicitudValidacionJpaEntity, Long> {

    // Todas las solicitudes pendientes, ordenadas de más antiguas a más recientes
    // JOIN FETCH: open-in-view está desactivado y "cliente" es LAZY,
    // así que hay que traer el cliente en la misma consulta.

    void deleteByClienteId(Long clienteId);

    @Query("select s from SolicitudValidacionJpaEntity s "
            + "join fetch s.cliente "
            + "where s.estado = :estado "
            + "order by s.dataSolicitud asc")
    List<SolicitudValidacionJpaEntity>
            findByEstadoOrderByDataSolicitudAsc(@Param("estado") String estado);

    // Solicitudes de un cliente
    List<SolicitudValidacionJpaEntity>
            findByClienteOrderByDataSolicitudDesc(ClientJpaEntity cliente);

    // Solicitudes pendientes de un cliente
    List<SolicitudValidacionJpaEntity>
            findByClienteAndEstadoOrderByDataSolicitudDesc(
                    ClientJpaEntity cliente,
                    String estado);

    // Comprobar si el cliente ya tiene una solicitud pendiente de ese tipo
    boolean existsByClienteAndTipoAndEstado(
            ClientJpaEntity cliente,
            String tipo,
            String estado);

    // Solicitudes de un cliente, tipo y estado concretos
    List<SolicitudValidacionJpaEntity> findByClienteAndTipoAndEstado(
            ClientJpaEntity cliente,
            String tipo,
            String estado);

    // Solicitudes de un tipo y estado determinados
    List<SolicitudValidacionJpaEntity> findByTipoAndEstado(
            String tipo,
            String estado);

}