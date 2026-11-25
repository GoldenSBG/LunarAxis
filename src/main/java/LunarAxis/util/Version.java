package LunarAxis.util;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Die Utility-Klasse {@code Version} liest und stellt die Versionsmetadaten der LunarAxis-Engine bereit.
 * <p>
 * Die Versionsnummer wird dynamisch aus der Konfigurationsdatei {@code lunaraxis.properties} aus dem Classpath
 * geladen. Falls die Datei nicht vorhanden ist oder ein Lesefehler auftritt, fällt das System
 * automatisch auf den Standardwert {@code "unknown"} zurück, ohne den Anwendungsfluss zu unterbrechen.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * currentVersion = Version.VERSION
 * System.out.println("LunarAxis Version: " + currentVersion)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public final class Version {

    /**
     * Die aktuell geladene Versionsnummer der LunarAxis-Engine (z. B. "1.2.6").
     * Bei fehlenden Metadaten enthält die Konstante den Wert {@code "unknown"}.
     */
    public static final String VERSION = loadVersion();

    /**
     * Privater Konstruktor zur Verhinderung von Instanziierungen, da es sich um eine reine Utility-Klasse handelt.
     */
    private Version() {}

    /**
     * Lädt den Versions-String aus der Eigenschaftsdatei {@code lunaraxis.properties} im Classpath.
     *
     * @return Die ausgelesene Versionsnummer oder {@code "unknown"}, falls das Laden fehlschlägt oder die Datei fehlt.
     */
    private static String loadVersion() {
        try (InputStream in = Version.class.getClassLoader().getResourceAsStream("lunaraxis.properties")) {
            if (in == null) {
                return "unknown";
            }

            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version", "unknown");
        } catch (Exception e) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("Version", "Version metadata could not be read")
                    .withActual("lunaraxis.properties")
                    .withExpected("A readable properties resource containing optional version metadata")
                    .withExplanation("The library will continue using the version value unknown"), e);
            return "unknown";
        }
    }
}