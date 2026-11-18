package cat.copernic.nothinganpri.controller;

import cat.copernic.nothinganpri.dto.ChatRequest;
import cat.copernic.nothinganpri.dto.ChatResponse;
import cat.copernic.nothinganpri.dto.PreguntaFrequent;
import cat.copernic.nothinganpri.service.ChatbotLimitException;
import cat.copernic.nothinganpri.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del chatbot.
 * Solo recibe la petición, llama al servicio y traduce los errores a códigos HTTP.
 */
@RestController
public class ChatbotController {

    /** Servicio de lógica del chatbot. */
    private final ChatbotService service;

    /**
     * Constructor del controlador del chatbot.
     *
     * @param service servicio del chatbot
     */
    public ChatbotController(ChatbotService service) {
        this.service = service;
    }

    /**
     * Procesa una pregunta del usuario y devuelve la respuesta del chatbot.
     * Endpoint público (también accesible para visitantes sin sesión).
     *
     * @param request cuerpo de la petición con el mensaje
     * @param authentication información de autenticación del usuario
     * @param http petición HTTP (para obtener la IP si no hay sesión)
     * @return respuesta del chatbot o mensaje de error con el código HTTP adecuado
     */
    @PostMapping("/api/chatbot/preguntar")
    public ResponseEntity<ChatResponse> preguntar(@RequestBody ChatRequest request,
                                                  Authentication authentication,
                                                  HttpServletRequest http) {
        boolean identificat = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        String email = identificat ? authentication.getName() : null;
        String clauLimit = identificat ? email : http.getRemoteAddr();

        try {
            return ResponseEntity.ok(new ChatResponse(
                    service.preguntar(request.getMissatge(), email, clauLimit)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ChatResponse(e.getMessage()));
        } catch (ChatbotLimitException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ChatResponse(e.getMessage()));
        }
    }

    /**
     * Devuelve las preguntas más frecuentes del chatbot.
     * Accesible solo para AGENT y ADMIN.
     *
     * @param limit número máximo de preguntas a devolver (por defecto 10)
     * @return lista de preguntas frecuentes
     */
    @GetMapping("/agent/chatbot/preguntes-frequents")
    public List<PreguntaFrequent> preguntesFrequents(@RequestParam(defaultValue = "10") int limit) {
        return service.preguntesFrequents(limit);
    }
}
