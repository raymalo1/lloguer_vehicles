package cat.copernic.nothinganpri.entity;

import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.TipusCombustible;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entitat JPA d'un vehicle. Només defineix el mapatge a la base de dades:
 * les validacions de les dades es fan a la capa de negoci (VehicleService).
 */
@Entity
@Table(name = "vehicle")
public class VehicleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String model;

    private Integer anyMatriculacio;

    @Enumerated(EnumType.STRING)
    private CategoriaVehicle categoria;

    @Enumerated(EnumType.STRING)
    private TipusCombustible combustible;

    private String color;

    private Integer potenciaCv;

    private Integer places;

    @Column(name = "preu_hora", precision = 10, scale = 2)
    private BigDecimal preuHora;

    private Integer minDies = 1;

    private Integer maxDies;

    private Integer limitQuilometres;

    @Column(precision = 10, scale = 2)
    private BigDecimal fiancaEstandard;

    private boolean actiu = true;

    private LocalDate dataFiDesactivacio;

    @Column(length = 2000)
    private String comentariPrivat;

    // Foto guardada a la mateixa taula del vehicle
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] foto;

    @Column(length = 50)
    private String fotoContentType;
    
    @Column(name = "data_baixa")
    private LocalDate dataBaixa;           // Per baixa permanent (RF41)

    @Column(name = "motiu_baixa", length = 500)
    private String motiuBaixa;             // Per baixa permanent
    
    @Column(name = "motiu_ultima_activacio", length = 1000)
    private String motiuUltimaActivacio;

    @Column(name = "data_ultima_activacio")
    private LocalDate dataUltimaActivacio;

    @Column(name = "import_ultima_activacio", precision = 10, scale = 2)
    private BigDecimal importUltimaActivacio;

    @Column(name = "motiu_ultima_desactivacio", length = 1000)
    private String motiuUltimaDesactivacio;

    @Column(name = "data_ultima_desactivacio")
    private LocalDate dataUltimaDesactivacio;

    @Column(name = "data_fi_desactivacio_prevista")
    private LocalDate dataFiDesactivacioPrevista;

    // ... getters y setters NUEVOS ...

    public String getMotiuUltimaActivacio() {
        return motiuUltimaActivacio;
    }

    public void setMotiuUltimaActivacio(String motiuUltimaActivacio) {
        this.motiuUltimaActivacio = motiuUltimaActivacio;
    }

    public LocalDate getDataUltimaActivacio() {
        return dataUltimaActivacio;
    }

    public void setDataUltimaActivacio(LocalDate dataUltimaActivacio) {
        this.dataUltimaActivacio = dataUltimaActivacio;
    }

    public BigDecimal getImportUltimaActivacio() {
        return importUltimaActivacio;
    }

    public void setImportUltimaActivacio(BigDecimal importUltimaActivacio) {
        this.importUltimaActivacio = importUltimaActivacio;
    }

    public String getMotiuUltimaDesactivacio() {
        return motiuUltimaDesactivacio;
    }

    public void setMotiuUltimaDesactivacio(String motiuUltimaDesactivacio) {
        this.motiuUltimaDesactivacio = motiuUltimaDesactivacio;
    }

    public LocalDate getDataUltimaDesactivacio() {
        return dataUltimaDesactivacio;
    }

    public void setDataUltimaDesactivacio(LocalDate dataUltimaDesactivacio) {
        this.dataUltimaDesactivacio = dataUltimaDesactivacio;
    }

    public LocalDate getDataFiDesactivacioPrevista() {
        return dataFiDesactivacioPrevista;
    }

    public void setDataFiDesactivacioPrevista(LocalDate dataFiDesactivacioPrevista) {
        this.dataFiDesactivacioPrevista = dataFiDesactivacioPrevista;
    }

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    /**
     * Constructor buit. JPA/Hibernate i Spring MVC (binding del formulari) el necessiten
     * per crear l'objecte i omplir-lo després amb els setters.
     */
    public VehicleJpaEntity() {
    }

    /**
     * Constructor amb les dades obligatòries d'un vehicle. La resta de camps
     * conserven el valor per defecte (minDies = 1, actiu = true) o queden buits.
     */
    public VehicleJpaEntity(String matricula, String marca, String model, Integer anyMatriculacio,
                            CategoriaVehicle categoria, TipusCombustible combustible, Integer places,
                            BigDecimal preuHora, BigDecimal fiancaEstandard) {
        this.matricula = matricula;
        this.marca = marca;
        this.model = model;
        this.anyMatriculacio = anyMatriculacio;
        this.categoria = categoria;
        this.combustible = combustible;
        this.places = places;
        this.preuHora = preuHora;
        this.fiancaEstandard = fiancaEstandard;
    }

    /**
     * Constructor complet, amb totes les dades llevat de l'id i de la foto.
     * Reutilitza el constructor de dades obligatòries per no repetir codi.
     */
    public VehicleJpaEntity(String matricula, String marca, String model, Integer anyMatriculacio,
                            CategoriaVehicle categoria, TipusCombustible combustible, String color,
                            Integer potenciaCv, Integer places, BigDecimal preuHora, Integer minDies,
                            Integer maxDies, Integer limitQuilometres, BigDecimal fiancaEstandard,
                            boolean actiu, LocalDate dataFiDesactivacio, String comentariPrivat) {
        this(matricula, marca, model, anyMatriculacio, categoria, combustible, places, preuHora, fiancaEstandard);
        this.color = color;
        this.potenciaCv = potenciaCv;
        this.minDies = minDies;
        this.maxDies = maxDies;
        this.limitQuilometres = limitQuilometres;
        this.actiu = actiu;
        this.dataFiDesactivacio = dataFiDesactivacio;
        this.comentariPrivat = comentariPrivat;
    }

    /**
     * Constructor totalment complet: inclou l'id i la foto.
     * Útil per reconstruir un vehicle existent (p. ex. en proves o en mapejos).
     * Per a un vehicle nou, passa null com a id perquè el generi la base de dades.
     */
    public VehicleJpaEntity(Long id, String matricula, String marca, String model, Integer anyMatriculacio,
                            CategoriaVehicle categoria, TipusCombustible combustible, String color,
                            Integer potenciaCv, Integer places, BigDecimal preuHora, Integer minDies,
                            Integer maxDies, Integer limitQuilometres, BigDecimal fiancaEstandard,
                            boolean actiu, LocalDate dataFiDesactivacio, String comentariPrivat,
                            byte[] foto, String fotoContentType) {
        this(matricula, marca, model, anyMatriculacio, categoria, combustible, color, potenciaCv, places,
                preuHora, minDies, maxDies, limitQuilometres, fiancaEstandard, actiu, dataFiDesactivacio,
                comentariPrivat);
        this.id = id;
        this.foto = foto;
        this.fotoContentType = fotoContentType;
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
     * Establece id.
     *
     * @param id nuevo valor de id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene matricula.
     *
     * @return valor de matricula
     */
    public String getMatricula() {
        return matricula;
    }

    /**
     * Establece matricula.
     *
     * @param matricula nuevo valor de matricula
     */
    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    /**
     * Obtiene marca.
     *
     * @return valor de marca
     */
    public String getMarca() {
        return marca;
    }

    /**
     * Establece marca.
     *
     * @param marca nuevo valor de marca
     */
    public void setMarca(String marca) {
        this.marca = marca;
    }

    /**
     * Obtiene model.
     *
     * @return valor de model
     */
    public String getModel() {
        return model;
    }

    /**
     * Establece model.
     *
     * @param model nuevo valor de model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Obtiene anyMatriculacio.
     *
     * @return valor de anyMatriculacio
     */
    public Integer getAnyMatriculacio() {
        return anyMatriculacio;
    }

    /**
     * Establece anyMatriculacio.
     *
     * @param anyMatriculacio nuevo valor de anyMatriculacio
     */
    public void setAnyMatriculacio(Integer anyMatriculacio) {
        this.anyMatriculacio = anyMatriculacio;
    }

    /**
     * Obtiene categoria.
     *
     * @return valor de categoria
     */
    public CategoriaVehicle getCategoria() {
        return categoria;
    }

    /**
     * Establece categoria.
     *
     * @param categoria nuevo valor de categoria
     */
    public void setCategoria(CategoriaVehicle categoria) {
        this.categoria = categoria;
    }

    /**
     * Obtiene combustible.
     *
     * @return valor de combustible
     */
    public TipusCombustible getCombustible() {
        return combustible;
    }

    /**
     * Establece combustible.
     *
     * @param combustible nuevo valor de combustible
     */
    public void setCombustible(TipusCombustible combustible) {
        this.combustible = combustible;
    }

    /**
     * Obtiene color.
     *
     * @return valor de color
     */
    public String getColor() {
        return color;
    }

    /**
     * Establece color.
     *
     * @param color nuevo valor de color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Obtiene potenciaCv.
     *
     * @return valor de potenciaCv
     */
    public Integer getPotenciaCv() {
        return potenciaCv;
    }

    /**
     * Establece potenciaCv.
     *
     * @param potenciaCv nuevo valor de potenciaCv
     */
    public void setPotenciaCv(Integer potenciaCv) {
        this.potenciaCv = potenciaCv;
    }

    /**
     * Obtiene places.
     *
     * @return valor de places
     */
    public Integer getPlaces() {
        return places;
    }

    /**
     * Establece places.
     *
     * @param places nuevo valor de places
     */
    public void setPlaces(Integer places) {
        this.places = places;
    }

    /**
     * Obtiene preuHora.
     *
     * @return valor de preuHora
     */
    public BigDecimal getPreuHora() {
        return preuHora;
    }

    /**
     * Establece preuHora.
     *
     * @param preuHora nuevo valor de preuHora
     */
    public void setPreuHora(BigDecimal preuHora) {
        this.preuHora = preuHora;
    }

    /**
     * Obtiene minDies.
     *
     * @return valor de minDies
     */
    public Integer getMinDies() {
        return minDies;
    }

    /**
     * Establece minDies.
     *
     * @param minDies nuevo valor de minDies
     */
    public void setMinDies(Integer minDies) {
        this.minDies = minDies;
    }

    /**
     * Obtiene maxDies.
     *
     * @return valor de maxDies
     */
    public Integer getMaxDies() {
        return maxDies;
    }

    /**
     * Establece maxDies.
     *
     * @param maxDies nuevo valor de maxDies
     */
    public void setMaxDies(Integer maxDies) {
        this.maxDies = maxDies;
    }

    /**
     * Obtiene limitQuilometres.
     *
     * @return valor de limitQuilometres
     */
    public Integer getLimitQuilometres() {
        return limitQuilometres;
    }

    /**
     * Establece limitQuilometres.
     *
     * @param limitQuilometres nuevo valor de limitQuilometres
     */
    public void setLimitQuilometres(Integer limitQuilometres) {
        this.limitQuilometres = limitQuilometres;
    }

    /**
     * Obtiene fiancaEstandard.
     *
     * @return valor de fiancaEstandard
     */
    public BigDecimal getFiancaEstandard() {
        return fiancaEstandard;
    }

    /**
     * Establece fiancaEstandard.
     *
     * @param fiancaEstandard nuevo valor de fiancaEstandard
     */
    public void setFiancaEstandard(BigDecimal fiancaEstandard) {
        this.fiancaEstandard = fiancaEstandard;
    }

    /**
     * Indica si actiu.
     *
     * @return true si actiu; false en caso contrario
     */
    public boolean isActiu() {
        return actiu;
    }

    /**
     * Establece actiu.
     *
     * @param actiu nuevo valor de actiu
     */
    public void setActiu(boolean actiu) {
        this.actiu = actiu;
    }

    /**
     * Obtiene dataFiDesactivacio.
     *
     * @return valor de dataFiDesactivacio
     */
    public LocalDate getDataFiDesactivacio() {
        return dataFiDesactivacio;
    }

    /**
     * Establece dataFiDesactivacio.
     *
     * @param dataFiDesactivacio nuevo valor de dataFiDesactivacio
     */
    public void setDataFiDesactivacio(LocalDate dataFiDesactivacio) {
        this.dataFiDesactivacio = dataFiDesactivacio;
    }

    /**
     * Obtiene comentariPrivat.
     *
     * @return valor de comentariPrivat
     */
    public String getComentariPrivat() {
        return comentariPrivat;
    }

    /**
     * Establece comentariPrivat.
     *
     * @param comentariPrivat nuevo valor de comentariPrivat
     */
    public void setComentariPrivat(String comentariPrivat) {
        this.comentariPrivat = comentariPrivat;
    }

    /**
     * Obtiene foto.
     *
     * @return valor de foto
     */
    public byte[] getFoto() {
        return foto;
    }

    /**
     * Establece foto.
     *
     * @param foto nuevo valor de foto
     */
    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    /**
     * Obtiene fotoContentType.
     *
     * @return valor de fotoContentType
     */
    public String getFotoContentType() {
        return fotoContentType;
    }

    /**
     * Establece fotoContentType.
     *
     * @param fotoContentType nuevo valor de fotoContentType
     */
    public void setFotoContentType(String fotoContentType) {
        this.fotoContentType = fotoContentType;
    }

    /** URL des d'on es serveix la foto, o null si el vehicle no en té. Les plantilles la fan servir. */
    @Transient
    public String getFotoUrl() {
        return (foto != null && id != null) ? "/vehicles/" + id + "/foto" : null;
    }

    /**
     * Obtiene dataBaixa.
     *
     * @return valor de dataBaixa
     */
    public LocalDate getDataBaixa() {
        return dataBaixa;
    }

    /**
     * Establece dataBaixa.
     *
     * @param dataBaixa nuevo valor de dataBaixa
     */
    public void setDataBaixa(LocalDate dataBaixa) {
        this.dataBaixa = dataBaixa;
    }

    /**
     * Obtiene motiuBaixa.
     *
     * @return valor de motiuBaixa
     */
    public String getMotiuBaixa() {
        return motiuBaixa;
    }

    /**
     * Establece motiuBaixa.
     *
     * @param motiuBaixa nuevo valor de motiuBaixa
     */
    public void setMotiuBaixa(String motiuBaixa) {
        this.motiuBaixa = motiuBaixa;
    }
    
}