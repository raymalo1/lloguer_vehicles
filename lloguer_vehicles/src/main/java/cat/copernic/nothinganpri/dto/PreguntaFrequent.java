/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.nothinganpri.dto;

import java.time.Instant;

/** Una fila de l'estadística de preguntes freqüents. */
public class PreguntaFrequent {

    private String pregunta;      // un exemple de com l'han escrita
    private long vegades;         // quantes cops s'ha fet
    private Instant ultimaVegada;

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public long getVegades() { return vegades; }
    public void setVegades(long vegades) { this.vegades = vegades; }

    public Instant getUltimaVegada() { return ultimaVegada; }
    public void setUltimaVegada(Instant ultimaVegada) { this.ultimaVegada = ultimaVegada; }
}