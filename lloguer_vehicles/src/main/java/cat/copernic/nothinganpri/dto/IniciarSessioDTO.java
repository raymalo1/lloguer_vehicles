package cat.copernic.nothinganpri.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para el formulario de inicio de sesión.
 * Contiene el correo electrónico y la contraseña del usuario.
 */
public class IniciarSessioDTO {

    /** Correo electrónico del usuario. */
    @NotBlank(message = "El correu és obligatori")
    @Email(message = "Introdueix un correu vàlid")
    private String email;

    /** Contraseña del usuario. */
    @NotBlank(message = "La contrasenya és obligatòria")
    private String password;

    /**
     * Obtiene el correo electrónico.
     *
     * @return correo electrónico del usuario
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece el correo electrónico.
     *
     * @param email correo electrónico del usuario
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contraseña.
     *
     * @return contraseña del usuario
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña.
     *
     * @param password contraseña del usuario
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
