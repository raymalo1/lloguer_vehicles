
package cat.copernic.nothinganpri.test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cat.copernic.nothinganpri.controller.ClientController;
import cat.copernic.nothinganpri.dto.DocumentClient;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.service.ClientService;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private MockMvc mockMvc;
    private ClientJpaEntity client;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(clientController)
                .build();

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

    // =========================================
    // MOSTRAR FITXA DEL CLIENT
    // =========================================

    @Test
    void testMostrarClientExisteix() throws Exception {

        when(clientService.findByDNI("12345678A"))
                .thenReturn(Optional.of(client));

        mockMvc.perform(get("/clients/12345678A"))
                .andExpect(status().isOk())
                .andExpect(view().name("fitxa-client"))
                .andExpect(model().attributeExists("client"));

        verify(clientService).findByDNI("12345678A");
    }

    @Test
    void testMostrarClientNoExisteix() throws Exception {

        when(clientService.findByDNI("99999999Z"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/clients/99999999Z"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));
    }

    // =========================================
    // MOSTRAR DOCUMENT DNI
    // =========================================

    @Test
    void testVeureDniCorrectament() throws Exception {

        byte[] dades = { 1, 2, 3 };

        DocumentClient document =
                new DocumentClient(dades, "image/png");

        when(clientService.obtenirDni("12345678A"))
                .thenReturn(Optional.of(document));

        mockMvc.perform(get("/clients/12345678A/document/dni"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string(
                        "Content-Disposition", "inline"))
                .andExpect(header().string(
                        "X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(dades));
    }

    @Test
    void testVeureDniNoExisteix() throws Exception {

        when(clientService.obtenirDni("99999999Z"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/clients/99999999Z/document/dni"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVeureDniAmbTipusNull() throws Exception {

        DocumentClient document =
                new DocumentClient(new byte[] { 1, 2 }, null);

        when(clientService.obtenirDni("12345678A"))
                .thenReturn(Optional.of(document));

        mockMvc.perform(get("/clients/12345678A/document/dni"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void testVeureDniAmbTipusInvalid() throws Exception {

        DocumentClient document =
                new DocumentClient(
                        new byte[] { 1, 2 },
                        "tipus-invalid");

        when(clientService.obtenirDni("12345678A"))
                .thenReturn(Optional.of(document));

        mockMvc.perform(get("/clients/12345678A/document/dni"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM));
    }

    // =========================================
    // MOSTRAR LLICENCIA
    // =========================================

    @Test
    void testVeureLlicenciaCorrectament() throws Exception {

        byte[] dades = { 4, 5, 6 };

        DocumentClient document =
                new DocumentClient(dades, "image/jpeg");

        when(clientService.obtenirLlicencia("12345678A"))
                .thenReturn(Optional.of(document));

        mockMvc.perform(get("/clients/12345678A/document/llicencia"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string(
                        "Content-Disposition", "inline"))
                .andExpect(header().string(
                        "X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(dades));
    }

    @Test
    void testVeureLlicenciaNoExisteix() throws Exception {

        when(clientService.obtenirLlicencia("99999999Z"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/clients/99999999Z/document/llicencia"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testVeureLlicenciaAmbTipusBuit() throws Exception {

        DocumentClient document =
                new DocumentClient(new byte[] { 1, 2 }, " ");

        when(clientService.obtenirLlicencia("12345678A"))
                .thenReturn(Optional.of(document));

        mockMvc.perform(get("/clients/12345678A/document/llicencia"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void testVeureLlicenciaAmbTipusNull() throws Exception {

        DocumentClient document =
                new DocumentClient(new byte[] { 1, 2 }, null);

        when(clientService.obtenirLlicencia("12345678A"))
                .thenReturn(Optional.of(document));

        mockMvc.perform(get("/clients/12345678A/document/llicencia"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM));
    }

    // =========================================
    // ELIMINAR CLIENT
    // =========================================

    @Test
    void testEliminarClientCorrectament() throws Exception {

        when(clientService.findById(1L))
                .thenReturn(Optional.of(client));

        when(clientService.eliminarClient(1L))
                .thenReturn(true);

        mockMvc.perform(post("/clients/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/clients"))
                .andExpect(flash().attribute(
                        "success",
                        "El client s'ha eliminat definitivament."));

        verify(clientService).findById(1L);
        verify(clientService).eliminarClient(1L);
    }

    @Test
    void testEliminarClientAmbReserves() throws Exception {

        when(clientService.findById(1L))
                .thenReturn(Optional.of(client));

        when(clientService.eliminarClient(1L))
                .thenReturn(false);

        mockMvc.perform(post("/clients/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/clients"))
                .andExpect(flash().attribute(
                        "error",
                        "No es pot eliminar el client perquè té reserves assignades."));

        verify(clientService).findById(1L);
        verify(clientService).eliminarClient(1L);

        verify(clientService, never()).findByDNI(anyString());
    }

    @Test
    void testEliminarClientNoExisteix() throws Exception {

        when(clientService.findById(1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/clients/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/clients"))
                .andExpect(flash().attribute(
                        "error",
                        "El client no existeix."));

        verify(clientService).findById(1L);
        verify(clientService, never()).eliminarClient(anyLong());
    }

    @Test
    void testEliminarClientAmbExcepcio() throws Exception {

        when(clientService.findById(1L))
                .thenReturn(Optional.of(client));

        when(clientService.eliminarClient(1L))
                .thenThrow(new RuntimeException("Error de base de dades"));

        mockMvc.perform(post("/clients/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/clients"))
                .andExpect(flash().attribute(
                        "error",
                        "No s'ha pogut eliminar el client perquè té registres associats o s'ha produït un error."));

        verify(clientService).findById(1L);
        verify(clientService).eliminarClient(1L);
    }
}
