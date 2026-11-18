package cat.copernic.nothinganpri.test;

import cat.copernic.nothinganpri.document.HistoricReservaDocument;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.ReservaJpaEntity;
import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.repository.HistoricReservaRepository;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaJpaRepository reservaRepository;

    @Mock
    private HistoricReservaRepository historicReservaRepository;

    @Mock
    private LogQueryService logQueryService;

    @InjectMocks
    private ReservaService service;

    private VehicleJpaEntity vehicle;
    private ClientJpaEntity client;
    private ReservaJpaEntity reserva;

    @BeforeEach
    void setUp() {
        vehicle = new VehicleJpaEntity();
        vehicle.setId(10L);
        vehicle.setMatricula("1234ABC");
        vehicle.setMarca("Renault");
        vehicle.setModel("Clio");

        client = new ClientJpaEntity();
        client.setId(64L);
        client.setNom("Marc");
        client.setCognoms("Cazorla");
        client.setEmail("marccazorlanavarro@gmail.com");
        client.setNumtarjetacredit("4111111111111111");

        reserva = new ReservaJpaEntity();
        reserva.setId(100L);
        reserva.setCodiReserva("ABC12345");
        reserva.setVehicle(vehicle);
        reserva.setClient(client);
        reserva.setDataInici(LocalDate.of(2026, 11, 19));
        reserva.setDataFi(LocalDate.of(2026, 11, 22));
        reserva.setImportTotal(396.0);
        reserva.setEstat(EstatReserva.ACTIVA);
    }

    @Test
    void crearReserva_DatesValidesSenseConflicte_DesaIRetornaReserva() throws Exception {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(0L);
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenAnswer(inv -> {
            ReservaJpaEntity r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(historicReservaRepository.save(any(HistoricReservaDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReservaJpaEntity nova = new ReservaJpaEntity();
        nova.setVehicle(vehicle);
        nova.setClient(client);
        nova.setDataInici(LocalDate.of(2026, 11, 19));
        nova.setDataFi(LocalDate.of(2026, 11, 22));
        nova.setImportTotal(396.0);

        ReservaJpaEntity resultat = service.crearReserva(nova);

        assertNotNull(resultat);
        assertEquals(100L, resultat.getId());
        verify(reservaRepository).save(any(ReservaJpaEntity.class));
        verify(historicReservaRepository).save(any(HistoricReservaDocument.class));
    }

    @Test
    void crearReserva_DataIniciPosteriorADataFi_LlançaExcepcio() {
        ReservaJpaEntity nova = new ReservaJpaEntity();
        nova.setVehicle(vehicle);
        nova.setClient(client);
        nova.setDataInici(LocalDate.of(2026, 11, 25));
        nova.setDataFi(LocalDate.of(2026, 11, 20));

        Exception ex = assertThrows(Exception.class, () -> service.crearReserva(nova));
        assertTrue(ex.getMessage() != null && (
                ex.getMessage().toLowerCase().contains("posterior")
                || ex.getMessage().toLowerCase().contains("data")
        ));
    }

    @Test
    void crearReserva_VehicleNoDisponible_LlançaExcepcio() {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(1L);

        ReservaJpaEntity nova = new ReservaJpaEntity();
        nova.setVehicle(vehicle);
        nova.setClient(client);
        nova.setDataInici(LocalDate.of(2026, 11, 19));
        nova.setDataFi(LocalDate.of(2026, 11, 22));

        Exception ex = assertThrows(Exception.class, () -> service.crearReserva(nova));
        assertTrue(ex.getMessage() != null && (
                ex.getMessage().toLowerCase().contains("disponible")
                || ex.getMessage().toLowerCase().contains("conflicte")
                || ex.getMessage().toLowerCase().contains("ocupat")
        ));
    }

    @Test
    void crearReserva_GuardaHistoricAmbAccioCreada() throws Exception {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(0L);
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenAnswer(inv -> {
            ReservaJpaEntity r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(historicReservaRepository.save(any(HistoricReservaDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReservaJpaEntity nova = new ReservaJpaEntity();
        nova.setVehicle(vehicle);
        nova.setClient(client);
        nova.setDataInici(LocalDate.of(2026, 11, 19));
        nova.setDataFi(LocalDate.of(2026, 11, 22));
        nova.setImportTotal(396.0);

        service.crearReserva(nova);

        ArgumentCaptor<HistoricReservaDocument> captor =
                ArgumentCaptor.forClass(HistoricReservaDocument.class);
        verify(historicReservaRepository).save(captor.capture());
        assertEquals(ReservaService.ACCIO_CREADA, captor.getValue().getAccio());
    }

    @Test
    void crearReservaClient_SenseTargeta_LlançaExcepcio() {
        client.setNumtarjetacredit(null);

        ReservaJpaEntity nova = new ReservaJpaEntity();
        nova.setVehicle(vehicle);
        nova.setClient(client);
        nova.setDataInici(LocalDate.of(2026, 11, 19));
        nova.setDataFi(LocalDate.of(2026, 11, 22));

        Exception ex = assertThrows(Exception.class, () -> service.crearReservaClient(nova));
        assertTrue(ex.getMessage() != null && ex.getMessage().toLowerCase().contains("targeta"));
    }

    @Test
    void crearReservaClient_AmbTargeta_CreaReserva() throws Exception {
        when(reservaRepository.countConflictingReserves(eq(10L), any(), any())).thenReturn(0L);
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenAnswer(inv -> {
            ReservaJpaEntity r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        when(historicReservaRepository.save(any(HistoricReservaDocument.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReservaJpaEntity nova = new ReservaJpaEntity();
        nova.setVehicle(vehicle);
        nova.setClient(client);
        nova.setDataInici(LocalDate.of(2026, 11, 19));
        nova.setDataFi(LocalDate.of(2026, 11, 22));
        nova.setImportTotal(396.0);

        ReservaJpaEntity resultat = service.crearReservaClient(nova);

        assertNotNull(resultat);
        assertEquals(100L, resultat.getId());
    }

    @Test
    void anullarReserva_Directe_LlançaExcepcio() {
        Exception ex = assertThrows(Exception.class, () -> service.anullarReserva(100L));
        assertTrue(ex.getMessage() != null && (
                ex.getMessage().toLowerCase().contains("sol·licitud")
                || ex.getMessage().toLowerCase().contains("no es pot anul")
        ));
        verify(reservaRepository, never()).save(any());
        verify(historicReservaRepository, never()).save(any());
    }

    @Test
    void anullarReserva_IdInexistent_LlançaExcepcio() {
        Exception ex = assertThrows(Exception.class, () -> service.anullarReserva(999L));
        assertTrue(ex.getMessage() != null && (
                ex.getMessage().toLowerCase().contains("sol·licitud")
                || ex.getMessage().toLowerCase().contains("no es pot anul")
        ));
        verify(historicReservaRepository, never()).save(any());
    }

    @Test
    void solicitarAnulacio_ClientPropietari_MarcaSolicitud() throws Exception {
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any(ReservaJpaEntity.class))).thenReturn(reserva);

        service.solicitarAnulacio(100L, "marccazorlanavarro@gmail.com");

        assertTrue(reserva.isAnulacioSolicitada());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void solicitarAnulacio_EmailDiferent_LlançaExcepcio() {
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        Exception ex = assertThrows(Exception.class,
                () -> service.solicitarAnulacio(100L, "altre@test.com"));
        assertTrue(ex.getMessage() != null && (
                ex.getMessage().toLowerCase().contains("només")
                || ex.getMessage().toLowerCase().contains("teves")
        ));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void solicitarAnulacio_JaSolicitada_LlançaExcepcio() {
        reserva.setAnulacioSolicitada(true);
        when(reservaRepository.findById(100L)).thenReturn(Optional.of(reserva));

        Exception ex = assertThrows(Exception.class,
                () -> service.solicitarAnulacio(100L, "marccazorlanavarro@gmail.com"));
        assertTrue(ex.getMessage() != null && (
                ex.getMessage().toLowerCase().contains("ja has")
                || ex.getMessage().toLowerCase().contains("sol·licitat")
        ));
    }
}