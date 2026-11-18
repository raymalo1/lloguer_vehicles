
package cat.copernic.nothinganpri.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;

/**
 * Entitat JPA per a clients de l'aplicació.
 * <p>
 * Extén {@link UsuariJpaEntity} afegint dades específiques del client:
 * DNI, carnet de conduir, targeta de crèdit, adreça, nacionalitat i fotos de documentació.
 * Les fotos del DNI i permís es guarden com a BLOB a la base de dades.
 * </p>
 *
 * @author Nothingan Pri Development Team
 * @since 1.0
 */
@Entity
public class ClientJpaEntity extends UsuariJpaEntity {

    private String DNI;
    private String tipusDocument;
    private String cadDNI;
    private String Lliconduir;
    private String cdLLiconduir;
    private String numtarjetacredit;
    private String adreca;
    private String nacionalitat;
    private String poblacio;
    private String fotoDniReverso;

    @Lob
    @Column(name = "foto_dni_anverso_dades", columnDefinition = "LONGBLOB")
    private byte[] fotoDniAnverso;

    @Column(length = 50)
    private String fotoDniAnversoTipus;

    @Lob
    @Column(name = "foto_permiso_dades", columnDefinition = "LONGBLOB")
    private byte[] fotoPermiso;

    @Column(length = 50)
    private String fotoPermisoTipus;

    @Column(name = "titular_targeta")
    private String titularTargeta;

    @Column(name = "caducitat_targeta")
    private String caducitatTargeta;

    /**
     * Constructor per defecte necessari per JPA.
     */
    public ClientJpaEntity() {
        super();
    }

    // Constructor con ID

    /**
     * Constructor complet amb tots els camps.
     *
     * @param DNI número de DNI
     * @param cadDNI caducitat del DNI
     * @param Lliconduir número de carnet de conduir
     * @param cdLLiconduir caducitat del carnet de conduir
     * @param numtarjetacredit número de targeta de crèdit
     * @param adreca adreça postal
     * @param nacionalitat nacionalitat
     * @param fotoDniReverso foto del revers del DNI (base64 o path)
     * @param fotoDniAnverso foto de l'anvers del DNI (bytes)
     * @param fotoDniAnversoTipus tipus MIME de la foto DNI anvers
     * @param fotoPermiso foto del permís de conduir (bytes)
     * @param fotoPermisoTipus tipus MIME de la foto permís
     * @param titularTargeta titular de la targeta
     * @param caducitatTargeta caducitat de la targeta
     * @param id identificador únic
     * @param nom nom
     * @param cognoms cognoms
     * @param telefon telèfon
     * @param email email
     * @param password contrasenya encriptada
     * @param rol rol (CLIENT)
     * @param provincia província de l'usuari
     */
    public ClientJpaEntity(
            String DNI,
            String cadDNI,
            String Lliconduir,
            String cdLLiconduir,
            String numtarjetacredit,
            String adreca,
            String nacionalitat,
            String fotoDniReverso,
            byte[] fotoDniAnverso,
            String fotoDniAnversoTipus,
            byte[] fotoPermiso,
            String fotoPermisoTipus,
            String titularTargeta,
            String caducitatTargeta,
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
        this.cadDNI = cadDNI;
        this.Lliconduir = Lliconduir;
        this.cdLLiconduir = cdLLiconduir;
        this.numtarjetacredit = numtarjetacredit;
        this.adreca = adreca;
        this.nacionalitat = nacionalitat;
        this.fotoDniReverso = fotoDniReverso;
        this.fotoDniAnverso = fotoDniAnverso;
        this.fotoDniAnversoTipus = fotoDniAnversoTipus;
        this.fotoPermiso = fotoPermiso;
        this.fotoPermisoTipus = fotoPermisoTipus;
        this.titularTargeta = titularTargeta;
        this.caducitatTargeta = caducitatTargeta;
    }

    // Constructor sin ID

