
package cat.copernic.nothinganpri.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import cat.copernic.nothinganpri.dto.AgentForm;
import cat.copernic.nothinganpri.entity.AgentJpaEntity;
import cat.copernic.nothinganpri.repository.AgentRepository;
import cat.copernic.nothinganpri.service.AgentService;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.UsuariService;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    private static final List<String> ROLES_LISTAR =
            List.of("ADMINISTRADOR", "AGENT", "ADMIN");

    private static final List<String> ROLES_CERCAR =
            List.of("ADMINISTRADOR", "AGENT", "ADMIN");

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private UsuariService usuariService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LogQueryService logQueryService;

    @InjectMocks
    private AgentService agentService;

    private AgentJpaEntity agent;

    @BeforeEach
    void setUp() {
        agent = new AgentJpaEntity(
                "12345678A",
                LocalDate.of(2024, 1, 1),
                "Joan",
                "",
                "600123456",
                "joan@gmail.com",
                "password",
                "AGENT",
                "Barcelona"
        );
    }

    // ==============================
    // LLISTAR TOTS
    // ==============================

    @Test
    void testLlistarTots() {

        List<AgentJpaEntity> agents = List.of(agent);

        when(agentRepository.findByRolIn(ROLES_LISTAR))
                .thenReturn(agents);

        List<AgentJpaEntity> resultat = agentService.llistarTots();

        assertEquals(1, resultat.size());
        assertEquals(agent, resultat.get(0));

        verify(agentRepository).findByRolIn(ROLES_LISTAR);
    }

    // ==============================
    // CERCAR FILTRES
    // ==============================

    @Test
    void testCercarFiltres() {

        when(agentRepository.cercarAgents(
                "12345678A", true, ROLES_CERCAR))
                .thenReturn(List.of(agent));

        List<AgentJpaEntity> resultat =
                agentService.cercarFiltres(" 12345678A ", true);

        assertEquals(1, resultat.size());
        assertEquals(agent, resultat.get(0));

        verify(agentRepository).cercarAgents(
                "12345678A", true, ROLES_CERCAR);
    }

    @Test
    void testCercarFiltresAmbDniNull() {

        when(agentRepository.cercarAgents(
                null, true, ROLES_CERCAR))
                .thenReturn(List.of(agent));

        List<AgentJpaEntity> resultat =
                agentService.cercarFiltres(null, true);

        assertEquals(1, resultat.size());

        verify(agentRepository).cercarAgents(
                null, true, ROLES_CERCAR);
    }

    @Test
    void testCercarFiltresAmbDniBuit() {

        when(agentRepository.cercarAgents(
                null, false, ROLES_CERCAR))
                .thenReturn(List.of());

        List<AgentJpaEntity> resultat =
                agentService.cercarFiltres("   ", false);

        assertTrue(resultat.isEmpty());

        verify(agentRepository).cercarAgents(
                null, false, ROLES_CERCAR);
    }

    // ==============================
    // FILTRAR PER DNI
    // ==============================

    @Test
    void testFiltrarPerDNI() {

        when(agentRepository.findByActivoTrueAndDNIContainingIgnoreCaseAndRolIn(
                "123", ROLES_CERCAR))
                .thenReturn(List.of(agent));

        List<AgentJpaEntity> resultat =
                agentService.filtrarPerDNI("123");

        assertEquals(1, resultat.size());
        assertEquals(agent, resultat.get(0));

        verify(agentRepository)
                .findByActivoTrueAndDNIContainingIgnoreCaseAndRolIn(
                        "123", ROLES_CERCAR);
    }

    // ==============================
    // BUSCAR AGENT PER DNI
    // ==============================

    @Test
    void testBuscarAgentPerDNI() {

        when(agentRepository.findByDNIAndRolIn(
                "12345678A", ROLES_CERCAR))
                .thenReturn(agent);

        AgentJpaEntity resultat =
                agentService.buscarAgentPerDNI("12345678A");

        assertEquals(agent, resultat);

        verify(agentRepository).findByDNIAndRolIn(
                "12345678A", ROLES_CERCAR);
    }

    // ==============================
    // GUARDAR AGENT
    // ==============================

    @Test
    void testGuardarCorrectament() {

        agent.setDataContracte(null);

        when(agentRepository.existsByDNI("12345678A"))
                .thenReturn(false);

        when(usuariService.emailExisteix("joan@gmail.com"))
                .thenReturn(false);

        when(usuariService.telefonExisteix("600123456"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("passwordEncriptada");

        agentService.guardar(agent);

        assertEquals("passwordEncriptada", agent.getPassword());
        assertEquals(LocalDate.now(), agent.getDataContracte());

        verify(passwordEncoder).encode("password");
        verify(agentRepository).save(agent);
    }

    @Test
    void testGuardarAmbDNIDuplicat() {

        when(agentRepository.existsByDNI("12345678A"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals(
                "Aquest DNI ja està registrat.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("guardar un agent"));
    }

    @Test
    void testGuardarAmbEmailDuplicat() {

        when(agentRepository.existsByDNI("12345678A"))
                .thenReturn(false);

        when(usuariService.emailExisteix("joan@gmail.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals(
                "Aquest email ja està registrat.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("guardar un agent"));
    }

    @Test
    void testGuardarAmbTelefonDuplicat() {

        when(agentRepository.existsByDNI("12345678A"))
                .thenReturn(false);

        when(usuariService.emailExisteix("joan@gmail.com"))
                .thenReturn(false);

        when(usuariService.telefonExisteix("600123456"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals(
                "Aquest telèfon ja està registrat.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("guardar un agent"));
    }

    @Test
    void testGuardarAmbProvinciaInvalida() {

        agent.setProvincia("ProvinciaInventada");

        when(agentRepository.existsByDNI("12345678A"))
                .thenReturn(false);

        when(usuariService.emailExisteix("joan@gmail.com"))
                .thenReturn(false);

        when(usuariService.telefonExisteix("600123456"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals(
                "La província no és vàlida.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testGuardarAmbRolInvalid() {

        agent.setRol("CLIENT");

        when(agentRepository.existsByDNI("12345678A"))
                .thenReturn(false);

        when(usuariService.emailExisteix("joan@gmail.com"))
                .thenReturn(false);

        when(usuariService.telefonExisteix("600123456"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals(
                "El rol seleccionat no és vàlid.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testGuardarAmbContrasenyaBuida() {

        agent.setPassword(" ");

        when(agentRepository.existsByDNI("12345678A"))
                .thenReturn(false);

        when(usuariService.emailExisteix("joan@gmail.com"))
                .thenReturn(false);

        when(usuariService.telefonExisteix("600123456"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.guardar(agent));

        assertEquals(
                "La contrasenya és obligatòria.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    // ==============================
    // BUSCAR PER ID
    // ==============================

    @Test
    void testBuscarPerIdExisteix() {

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        AgentJpaEntity resultat =
                agentService.buscarPerId(1L);

        assertEquals(agent, resultat);

        verify(agentRepository).findById(1L);
    }

    @Test
    void testBuscarPerIdNoExisteix() {

        when(agentRepository.findById(1L))
                .thenReturn(Optional.empty());

        AgentJpaEntity resultat =
                agentService.buscarPerId(1L);

        assertNull(resultat);

        verify(agentRepository).findById(1L);
    }

    // ==============================
    // ACTUALITZAR AGENT
    // ==============================

    @Test
    void testActualitzarCorrectament() {

        AgentForm form = mock(AgentForm.class);

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");
        when(form.getNom()).thenReturn("Maria");
        when(form.getCognoms()).thenReturn("Garcia");
        when(form.getProvincia()).thenReturn("Barcelona");
        when(form.getRol()).thenReturn("AGENT");
        when(form.getDataContracte())
                .thenReturn(LocalDate.of(2025, 1, 1));

        when(usuariService.emailExisteixEnAltreUsuari(
                "nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari(
                "611222333", 1L))
                .thenReturn(false);

        when(agentRepository.save(agent))
                .thenReturn(agent);

        AgentJpaEntity resultat =
                agentService.actualitzar(1L, form);

        assertEquals("12345678A", resultat.getDNI());
        assertEquals("Maria", resultat.getNom());
        assertEquals("Garcia", resultat.getCognoms());
        assertEquals("611222333", resultat.getTelefon());
        assertEquals("nou@gmail.com", resultat.getEmail());
        assertEquals("Barcelona", resultat.getProvincia());
        assertEquals("AGENT", resultat.getRol());

        assertEquals(
                LocalDate.of(2025, 1, 1),
                resultat.getDataContracte());

        verify(agentRepository).save(agent);
    }

    @Test
    void testActualitzarConservaDNIOriginal() {

        AgentForm form = mock(AgentForm.class);

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");
        when(form.getNom()).thenReturn("Maria");
        when(form.getCognoms()).thenReturn("Garcia");
        when(form.getProvincia()).thenReturn("Barcelona");
        when(form.getRol()).thenReturn("AGENT");

        when(usuariService.emailExisteixEnAltreUsuari(
                "nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari(
                "611222333", 1L))
                .thenReturn(false);

        agentService.actualitzar(1L, form);

        assertEquals("12345678A", agent.getDNI());

        verify(agentRepository).save(agent);
    }

    @Test
    void testActualitzarConservaDataContracteSiEsNull() {

        AgentForm form = mock(AgentForm.class);

        LocalDate dataOriginal = agent.getDataContracte();

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");
        when(form.getNom()).thenReturn("Maria");
        when(form.getCognoms()).thenReturn("Garcia");
        when(form.getProvincia()).thenReturn("Barcelona");
        when(form.getRol()).thenReturn("AGENT");
        when(form.getDataContracte()).thenReturn(null);

        when(usuariService.emailExisteixEnAltreUsuari(
                "nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari(
                "611222333", 1L))
                .thenReturn(false);

        agentService.actualitzar(1L, form);

        assertEquals(dataOriginal, agent.getDataContracte());

        verify(agentRepository).save(agent);
    }

    @Test
    void testActualitzarAgentNoExisteix() {

        AgentForm form = mock(AgentForm.class);

        when(agentRepository.findById(1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualitzar(1L, form));

        assertEquals(
                "L'agent no existeix.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("actualitzar un agent"));
    }

    @Test
    void testActualitzarAmbEmailDuplicat() {

        AgentForm form = mock(AgentForm.class);

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        when(form.getEmail())
                .thenReturn("duplicat@gmail.com");

        when(usuariService.emailExisteixEnAltreUsuari(
                "duplicat@gmail.com", 1L))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualitzar(1L, form));

        assertEquals(
                "Aquest email ja està registrat.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("actualitzar un agent"));
    }

    @Test
    void testActualitzarAmbTelefonDuplicat() {

        AgentForm form = mock(AgentForm.class);

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");

        when(usuariService.emailExisteixEnAltreUsuari(
                "nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari(
                "611222333", 1L))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualitzar(1L, form));

        assertEquals(
                "Aquest telèfon ja està registrat.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("actualitzar un agent"));
    }

    @Test
    void testActualitzarAmbProvinciaInvalida() {

        AgentForm form = mock(AgentForm.class);

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");
        when(form.getProvincia()).thenReturn("ProvinciaInventada");

        when(usuariService.emailExisteixEnAltreUsuari(
                "nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari(
                "611222333", 1L))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualitzar(1L, form));

        assertEquals(
                "La província no és vàlida.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
    }

    @Test
    void testActualitzarAmbRolInvalid() {

        AgentForm form = mock(AgentForm.class);

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        when(form.getEmail()).thenReturn("nou@gmail.com");
        when(form.getTelefon()).thenReturn("611222333");
        when(form.getProvincia()).thenReturn("Barcelona");
        when(form.getRol()).thenReturn("CLIENT");

        when(usuariService.emailExisteixEnAltreUsuari(
                "nou@gmail.com", 1L))
                .thenReturn(false);

        when(usuariService.telefonExisteixEnAltreUsuari(
                "611222333", 1L))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.actualitzar(1L, form));

        assertEquals(
                "El rol seleccionat no és vàlid.",
                exception.getMessage());

        verify(agentRepository, never()).save(any());
    }

    // ==============================
    // ELIMINAR AGENT
    // ==============================

    @Test
    void testEliminarCorrectament() {

        when(agentRepository.findById(1L))
                .thenReturn(Optional.of(agent));

        agentService.eliminar(1L);

        verify(agentRepository).findById(1L);
        verify(agentRepository).delete(agent);

        verify(logQueryService).registrarInfo(
                eq("AGENTS"),
                contains("Agent eliminat definitivament"));
    }

    @Test
    void testEliminarAgentNoExisteix() {

        when(agentRepository.findById(1L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agentService.eliminar(1L));

        assertEquals(
                "L'agent no existeix.",
                exception.getMessage());

        verify(agentRepository, never()).delete(any());

        verify(logQueryService).registrarError(
                eq("AGENTS"),
                contains("eliminar un agent"));
    }
}
