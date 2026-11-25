package LunarAxis.diagnostics;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Die Klasse {@code Logger} bildet das zentrale Protokollierungssystem der LunarAxis-Bibliothek.
 * <p>
 * Sie erlaubt das strukturierte Logging von Nachrichten, Warnungen und Fehlern mit unterschiedlichen Schweregraden ({@link LogLevel}).
 * Ausnahmen (Exceptions) werden hierbei ausschließlich durch explizite Logger-Aufrufe protokolliert und kontrollieren
 * keine unbehandelten Interpreter-Exceptions. Der Logger ist sowohl intern innerhalb der Engine
 * als auch direkt von TigerJython- oder Java-Nutzern verwendbar.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * Logger.info("Game", "Engine erfolgreich gestartet")
 * Logger.error("Physics", "Kollision konnte nicht berechnet werden", exception)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public final class Logger {

    private static volatile LogLevel currentLevel = LogLevel.INFO;
    private static volatile boolean diagnosticMode = false;

    private static final Object OUTPUT_LOCK = new Object();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Privater Konstruktor zur Verhinderung von Instanziierungen, da es sich um eine reine Utility-Klasse handelt.
     */
    private Logger() {
    }

    /**
     * Setzt das minimale Schwellenwert-Log-Level. Nachrichten mit geringerer Priorität werden ignoriert.
     *
     * @param level Das neue {@link LogLevel}.
     */
    public static void setLevel(LogLevel level) {
        if (level != null) {
            currentLevel = level;
        }
    }

    /**
     * Gibt das aktuell aktive minimale Log-Level zurück.
     *
     * @return Das aktuelle {@link LogLevel}.
     */
    public static LogLevel getLevel() {
        return currentLevel;
    }

    /**
     * Aktiviert oder deaktiviert den Diagnosemodus (Diagnostic Mode).
     * Im Diagnosemodus werden erweiterte technische Details wie vollständige Stacktraces ausgegeben.
     *
     * @param enabled {@code true} zum Aktivieren, sonst {@code false}.
     */
    public static void setDiagnosticMode(boolean enabled) {
        diagnosticMode = enabled;
    }

    /**
     * Prüft, ob der Diagnosemodus aktiv ist.
     *
     * @return {@code true}, wenn aktiv, sonst {@code false}.
     */
    public static boolean isDiagnosticMode() {
        return diagnosticMode;
    }

    // API für User

    /**
     * Protokolliert eine Debug-Nachricht ohne spezifische Quellangabe.
     *
     * @param message Die Protokollnachricht.
     */
    public static void debug(String message) {
        debug(null, message);
    }

    /**
     * Protokolliert eine Informationsnachricht ohne spezifische Quellangabe.
     *
     * @param message Die Protokollnachricht.
     */
    public static void info(String message) {
        info(null, message);
    }

    /**
     * Protokolliert eine Warnung ohne spezifische Quellangabe.
     *
     * @param message Die Warnmeldung.
     */
    public static void warn(String message) {
        warn(null, message);
    }

    /**
     * Protokolliert eine Fehlermeldung ohne spezifische Quellangabe.
     *
     * @param message Die Fehlermeldung.
     */
    public static void error(String message) {
        error(null, message);
    }

    /**
     * Protokolliert einen kritischen Fehler (Fatal) ohne spezifische Quellangabe.
     *
     * @param message Die Fehlermeldung.
     */
    public static void fatal(String message) {
        fatal(null, message);
    }

    // API für Engine

    /**
     * Protokolliert eine Debug-Nachricht mit angegebener Quelle.
     *
     * @param source  Die Quellkomponente.
     * @param message Die Nachricht.
     */
    public static void debug(String source, String message) {
        simple(LogLevel.DEBUG, source, message);
    }

    /**
     * Protokolliert eine Informationsnachricht mit angegebener Quelle.
     *
     * @param source  Die Quellkomponente.
     * @param message Die Nachricht.
     */
    public static void info(String source, String message) {
        simple(LogLevel.INFO, source, message);
    }

    /**
     * Protokolliert eine strukturierte Warnung mit angegebener Quelle.
     *
     * @param source  Die Quellkomponente.
     * @param message Die Warnmeldung.
     */
    public static void warn(String source, String message) {
        report(LogLevel.WARN, ErrorDetails.of(source, message), null);
    }

    /**
     * Protokolliert einen strukturierten Fehler mit angegebener Quelle.
     *
     * @param source  Die Quellkomponente.
     * @param message Die Fehlermeldung.
     */
    public static void error(String source, String message) {
        report(LogLevel.ERROR, ErrorDetails.of(source, message), null);
    }

    /**
     * Protokolliert einen strukturierten kritischen Fehler mit angegebener Quelle.
     *
     * @param source  Die Quellkomponente.
     * @param message Die Fehlermeldung.
     */
    public static void fatal(String source, String message) {
        report(LogLevel.FATAL, ErrorDetails.of(source, message), null);
    }

    // Exception Logging

    /**
     * Protokolliert eine Warnung im Zusammenhang mit einer Exception.
     *
     * @param source    Die Quellkomponente.
     * @param message   Die Warnmeldung.
     * @param throwable Die zugehörige {@link Throwable}-Exception.
     */
    public static void warn(String source, String message, Throwable throwable) {
        report(LogLevel.WARN, ErrorDetails.of(source, message), throwable);
    }

    /**
     * Protokolliert einen Fehler im Zusammenhang mit einer Exception.
     *
     * @param source    Die Quellkomponente.
     * @param message   Die Fehlermeldung.
     * @param throwable Die zugehörige {@link Throwable}-Exception.
     */
    public static void error(String source, String message, Throwable throwable) {
        report(LogLevel.ERROR, ErrorDetails.of(source, message), throwable);
    }

    /**
     * Protokolliert einen kritischen Fehler im Zusammenhang mit einer Exception.
     *
     * @param source    Die Quellkomponente.
     * @param message   Die Fehlermeldung.
     * @param throwable Die zugehörige {@link Throwable}-Exception.
     */
    public static void fatal(String source, String message, Throwable throwable) {
        report(LogLevel.FATAL, ErrorDetails.of(source, message), throwable);
    }

    /**
     * Meldet eine Exception als Fehlerprotokoll.
     *
     * @param source    Die Quellkomponente.
     * @param message   Die Nachricht.
     * @param throwable Die {@link Throwable}-Exception.
     */
    public static void reportException(String source, String message, Throwable throwable) {
        error(source, message, throwable);
    }

    /**
     * Verwendet bereits vorhandene strukturierte Details einer Exception für den Bericht.
     *
     * @param throwable Die {@link Throwable}-Exception mit verknüpften Details.
     */
    public static void reportException(Throwable throwable) {
        report(LogLevel.ERROR, null, throwable);
    }

    /**
     * Erstellt einen allgemeinen, strukturierten Protokollbericht (auch ohne Exception verwendbar).
     * <p>
     * Explizit übergebene {@link ErrorDetails} haben Vorrang vor in der Exception hinterlegten Details.
     * Fehlende Felder werden automatisch ergänzt.
     * </p>
     *
     * @param level     Das {@link LogLevel} des Berichts.
     * @param details   Optionale {@link ErrorDetails} oder {@code null}.
     * @param throwable Optionale {@link Throwable}-Exception oder {@code null}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code level} {@code null} ist.
     */
    public static void report(LogLevel level, ErrorDetails details, Throwable throwable) {
        if (level == null) {
            throw Errors.argument(ErrorDetails.of("Logger", "Log level must not be null")
                    .withActual(null)
                    .withExpected("DEBUG, INFO, WARN, ERROR or FATAL")
                    .withExplanation("A log level is required to filter and route the report"), null);
        }

        LogLevel threshold = currentLevel;
        boolean diagnostics = diagnosticMode;

        if (level.getPriority() < threshold.getPriority()) {
            return;
        }

        ErrorDetails stored = Errors.detailsOf(throwable);
        StackTraceElement location;

        if (throwable != null) {
            location = ErrorDetails.findLocation(throwable);
        } else if (details != null) {
            location = details.getCallSite();
        } else {
            location = ErrorDetails.findLocation(new Throwable());
        }

        String component = choose(details == null ? null : details.getComponent(), stored == null ? null : stored.getComponent(),"LunarAxis");
        String title = choose(details == null ? null : details.getTitle(), stored == null ? null : stored.getTitle(), throwable == null ? null : throwable.getMessage());
        title = choose(title, null, "No error description available");

        String expected = choose(details == null ? null : details.getExpected(), stored == null ? null : stored.getExpected(), "Not specified");
        String explanation = choose(details == null ? null : details.getExplanation(), stored == null ? null : stored.getExplanation(), "No additional explanation provided");

        String actual = "Not specified";
        if (details != null && details.hasActual()) {
            actual = details.getActual();
        } else if (stored != null && stored.hasActual()) {
            actual = stored.getActual();
        }

        StringBuilder output = new StringBuilder();
        output.append("[LunarAxis ").append(level.name()).append("] ")
                .append(component).append('\n').append('\n');

        field(output, "Error", title);

        if (throwable == null) {
            output.append("Location: Log message invocation point\n");
        }

        field(output, "Class", location == null ? "Cannot be determined" : location.getClassName());
        field(output, "Method", location == null ? "Cannot be determined" : location.getMethodName());
        field(output, "File", location == null || location.getFileName() == null ? "Cannot be determined" : location.getFileName());
        field(output, "Line", location == null || location.getLineNumber() < 0 ? "Cannot be determined" : Integer.toString(location.getLineNumber()));

        output.append('\n');
        field(output, "Passed value", actual);
        field(output, "Expected", expected);
        output.append("\nExplanation:\n");
        output.append(multiline(explanation)).append('\n');

        appendCauses(output, title, throwable);

        if (diagnostics && throwable != null) {
            output.append("\nTechnical details:\n");
            field(output, "Exception", throwable.getClass().getName());
            field(output, "Location", location == null ? "Cannot be determined" : location.toString());

            StringWriter writer = new StringWriter();
            PrintWriter printer = new PrintWriter(writer);
            throwable.printStackTrace(printer);
            printer.flush();

            output.append("\nFull stack trace:\n");
            output.append(writer);
        }

        emit(level, output.toString().stripTrailing());
    }

    /**
     * Interne Hilfsmethode zum Ausgeben einfacher, unstrukturierter Log-Nachrichten.
     *
     * @param level   Das {@link LogLevel}.
     * @param source  Die Quellkomponente.
     * @param message Die Nachricht.
     */
    private static void simple(LogLevel level, String source, String message) {
        if (level.getPriority() < currentLevel.getPriority()) {
            return;
        }
        String text = "[LunarAxis " + level.name() + "] " + choose(source, null, "LunarAxis") + ": " + choose(message, null, "No message provided");
        emit(level, text);
    }

    /**
     * Wählt den ersten gültigen (nicht leeren) String aus oder gibt einen Fallback zurück.
     *
     * @param first    Erster String.
     * @param second   Zweiter String.
     * @param fallback Fallback-Wert.
     * @return Der gewählte String.
     */
    private static String choose(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }

    /**
     * Formatiert Text für mehrzeilige Ausgaben durch Einrückungen.
     *
     * @param text Der Ursprungstext.
     * @return Der formatierte Text.
     */
    private static String multiline(String text) {
        return String.valueOf(text).replace("\r\n", "\n").replace('\r', '\n').replace("\n", "\n    ");
    }

    /**
     * Fügt ein formatiertes Feld mit Label und Wert an den {@link StringBuilder} an.
     *
     * @param output Der {@link StringBuilder}.
     * @param label  Das Feld-Label.
     * @param value  Der Feldwert.
     */
    private static void field(StringBuilder output, String label, String value) {
        output.append(label).append(": ").append(multiline(value)).append('\n');
    }

    /**
     * Hängt Ursachen (Causes) und unterdrückte Exceptions (Suppressed) rekursiv an den Log-Bericht an.
     *
     * @param output    Der {@link StringBuilder}.
     * @param title     Der Fehlertitel.
     * @param throwable Die {@link Throwable}-Exception.
     */
    private static void appendCauses(StringBuilder output, String title, Throwable throwable) {
        if (throwable == null) {
            return;
        }

        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        Set<String> messages = new LinkedHashSet<>();
        messages.add(title.trim());

        List<Throwable> pending = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        pending.add(throwable);
        labels.add("Details");

        for (int i = 0; i < pending.size(); i++) {
            Throwable current = pending.get(i);
            String label = labels.get(i);

            if (!visited.add(current)) {
                continue;
            }

            String message = current.getMessage();
            if (message != null && !message.isBlank()) {
                String trimmed = message.trim();
                if (messages.add(trimmed)) {
                    field(output, label, trimmed);
                } else if (current != throwable) {
                    field(output, label, current.getClass().getName() + " with a message already shown");
                }
            } else {
                field(output, label, current.getClass().getName() + " without an error message");
            }

            Throwable cause = current.getCause();
            if (cause != null) {
                pending.add(cause);
                labels.add("Caused");
            }

            for (Throwable suppressed : current.getSuppressed()) {
                pending.add(suppressed);
                labels.add("Suppressed");
            }
        }
    }

    /**
     * Gibt die formatierte Log-Nachricht thread-sicher auf der Standardkonsole ({@code System.out} oder {@code System.err}) aus.
     *
     * @param level   Das {@link LogLevel}.
     * @param message Die finale Textnachricht.
     */
    private static void emit(LogLevel level, String message) {
        synchronized (OUTPUT_LOCK) {
            if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
                System.err.println(message);
            } else {
                System.out.println(message);
            }
        }
    }

    /**
     * Internes Legacy-Log-Verfahren mit Zeitstempel und formatierter Quellspalte.
     *
     * @param level   Das {@link LogLevel}.
     * @param source  Die Quellkomponente.
     * @param message Die Nachricht.
     */
    private static void log(LogLevel level, String source, String message) {
        if (level.getPriority() < currentLevel.getPriority()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);
        StringBuilder builder = new StringBuilder();

        builder.append("[").append(timestamp).append("] ");

        builder.append("[").append(String.format("%-5s", level.name())).append("] ");

        if (source != null && !source.isBlank()) {
            // Source wird auf 15 Zeichen formatiert für eine saubere Konsolen-Ausgabe
            builder.append("[").append(String.format("%-15s", source)).append("] ");
        }

        builder.append(message);

        if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
            System.err.println(builder);
        } else {
            System.out.println(builder);
        }
    }
}