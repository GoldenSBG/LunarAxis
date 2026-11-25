package LunarAxis.diagnostics;

/**
 * Die Enumeration {@code LogLevel} definiert die verfügbarten Schweregrade (Log-Level) für Protokollierungen in der LunarAxis-Bibliothek.
 * <p>
 * Je höher der numerische Prioritätswert ist, desto wichtiger ist die jeweilige Meldung.
 * Die Rangfolge lautet aufsteigend:
 * {@code DEBUG < INFO < WARN < ERROR < FATAL}
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public enum LogLevel {

    /**
     * Detaillierte Diagnoseinformationen für Entwicklungs- und Fehlersuchzwecke.
     */
    DEBUG(0),

    /**
     * Allgemeine Informtionsmeldungen über den normalen Programmverlauf.
     */
    INFO(1),

    /**
     * Warnungen über unerwartete Situationen oder potenzielle Probleme, die den Betrieb nicht sofort abbrechen.
     */
    WARN(2),

    /**
     * Fehler, bei denen bestimmte Operationen oder Teilbereiche fehlgeschlagen sind.
     */
    ERROR(3),

    /**
     * Schwerwiegende, kritische Fehler, die zum sofortigen Abbruch der Anwendung führen können.
     */
    FATAL(4);

    private final int priority;

    /**
     * Initialisiert ein Log-Level mit einer festen Priorität.
     *
     * @param priority Der numerische Prioritätswert.
     */
    LogLevel(int priority) {
        this.priority = priority;
    }

    /**
     * Gibt den numerischen Prioritätswert des Log-Levels zurück.
     *
     * @return Die Priorität als {@code int}.
     */
    public int getPriority() {
        return priority;
    }
}