
package cat.copernic.nothinganpri.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Entidad JPA que representa a un administrador del sistema.
 * Hereda de {@link UsuariJpaEntity}.
 */
@Entity
@DiscriminatorValue("AdminJpaEntity")
public class AdminJpaEntity extends UsuariJpaEntity {

    /**
     * Constructor vacío.
     */
    public AdminJpaEntity() {
        super();
    }

    /**
     * Constructor completo con identificador.
     *
     * @param id identificador del administrador
     * @param nom nombre
     * @param cognoms apellidos
     * @param telefon teléfono
     * @param email correo electrónico
     * @param password contraseña cifrada
     * @param rol rol del usuario
     * @param provincia provincia del usuario
     */
    public AdminJpaEntity(
            Long id,
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol,
            String provincia) {

        super(id, nom, cognoms, telefon, email, password, rol, provincia);
    }

    /**
     * Constructor sin identificador (para nuevos administradores).
     *
     * @param nom nombre
     * @param cognoms apellidos
     * @param telefon teléfono
     * @param email correo electrónico
     * @param password contraseña cifrada
     * @param rol rol del usuario
     * @param provincia provincia del usuario
     */
    public AdminJpaEntity(
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol,
            String provincia) {

        super(nom, cognoms, telefon, email, password, rol, provincia);
    }
}
