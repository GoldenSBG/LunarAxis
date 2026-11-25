package LunarAxis.diagnostics;

/**
 * Die Utility-Klasse {@code SystemInfo} stellt plattform- und laufzeitspezifische Systeminformationen
 * für Diagnose- und Logging-Zwecke der LunarAxis-Engine bereit.
 * <p>
 * Sie greift auf System-Properties und Laufzeitparameter zu, um Details wie Java-Version,
 * Betriebssystem, Architektur, aktiven Benutzer und verfügbaren Arbeitsspeicher abzufragen.
 * Alle Methoden sind statisch; eine Instanziierung der Klasse ist nicht möglich.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public final class SystemInfo {

    /**
     * Privater Konstruktor zur Verhinderung von Instanziierungen.
     */
    private SystemInfo() {
    }

    /**
     * Gibt die Version der aktuell verwendeten Java-Laufzeitumgebung zurück.
     *
     * @return Ein {@link String} mit der Java-Versionsnummer (z. B. "17.0.1").
     */
    public static String javaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Gibt den Namen des Betriebssystems zurück.
     *
     * @return Ein {@link String} mit dem Namen des Betriebssystems (z. B. "Windows 11" oder "Mac OS X").
     */
    public static String osName() {
        return System.getProperty("os.name");
    }

    /**
     * Gibt die Prozessor-Architektur des Systems zurück.
     *
     * @return Ein {@link String} mit der Architektur (z. B. "amd64" oder "aarch64").
     */
    public static String osArch() {
        return System.getProperty("os.arch");
    }

    /**
     * Gibt den Namen des aktuell angemeldeten System-Benutzers zurück.
     *
     * @return Ein {@link String} mit dem Benutzernamen.
     */
    public static String user() {
        return System.getProperty("user.name");
    }

    /**
     * Ermittelt den maximal für die Java Virtual Machine (JVM) verfügbaren Arbeitsspeicher in Megabyte (MB).
     *
     * @return Der maximale Arbeitsspeicher in MB als {@code long}.
     */
    public static long maxMemoryMB() {
        return Runtime.getRuntime().maxMemory() / 1024 / 1024;
    }
}