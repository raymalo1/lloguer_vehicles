package cat.copernic.nothinganpri.test;

import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.enums.TipusCombustible;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.repository.VehicleJpaRepository;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleJpaRepository repository;

    @Mock
    private ReservaJpaRepository reservaRepository;

    @Mock
    private LogQueryService logQueryService;

    @InjectMocks
    private VehicleService service;

    private VehicleJpaEntity vehicle;

    @BeforeEach
    void setUp() {
        vehicle = new VehicleJpaEntity();
        vehicle.setId(1L);
        vehicle.setMatricula("1234BCD");  // ← FIX: matrícula válida (4 dígitos + 3 consonantes, sin vocales ni Ñ/Q)
        vehicle.setMarca("Seat");
        vehicle.setModel("Leon");
        vehicle.setAnyMatriculacio(2022);
        vehicle.setCategoria(CategoriaVehicle.TURISME);
        vehicle.setCombustible(TipusCombustible.GASOLINA);
        vehicle.setPlaces(5);
        vehicle.setPreuHora(new BigDecimal("10.00"));
        vehicle.setFiancaEstandard(new BigDecimal("200.00"));
        vehicle.setMinDies(1);
        vehicle.setMaxDies(30);
        vehicle.setActiu(true);
    }

    @Test
    void guardar_ActualitzacioVehicleValid_RetornaVehicleDesat() {
        when(repository.findByMatricula("1234BCD")).thenReturn(Optional.of(vehicle));  // ← FIX
        when(repository.save(any(VehicleJpaEntity.class))).thenReturn(vehicle);

        VehicleJpaEntity resultat = service.guardar(vehicle);

        assertNotNull(resultat);
        assertEquals("1234BCD", resultat.getMatricula());  // ← FIX
        verify(repository).save(vehicle);
    }

    @Test
    void guardar_MatriculaDuplicadaDiferentId_LlançaExcepcio() {
        VehicleJpaEntity altre = new VehicleJpaEntity();
        altre.setId(2L);
        altre.setMatricula("1234BCD");  // ← FIX: misma matrícula válida

        // ← FIX: lenient() para evitar UnnecessaryStubbingException
        lenient().when(repository.findByMatricula("1234BCD")).thenReturn(Optional.of(altre));

        assertThrows(IllegalArgumentException.class, () -> service.guardar(vehicle));
        verify(repository, never()).save(any());
    }

    @Test
    void donarDeBaixa_SenseReserves_EsborraDefinitivament() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservaRepository.existsByVehicleId(1L)).thenReturn(false);

        boolean resultat = service.donarDeBaixa(1L);

        assertTrue(resultat);
        verify(repository).delete(vehicle);
    }

    @Test
    void donarDeBaixa_AmbReservesVigents_LlançaExcepcio() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservaRepository.existsByVehicleId(1L)).thenReturn(true);
        when(reservaRepository.existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(
                eq(1L),
                anyList(),
                any(LocalDate.class)
        )).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.donarDeBaixa(1L));
        verify(repository, never()).delete(any());
        verify(repository, never()).save(any());
    }

    @Test
    void donarDeBaixa_NomesReservesPassades_BaixaLogica() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(reservaRepository.existsByVehicleId(1L)).thenReturn(true);
        when(reservaRepository.existsByVehicleIdAndEstatInAndDataFiGreaterThanEqual(
                eq(1L),
                anyList(),
                any(LocalDate.class)
        )).thenReturn(false);
        when(repository.save(any(VehicleJpaEntity.class))).thenReturn(vehicle);

        boolean resultat = service.donarDeBaixa(1L);

        assertFalse(resultat);
        assertFalse(vehicle.isActiu());
        verify(repository).save(vehicle);
        verify(repository, never()).delete(any());
    }
}