    /**
     * Constructor sense ID (per a noves entitats).
     *
     * @param DNI número de DNI
     * @param cadDNI caducitat del DNI
     * @param Lliconduir número de carnet de conduir
     * @param cdLLiconduir caducitat del carnet de conduir
     * @param numtarjetacredit número de targeta de crèdit
     * @param adreca adreça postal
     * @param nacionalitat nacionalitat
     * @param fotoDniReverso foto del revers del DNI
     * @param fotoDniAnverso foto de l'anvers del DNI (bytes)
     * @param fotoDniAnversoTipus tipus MIME de la foto DNI anvers
     * @param fotoPermiso foto del permís de conduir (bytes)
     * @param fotoPermisoTipus tipus MIME de la foto permís
     * @param titularTargeta titular de la targeta
     * @param caducitatTargeta caducitat de la targeta
     * @param nom nom
     * @param cognoms cognoms
     * @param telefon telèfon
     * @param email email
     * @param password contrasenya encriptada
     * @param rol rol (CLIENT)
     * @param provincia província de l'usuari
     */
    public ClientJpaEntity(
            String DNI,
            String cadDNI,
            String Lliconduir,
            String cdLLiconduir,
            String numtarjetacredit,
            String adreca,
            String nacionalitat,
            String fotoDniReverso,
            byte[] fotoDniAnverso,
            String fotoDniAnversoTipus,
            byte[] fotoPermiso,
            String fotoPermisoTipus,
            String titularTargeta,
            String caducitatTargeta,
            String nom,
            String cognoms,
            String telefon,
            String email,
            String password,
            String rol,
            String provincia) {

        super(nom, cognoms, telefon, email, password, rol, provincia);

        this.DNI = DNI;
        this.cadDNI = cadDNI;
        this.Lliconduir = Lliconduir;
        this.cdLLiconduir = cdLLiconduir;
        this.numtarjetacredit = numtarjetacredit;
        this.adreca = adreca;
        this.nacionalitat = nacionalitat;
        this.fotoDniReverso = fotoDniReverso;
        this.fotoDniAnverso = fotoDniAnverso;
        this.fotoDniAnversoTipus = fotoDniAnversoTipus;
        this.fotoPermiso = fotoPermiso;
        this.fotoPermisoTipus = fotoPermisoTipus;
        this.titularTargeta = titularTargeta;
        this.caducitatTargeta = caducitatTargeta;
    }

    /**
     * Obté el DNI.
     *
     * @return DNI del client
     */
    public String getDNI() {
        return DNI;
    }

    /**
     * Estableix el DNI.
     *
     * @param DNI DNI del client
     */
    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getTipusDocument() {
        return tipusDocument;
    }

    public void setTipusDocument(String tipusDocument) {
        this.tipusDocument = tipusDocument;
    }

    /**
     * Obté la caducitat del DNI.
     *
     * @return caducitat del DNI
     */
    public String getCadDNI() {
        return cadDNI;
    }

    /**
     * Estableix la caducitat del DNI.
     *
     * @param cadDNI caducitat del DNI
     */
    public void setCadDNI(String cadDNI) {
        this.cadDNI = cadDNI;
    }

    /**
     * Obté el número de carnet de conduir.
     *
     * @return carnet de conduir
     */
    public String getLliconduir() {
        return Lliconduir;
    }

    /**
     * Estableix el número de carnet de conduir.
     *
     * @param Lliconduir carnet de conduir
     */
    public void setLliconduir(String Lliconduir) {
        this.Lliconduir = Lliconduir;
    }

    /**
     * Obté la caducitat del carnet de conduir.
     *
     * @return caducitat del carnet
     */
    public String getCdLLiconduir() {
        return cdLLiconduir;
    }

    /**
     * Estableix la caducitat del carnet de conduir.
     *
     * @param cdLLiconduir caducitat del carnet
     */
    public void setCdLLiconduir(String cdLLiconduir) {
        this.cdLLiconduir = cdLLiconduir;
    }

    /**
     * Obté el número de targeta de crèdit.
     *
     * @return número de targeta
     */
    public String getNumtarjetacredit() {
        return numtarjetacredit;
    }

