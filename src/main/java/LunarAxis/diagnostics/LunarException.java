package LunarAxis.diagnostics;

/**
 * Die Klasse {@code LunarException} repräsentiert die spezifische Standard-Ausnahme (Exception) der LunarAxis-Bibliothek.
 * <p>
 * Sie erweitert {@link RuntimeException} und dient dazu, bibliotheksspezifische Fehler und Unregelmässigkeiten
 * klar von normalen, systemweiten Java-Fehlern zu trennen. Die Ausnahme speichert Nachricht
 * und Ursache; das Protokollieren erfolgt dabei ausdrücklich kontrolliert durch den Aufrufer und nicht
 * automatisch beim Erzeugen der Instanz.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class LunarException extends RuntimeException {

    /**
     * Erstellt eine neue {@code LunarException} mit einer spezifischen Fehlermeldung.
     *
     * @param message Die Beschreibung des Fehlers.
     */
    public LunarException(String message) {
        super(message);
        //Logger.error("LunarException", message);
    }

    /**
     * Erstellt eine neue {@code LunarException} mit einer Fehlermeldung und einer zugrunde liegenden Ursache.
     *
     * @param message Die Beschreibung des Fehlers.
     * @param cause   Die ursprüngliche {@link Throwable}-Ursache.
     */
    public LunarException(String message, Throwable cause) {
        super(message, cause);
        //Logger.error("LunarException", message, cause);
    }

    /**
     * Erstellt eine neue {@code LunarException} basierend auf einer übergeordneten Ursache.
     *
     * @param cause Die ursprüngliche {@link Throwable}-Ursache.
     */
    public LunarException(Throwable cause) {
        super(cause);
        //Logger.error("LunarException", cause != null ? cause.getMessage() : "Unbekannter Fehler", cause);
    }
}