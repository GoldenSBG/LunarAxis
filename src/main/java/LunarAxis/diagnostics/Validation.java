package LunarAxis.diagnostics;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Die Klasse {@code Validation} bietet eine Sammlung von Validierungsmethoden ohne Logging-Nebeneffekte.
 * <p>
 * Bestehende {@link LunarException}-Verträge bleiben erhalten, während neue semantische Prüfungen
 * gezielt passende Java-Standard-Exceptiontypen (wie {@link IllegalArgumentException}, {@link NullPointerException}
 * oder {@link ArithmeticException}) auslösen.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public final class Validation {

    /**
     * Privater Konstruktor zur Verhinderung von Instanziierungen.
     */
    private Validation() {
    }

    /**
     * Erstellt standardisierte {@link ErrorDetails} für Validierungsprüfungen.
     *
     * @param message Die Fehlermeldung.
     * @param actual  Der tatsächliche Ist-Wert.
     * @param expected Die Erwartung an den Wert.
     * @return Die erzeugte {@link ErrorDetails}-Instanz.
     */
    private static ErrorDetails details(String message, Object actual, String expected) {
        return ErrorDetails.of(null, message)
                .withActual(actual).withExpected(expected)
                .withExplanation("The specified requirement is not met");
    }

    /**
     * Prüft, ob ein Objekt ungleich {@code null} ist.
     * Löst im Fehlerfall eine {@link LunarException} aus.
     *
     * @param object  Das zu prüfende Objekt.
     * @param message Die Fehlermeldung.
     * @throws LunarException (Lunar-Error) Wenn das Objekt {@code null} ist.
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw Errors.lunar(details(message, null, "A non-null reference"), null);
        }
    }

    /**
     * Prüft, ob ein Text nicht leer und nicht nur aus Leerzeichen bestehend ist.
     * Löst im Fehlerfall eine {@link LunarException} aus.
     *
     * @param text    Der zu prüfende Text-String.
     * @param message Die Fehlermeldung.
     * @throws LunarException (Lunar-Error) Wenn der Text {@code null} oder leer ist.
     */
    public static void notBlank(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw Errors.lunar(details(message, text, "A non-empty text with content"), null);
        }
    }

    /**
     * Prüft, ob eine Bedingung {@code true} ist.
     * Löst im Fehlerfall eine {@link LunarException} aus.
     *
     * @param condition Die zu prüfende Bedingung.
     * @param message   Die Fehlermeldung.
     * @throws LunarException (Lunar-Error) Wenn die Bedingung {@code false} ist.
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw Errors.lunar(details(message, condition, "true"), null);
        }
    }

    /**
     * Prüft, ob eine Bedingung {@code false} ist.
     * Löst im Fehlerfall eine {@link LunarException} aus.
     *
     * @param condition Die zu prüfende Bedingung.
     * @param message   Die Fehlermeldung.
     * @throws LunarException (Lunar-Error) Wenn die Bedingung {@code true} ist.
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw Errors.lunar(details(message, condition, "false"), null);
        }
    }

    /**
     * Prüft, ob ein {@code float}-Wert positiv ist (erlaubt positive Infinity, lehnt NaN und Werte &le; 0 ab).
     *
     * @param value   Der zu prüfende Wert.
     * @param message Die Fehlermeldung.
     * @throws LunarException (Lunar-Error) Wenn der Wert {@code NaN} oder &le; 0 ist.
     */
    public static void positive(float value, String message) {
        if (Float.isNaN(value) || value <= 0.0f) {
            throw Errors.lunar(details(message, value, "A number greater than 0"), null);
        }
    }

    /**
     * Prüft, ob ein {@code float}-Wert nicht negativ ist (erlaubt positive Infinity und beide Nullen).
     *
     * @param value   Der zu prüfende Wert.
     * @param message Die Fehlermeldung.
     * @throws LunarException (Lunar-Error) Wenn der Wert {@code NaN} oder negativ ist.
     */
    public static void nonNegative(float value, String message) {
        if (Float.isNaN(value) || value < 0.0f) {
            throw Errors.lunar(details(message, value, "A number greater than or equal to 0"), null);
        }
    }

    /**
     * Prüft, ob ein Wert innerhalb eines inklusiven Bereichs {@code [min, max]} liegt.
     *
     * @param value   Der zu prüfende Wert.
     * @param min     Die untere Grenze.
     * @param max     Die obere Grenze.
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn die Grenzen ungültig sind ({@code min > max} oder {@code NaN}).
     * @throws LunarException           (Lunar-Error) Wenn der Wert ausserhalb des Bereichs liegt.
     */
    public static void inRange(float value, float min, float max, String message) {
        if (Float.isNaN(min) || Float.isNaN(max) || min > max) {
            throw Errors.argument(details(
                    "Invalid value range",
                            "min=" + min + ", max=" + max,
                            "min <= max; no NaN boundaries"), null);
        }

        if (Float.isNaN(value) || value < min || value > max) {
            throw Errors.lunar(details(message, value, "[" + min + ", " + max + "]"), null);
        }
    }

    /**
     * Validiert ein Methodenargument (Bedingung muss {@code true} sein).
     *
     * @param condition Die Bedingung.
     * @param message   Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn die Bedingung {@code false} ist.
     */
    public static void requireArgument(boolean condition, String message) {
        if (!condition) {
            throw Errors.argument(details(message, condition, "The argument condition must be true"), null);
        }
    }

    /**
     * Validiert den Systemzustand (Bedingung muss {@code true} sein).
     *
     * @param condition Die Bedingung.
     * @param message   Die Fehlermeldung.
     * @throws IllegalStateException Wenn die Bedingung {@code false} ist.
     */
    public static void requireState(boolean condition, String message) {
        if (!condition) {
            throw Errors.state(details(message, condition, "The state condition must be true"), null);
        }
    }

    /**
     * Prüft, ob eine {@link Collection} ungleich {@code null} und nicht leer ist.
     *
     * @param collection Die zu prüfende Collection.
     * @param message    Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn die Collection {@code null} oder leer ist.
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw Errors.argument(details(message, collection == null ? "null" : "Size: 0",
                            "An existing collection with at least one element"), null);
        }
    }

    /**
     * Prüft, ob ein {@code double}-Wert endlich ist (weder {@code NaN} noch Unendlich).
     *
     * @param value   Der zu prüfende Wert.
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn der Wert nicht endlich ist.
     */
    public static void finite(double value, String message) {
        if (!Double.isFinite(value)) {
            throw Errors.argument(details(message, value, "A finite number excluding NaN and positive or negative Infinity"), null);
        }
    }

    /**
     * Prüft, ob ein {@code float}-Wert endlich ist (weder {@code NaN} noch Unendlich).
     *
     * @param value   Der zu prüfende Wert.
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn der Wert nicht endlich ist.
     */
    public static void finite(float value, String message) {
        if (!Float.isFinite(value)) {
            throw Errors.argument(details(message, value, "A finite number excluding NaN and positive or negative Infinity"), null);
        }
    }

    /**
     * Prüft, ob ein {@code double}-Wert endlich und ungleich {@code 0.0} ist.
     *
     * @param value   Der zu prüfende Wert.
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn der Wert 0 oder nicht endlich ist.
     */
    public static void nonZero(double value, String message) {
        finite(value, message);

        if (value == 0.0d) {
            throw Errors.argument(details(message, value, "A finite number other than 0"), null);
        }
    }

    /**
     * Prüft, ob ein Index innerhalb gültiger Array-/Listengrenzen liegt.
     *
     * @param index   Der zu prüfende Index.
     * @param size    Die Grösse/Länge (muss &ge; 0 sein).
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn {@code size < 0}.
     * @throws IndexOutOfBoundsException Wenn der Index ausserhalb von {@code [0, size-1]} liegt.
     */
    public static void indexInBounds(int index, int size, String message) {
        if (size < 0) {
            throw Errors.argument(details("Invalid size", size, "A size >= 0"), null);
        }
        if (index < 0 || index >= size) {
            throw Errors.index(details(message, index, "0 <= Index < " + size), null);
        }
    }

    /**
     * Prüft auf Nicht-Null-Referenz mit dem modernen {@link NullPointerException}-Vertrag.
     *
     * @param value   Das zu prüfende Objekt.
     * @param message Die Fehlermeldung.
     * @throws NullPointerException Wenn das Objekt {@code null} ist.
     */
    public static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw Errors.nullValue(details(message, null, "A non-null reference"), null);
        }
    }

    /**
     * Prüft, ob ein Text ungleich {@code null} und nicht blank ist (berücksichtigt Java-Whitespace).
     *
     * @param value   Der zu prüfende Text.
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn der Text blank oder {@code null} ist.
     */
    public static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw Errors.argument(details(message, value, "A non-empty text with content"), null);
        }
    }

    /**
     * Überprüft, ob ein übergebenes Objekt ein valides Java-Array mit mindestens einem Element ist.
     *
     * @param array   Das zu prüfende Array-Objekt.
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn kein Array vorliegt, es {@code null} oder leer ist.
     */
    public static void notEmptyArray(Object array, String message) {
        if (array == null || !array.getClass().isArray()) {
            throw Errors.argument(details(message, array == null ? "null" : array.getClass().getName(),
                            "A Java array with at least one element"), null);
        }

        if (Array.getLength(array) == 0) {
            throw Errors.argument(details(message, "Length: 0", "Array length > 0"), null);
        }
    }

    /**
     * Prüft, ob ein {@code double}-Wert negativ ist.
     *
     * @param value   Der zu prüfende Wert.
     * @param message Die Fehlermeldung.
     * @throws IllegalArgumentException Wenn der Wert nicht negativ oder {@code NaN} ist.
     */
    public static void negative(double value, String message) {
        if (Double.isNaN(value) || value >= 0.0d) {
            throw Errors.argument(details(message, value, "A number less than 0"), null);
        }
    }

    /**
     * Prüft, ob ein Divisor ungleich {@code 0.0} ist.
     *
     * @param divisor Der zu prüfende Divisor.
     * @param message Die Fehlermeldung.
     * @throws ArithmeticException Wenn der Divisor {@code +0.0} oder {@code -0.0} ist.
     */
    public static void divisorNonZero(double divisor, String message) {
        if (divisor == 0.0d) {
            throw Errors.arithmetic(details(message, divisor, "A divisor not equal to 0"), null);
        }
    }

    /**
     * Prüft, ob eine Operation unterstützt wird.
     *
     * @param supported {@code true}, wenn unterstützt, sonst {@code false}.
     * @param message   Die Fehlermeldung.
     * @throws UnsupportedOperationException Wenn die Operation nicht unterstützt wird.
     */
    public static void requireSupported(boolean supported, String message) {
        if (!supported) {
            throw Errors.unsupported(details(message, false, "A supported operation"), null);
        }
    }

    /**
     * Überprüft eine Argument-Bedingung anhand spezifischer {@link ErrorDetails}.
     *
     * @param condition Die Bedingung.
     * @param details   Die strukturierten {@link ErrorDetails}.
     * @throws IllegalArgumentException Wenn die Bedingung {@code false} ist.
     */
    public static void checkArgument(boolean condition, ErrorDetails details) {
        if (!condition) {
            throw Errors.argument(details, null);
        }
    }

    /**
     * Überprüft eine Zustands-Bedingung anhand spezifischer {@link ErrorDetails}.
     *
     * @param condition Die Bedingung.
     * @param details   Die strukturierten {@link ErrorDetails}.
     * @throws IllegalStateException Wenn die Bedingung {@code false} ist.
     */
    public static void checkState(boolean condition, ErrorDetails details) {
        if (!condition) {
            throw Errors.state(details, null);
        }
    }

    /**
     * Überprüft eine arithmetische Bedingung anhand spezifischer {@link ErrorDetails}.
     *
     * @param condition Die Bedingung.
     * @param details   Die strukturierten {@link ErrorDetails}.
     * @throws ArithmeticException Wenn die Bedingung {@code false} ist.
     */
    public static void checkArithmetic(boolean condition, ErrorDetails details) {
        if (!condition) {
            throw Errors.arithmetic(details, null);
        }
    }

    /**
     * Überprüft eine Unterstützungs-Bedingung anhand spezifischer {@link ErrorDetails}.
     *
     * @param condition Die Bedingung.
     * @param details   Die strukturierten {@link ErrorDetails}.
     * @throws UnsupportedOperationException Wenn die Bedingung {@code false} ist.
     */
    public static void checkSupported(boolean condition, ErrorDetails details) {
        if (!condition) {
            throw Errors.unsupported(details, null);
        }
    }
}