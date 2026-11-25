
package LunarAxis.core;

import LunarAxis.diagnostics.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Interne Hilfsklasse zur Ausgabe der Start- und Metadaten der LunarAxis-Bibliothek.
 * <p>
 * Liest beim Initialisieren der Engine die Konfigurationsdatei {@code lunaraxis.properties} ein,
 * um Name, Versionsnummer, Entwickler-Informationen sowie den Stabilitätsstatus über den
 * zentralen {@link Logger} auf der Konsole auszugeben.
 * Diese Klasse ist paket-privat und gehört nicht zur öffentlichen Entwickler-API.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.8
 */
final class LunarAxis {

    /**
     * Privater Konstruktor zur Verhinderung von Instanziierungen.
     */
    private LunarAxis() {
    }

    /**
     * Liest die Bibliotheks-Eigenschaften aus der Konfigurationsdatei ein und gibt die
     * Start-Informationen (Version, Entwickler, Umgebung) über den Logger aus.
     */
    static void printStartupInfo() {
        Properties properties = new Properties();

        try (InputStream stream = LunarAxis.class.getClassLoader().getResourceAsStream("lunaraxis.properties")) {
            if (stream != null) {
                properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }

        } catch (IOException | SecurityException exception) {
            Logger.warn("LunarAxis", "Could not load library metadata: " + exception.getMessage());
        }

        String name = properties.getProperty("name", "LunarAxis");
        String version = properties.getProperty("version", "Unknown");
        String author = properties.getProperty("author", "Unknown");

        String status = getStatus(version);

        Logger.info("LunarAxis", name + " v" + version + " [" + status + "]");
        Logger.info("LunarAxis", "Developed by " + author);
        Logger.info("LunarAxis", "Java 11+ | TigerJython | Java2D");
    }

    /**
     * Bestimmt den Stabilitätsstatus der Bibliothek anhand des Versions-Strings.
     *
     * @param version Der Versions-String (z. B. "1.2.8-beta" oder "1.2.8").
     * @return Der lesbare Status (z. B. "Development", "Beta" oder "Stable").
     */
    private static String getStatus(String version) {
        String v = version.toLowerCase();

        if (v.contains("snapshot")) {
            return "Development";
        }
        if (v.contains("beta")) {
            return "Beta";
        }
        if (v.contains("alpha")) {
            return "Alpha";
        }
        if (v.contains("rc")) {
            return "Released";
        }

        return "Stable";
    }
}
