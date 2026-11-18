
package cat.copernic.nothinganpri.entity;

import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class AgentJpaEntity extends UsuariJpaEntity {

    private String DNI;

    private LocalDate dataContracte;

    // Constructor vacío requerido por JPA
    /**
     * Constructor vacío.
     */
    public AgentJpaEntity() {
        super();
    }

    // Constructor con ID
    public AgentJpaEntity(
            String DNI,
            LocalDate dataContracte,
            Long id,
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol,
            String provincia) {

        super(id, nom, cognoms, telefon, email, password, rol, provincia);

        this.DNI = DNI;
        this.dataContracte = dataContracte;
    }

    // Constructor sin ID
    public AgentJpaEntity(
            String DNI,
            LocalDate dataContracte,
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol,
            String provincia) {

        super(nom, cognoms, telefon, email, password, rol, provincia);

        this.DNI = DNI;
        this.dataContracte = dataContracte;
    }

    /**
     * Obtiene dataContracte.
     *
     * @return valor de dataContracte
     */
    public LocalDate getDataContracte() {
        return dataContracte;
    }

    /**
     * Establece dataContracte.
     *
     * @param dataContracte nuevo valor de dataContracte
     */
    public void setDataContracte(LocalDate dataContracte) {
        this.dataContracte = dataContracte;
    }

    /**
     * Obtiene dNI.
     *
     * @return valor de dNI
     */
    public String getDNI() {
        return DNI;
    }

    /**
     * Establece dNI.
     *
     * @param DNI nuevo valor de dNI
     */
    public void setDNI(String DNI) {
        this.DNI = DNI;
    }
}
