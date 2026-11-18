package cat.copernic.nothinganpri.entity;

import jakarta.persistence.*;

/**
 * Entitat base abstracta per a tots els usuaris del sistema.
 * <p>
 * Conté els camps comuns: identificador, nom, cognoms, telèfon, email,
 * contrasenya (encriptada), rol i estat d'activació.
 * Les subclasses concretes són {@link ClientJpaEntity},
 * {@link AgentJpaEntity} i {@link AdminJpaEntity}.
 * </p>
 *
 * @author Nothingan Pri Development Team
 * @since 1.0
 */
@Entity
@Table(name = "usuaris")
public abstract class UsuariJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String cognoms;

    @Column(nullable = false)
    private String telefon;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    // Conservat de HEAD: província de l'usuari.
    @Column(nullable = false)
    private String provincia;

    /**
     * Constructor per defecte necessari per JPA.
     */
    public UsuariJpaEntity() {
    }

    /**
     * Constructor complet amb tots els camps.
     *
     * @param id identificador únic
     * @param nom nom de l'usuari
     * @param cognoms cognoms de l'usuari
     * @param telefon telèfon de contacte
     * @param email email únic de l'usuari
     * @param password contrasenya encriptada
     * @param rol rol de l'usuari (CLIENT, AGENT, ADMIN, ADMINISTRADOR)
     * @param provincia província de l'usuari
     */
    public UsuariJpaEntity(
            Long id,
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol,
            String provincia) {

        this.id = id;
        this.nom = nom;
        this.cognoms = cognoms;
        this.telefon = telefon;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.provincia = provincia;
    }

    /**
     * Constructor sense ID (per a noves entitats).
     *
     * @param nom nom de l'usuari
     * @param cognoms cognoms de l'usuari
     * @param telefon telèfon de contacte
     * @param email email únic de l'usuari
     * @param password contrasenya encriptada
     * @param rol rol de l'usuari
     * @param provincia província de l'usuari
     */
    public UsuariJpaEntity(
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol,
            String provincia) {

        this.nom = nom;
        this.cognoms = cognoms;
        this.telefon = telefon;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.provincia = provincia;
    }

    /**
     * Obté l'identificador únic.
     *
     * @return ID de l'usuari
     */
    public Long getId() {
        return id;
    }

    /**
     * Estableix l'identificador únic.
     *
     * @param id ID de l'usuari
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obté el nom.
     *
     * @return nom de l'usuari
     */
    public String getNom() {
        return nom;
    }

    /**
     * Estableix el nom.
     *
     * @param nom nom de l'usuari
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Obté els cognoms.
     *
     * @return cognoms de l'usuari
     */
    public String getCognoms() {
        return cognoms;
    }

    /**
     * Estableix els cognoms.
     *
     * @param cognoms cognoms de l'usuari
     */
    public void setCognoms(String cognoms) {
        this.cognoms = cognoms;
    }

    /**
     * Obté el telèfon.
     *
     * @return telèfon de contacte
     */
    public String getTelefon() {
        return telefon;
    }

    /**
     * Estableix el telèfon.
     *
     * @param telefon telèfon de contacte
     */
    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    /**
     * Obté l'email.
     *
     * @return email únic de l'usuari
     */
    public String getEmail() {
        return email;
    }

    /**
     * Estableix l'email.
     *
     * @param email email únic de l'usuari
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obté la contrasenya encriptada.
     *
     * @return contrasenya encriptada
     */
    public String getPassword() {
        return password;
    }

    /**
     * Estableix la contrasenya encriptada.
     *
     * @param password contrasenya encriptada
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obté el rol.
     *
     * @return rol de l'usuari
     */
    public String getRol() {
        return rol;
    }

    /**
     * Estableix el rol.
     *
     * @param rol rol de l'usuari
     */
    public void setRol(String rol) {
        this.rol = rol;
    }

    /**
     * Comprova si l'usuari està actiu.
     *
     * @return {@code true} si actiu, {@code false} si desactivat
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Estableix l'estat d'activació.
     *
     * @param activo {@code true} per activar, {@code false} per desactivar
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Obté la província de l'usuari.
     *
     * @return província de l'usuari
     */
    public String getProvincia() {
        return provincia;
    }

    /**
     * Estableix la província de l'usuari.
     *
     * @param provincia província de l'usuari
     */
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }
}
