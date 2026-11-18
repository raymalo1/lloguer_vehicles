
package cat.copernic.nothinganpri.dto;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistreForm {

    @NotBlank(message = "El nom és obligatori")
    private String nom;

    @NotBlank(message = "Els cognoms són obligatoris")
    private String cognoms;

    @NotBlank(message = "El correu electrònic és obligatori")
    @Email(message = "El correu electrònic no és vàlid")
    private String email;

    @NotBlank(message = "El telèfon és obligatori")
    private String telefon;

    @NotBlank(message = "La nacionalitat és obligatòria")
    private String nacionalitat;

    @NotBlank(message = "L'adreça és obligatòria")
    private String adreca;

    @NotBlank(message = "La població és obligatòria")
    @Size(max = 100, message = "La població no pot superar els 100 caràcters")
    private String poblacio;

    @NotBlank(message = "La província és obligatòria")
    private String provincia;

    @NotBlank(message = "La contrasenya és obligatòria")
    @Size(min = 8, message = "La contrasenya ha de tenir almenys 8 caràcters")
    private String password;

    /**
     * Comprova que el telèfon sigui un número internacional vàlid.
     * Cal introduir-lo amb el prefix del país, per exemple: +34.
     */
    @AssertTrue(message = "Introdueix un telèfon internacional vàlid amb el prefix del país (ex. +34)")
    public boolean isTelefonValid() {

        if (telefon == null || telefon.isBlank()) {
            return true;
        }

        String telefonNet = telefon.trim();

        // Obliga a utilitzar el prefix internacional.
        if (!telefonNet.startsWith("+")) {
            return false;
        }

        try {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();

            // En format internacional no cal indicar cap regió.
            var numero = util.parse(telefonNet, null);

            // Comprova que el número compleixi el pla de numeració.
            return util.isValidNumber(numero);

        } catch (NumberParseException e) {
            return false;
        }
    }

    // GETTERS I SETTERS

    /**
     * Obtiene nom.
     *
     * @return valor de nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * Establece nom.
     *
     * @param nom nuevo valor de nom
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Obtiene cognoms.
     *
     * @return valor de cognoms
     */
    public String getCognoms() {
        return cognoms;
    }

    /**
     * Establece cognoms.
     *
     * @param cognoms nuevo valor de cognoms
     */
    public void setCognoms(String cognoms) {
        this.cognoms = cognoms;
    }

    /**
     * Obtiene email.
     *
     * @return valor de email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece email.
     *
     * @param email nuevo valor de email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene telefon.
     *
     * @return valor de telefon
     */
    public String getTelefon() {
        return telefon;
    }

    /**
     * Establece telefon.
     *
     * @param telefon nuevo valor de telefon
     */
    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    /**
     * Obtiene nacionalitat.
     *
     * @return valor de nacionalitat
     */
    public String getNacionalitat() {
        return nacionalitat;
    }

    /**
     * Establece nacionalitat.
     *
     * @param nacionalitat nuevo valor de nacionalitat
     */
    public void setNacionalitat(String nacionalitat) {
        this.nacionalitat = nacionalitat;
    }

    /**
     * Obtiene adreca.
     *
     * @return valor de adreca
     */
    public String getAdreca() {
        return adreca;
    }

    /**
     * Establece adreca.
     *
     * @param adreca nuevo valor de adreca
     */
    public void setAdreca(String adreca) {
        this.adreca = adreca;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    /**
     * Obtiene password.
     *
     * @return valor de password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece password.
     *
     * @param password nuevo valor de password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obté la població.
     *
     * @return població
     */
    public String getPoblacio() {
        return poblacio;
    }

    /**
     * Estableix la població.
     *
     * @param poblacio població
     */
    public void setPoblacio(String poblacio) {
        this.poblacio = poblacio;
    }
}
