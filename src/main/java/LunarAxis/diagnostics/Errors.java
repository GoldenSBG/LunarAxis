package LunarAxis.diagnostics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Die Klasse {@code Errors} fungiert als zentrale Exception-Fabrik (Factory) für die LunarAxis-Engine.
 * <p>
 * Sie ermöglicht das Erzeugen und Verknüpfen von standardisierten, aussagekräftigen Fehlern
 * unter Beibehaltung der originalen Java-Exceptiontypen sowie deren checked/unchecked-Verträge.
 * Zugehörige {@link ErrorDetails} werden über eine schwache Map ({@link WeakHashMap}) speicherschonend
 * direkt an die jeweilige Exception gebunden, ohne den eigentlichen Stacktrace oder die Ursache zu verändern.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public final class Errors {

    private static final Map<Throwable, ErrorDetails> DETAILS = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Privater Konstruktor zur Verhinderung von Instanziierungen, da es sich um eine reine Utility-Klasse handelt.
     */
    private Errors() {
    }

    /**
     * Verknüpft eine bestehende Exception mit detaillierten {@link ErrorDetails}.
     * <p>
     * Der konkrete Exceptiontyp, Stacktrace, die Ursache (Cause) sowie unterdrückte Ausnahmen (Suppressed Exceptions)
     * bleiben dabei vollständig erhalten. Dies ermöglicht auch die Verwendung spezieller Java-Standard-Exceptions,
     * für die keine eigene Fabrik-Methode existiert.
     * </p>
     *
     * @param exception Die zu beschreibende Exception (muss ungleich {@code null} sein).
     * @param details   Die zuzuordnenden {@link ErrorDetails} (müssen ungleich {@code null} sein).
     * @param <T>       Der konkrete Typ der Exception.
     * @return Die modifizierte Exception mit referenzierten Details.
     */
    public static <T extends Throwable> T describe(T exception, ErrorDetails details) {
        if (exception == null) {
            throw new NullPointerException("Exception must not be null");
        }
        if (details == null) {
            throw new NullPointerException("Details must not be null");
        }

        DETAILS.put(exception, details);
        return exception;
    }

    /**
     * Ruft die verknüpften {@link ErrorDetails} einer Exception ab.
     *
     * @param exception Die abzufragende Exception.
     * @return Die zugehörigen {@link ErrorDetails} oder {@code null}, falls keine verknüpft sind.
     */
    public static ErrorDetails detailsOf(Throwable exception) {
        return exception == null ? null : DETAILS.get(exception);
    }

    /**
     * Extrahiert eine sichere Fehlermeldung aus den {@link ErrorDetails}.
     *
     * @param details Die {@link ErrorDetails}.
     * @return Der Fehlertitel als String oder ein Fallback-Text, falls leer.
     */
    private static String message(ErrorDetails details) {
        if (details == null) {
            throw new NullPointerException("Details must not be null");
        }

        String title = details.getTitle();
        return title == null || title.isBlank() ? "Error without further description" : title;
    }

    /**
     * Erzeugt eine neue {@link IllegalArgumentException} mit verknüpften Fehlerdetails.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link IllegalArgumentException}.
     */
    public static IllegalArgumentException argument(ErrorDetails details, Throwable cause) {
        return describe(new IllegalArgumentException(message(details), cause), details);
    }

    /**
     * Erzeugt eine neue {@link IllegalStateException} mit verknüpften Fehlerdetails.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link IllegalStateException}.
     */
    public static IllegalStateException state(ErrorDetails details, Throwable cause) {
        return describe(new IllegalStateException(message(details), cause), details);
    }

    /**
     * Erzeugt eine neue {@link NullPointerException} mit verknüpften Fehlerdetails.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link NullPointerException}.
     */
    public static NullPointerException nullValue(ErrorDetails details, Throwable cause) {
        NullPointerException exception = new NullPointerException(message(details));

        if (cause != null) {
            exception.initCause(cause);
        }
        return describe(exception, details);
    }

    /**
     * Erzeugt eine neue {@link IndexOutOfBoundsException} mit verknüpften Fehlerdetails.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link IndexOutOfBoundsException}.
     */
    public static IndexOutOfBoundsException index(ErrorDetails details, Throwable cause) {
        IndexOutOfBoundsException exception = new IndexOutOfBoundsException(message(details));

        if (cause != null) {
            exception.initCause(cause);
        }
        return describe(exception, details);
    }

    /**
     * Erzeugt eine neue {@link ArithmeticException} mit verknüpften Fehlerdetails.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link ArithmeticException}.
     */
    public static ArithmeticException arithmetic(ErrorDetails details, Throwable cause) {
        ArithmeticException exception = new ArithmeticException(message(details));

        if (cause != null) {
            exception.initCause(cause);
        }
        return describe(exception, details);
    }

    /**
     * Erzeugt eine neue {@link UnsupportedOperationException} mit verknüpften Fehlerdetails.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link UnsupportedOperationException}.
     */
    public static UnsupportedOperationException unsupported(ErrorDetails details, Throwable cause) {
        return describe(new UnsupportedOperationException(message(details), cause), details);
    }

    /**
     * Erzeugt eine checked {@link IOException}.
     * <p>
     * Der Aufrufer muss diese Exception zwingend abfangen (try-catch) oder in der Methodensignatur deklarieren (throws).
     * </p>
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link IOException}.
     */
    public static IOException io(ErrorDetails details, Throwable cause) {
        return describe(new IOException(message(details), cause), details);
    }

    /**
     * Erzeugt eine checked {@link FileNotFoundException}.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link FileNotFoundException}.
     */
    public static FileNotFoundException fileNotFound(ErrorDetails details, Throwable cause) {
        FileNotFoundException exception = new FileNotFoundException(message(details));

        if (cause != null) {
            exception.initCause(cause);
        }
        return describe(exception, details);
    }

    /**
     * Erzeugt eine allgemeine {@link RuntimeException} mit verknüpften Fehlerdetails.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link RuntimeException}.
     */
    public static RuntimeException runtime(ErrorDetails details, Throwable cause) {
        return describe(new RuntimeException(message(details), cause), details);
    }

    /**
     * Erzeugt eine spezifische {@link LunarException} für interne Engine-Fehler und bestehende Catch-Verträge.
     *
     * @param details Die strukturierten {@link ErrorDetails}.
     * @param cause   Die zugrunde liegende Ursache (Throwable) oder {@code null}.
     * @return Die erzeugte {@link LunarException}.
     */
    public static LunarException lunar(ErrorDetails details, Throwable cause) {
        return describe(new LunarException(message(details), cause), details);
    }
}