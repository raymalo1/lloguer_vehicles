package cat.copernic.nothinganpri.dto;

/**
 * Cuerpo de la petición POST /api/chatbot/preguntar.
 * Contiene el mensaje que el usuario envía al chatbot.
 */
public class ChatRequest {

    /** Mensaje o pregunta del usuario. */
    private String missatge;

    /**
     * Obtiene el mensaje enviado por el usuario.
     *
     * @return el texto de la pregunta
     */
    public String getMissatge() {
        return missatge;
    }

    /**
     * Establece el mensaje enviado por el usuario.
     *
     * @param missatge texto de la pregunta
     */
    public void setMissatge(String missatge) {
        this.missatge = missatge;
    }
}
