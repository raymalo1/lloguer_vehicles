package cat.copernic.nothinganpri.service;

/**
 * Excepción lanzada cuando el usuario ha realizado demasiadas preguntas
 * al chatbot en poco tiempo (límite de rate limiting).
 */
public class ChatbotLimitException extends RuntimeException {

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param missatge mensaje descriptivo del error de límite
     */
    public ChatbotLimitException(String missatge) {
        super(missatge);
    }
}
