package cat.copernic.nothinganpri.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TargetaForm {

    @NotBlank(message = "El titular es obligatorio")
    @Size(max = 100, message = "El titular no puede superar los 100 caracteres")
    @Pattern(
        regexp = "^[\\p{L}]+(?:[ '\\u2019-][\\p{L}]+)*$",
        message = "El titular solo puede contener letras y separadores entre palabras"
    )
    private String titularTargeta;

    // El número puede llegar con espacios, pero se normaliza al guardarlo.
    @Pattern(
        regexp = "^$|^[0-9 ]{13,23}$",
        message = "Número de tarjeta no válido"
    )
    private String numtarjetacredit;

    @NotBlank(message = "La caducidad es obligatoria")
    @Pattern(
        regexp = "^(0[1-9]|1[0-2])/\\d{2}$",
        message = "La caducidad debe tener formato MM/aa"
    )
    private String caducitatTargeta;

    public String getTitularTargeta() {
        return titularTargeta;
    }

    public void setTitularTargeta(String titularTargeta) {
        this.titularTargeta = titularTargeta == null
                ? null
                : titularTargeta.trim();
    }

    public String getNumtarjetacredit() {
        return numtarjetacredit;
    }

    public void setNumtarjetacredit(String numtarjetacredit) {
        this.numtarjetacredit = numtarjetacredit == null
                ? null
                : numtarjetacredit.replaceAll("\\s+", "");
    }

    public String getCaducitatTargeta() {
        return caducitatTargeta;
    }

    public void setCaducitatTargeta(String caducitatTargeta) {
        this.caducitatTargeta = caducitatTargeta == null
                ? null
                : caducitatTargeta.trim();
    }
}
