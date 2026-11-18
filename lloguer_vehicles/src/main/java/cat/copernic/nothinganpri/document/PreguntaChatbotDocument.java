/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.nothinganpri.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Una pregunta feta al chatbot (col·lecció "chatbot_preguntes" de MongoDB).
 * Serveix per saber quines són les preguntes més freqüents.
 */
@Document(collection = "chatbot_preguntes")
public class PreguntaChatbotDocument {

    @Id
    private String id;

    // Text tal com l'ha escrit l'usuari
    private String pregunta;

    // Sense majúscules, accents ni signes: "¿Qué es la FIANZA?" i "que es la fianza" compten com la mateixa
    @Indexed
    private String preguntaNormalitzada;

    // Resposta donada; null si la IA no ha pogut respondre
    private String resposta;

    // false si ha fallat la IA i s'ha donat la resposta de reserva
    private boolean resolta;

    // Només si l'usuari tenia la sessió iniciada
    private String usuariEmail;

    @Indexed
    private Instant dataHora;

    private long duradaMs;

    /**
     * Constructor vacío.
     */
    public PreguntaChatbotDocument() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getPreguntaNormalitzada() { return preguntaNormalitzada; }
    public void setPreguntaNormalitzada(String preguntaNormalitzada) { this.preguntaNormalitzada = preguntaNormalitzada; }

    public String getResposta() { return resposta; }
    public void setResposta(String resposta) { this.resposta = resposta; }

    public boolean isResolta() { return resolta; }
    public void setResolta(boolean resolta) { this.resolta = resolta; }

    public String getUsuariEmail() { return usuariEmail; }
    public void setUsuariEmail(String usuariEmail) { this.usuariEmail = usuariEmail; }

    public Instant getDataHora() { return dataHora; }
    public void setDataHora(Instant dataHora) { this.dataHora = dataHora; }

    public long getDuradaMs() { return duradaMs; }
    public void setDuradaMs(long duradaMs) { this.duradaMs = duradaMs; }
}