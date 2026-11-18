package cat.copernic.nothinganpri.dto;

/**
 * Respuesta del chatbot.
 * También se utiliza para enviar mensajes de error al cliente.
 */
public class ChatResponse {

    /** Texto de la respuesta generada por el chatbot. */
    private String resposta;

    /**
     * Constructor vacío.
     */
    public ChatResponse() {
    }

    /**
     * Constructor que inicializa la respuesta.
     *
     * @param resposta texto de la respuesta
     */
    public ChatResponse(String resposta) {
        this.resposta = resposta;
    }

    /**
     * Obtiene el texto de la respuesta.
     *
     * @return texto de la respuesta del chatbot
     */
    public String getResposta() {
        return resposta;
    }

    /**
     * Establece el texto de la respuesta.
     *
     * @param resposta texto de la respuesta
     */
    public void setResposta(String resposta) {
        this.resposta = resposta;
    }
}
