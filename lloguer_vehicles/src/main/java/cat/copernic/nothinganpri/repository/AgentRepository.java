package cat.copernic.nothinganpri.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cat.copernic.nothinganpri.entity.AgentJpaEntity;

/**
 * Repositori per gestionar les operacions dels agents.
 */
public interface AgentRepository extends JpaRepository<AgentJpaEntity, Long> {

        // Buscar agents i administradors independentment del seu estat
        List<AgentJpaEntity> findByRolIn(List<String> roles);

        // Buscar agents actius per rol
        List<AgentJpaEntity> findByActivoTrueAndRolIn(List<String> roles);

        // Filtrar agents actius per DNI i rol
        List<AgentJpaEntity> findByActivoTrueAndDNIContainingIgnoreCaseAndRolIn(
                        String dni, List<String> roles);

        // Buscar un agent actiu per DNI i rol
        AgentJpaEntity findByDNIAndActivoTrueAndRolIn(
                        String dni, List<String> roles);

        boolean existsByDNI(String DNI);

        /**
         * Comprova si el DNI pertany a un altre agent, excloent-ne l'identificador
         * indicat.
         *
         * @param DNI DNI que es vol comprovar.
         * @param id  identificador de l'agent que s'ha d'excloure.
         * @return true si el DNI ja pertany a un altre agent.
         */
        boolean existsByDNIAndIdNot(String DNI, Long id);

        // Buscar un agent per DNI i rol
        AgentJpaEntity findByDNIAndRolIn(String dni, List<String> roles);

        /**
         * Cerca agents mitjançant filtres opcionals de DNI i estat d'activitat.
         * Si un filtre és null, no s'aplica a la cerca.
         *
         * @param dni    DNI o fragment del DNI que es vol cercar.
         * @param activo estat d'activitat de l'agent.
         * @param roles  llista de rols per limitar els resultats.
         * @return llista d'agents que compleixen els filtres indicats.
         */
        @Query("SELECT a FROM AgentJpaEntity a WHERE " +
                        "a.rol IN :roles AND " +
                        "(:dni IS NULL OR LOWER(a.DNI) LIKE LOWER(CONCAT('%', :dni, '%'))) AND " +
                        "(:activo IS NULL OR a.activo = :activo)")
        List<AgentJpaEntity> cercarAgents(
                        @Param("dni") String dni,
                        @Param("activo") Boolean activo,
                        @Param("roles") List<String> roles);
}