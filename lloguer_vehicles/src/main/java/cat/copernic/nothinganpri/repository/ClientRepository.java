
package cat.copernic.nothinganpri.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;

/**
 * Repositorio para gestionar las operaciones de acceso a los datos de los clientes.
 */
@Repository
public interface ClientRepository extends JpaRepository<ClientJpaEntity, Long> {

    /**
     * Busca los clientes activos según su rol.
     */
    List<ClientJpaEntity> findByRolAndActivoTrue(String rol);

    /**
     * Busca un cliente por su DNI y rol.
     */
    Optional<ClientJpaEntity> findByDNIAndRol(String DNI, String rol);

    /**
     * Busca un cliente por su dirección de correo electrónico.
     */
    Optional<ClientJpaEntity> findByEmail(String email);

    /**
     * RF26: Busca clientes aplicando diferentes filtros opcionales.
     */
    @Query("SELECT c FROM ClientJpaEntity c WHERE " +
            "c.rol = :rol AND " +
            "(:nacionalitat IS NULL OR LOWER(c.nacionalitat) LIKE LOWER(CONCAT('%', :nacionalitat, '%'))) AND " +
            "(:provincia IS NULL OR LOWER(c.provincia) LIKE LOWER(CONCAT('%', :provincia, '%'))) AND " +
            "(:dni IS NULL OR LOWER(c.DNI) LIKE LOWER(CONCAT('%', :dni, '%'))) AND " +
            "(:cognoms IS NULL OR LOWER(c.cognoms) LIKE LOWER(CONCAT('%', :cognoms, '%'))) AND " +
            "(:telefon IS NULL OR LOWER(c.telefon) LIKE LOWER(CONCAT('%', :telefon, '%'))) AND " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    List<ClientJpaEntity> cercarClients(
            @Param("nacionalitat") String nacionalitat,
            @Param("provincia") String provincia,
            @Param("dni") String dni,
            @Param("cognoms") String cognoms,
            @Param("telefon") String telefon,
            @Param("email") String email,
            @Param("rol") String rol
    );
}
