
package cat.copernic.nothinganpri.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import cat.copernic.nothinganpri.dto.DocumentClient;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.repository.ClientRepository;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.repository.SolicitudValidacionRepository;
import cat.copernic.nothinganpri.service.ClientService;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.TargetaEncryptionService;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ReservaJpaRepository reservaRepository;

    @Mock
    private LogQueryService logQueryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SolicitudValidacionRepository solicitudValidacionRepository;

    @Mock
    private TargetaEncryptionService targetaEncryptionService;

    @InjectMocks
    private ClientService clientService;

    private ClientJpaEntity client;

    @BeforeEach
    void setUp() {
        client = new ClientJpaEntity(
                "12345678A",
                "2030-01-01",
                "B",
                "2031-01-01",
                "123456789",
                "Barcelona",
                "Espanyola",
                "reverso.png",
                new byte[] { 1, 2, 3 },
                "image/png",
                new byte[] { 4, 5, 6 },
                "image/jpeg",
                null,
                null,
                "Joan",
                "",
                "600123456",
                "joan@gmail.com",
                "password",
                "CLIENT",
                "Barcelona"
        );
    }

    @Test
    void testLlistarTots() {
        List<ClientJpaEntity> clients = Arrays.asList(client);

        when(clientRepository.findByRolAndActivoTrue("CLIENT"))
                .thenReturn(clients);

        List<ClientJpaEntity> resultat = clientService.llistarTots();

        assertEquals(1, resultat.size());
        assertEquals(client, resultat.get(0));

        verify(clientRepository).findByRolAndActivoTrue("CLIENT");
    }

    @Test
    void testLlistarTotsSenseClients() {
        when(clientRepository.findByRolAndActivoTrue("CLIENT"))
                .thenReturn(Collections.emptyList());

        List<ClientJpaEntity> resultat = clientService.llistarTots();

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testCercarFiltresAmbValors() {
        when(clientRepository.cercarClients(
                "Espanyola",
                "Barcelona",
                "12345678A",
                "Garcia",
                "600123456",
                "joan@gmail.com",
                null, "CLIENT"
        )).thenReturn(Arrays.asList(client));

        List<ClientJpaEntity> resultat = clientService.cercarFiltres(
                "Espanyola",
                "Barcelona",
                "12345678A",
                "Garcia",
                "600123456",
                "joan@gmail.com", null
        );

        assertEquals(1, resultat.size());
        assertEquals(client, resultat.get(0));

        verify(clientRepository).cercarClients(
                "Espanyola",
                "Barcelona",
                "12345678A",
                "Garcia",
                "600123456",
                "joan@gmail.com",
                null, "CLIENT"
        );
    }

    @Test
    void testCercarFiltresAmbValorsBuits() {
        when(clientRepository.cercarClients(
                null, null, null, null, null, null, null, "CLIENT"
        )).thenReturn(Collections.emptyList());

        List<ClientJpaEntity> resultat = clientService.cercarFiltres(
                " ",
                "",
                null,
                "   ",
                " ",
                "", null
        );

        assertTrue(resultat.isEmpty());

        verify(clientRepository).cercarClients(
                null, null, null, null, null, null, null, "CLIENT"
        );
    }

    @Test
    void testCercarFiltresEliminaEspais() {
        when(clientRepository.cercarClients(
                "Espanyola",
                "Barcelona",
                "12345678A",
                "Garcia",
                "600123456",
                "joan@gmail.com",
                null, "CLIENT"
        )).thenReturn(Arrays.asList(client));

        clientService.cercarFiltres(
                " Espanyola ",
                " Barcelona ",
                " 12345678A ",
                " Garcia ",
                " 600123456 ",
                " joan@gmail.com ", null
        );

        verify(clientRepository).cercarClients(
                "Espanyola",
                "Barcelona",
                "12345678A",
                "Garcia",
                "600123456",
                "joan@gmail.com",
                null, "CLIENT"
        );
    }

    @Test
    void testCercarFiltresAmbUnSolFiltre() {
        when(clientRepository.cercarClients(
                null, "Barcelona", null, null, null, null, null, "CLIENT"
        )).thenReturn(Arrays.asList(client));

        List<ClientJpaEntity> resultat = clientService.cercarFiltres(
                null, "Barcelona", null, null, null, null, null
        );

        assertEquals(1, resultat.size());

        verify(clientRepository).cercarClients(
                null, "Barcelona", null, null, null, null, null, "CLIENT"
        );
    }

    @Test
    void testCercarFiltresNomésEmail() {
        when(clientRepository.cercarClients(
                null, null, null, null, null, "joan@gmail.com", null, "CLIENT"
        )).thenReturn(Arrays.asList(client));

        List<ClientJpaEntity> resultat = clientService.cercarFiltres(
                null, null, null, null, null, "joan@gmail.com", null
        );

        assertEquals(1, resultat.size());
        assertEquals(client, resultat.get(0));

        verify(clientRepository).cercarClients(
                null, null, null, null, null, "joan@gmail.com", null, "CLIENT"
        );
    }

    @Test
    void testFindByDNIExisteix() {
        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<ClientJpaEntity> resultat =
                clientService.findByDNI("12345678A");

        assertTrue(resultat.isPresent());
        assertEquals(client, resultat.get());
    }

    @Test
    void testFindByDNINoExisteix() {
        when(clientRepository.findByDNIAndRol("99999999Z", "CLIENT"))
                .thenReturn(Optional.empty());

        Optional<ClientJpaEntity> resultat =
                clientService.findByDNI("99999999Z");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testFindByEmailExisteix() {
        when(clientRepository.findByEmail("joan@gmail.com"))
                .thenReturn(Optional.of(client));

        Optional<ClientJpaEntity> resultat =
                clientService.findByEmail("joan@gmail.com");

        assertTrue(resultat.isPresent());
        assertEquals(client, resultat.get());

        verify(clientRepository).findByEmail("joan@gmail.com");
    }

    @Test
    void testFindByEmailNoExisteix() {
        when(clientRepository.findByEmail("noexisteix@gmail.com"))
                .thenReturn(Optional.empty());

        Optional<ClientJpaEntity> resultat =
                clientService.findByEmail("noexisteix@gmail.com");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testFindByEmailEliminaEspais() {
        when(clientRepository.findByEmail("joan@gmail.com"))
                .thenReturn(Optional.of(client));

        Optional<ClientJpaEntity> resultat =
                clientService.findByEmail(" joan@gmail.com ");

        assertTrue(resultat.isPresent());

        verify(clientRepository).findByEmail("joan@gmail.com");
    }

    @Test
    void testFindByEmailBuit() {
        when(clientRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        Optional<ClientJpaEntity> resultat =
                clientService.findByEmail(" ");

        assertTrue(resultat.isEmpty());

        verify(clientRepository).findByEmail(null);
    }

    @Test
    void testFindByIdExisteix() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        Optional<ClientJpaEntity> resultat = clientService.findById(1L);

        assertTrue(resultat.isPresent());
        assertEquals(client, resultat.get());
    }

    @Test
    void testFindByIdNoExisteix() {
        when(clientRepository.findById(2L))
                .thenReturn(Optional.empty());

        Optional<ClientJpaEntity> resultat = clientService.findById(2L);

        assertTrue(resultat.isEmpty());
    }

    // =========================================
    // TESTS ELIMINAR CLIENT
    // =========================================

    @Test
    void testEliminarClientCorrectament() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        when(reservaRepository.existsByClientId(1L))
                .thenReturn(false);

        boolean resultat = clientService.eliminarClient(1L);

        assertTrue(resultat);

        verify(reservaRepository).existsByClientId(1L);
        verify(solicitudValidacionRepository).deleteByClienteId(1L);
        verify(clientRepository).delete(client);
        verify(clientRepository).flush();

        verify(logQueryService, never()).registrarError(
                anyString(), anyString()
        );
    }

    @Test
    void testEliminarClientNoExisteix() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.empty());

        boolean resultat = clientService.eliminarClient(1L);

        assertFalse(resultat);

        verify(reservaRepository, never()).existsByClientId(anyLong());
        verify(solicitudValidacionRepository, never()).deleteByClienteId(anyLong());
        verify(clientRepository, never()).delete(any());
        verify(clientRepository, never()).flush();
    }

    @Test
    void testEliminarClientAmbReservesActives() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        when(reservaRepository.existsByClientId(1L))
                .thenReturn(true);

        boolean resultat = clientService.eliminarClient(1L);

        assertFalse(resultat);

        verify(reservaRepository).existsByClientId(1L);
        verify(solicitudValidacionRepository, never()).deleteByClienteId(anyLong());
        verify(clientRepository, never()).delete(any());
        verify(clientRepository, never()).flush();
    }

    @Test
    void testEliminarClientAmbErrorAlEliminar() {
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(client));

        when(reservaRepository.existsByClientId(1L))
                .thenReturn(false);

        doThrow(new RuntimeException("Error de base de dades"))
                .when(solicitudValidacionRepository)
                .deleteByClienteId(1L);

        assertThrows(
                RuntimeException.class,
                () -> clientService.eliminarClient(1L)
        );

        verify(logQueryService).registrarError(
                eq("CLIENTS"),
                contains("eliminarClient")
        );

        verify(clientRepository, never()).delete(any());
    }

    // =========================================
    // TESTS DOCUMENT DNI
    // =========================================

    @Test
    void testObtenirDniCorrectament() {
        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirDni("12345678A");

        assertTrue(resultat.isPresent());
        assertArrayEquals(
                new byte[] { 1, 2, 3 },
                resultat.get().getDades()
        );
        assertEquals("image/png", resultat.get().getTipus());
    }

    @Test
    void testObtenirDniClientNoExisteix() {
        when(clientRepository.findByDNIAndRol("99999999Z", "CLIENT"))
                .thenReturn(Optional.empty());

        Optional<DocumentClient> resultat =
                clientService.obtenirDni("99999999Z");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testObtenirDniSenseFotografia() {
        client.setFotoDniAnverso(null);

        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirDni("12345678A");

        assertTrue(resultat.isEmpty());
    }

    // =========================================
    // TESTS LLICENCIA
    // =========================================

    @Test
    void testObtenirLlicenciaCorrectament() {
        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirLlicencia("12345678A");

        assertTrue(resultat.isPresent());
        assertArrayEquals(
                new byte[] { 4, 5, 6 },
                resultat.get().getDades()
        );
        assertEquals("image/jpeg", resultat.get().getTipus());
    }

    @Test
    void testObtenirLlicenciaClientNoExisteix() {
        when(clientRepository.findByDNIAndRol("99999999Z", "CLIENT"))
                .thenReturn(Optional.empty());

        Optional<DocumentClient> resultat =
                clientService.obtenirLlicencia("99999999Z");

        assertTrue(resultat.isEmpty());
    }

    @Test
    void testObtenirLlicenciaSenseFotografia() {
        client.setFotoPermiso(null);

        when(clientRepository.findByDNIAndRol("12345678A", "CLIENT"))
                .thenReturn(Optional.of(client));

        Optional<DocumentClient> resultat =
                clientService.obtenirLlicencia("12345678A");

        assertTrue(resultat.isEmpty());
    }
}
