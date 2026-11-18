
package cat.copernic.nothinganpri.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Locale;

public class DocumentacioForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String LETRES_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";

    @NotBlank(message = "El tipus de document és obligatori")
    private String tipusDocument;

    @NotBlank(message = "El número del document és obligatori")
    private String numeroDocument;

    @NotNull(message = "La data de caducitat del document és obligatòria")
    @FutureOrPresent(message = "La data de caducitat del document ha de ser avui o una data futura")
    private LocalDate dataCaducitat;

    @NotBlank(message = "El tipus de llicència és obligatori")
    @Pattern(
        regexp = "^(AM|A1|A2|A|B|BE|C1|C1E|C|CE|D1|D1E|D|DE)$",
        message = "El tipus de llicència no és vàlid"
    )
    private String tipusLlicencia;

    @NotNull(message = "La data de caducitat de la llicència és obligatòria")
    @FutureOrPresent(message = "La data de caducitat de la llicència ha de ser avui o una data futura")
    private LocalDate dataCaducitatLlicencia;

    private String nomDocumentIdentitat;
    private String nomLlicenciaConduir;

    private String caducitatDocument;
    private String caducitatLlicencia;

    // Imatges llegides i validades amb ImatgeUtil.llegir.
    // Es guarden a la base de dades en registrar el client.
    private FitxerPujat documentIdentitat;
    private FitxerPujat llicenciaConduir;

    /**
     * Elimina espais i passa el text a majúscules.
     *
     * @param s text que es vol normalitzar
     * @return text normalitzat
     */
    private static String net(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Comprova que el número del document coincideixi
     * amb el tipus de document seleccionat.
     *
     * DNI: 8 números i una lletra de control.
     * NIE: X, Y o Z + 7 números + una lletra de control.
     * PASAPORT: entre 5 i 20 caràcters alfanumèrics.
     */
    @AssertTrue(message = "El número del document no és vàlid per al tipus seleccionat")
    public boolean isNumeroDocumentValid() {

        if (tipusDocument == null || tipusDocument.isBlank()
                || numeroDocument == null || numeroDocument.isBlank()) {
            return true;
        }

        String tipus = net(tipusDocument);
        String numero = net(numeroDocument);

        switch (tipus) {
            case "DNI":
                return validarDNI(numero);

            case "NIE":
                return validarNIE(numero);

            case "PASAPORT":
            case "PASAPORTE":
            case "PASSPORT":
                return numero.matches("^[A-Z0-9]{5,20}$");

            default:
                return false;
        }
    }

    /**
     * Valida el format i la lletra de control del DNI.
     *
     * @param numero número del DNI
     * @return true si el DNI és vàlid
     */
    private boolean validarDNI(String numero) {

        if (!numero.matches("^[0-9]{8}[A-Z]$")) {
            return false;
        }

        try {
            int valor = Integer.parseInt(numero.substring(0, 8));
            char lletraEsperada = LETRES_DNI.charAt(valor % 23);

            return numero.charAt(8) == lletraEsperada;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida el format i la lletra de control del NIE.
     *
     * @param numero número del NIE
     * @return true si el NIE és vàlid
     */
    private boolean validarNIE(String numero) {

        if (!numero.matches("^[XYZ][0-9]{7}[A-Z]$")) {
            return false;
        }

        char primeraLletra = numero.charAt(0);

        String prefix;

        switch (primeraLletra) {
            case 'X':
                prefix = "0";
                break;
            case 'Y':
                prefix = "1";
                break;
            case 'Z':
                prefix = "2";
                break;
            default:
                return false;
        }

        String numeroConvertit = prefix + numero.substring(1, 8);

        try {
            int valor = Integer.parseInt(numeroConvertit);
            char lletraEsperada = LETRES_DNI.charAt(valor % 23);

            return numero.charAt(8) == lletraEsperada;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // GETTERS I SETTERS

    public String getTipusDocument() {
        return tipusDocument;
    }

    public void setTipusDocument(String tipusDocument) {
        this.tipusDocument = net(tipusDocument);
    }

    /**
     * Obtiene caducitatDocument.
     *
     * @return valor de caducitatDocument
     */
    public String getCaducitatDocument() {
        return caducitatDocument;
    }

    /**
     * Establece caducitatDocument.
     *
     * @param caducitatDocument nuevo valor de caducitatDocument
     */
    public void setCaducitatDocument(String caducitatDocument) {
        this.caducitatDocument = caducitatDocument;
    }

    /**
     * Obtiene caducitatLlicencia.
     *
     * @return valor de caducitatLlicencia
     */
    public String getCaducitatLlicencia() {
        return caducitatLlicencia;
    }

    /**
     * Establece caducitatLlicencia.
     *
     * @param caducitatLlicencia nuevo valor de caducitatLlicencia
     */
    public void setCaducitatLlicencia(String caducitatLlicencia) {
        this.caducitatLlicencia = caducitatLlicencia;
    }

    /**
     * Obtiene numeroDocument.
     *
     * @return valor de numeroDocument
     */
    public String getNumeroDocument() {
        return numeroDocument;
    }

    /**
     * Establece numeroDocument.
     *
     * @param numeroDocument nuevo valor de numeroDocument
     */
    public void setNumeroDocument(String numeroDocument) {
        this.numeroDocument = net(numeroDocument);
    }

    /**
     * Obtiene dataCaducitat.
     *
     * @return valor de dataCaducitat
     */
    public LocalDate getDataCaducitat() {
        return dataCaducitat;
    }

    /**
     * Establece dataCaducitat.
     *
     * @param dataCaducitat nuevo valor de dataCaducitat
     */
    public void setDataCaducitat(LocalDate dataCaducitat) {
        this.dataCaducitat = dataCaducitat;
    }

    /**
     * Obtiene tipusLlicencia.
     *
     * @return valor de tipusLlicencia
     */
    public String getTipusLlicencia() {
        return tipusLlicencia;
    }

    /**
     * Establece tipusLlicencia.
     *
     * @param tipusLlicencia nuevo valor de tipusLlicencia
     */
    public void setTipusLlicencia(String tipusLlicencia) {
        this.tipusLlicencia = net(tipusLlicencia);
    }

    /**
     * Obtiene dataCaducitatLlicencia.
     *
     * @return valor de dataCaducitatLlicencia
     */
    public LocalDate getDataCaducitatLlicencia() {
        return dataCaducitatLlicencia;
    }

    /**
     * Establece dataCaducitatLlicencia.
     *
     * @param dataCaducitatLlicencia nuevo valor de dataCaducitatLlicencia
     */
    public void setDataCaducitatLlicencia(LocalDate dataCaducitatLlicencia) {
        this.dataCaducitatLlicencia = dataCaducitatLlicencia;
    }

    /**
     * Obtiene nomDocumentIdentitat.
     *
     * @return valor de nomDocumentIdentitat
     */
    public String getNomDocumentIdentitat() {
        return nomDocumentIdentitat;
    }

    /**
     * Establece nomDocumentIdentitat.
     *
     * @param nomDocumentIdentitat nuevo valor de nomDocumentIdentitat
     */
    public void setNomDocumentIdentitat(String nomDocumentIdentitat) {
        this.nomDocumentIdentitat = nomDocumentIdentitat;
    }

    /**
     * Obtiene nomLlicenciaConduir.
     *
     * @return valor de nomLlicenciaConduir
     */
    public String getNomLlicenciaConduir() {
        return nomLlicenciaConduir;
    }

    /**
     * Establece nomLlicenciaConduir.
     *
     * @param nomLlicenciaConduir nuevo valor de nomLlicenciaConduir
     */
    public void setNomLlicenciaConduir(String nomLlicenciaConduir) {
        this.nomLlicenciaConduir = nomLlicenciaConduir;
    }

    /**
     * Obtiene documentIdentitat.
     *
     * @return valor de documentIdentitat
     */
    public FitxerPujat getDocumentIdentitat() {
        return documentIdentitat;
    }

    /**
     * Establece documentIdentitat.
     *
     * @param documentIdentitat nuevo valor de documentIdentitat
     */
    public void setDocumentIdentitat(FitxerPujat documentIdentitat) {
        this.documentIdentitat = documentIdentitat;
    }

    /**
     * Obtiene llicenciaConduir.
     *
     * @return valor de llicenciaConduir
     */
    public FitxerPujat getLlicenciaConduir() {
        return llicenciaConduir;
    }

    /**
     * Establece llicenciaConduir.
     *
     * @param llicenciaConduir nuevo valor de llicenciaConduir
     */
    public void setLlicenciaConduir(FitxerPujat llicenciaConduir) {
        this.llicenciaConduir = llicenciaConduir;
    }
}
