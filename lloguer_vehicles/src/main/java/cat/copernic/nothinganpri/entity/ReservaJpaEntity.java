
    package cat.copernic.nothinganpri.entity;

    import cat.copernic.nothinganpri.enums.EstatReserva;
    import jakarta.persistence.*;
    import java.time.LocalDate;
    import java.time.LocalTime;

    @Entity
    @Table(name = "reserves")
    public class ReservaJpaEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(unique = true, nullable = false)
        private String codiReserva;

        @ManyToOne
        @JoinColumn(name = "vehicle_id", nullable = false)
        private VehicleJpaEntity vehicle;

        @ManyToOne
        @JoinColumn(name = "usuari_id", nullable = false)
        private UsuariJpaEntity client;

        @Column(nullable = false)
        private LocalDate dataInici;

        // Hora de recollida del vehicle
        @Column
        private LocalTime horaInici;

        @Column(nullable = false)
        private LocalDate dataFi;

        private Double importTotal;
        @Column
        private LocalTime horaFi;  // Hora de retorn

        @Column(nullable = false)
        private boolean acceptaDocumentacio = false;  // Checkbox documentación

        public LocalTime getHoraFi() {
            return horaFi;
        }

        public void setHoraFi(LocalTime horaFi) {
            this.horaFi = horaFi;
        }

        public boolean isAcceptaDocumentacio() {
            return acceptaDocumentacio;
        }

        public void setAcceptaDocumentacio(boolean acceptaDocumentacio) {
            this.acceptaDocumentacio = acceptaDocumentacio;
        }

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private EstatReserva estat = EstatReserva.ACTIVA;

        // El client ha sol·licitat l'anul·lació.
        // La reserva continua ACTIVA fins que l'agent decideix.
        @Column(nullable = false)
        private boolean anulacioSolicitada = false;

        // Relación 1:1 con la Entrega del vehículo
        @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
        private EntregaJpaEntity entrega;

        // --- Constructores ---
        /**
         * Constructor vacío.
         */
        public ReservaJpaEntity() {
        }

        /**
         * Constructor de ReservaJpaEntity con datos básicos.
         *
         * @param codiReserva código de la reserva
         * @param vehicle vehículo reservado
         * @param client cliente de la reserva
         * @param dataInici fecha de inicio
         * @param dataFi fecha de fin
         */
        public ReservaJpaEntity(String codiReserva, VehicleJpaEntity vehicle,
                UsuariJpaEntity client, LocalDate dataInici, LocalDate dataFi) {
            this.codiReserva = codiReserva;
            this.vehicle = vehicle;
            this.client = client;
            this.dataInici = dataInici;
            this.dataFi = dataFi;
            this.estat = EstatReserva.ACTIVA;
        }

        /**
         * Constructor de ReservaJpaEntity con importe total.
         *
         * @param codiReserva código de la reserva
         * @param vehicle vehículo reservado
         * @param client cliente de la reserva
         * @param dataInici fecha de inicio
         * @param dataFi fecha de fin
         * @param importTotal importe total de la reserva
         */
        public ReservaJpaEntity(String codiReserva, VehicleJpaEntity vehicle,
                UsuariJpaEntity client, LocalDate dataInici,
                LocalDate dataFi, Double importTotal) {
            this.codiReserva = codiReserva;
            this.vehicle = vehicle;
            this.client = client;
            this.dataInici = dataInici;
            this.dataFi = dataFi;
            this.importTotal = importTotal;
            this.estat = EstatReserva.ACTIVA;
        }

        // --- Getters i Setters ---
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
         * Obtiene codiReserva.
         *
         * @return valor de codiReserva
         */
        public String getCodiReserva() {
            return codiReserva;
        }

        /**
         * Establece codiReserva.
         *
         * @param codiReserva nuevo valor de codiReserva
         */
        public void setCodiReserva(String codiReserva) {
            this.codiReserva = codiReserva;
        }

        /**
         * Obtiene vehicle.
         *
         * @return valor de vehicle
         */
        public VehicleJpaEntity getVehicle() {
            return vehicle;
        }

        /**
         * Establece vehicle.
         *
         * @param vehicle nuevo valor de vehicle
         */
        public void setVehicle(VehicleJpaEntity vehicle) {
            this.vehicle = vehicle;
        }

        /**
         * Obtiene client.
         *
         * @return valor de client
         */
        public UsuariJpaEntity getClient() {
            return client;
        }

        /**
         * Establece client.
         *
         * @param client nuevo valor de client
         */
        public void setClient(UsuariJpaEntity client) {
            this.client = client;
        }

        /**
         * Obtiene dataInici.
         *
         * @return valor de dataInici
         */
        public LocalDate getDataInici() {
            return dataInici;
        }

        /**
         * Establece dataInici.
         *
         * @param dataInici nuevo valor de dataInici
         */
        public void setDataInici(LocalDate dataInici) {
            this.dataInici = dataInici;
        }

        /**
         * Obtiene horaInici.
         *
         * @return valor de horaInici
         */
        public LocalTime getHoraInici() {
            return horaInici;
        }

        /**
         * Establece horaInici.
         *
         * @param horaInici nuevo valor de horaInici
         */
        public void setHoraInici(LocalTime horaInici) {
            this.horaInici = horaInici;
        }

        /**
         * Obtiene dataFi.
         *
         * @return valor de dataFi
         */
        public LocalDate getDataFi() {
            return dataFi;
        }

        /**
         * Establece dataFi.
         *
         * @param dataFi nuevo valor de dataFi
         */
        public void setDataFi(LocalDate dataFi) {
            this.dataFi = dataFi;
        }

        /**
         * Obtiene importTotal.
         *
         * @return valor de importTotal
         */
        public Double getImportTotal() {
            return importTotal;
        }

        /**
         * Establece importTotal.
         *
         * @param importTotal nuevo valor de importTotal
         */
        public void setImportTotal(Double importTotal) {
            this.importTotal = importTotal;
        }

        /**
         * Obtiene estat.
         *
         * @return valor de estat
         */
        public EstatReserva getEstat() {
            if (this.estat == null) {
                return EstatReserva.ACTIVA;
            }
            return this.estat;
        }

        /**
         * Establece estat.
         *
         * @param estat nuevo valor de estat
         */
        public void setEstat(EstatReserva estat) {
            this.estat = estat;
        }

        /**
         * Indica si anulacioSolicitada.
         *
         * @return true si anulacioSolicitada; false en caso contrario
         */
        public boolean isAnulacioSolicitada() {
            return anulacioSolicitada;
        }

        /**
         * Establece anulacioSolicitada.
         *
         * @param anulacioSolicitada nuevo valor de anulacioSolicitada
         */
        public void setAnulacioSolicitada(boolean anulacioSolicitada) {
            this.anulacioSolicitada = anulacioSolicitada;
        }

        /**
         * Obtiene entrega.
         *
         * @return valor de entrega
         */
        public EntregaJpaEntity getEntrega() {
            return entrega;
        }

        /**
         * Establece entrega.
         *
         * @param entrega nuevo valor de entrega
         */
        public void setEntrega(EntregaJpaEntity entrega) {
            this.entrega = entrega;
            if (entrega != null && entrega.getReserva() != this) {
                entrega.setReserva(this);
            }
        }
    }
