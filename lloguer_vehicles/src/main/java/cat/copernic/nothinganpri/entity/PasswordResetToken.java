
package cat.copernic.nothinganpri.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuari_id", nullable = false)
    private UsuariJpaEntity usuari;

    @Column(nullable = false)
    private LocalDateTime dataCaducitat;

    @Column(nullable = false)
    private boolean utilitzat = false;

    /**
     * Constructor vacío.
     */
    public PasswordResetToken() {
    }

    public PasswordResetToken(
            String token,
            UsuariJpaEntity usuari,
            LocalDateTime dataCaducitat) {
        this.token = token;
        this.usuari = usuari;
        this.dataCaducitat = dataCaducitat;
        this.utilitzat = false;
    }

    /**
     * Obtiene id.
     *
     * @return valor de id
     */
    public Long getId() {
        return id;
    }

    /**
     * Obtiene token.
     *
     * @return valor de token
     */
    public String getToken() {
        return token;
    }

    /**
     * Establece token.
     *
     * @param token nuevo valor de token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Obtiene usuari.
     *
     * @return valor de usuari
     */
    public UsuariJpaEntity getUsuari() {
        return usuari;
    }

    /**
     * Establece usuari.
     *
     * @param usuari nuevo valor de usuari
     */
    public void setUsuari(UsuariJpaEntity usuari) {
        this.usuari = usuari;
    }

    /**
     * Obtiene dataCaducitat.
     *
     * @return valor de dataCaducitat
     */
    public LocalDateTime getDataCaducitat() {
        return dataCaducitat;
    }

    /**
     * Establece dataCaducitat.
     *
     * @param dataCaducitat nuevo valor de dataCaducitat
     */
    public void setDataCaducitat(LocalDateTime dataCaducitat) {
        this.dataCaducitat = dataCaducitat;
    }

    /**
     * Indica si utilitzat.
     *
     * @return true si utilitzat; false en caso contrario
     */
    public boolean isUtilitzat() {
        return utilitzat;
    }

    /**
     * Establece utilitzat.
     *
     * @param utilitzat nuevo valor de utilitzat
     */
    public void setUtilitzat(boolean utilitzat) {
        this.utilitzat = utilitzat;
    }
}
