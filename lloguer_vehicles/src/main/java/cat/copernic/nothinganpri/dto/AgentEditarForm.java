
package cat.copernic.nothinganpri.dto;

import cat.copernic.nothinganpri.validacio.ProvinciaValida;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Formulario de edición de los datos de un agente.
 * Contiene los campos editables del agente y las validaciones correspondientes.
 */
public class AgentEditarForm {

    @NotBlank(message = "El nom és obligatori")
    @Size(max = 50, message = "El nom no pot superar els 50 caràcters")
    @Pattern(
        regexp = "^[\\p{L}]+(?:[ '’-][\\p{L}]+)*$",
        message = "El nom només pot contenir lletres i separadors entre paraules"
    )
    private String nom;

    @NotBlank(message = "Els cognoms són obligatoris")
    @Size(max = 100, message = "Els cognoms no poden superar els 100 caràcters")
    @Pattern(
        regexp = "^[\\p{L}]+(?:[ '’-][\\p{L}]+)*$",
        message = "Els cognoms només poden contenir lletres i separadors entre paraules"
    )
    private String cognoms;

    @NotBlank(message = "El telèfon és obligatori")
    @Pattern(
        regexp = "^[0-9]{9}$",
        message = "El telèfon ha de contenir exactament 9 números"
    )
    private String telefon;

    @NotBlank(message = "El correu electrònic és obligatori")
    @Email(message = "Introdueix una adreça electrònica vàlida")
    private String email;

    @NotBlank(message = "L'adreça és obligatòria")
    @Size(max = 150, message = "L'adreça no pot superar els 150 caràcters")
    private String adreca;

    @NotBlank(message = "La població és obligatòria")
    @Size(max = 100, message = "La població no pot superar els 100 caràcters")
    private String poblacio;

    @NotBlank(message = "La província és obligatòria")
    @Size(max = 80, message = "La província no pot superar els 80 caràcters")
    @ProvinciaValida
    private String provincia;

    @NotBlank(message = "La nacionalitat és obligatòria")
    @Size(max = 50, message = "La nacionalitat no pot superar els 50 caràcters")
    private String nacionalitat;
    
    /**
    * Mode premium del client: aplica un 25% de descompte a la fiança dels vehicles.
    */
   private boolean premium;

    // UTILIDAD

    /**
     * Elimina los espacios al principio y al final del texto.
     *
     * @param s texto que se quiere normalizar
     * @return texto sin espacios exteriores
     */
    private static String net(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * Obtiene el nombre del agente.
     *
     * @return nombre del agente
     */
    public String getNom() {
        return nom;
    }

    /**
     * Establece el nombre del agente.
     *
     * @param nom nombre del agente
     */
    public void setNom(String nom) {
        this.nom = net(nom);
    }

    /**
     * Obtiene los apellidos del agente.
     *
     * @return apellidos del agente
     */
    public String getCognoms() {
        return cognoms;
    }

    /**
     * Establece los apellidos del agente.
     *
     * @param cognoms apellidos del agente
     */
    public void setCognoms(String cognoms) {
        this.cognoms = net(cognoms);
    }

    /**
     * Obtiene el teléfono del agente.
     *
     * @return teléfono de contacto
     */
    public String getTelefon() {
        return telefon;
    }

    /**
     * Establece el teléfono del agente.
     *
     * @param telefon teléfono de contacto
     */
    public void setTelefon(String telefon) {
        this.telefon = net(telefon);
    }

    /**
     * Obtiene el correo electrónico del agente.
     *
     * @return correo electrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico del agente.
     *
     * @param email correo electrónico
     */
    public void setEmail(String email) {
        this.email = net(email);
    }

    /**
     * Obtiene la dirección del agente.
     *
     * @return dirección del agente
     */
    public String getAdreca() {
        return adreca;
    }

    /**
     * Establece la dirección del agente.
     *
     * @param adreca dirección del agente
     */
    public void setAdreca(String adreca) {
        this.adreca = net(adreca);
    }

    /**
     * Obtiene la provincia del agente.
     *
     * @return provincia del agente
     */
    public String getProvincia() {
        return provincia;
    }

    /**
     * Establece la provincia del agente.
     *
     * @param provincia provincia del agente
     */
    public void setProvincia(String provincia) {
        this.provincia = net(provincia);
    }

    /**
     * Obtiene la nacionalidad del agente.
     *
     * @return nacionalidad del agente
     */
    public String getNacionalitat() {
        return nacionalitat;
    }

    /**
     * Establece la nacionalidad del agente.
     *
     * @param nacionalitat nacionalidad del agente
     */
    public void setNacionalitat(String nacionalitat) {
        this.nacionalitat = net(nacionalitat);
    }
    /**
    * Indica si el mode premium està activat.
    *
    * @return {@code true} si el mode premium està actiu
    */
   public boolean isPremium() {
       return premium;
   }

   /**
    * Activa o desactiva el mode premium.
    *
    * @param premium {@code true} per activar, {@code false} per desactivar
    */
   public void setPremium(boolean premium) {
       this.premium = premium;
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
        this.poblacio = net(poblacio);
    }
}