    /**
     * Estableix el número de targeta de crèdit.
     *
     * @param numtarjetacredit número de targeta
     */
    public void setNumtarjetacredit(String numtarjetacredit) {
        this.numtarjetacredit = numtarjetacredit;
    }

    /**
     * Obté l'adreça.
     *
     * @return adreça postal
     */
    public String getAdreca() {
        return adreca;
    }

    /**
     * Estableix l'adreça.
     *
     * @param adreca adreça postal
     */
    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    /**
     * Obté la nacionalitat.
     *
     * @return nacionalitat
     */
    public String getNacionalitat() {
        return nacionalitat;
    }

    /**
     * Estableix la nacionalitat.
     *
     * @param nacionalitat nacionalitat
     */
    public void setNacionalitat(String nacionalitat) {
        this.nacionalitat = nacionalitat;
    }

    public String getPoblacio() {
        return poblacio;
    }

    public void setPoblacio(String poblacio) {
        this.poblacio = poblacio;
    }

    /**
     * Obté la foto de l'anvers del DNI.
     *
     * @return bytes de la imatge
     */
    public byte[] getFotoDniAnverso() {
        return fotoDniAnverso;
    }

    /**
     * Estableix la foto de l'anvers del DNI.
     *
     * @param fotoDniAnverso bytes de la imatge
     */
    public void setFotoDniAnverso(byte[] fotoDniAnverso) {
        this.fotoDniAnverso = fotoDniAnverso;
    }

    /**
     * Obté el tipus MIME de la foto DNI anvers.
     *
     * @return tipus MIME (image/jpeg, image/png, etc.)
     */
    public String getFotoDniAnversoTipus() {
        return fotoDniAnversoTipus;
    }

    /**
     * Estableix el tipus MIME de la foto DNI anvers.
     *
     * @param fotoDniAnversoTipus tipus MIME
     */
    public void setFotoDniAnversoTipus(String fotoDniAnversoTipus) {
        this.fotoDniAnversoTipus = fotoDniAnversoTipus;
    }

    /**
     * Obté la foto del revers del DNI (base64 o path).
     *
     * @return foto revers DNI
     */
    public String getFotoDniReverso() {
        return fotoDniReverso;
    }

    /**
     * Estableix la foto del revers del DNI.
     *
     * @param fotoDniReverso foto revers DNI
     */
    public void setFotoDniReverso(String fotoDniReverso) {
        this.fotoDniReverso = fotoDniReverso;
    }

    /**
     * Obté la foto del permís de conduir.
     *
     * @return bytes de la imatge
     */
    public byte[] getFotoPermiso() {
        return fotoPermiso;
    }

    /**
     * Estableix la foto del permís de conduir.
     *
     * @param fotoPermiso bytes de la imatge
     */
    public void setFotoPermiso(byte[] fotoPermiso) {
        this.fotoPermiso = fotoPermiso;
    }

    /**
     * Obté el tipus MIME de la foto del permís.
     *
     * @return tipus MIME
     */
    public String getFotoPermisoTipus() {
        return fotoPermisoTipus;
    }

    /**
     * Estableix el tipus MIME de la foto del permís.
     *
     * @param fotoPermisoTipus tipus MIME
     */
    public void setFotoPermisoTipus(String fotoPermisoTipus) {
        this.fotoPermisoTipus = fotoPermisoTipus;
    }

    /**
     * Obté el titular de la targeta.
     *
     * @return titular de la targeta
     */
    public String getTitularTargeta() {
        return titularTargeta;
    }

    /**
     * Estableix el titular de la targeta.
     *
     * @param titularTargeta titular de la targeta
     */
    public void setTitularTargeta(String titularTargeta) {
        this.titularTargeta = titularTargeta;
    }

    /**
     * Obté la caducitat de la targeta.
     *
     * @return caducitat (MM/AA)
     */
    public String getCaducitatTargeta() {
        return caducitatTargeta;
    }

    /**
     * Estableix la caducitat de la targeta.
     *
     * @param caducitatTargeta caducitat (MM/AA)
     */
    public void setCaducitatTargeta(String caducitatTargeta) {
        this.caducitatTargeta = caducitatTargeta;
    }
}
