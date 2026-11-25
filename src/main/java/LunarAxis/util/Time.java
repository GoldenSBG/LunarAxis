package LunarAxis.util;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;

/**
 * Die Utility-Klasse {@code Time} bietet präzise Zeitmessungs- und Verzögerungsfunktionen.
 * <p>
 * Sie kann für Timer, Cooldowns, Animations-Berechnungen und Debugging-Zwecke
 * innerhalb der LunarAxis-Engine verwendet werden.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * startTime = Time.millis()
 * Time.sleep(500) // 500 ms pausieren
 * elapsedNanos = Time.nanos()
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public final class Time {

    /**
     * Privater Konstruktor zur Verhinderung von Instanziierungen.
     */
    private Time() {
    }

    /**
     * Gibt die aktuelle Systemzeit in Millisekunden zurück.
     * Basierend auf {@link System#currentTimeMillis()}.
     *
     * @return Die aktuelle Zeit in Millisekunden.
     */
    public static long millis() {
        return System.currentTimeMillis();
    }

    /**
     * Gibt den aktuellen Wert der hochauflösenden Systemzeit in Nanosekunden zurück.
     * Primär geeignet für präzise Leistungs- und Delta-Time-Messungen.
     * Basierend auf {@link System#nanoTime()}.
     *
     * @return Die aktuelle Zeit in Nanosekunden.
     */
    public static long nanos() {
        return System.nanoTime();
    }

    /**
     * Pausiert den aktuellen Ausführungs-Thread für die angegebene Anzahl an Millisekunden.
     * <p>
     * Fängt Unterbrechungen ({@link InterruptedException}) ab, stellt den Interrupt-Status des Threads
     * wieder her und protokolliert eine Warnung.
     * </p>
     *
     * @param millis Die Schlafdauer in Millisekunden (muss &ge; 0 sein).
     * @throws RuntimeException (Lunar-Error) Wenn {@code millis} negativ ist.
     */
    public static void sleep(long millis) {
        if (millis < 0) {
            throw Errors.argument(ErrorDetails.of("Time", "Sleep duration must not be negative")
                    .withActual(millis)
                    .withExpected("A duration greater than or equal to 0 milliseconds")
                    .withExplanation("A thread cannot sleep for a negative duration"), null);
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.report(LogLevel.WARN, ErrorDetails.of("Time", "Sleep was interrupted")
                    .withActual(millis)
                    .withExpected("The requested wait to finish without interruption")
                    .withExplanation("The wait ended early and the thread interrupt status has been restored"), e);
        }
    }
}