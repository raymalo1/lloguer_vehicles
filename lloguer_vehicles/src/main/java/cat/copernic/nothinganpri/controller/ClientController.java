
package cat.copernic.nothinganpri.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.nothinganpri.dto.DocumentClient;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.service.ClientService;

/**
 * Controlador encargado de gestionar las operaciones relacionadas
 * con la consulta y eliminación de clientes.
 *
 * Permite mostrar la ficha de un cliente, consultar sus documentos
 * de identidad y licencia de conducir, y realizar su eliminación definitiva.
 */
@Controller
public class ClientController {

    @Autowired
    private ClientService clientService;

    /**
     * Muestra la ficha de un cliente identificado mediante su DNI.
     *
     * Si el cliente no existe, redirige al listado de clientes.
     *
     * @param dni documento de identidad del cliente
     * @param model modelo donde se almacenarán los datos del cliente
     * @return nombre de la vista de la ficha del cliente o ruta de redirección
     */
    @GetMapping("/clients/{dni}")
    public String mostrarClient(
            @PathVariable String dni,
            Model model) {

        Optional<ClientJpaEntity> client = clientService.findByDNI(dni);

        if (client.isEmpty()) {
            return "redirect:/clients";
        }

        model.addAttribute("client", client.get());

        return "fitxa-client";
    }

    /**
     * Recupera el documento de identidad de un cliente.
     *
     * Obtiene el documento mediante el DNI y genera la respuesta HTTP
     * correspondiente.
     *
     * @param dni documento de identidad del cliente
     * @return respuesta HTTP con los datos del documento o un error 404
     */
    @GetMapping("/clients/{dni}/document/dni")
    public ResponseEntity<byte[]> veureDni(
            @PathVariable String dni) {

        Optional<DocumentClient> document = clientService.obtenirDni(dni);

        return crearRespostaDocument(document);
    }

    /**
     * Recupera la licencia de conducir de un cliente.
     *
     * Obtiene el documento mediante el DNI y genera la respuesta HTTP
     * correspondiente.
     *
     * @param dni documento de identidad del cliente
     * @return respuesta HTTP con los datos de la licencia o un error 404
     */
    @GetMapping("/clients/{dni}/document/llicencia")
    public ResponseEntity<byte[]> veureLlicencia(
            @PathVariable String dni) {

        Optional<DocumentClient> document = clientService.obtenirLlicencia(dni);

        return crearRespostaDocument(document);
    }

    /**
     * Genera una respuesta HTTP a partir de un documento del cliente.
     *
     * Si el documento no existe, devuelve una respuesta HTTP 404.
     * Si existe, establece las cabeceras de seguridad, el tipo MIME
     * y el contenido del documento.
     *
     * @param document documento opcional que se desea mostrar
     * @return respuesta HTTP con el contenido del documento o error 404
     */
    private ResponseEntity<byte[]> crearRespostaDocument(
            Optional<DocumentClient> document) {

        if (document.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DocumentClient dades = document.get();

        MediaType mediaType = obtenirMediaType(dades.getTipus());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline")
                .header(
                        "X-Content-Type-Options",
                        "nosniff")
                .contentType(mediaType)
                .body(dades.getDades());
    }

    /**
     * Obtiene el tipo MIME correspondiente a una cadena de texto.
     *
     * Si el tipo recibido es nulo, está vacío o no es válido,
     * devuelve el tipo genérico APPLICATION_OCTET_STREAM.
     *
     * @param tipus cadena que contiene el tipo MIME del documento
     * @return tipo MIME correspondiente o tipo genérico por defecto
     */
    private MediaType obtenirMediaType(String tipus) {

        if (tipus == null || tipus.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(tipus);

        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * Elimina definitivamente un cliente mediante su identificador.
     *
     * Comprueba previamente que el cliente exista y solicita su eliminación
     * al servicio correspondiente. Si tiene reservas asignadas o se produce
     * algún error, muestra el mensaje correspondiente.
     *
     * @param id identificador del cliente que se desea eliminar
     * @param redirectAttributes atributos que se conservarán durante la redirección
     * @return ruta de redirección a la gestión de clientes
     */
    @PostMapping("/clients/eliminar/{id}")
    public String eliminarClient(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Optional<ClientJpaEntity> client = clientService.findById(id);

        if (client.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "El client no existeix.");

            return "redirect:/agent/clients";
        }

        try {
            boolean eliminat = clientService.eliminarClient(id);

            if (!eliminat) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "No es pot eliminar el client perquè té reserves assignades.");

                /**
                 * Redirige a la página de gestión de clientes cuando
                 * no es puede completar la eliminación.
                 */
                return "redirect:/agent/clients";
            }

            redirectAttributes.addFlashAttribute(
                    "success",
                    "El client s'ha eliminat definitivament.");

            return "redirect:/agent/clients";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No s'ha pogut eliminar el client perquè té registres associats o s'ha produït un error.");

            return "redirect:/agent/clients";
        }
    }
}
