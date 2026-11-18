
package cat.copernic.nothinganpri.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Crida a l'API de Groq (OpenAI-compatible). Només fa la crida HTTP.
 */
@Component
public class GroqClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final LogQueryService logQueryService;

    public GroqClient(
            @Value("${groq.api-key:}") String apiKey,
            @Value("${groq.model:openai/gpt-oss-120b}") String model,
            @Value("${groq.url:https://api.groq.com/openai/v1}") String baseUrl,
            @Value("${groq.timeout-ms:15000}") int timeoutMs,
            LogQueryService logQueryService) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        this.logQueryService = logQueryService;
    }

    /** Retorna la resposta de la IA, o buit si no està configurada o la crida falla. */
    public Optional<String> generar(String instruccions, String pregunta) {
        if (apiKey.isEmpty()) {
            logQueryService.registrarWarning(
                    "GROQ",
                    "Groq no està configurat: defineix la variable d'entorn GROQ_API_KEY"
            );
            return Optional.empty();
        }

        // Estructura estàndard d'OpenAI/Groq
        Map<String, Object> cos = Map.of(
                "model", model,
                "temperature", 0.3,
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "system", "content", instruccions),
                        Map.of("role", "user", "content", pregunta)
                )
        );

        try {
            Map<?, ?> resposta = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(cos)
                    .retrieve()
                    .body(Map.class);

            return extreureText(resposta);
        } catch (RestClientException e) {
            logQueryService.registrarError(
                    "GROQ",
                    "Error cridant l'API de Groq: " + e.getMessage()
            );
            return Optional.empty();
        }
    }

    /** Extreu el text del camp choices[0].message.content del JSON de Groq/OpenAI. */
    public static Optional<String> extreureText(Map<?, ?> arrel) {
        if (arrel == null
                || !(arrel.get("choices") instanceof List<?> choices) || choices.isEmpty()
                || !(choices.get(0) instanceof Map<?, ?> choice)
                || !(choice.get("message") instanceof Map<?, ?> message)
                || !(message.get("content") instanceof String content)) {
            return Optional.empty();
        }

        String resultat = content.trim();
        return resultat.isEmpty() ? Optional.empty() : Optional.of(resultat);
    }
}
