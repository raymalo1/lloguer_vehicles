
package cat.copernic.nothinganpri.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * Servei per consultar, registrar i gestionar els logs de l'aplicació.
 */
@Service
public class LogQueryService {

    private static final Path LOG_FILE =
            Paths.get("./logs/application.log");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Detecta el començament de cada registre del log.
    // Accepta logs amb mil·lisegons i sense mil·lisegons.
    private static final Pattern LOG_START = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{3})?\\s+(TRACE|DEBUG|INFO|WARN|ERROR)\\s+.*"
    );

    private static final Pattern LOG_LEVEL = Pattern.compile(
            "\\b(ERROR|WARN|INFO|DEBUG|TRACE)\\b"
    );

    /**
     * Retorna tots els logs agrupats.
     */
    public List<String> getLogs() throws IOException {
        return getGroupedLogs();
    }

    /**
     * Registra un error al fitxer de logs.
     */
    public void registrarError(String modulo, String mensaje) {
        registrarNivel("ERROR", modulo, mensaje);
    }

    /**
     * Registra un avís al fitxer de logs.
     */
    public void registrarWarning(String modulo, String mensaje) {
        registrarNivel("WARN", modulo, mensaje);
    }

    /**
     * Registra informació al fitxer de logs.
     */
    public void registrarInfo(String modulo, String mensaje) {
        registrarNivel("INFO", modulo, mensaje);
    }

    /**
     * Escriu un missatge al fitxer de logs.
     */
    private synchronized void registrarNivel(
            String nivel,
            String modulo,
            String mensaje) {

        try {
            Path directorio = LOG_FILE.getParent();

            if (directorio != null) {
                Files.createDirectories(directorio);
            }

            String fecha = LocalDateTime.now().format(FORMATTER);

            String linea = fecha
                    + " " + nivel
                    + " [" + modulo + "] "
                    + mensaje
                    + System.lineSeparator();

            Files.writeString(
                    LOG_FILE,
                    linea,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            System.err.println(
                    "Error al registrar el log: " + e.getMessage()
            );
        }
    }

    /**
     * Calcula les estadístiques dels logs.
     */
    public Map<String, Long> getStatistics(List<String> logs) {

        Map<String, Long> statistics = new HashMap<>();

        long errors = 0;
        long warnings = 0;
        long information = 0;

        for (String log : logs) {

            String firstLine = log.split("\\R", 2)[0];

            Matcher matcher = LOG_LEVEL.matcher(firstLine);

            if (matcher.find()) {

                String level = matcher.group(1);

                switch (level) {

                    case "ERROR":
                        errors++;
                        break;

                    case "WARN":
                        warnings++;
                        break;

                    case "INFO":
                        information++;
                        break;

                    default:
                        break;
                }
            }
        }

        statistics.put("total", (long) logs.size());
        statistics.put("errors", errors);
        statistics.put("warnings", warnings);
        statistics.put("information", information);

        return statistics;
    }

    /**
     * Agrupa les línies de cada log i mostra primer els més recents.
     */
    private List<String> getGroupedLogs() throws IOException {

        if (!Files.exists(LOG_FILE)) {
            return Collections.emptyList();
        }

        List<String> lines = Files.readAllLines(LOG_FILE);

        List<String> groupedLogs = new ArrayList<>();

        StringBuilder currentLog = null;

        for (String line : lines) {

            // Comprovar si la línia comença un registre nou.
            boolean isNewLog = LOG_START.matcher(line).matches();

            if (isNewLog) {

                // Guardar el registre anterior.
                if (currentLog != null) {
                    groupedLogs.add(currentLog.toString());
                }

                // Començar un registre nou.
                currentLog = new StringBuilder(line);

            } else if (currentLog != null) {

                // Mantenir juntes les línies addicionals
                // de les excepcions (stack trace).
                currentLog.append(System.lineSeparator())
                          .append(line);
            }
        }

        // Guardar l'últim registre.
        if (currentLog != null) {
            groupedLogs.add(currentLog.toString());
        }

        // Mostrar primer els registres més recents.
        Collections.reverse(groupedLogs);

        return groupedLogs;
    }

    /**
     * Esborra tot el contingut del fitxer de logs.
     */
    public void clearLogs() throws IOException {

        Files.writeString(
                LOG_FILE,
                "",
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}
