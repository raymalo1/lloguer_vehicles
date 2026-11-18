package cat.copernic.nothinganpri.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lliuraments")
public class EntregaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private ReservaJpaEntity reserva;

    // ===== LLIURAMENT (ENTREGA) =====
    @Column(nullable = false)
    private LocalDateTime dataLliurament;

    private Integer quilometresInicials;

    @Column(name = "observacions", length = 1000)
    private String observacionsLliurament;

    // FOTOS LLIURAMENT - FIX: MEDIUMBLOB para 16MB
    @Lob
    @Column(name = "foto_lliurament_1", columnDefinition = "MEDIUMBLOB")
    private byte[] fotoLliurament1;

    @Column(name = "foto_lliurament_1_tipus", length = 100)
    private String fotoLliurament1Tipus;

    @Lob
    @Column(name = "foto_lliurament_2", columnDefinition = "MEDIUMBLOB")
    private byte[] fotoLliurament2;

    @Column(name = "foto_lliurament_2_tipus", length = 100)
    private String fotoLliurament2Tipus;

    @Lob
    @Column(name = "foto_lliurament_3", columnDefinition = "MEDIUMBLOB")
    private byte[] fotoLliurament3;

    @Column(name = "foto_lliurament_3_tipus", length = 100)
    private String fotoLliurament3Tipus;

    // ===== RETORN (DEVOLUCIÓ) =====
    private LocalDateTime dataRetorn;

    private Integer quilometresFinals;

    @Column(length = 1000)
    private String observacionsRetorn;

    // FOTOS RETORN - FIX: MEDIUMBLOB para 16MB
    @Lob
    @Column(name = "foto_retorn_1", columnDefinition = "MEDIUMBLOB")
    private byte[] fotoRetorn1;

    @Column(name = "foto_retorn_1_tipus", length = 100)
    private String fotoRetorn1Tipus;

    @Lob
    @Column(name = "foto_retorn_2", columnDefinition = "MEDIUMBLOB")
    private byte[] fotoRetorn2;

    @Column(name = "foto_retorn_2_tipus", length = 100)
    private String fotoRetorn2Tipus;

    @Lob
    @Column(name = "foto_retorn_3", columnDefinition = "MEDIUMBLOB")
    private byte[] fotoRetorn3;

    @Column(name = "foto_retorn_3_tipus", length = 100)
    private String fotoRetorn3Tipus;

    // ===== NOU: Dades addicionals de retorn (RF48) =====
    private LocalDateTime dataHoraRetorn;

    private Long horesTarda;

    private Boolean danysImputablesClient;

    private BigDecimal importFiancaRetinguda;

    // --- Constructors ---
    public EntregaJpaEntity() {}

    public EntregaJpaEntity(ReservaJpaEntity reserva, LocalDateTime dataLliurament,
                            Integer quilometresInicials, String observacionsLliurament) {
        this.reserva = reserva;
        this.dataLliurament = dataLliurament;
        this.quilometresInicials = quilometresInicials;
        this.observacionsLliurament = observacionsLliurament;
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReservaJpaEntity getReserva() { return reserva; }
    public void setReserva(ReservaJpaEntity reserva) { this.reserva = reserva; }

    public LocalDateTime getDataLliurament() { return dataLliurament; }
    public void setDataLliurament(LocalDateTime dataLliurament) { this.dataLliurament = dataLliurament; }

    public Integer getQuilometresInicials() { return quilometresInicials; }
    public void setQuilometresInicials(Integer quilometresInicials) { this.quilometresInicials = quilometresInicials; }

    public String getObservacionsLliurament() { return observacionsLliurament; }
    public void setObservacionsLliurament(String observacionsLliurament) { this.observacionsLliurament = observacionsLliurament; }

    // Retorn
    public LocalDateTime getDataRetorn() { return dataRetorn; }
    public void setDataRetorn(LocalDateTime dataRetorn) { this.dataRetorn = dataRetorn; }

    public Integer getQuilometresFinals() { return quilometresFinals; }
    public void setQuilometresFinals(Integer quilometresFinals) { this.quilometresFinals = quilometresFinals; }

    public String getObservacionsRetorn() { return observacionsRetorn; }
    public void setObservacionsRetorn(String observacionsRetorn) { this.observacionsRetorn = observacionsRetorn; }

    // Fotos lliurament
    public byte[] getFotoLliurament1() { return fotoLliurament1; }
    public void setFotoLliurament1(byte[] fotoLliurament1) { this.fotoLliurament1 = fotoLliurament1; }
    public String getFotoLliurament1Tipus() { return fotoLliurament1Tipus; }
    public void setFotoLliurament1Tipus(String fotoLliurament1Tipus) { this.fotoLliurament1Tipus = fotoLliurament1Tipus; }
    public byte[] getFotoLliurament2() { return fotoLliurament2; }
    public void setFotoLliurament2(byte[] fotoLliurament2) { this.fotoLliurament2 = fotoLliurament2; }
    public String getFotoLliurament2Tipus() { return fotoLliurament2Tipus; }
    public void setFotoLliurament2Tipus(String fotoLliurament2Tipus) { this.fotoLliurament2Tipus = fotoLliurament2Tipus; }
    public byte[] getFotoLliurament3() { return fotoLliurament3; }
    public void setFotoLliurament3(byte[] fotoLliurament3) { this.fotoLliurament3 = fotoLliurament3; }
    public String getFotoLliurament3Tipus() { return fotoLliurament3Tipus; }
    public void setFotoLliurament3Tipus(String fotoLliurament3Tipus) { this.fotoLliurament3Tipus = fotoLliurament3Tipus; }

    // Fotos retorn
    public byte[] getFotoRetorn1() { return fotoRetorn1; }
    public void setFotoRetorn1(byte[] fotoRetorn1) { this.fotoRetorn1 = fotoRetorn1; }
    public String getFotoRetorn1Tipus() { return fotoRetorn1Tipus; }
    public void setFotoRetorn1Tipus(String fotoRetorn1Tipus) { this.fotoRetorn1Tipus = fotoRetorn1Tipus; }
    public byte[] getFotoRetorn2() { return fotoRetorn2; }
    public void setFotoRetorn2(byte[] fotoRetorn2) { this.fotoRetorn2 = fotoRetorn2; }
    public String getFotoRetorn2Tipus() { return fotoRetorn2Tipus; }
    public void setFotoRetorn2Tipus(String fotoRetorn2Tipus) { this.fotoRetorn2Tipus = fotoRetorn2Tipus; }
    public byte[] getFotoRetorn3() { return fotoRetorn3; }
    public void setFotoRetorn3(byte[] fotoRetorn3) { this.fotoRetorn3 = fotoRetorn3; }
    public String getFotoRetorn3Tipus() { return fotoRetorn3Tipus; }
    public void setFotoRetorn3Tipus(String fotoRetorn3Tipus) { this.fotoRetorn3Tipus = fotoRetorn3Tipus; }

    // Getters / Setters nous (RF48)
    public LocalDateTime getDataHoraRetorn() { return dataHoraRetorn; }
    public void setDataHoraRetorn(LocalDateTime dataHoraRetorn) { this.dataHoraRetorn = dataHoraRetorn; }

    public Long getHoresTarda() { return horesTarda; }
    public void setHoresTarda(Long horesTarda) { this.horesTarda = horesTarda; }

    public Boolean getDanysImputablesClient() { return danysImputablesClient; }
    public void setDanysImputablesClient(Boolean danysImputablesClient) { this.danysImputablesClient = danysImputablesClient; }

    public BigDecimal getImportFiancaRetinguda() { return importFiancaRetinguda; }
    public void setImportFiancaRetinguda(BigDecimal importFiancaRetinguda) { this.importFiancaRetinguda = importFiancaRetinguda; }

    // Helpers per templates
    /**
     * Metodo teFotosLliurament.
     * @return resultado de la operacion
     */
    public boolean teFotosLliurament() {
        return fotoLliurament1 != null || fotoLliurament2 != null || fotoLliurament3 != null;
    }
    /**
     * Metodo teFotosRetorn.
     * @return resultado de la operacion
     */
    public boolean teFotosRetorn() {
        return fotoRetorn1 != null || fotoRetorn2 != null || fotoRetorn3 != null;
    }
    public boolean isRetornat() { return dataRetorn != null; }
}