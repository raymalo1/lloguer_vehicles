
package cat.copernic.nothinganpri.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cat.copernic.nothinganpri.controller.AgentController;
import cat.copernic.nothinganpri.entity.AgentJpaEntity;
import cat.copernic.nothinganpri.service.AgentService;
import cat.copernic.nothinganpri.service.ClientService;
import cat.copernic.nothinganpri.service.LogQueryService;
import cat.copernic.nothinganpri.service.SolicitudAnulacioService;
import cat.copernic.nothinganpri.service.SolicitudValidacionService;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private AgentService agentService;

    @Mock
    private ClientService clientService;

    @Mock
    private SolicitudValidacionService solicitudService;

    @Mock
    private SolicitudAnulacioService solicitudAnulacioService;

    @Mock
    private LogQueryService logQueryService;

    private AgentController agentController;

    private MockMvc mockMvc;

    private AgentJpaEntity agent;

    @BeforeEach
    void setUp() {

        agentController = new AgentController(
                agentService,
                clientService,
                solicitudService,
                solicitudAnulacioService,
                logQueryService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(agentController)
                .build();

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

    // ==========================================
    // MOSTRAR CLIENTES
    // ==========================================

    @Test
    void testMostrarClients() throws Exception {

        when(clientService.cercarFiltres(
                null, null, null, null, null, null
        )).thenReturn(List.of());

        when(solicitudService.obtenerPendientes())
                .thenReturn(List.of());

        mockMvc.perform(get("/agent/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("llistat-clients"))
                .andExpect(model().attributeExists("clients"))
                .andExpect(model().attributeExists("solicitudPorCliente"))
                .andExpect(model().attributeExists("solicitudesDatos"))
                .andExpect(model().attributeExists("solicitudesDocumentacion"))
                .andExpect(model().attributeExists("solicitudesTarjeta"))
                .andExpect(model().attributeExists("canvisPerSolicitud"));

        verify(clientService).cercarFiltres(
                null, null, null, null, null, null
        );

        verify(solicitudService).obtenerPendientes();
    }

    // ==========================================
    // MOSTRAR SOLICITUDES
    // ==========================================

    @Test
    void testMostrarSolicitudes() throws Exception {

        when(solicitudService.obtenerPendientes())
                .thenReturn(List.of());

        mockMvc.perform(get("/agent/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(view().name("agent/solicitudes"))
                .andExpect(model().attributeExists("solicitudes"));

        verify(solicitudService).obtenerPendientes();
    }

    // ==========================================
    // LISTAR AGENTES
    // ==========================================

    @Test
    void testLlistarAgents() throws Exception {

        when(agentService.cercarFiltres(null, true))
                .thenReturn(List.of(agent));

        mockMvc.perform(get("/admin/agents"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestio-agents"))
                .andExpect(model().attributeExists("agents"))
                .andExpect(model().attribute("estat", "true"));

        verify(agentService).cercarFiltres(null, true);
    }

    // ==========================================
    // MOSTRAR FORMULARIO NUEVO AGENTE
    // ==========================================

    @Test
    void testMostrarFormulariAgent() throws Exception {

        mockMvc.perform(get("/admin/agents/nou"))
                .andExpect(status().isOk())
                .andExpect(view().name("nou-agent"))
                .andExpect(model().attributeExists("agent"))
                .andExpect(model().attribute(
                        "agent",
                        org.hamcrest.Matchers.hasProperty(
                                "dataContracte",
                                org.hamcrest.Matchers.notNullValue()
                        )
                ));
    }

    // ==========================================
    // FILTRAR AGENTES
    // ==========================================

    @Test
    void testFiltrarAgents() throws Exception {

        when(agentService.cercarFiltres("123", true))
                .thenReturn(List.of(agent));

        mockMvc.perform(post("/admin/agents/filtrar")
                .param("dni", "123")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestio-agents"))
                .andExpect(model().attributeExists("agents"))
                .andExpect(model().attribute("dni", "123"))
                .andExpect(model().attribute("estat", "true"));

        verify(agentService).cercarFiltres("123", true);
    }

    @Test
    void testFiltrarAgentsSenseResultats() throws Exception {

        when(agentService.cercarFiltres("999", null))
                .thenReturn(List.of());

        mockMvc.perform(post("/admin/agents/filtrar")
                .param("dni", "999"))
                .andExpect(status().isOk())
                .andExpect(view().name("gestio-agents"))
                .andExpect(model().attributeExists("agents"))
                .andExpect(model().attribute("dni", "999"))
                .andExpect(model().attribute("estat", ""));

        verify(agentService).cercarFiltres("999", null);
    }
}
