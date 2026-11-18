package cat.copernic.nothinganpri.repository;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface UsuariJpaRepository extends JpaRepository<UsuariJpaEntity, Long> {

    Optional<UsuariJpaEntity> findByEmail(String email);

    List<ClientJpaEntity> findByRol(String rol);

    Optional<UsuariJpaEntity> findByTelefon(String telefon);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByTelefonAndIdNot(String telefon, Long id);
}
