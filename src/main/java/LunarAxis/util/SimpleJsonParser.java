package LunarAxis.util;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.LunarException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Klasse {@code SimpleJsonParser} ist ein leichtgewichtiger, abhängigkeitsfreier JSON-Parser.
 * <p>
 * Er dient primär dem Einlesen von Konfigurations- und Spieldateien (wie z. B. Leveldaten oder Savegames).
 * Der Parser unterstützt Objekte, Arrays, Zeichenketten mit Escape-Sequenzen (inklusive Unicode),
 * Zahlen (Integer, Long, Double) sowie Booleans und Null-Werte.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * parser = SimpleJsonParser("{\"score\": 100, \"name\": \"Paddy\"}")
 * dataMap = parser.parseObject()
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class SimpleJsonParser {
    private final String json;
    private int pos = 0;

    /**
     * Erstellt einen neuen {@code SimpleJsonParser} mit dem zu parsenden JSON-Text.
     *
     * @param json Der zu parsende JSON-String.
     * @throws LunarException (Lunar-Error) Wenn {@code json} {@code null} ist.
     */
    public SimpleJsonParser(String json) {
        if (json == null) {
            throw Errors.lunar(ErrorDetails.of("SimpleJsonParser", "JSON content must not be null")
                    .withActual(null)
                    .withExpected("A string containing a JSON object")
                    .withExplanation("The parser requires existing text before it can read JSON values"), null);
        }

        this.json = json;
    }

    /**
     * Parst den eingegebenen JSON-String als Wurzel-Objekt und gibt diesen als Schlüssel-Wert-Map zurück.
     *
     * @return Eine {@link Map} mit den ausgelesenen Schlüssel-Wert-Paaren.
     * @throws LunarException (Lunar-Error) Wenn die JSON-Syntax ungültig ist oder unerwartete Zeichen am Ende folgen.
     */
    public Map<String, Object> parseObject() {
        Map<String, Object> result = parseObjectValue();
        skipWhitespace();

        if (pos < json.length()) {
            throw parseFailure("Unexpected characters after the JSON object", "End of input after the root object", null);
        }

        return result;
    }

    /**
     * Liest ein JSON-Objekt ab der aktuellen Zeichenposition ein.
     *
     * @return Eine {@link Map} des geparsten JSON-Objekts.
     * @throws LunarException (Lunar-Error) Wenn geschweifte Klammern oder Kommata fehlen.
     */
    private Map<String, Object> parseObjectValue() {
        skipWhitespace();
        expect('{');

        Map<String, Object> result = new HashMap<>();
        skipWhitespace();

        if (current() == '}') {
            pos++;
            return result;
        }

        while (true) {
            skipWhitespace();
            String key = parseString();

            skipWhitespace();
            expect(':');
            skipWhitespace();

            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();

            if (current() == '}') {
                pos++;
                return result;
            }
            expect(',');
        }
    }

    /**
     * Erkennt und parst den nächsten JSON-Wert basierend auf dem Startzeichen.
     *
     * @return Der geparste Wert (String, Map, List, Boolean, Number oder {@code null}).
     * @throws LunarException (Lunar-Error) Wenn kein gültiger JSON-Wert erkannt wird.
     */
    private Object parseValue() {
        skipWhitespace();
        char c = current();
        if (c == '"') {
            return parseString();
        }
        if (c == '{') {
            return parseObject();
        }
        if (c == '[') {
            return parseArray();
        }
        if (c == 't' || c == 'f') {
            return parseBoolean();
        }
        if (c == 'n') {
            parseNull(); return null;
        }
        if (c == '-' || Character.isDigit(c)) {
            return parseNumber();
        }

        throw parseFailure("Unexpected token while reading a JSON value", "A string, object, array, number, boolean or null", null);
    }

    /**
     * Parst eine in Anführungszeichen eingeschlossene Zeichenkette inklusive Escape-Sequenzen und Unicode-Zeichen.
     *
     * @return Der decodierte String.
     * @throws LunarException (Lunar-Error) Bei unvollständigen Anführungszeichen, Steuerzeichen oder ungültigem Unicode.
     */
    private String parseString() {
        expect('"');

        StringBuilder sb = new StringBuilder();

        while (current() != '"' && current() != '\0') {
            if (current() == '\\') {
                pos++;
                char escaped = current();
                switch (escaped) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        // Unicode-Escape ("\ uXXXX")
                        if (json.length() - pos <= 4) {
                            throw parseFailure("Incomplete Unicode escape", "Four hexadecimal digits after the Unicode escape marker", null);
                        }

                        String hex = json.substring(pos + 1, pos + 5);
                        for (int i = 0; i < hex.length(); i++) {
                            char digit = hex.charAt(i);
                            if (!((digit >= '0' && digit <= '9') || (digit >= 'a' && digit <= 'f') || (digit >= 'A' && digit <= 'F'))) {
                                throw parseFailure("Invalid Unicode escape", "Exactly four hexadecimal digits without a sign", null);
                            }
                        }

                        try {
                            int codePoint = Integer.parseInt(hex, 16);
                            sb.append((char) codePoint);
                            pos += 4;
                        } catch (NumberFormatException e) {
                            throw parseFailure("Invalid Unicode escape", "Four hexadecimal digits representing a UTF-16 code unit", e);
                        }
                        break;
                    default:
                        throw parseFailure("Unknown JSON escape sequence", "A supported JSON escape sequence", null);
                }
            } else {
                if (current() < 0x20) {
                    throw parseFailure("Unescaped control character in JSON string", "An escaped control character", null);
                }
                sb.append(current());
            }
            pos++;
        }

        if (current() == '"') {
            pos++;
        } else {
            throw parseFailure("Missing closing quotation mark", "A closing double quotation mark", null);
        }
        return sb.toString();
    }

    /**
     * Parst ein JSON-Array und gibt seine Elemente als Liste zurück.
     *
     * @return Eine {@link List} aller geparsten Elemente.
     * @throws LunarException (Lunar-Error) Bei fehlerhafter Array-Syntax.
     */
    private List<Object> parseArray() {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (current() == ']') {
            pos++;
            return result;
        }
        while (true) {
            result.add(parseValue());
            skipWhitespace();
            if (current() == ']') {
                pos++;
                break;
            }
            expect(',');
        }
        return result;
    }

    /**
     * Parst eine numerische JSON-Angabe (Integer, Long oder Double).
     *
     * @return Die Zahl als {@link Number}.
     * @throws LunarException (Lunar-Error) Bei fehlerhafter Zahlenformatierung oder Zahlenbereichsüberlauf.
     */
    private Number parseNumber() {
        int start = pos;
        if (current() == '-') {
            pos++;
        }

        // Ganzzahliger Teil
        if (!Character.isDigit(current())) {
            throw parseFailure("Missing integer digits", "At least one digit after an optional minus sign", null);
        }
        while (pos < json.length() && Character.isDigit(current())) {
            pos++;
        }

        // Nachkommastellen
        if (current() == '.') {
            pos++;
            if (!Character.isDigit(current())) {
                throw parseFailure("Missing fractional digits", "At least one digit after the decimal point", null);
            }
            while (pos < json.length() && Character.isDigit(current())) {
                pos++;
            }
        }

        // Exponent
        if (current() == 'e' || current() == 'E') {
            pos++;
            if (current() == '+' || current() == '-') {
                pos++;
            }
            if (!Character.isDigit(current())) {
                throw parseFailure("Missing exponent digits", "At least one exponent digit after an optional sign", null);
            }
            while (pos < json.length() && Character.isDigit(current())) {
                pos++;
            }
        }

        String numStr = json.substring(start, pos);
        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
            try {
                double value = Double.parseDouble(numStr);
                if (!Double.isFinite(value)) {
                    throw parseFailure("JSON number exceeds the supported floating-point range", "A finite double value", null);
                }
                return value;
            } catch (NumberFormatException e) {
                throw parseFailure("Invalid decimal number starting at position " + start, "A decimal number representable as double", e);
            }
        }

        try {
            long value = Long.parseLong(numStr);
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return (int) value;
            }
            return value;
        } catch (NumberFormatException e) {
            throw parseFailure("Invalid integer starting at position " + start, "An integer representable as long", e);
        }
    }

    /**
     * Parst die JSON-Literale {@code true} und {@code false}.
     *
     * @return Der entsprechende {@code boolean}-Wert.
     * @throws LunarException (Lunar-Error) Wenn kein gültiger Boolean-Wert vorliegt.
     */
    private boolean parseBoolean() {
        if (json.startsWith("true", pos)) {
            pos += 4;
            return true;
        }
        if (json.startsWith("false", pos)) {
            pos += 5;
            return false;
        }
        throw parseFailure("Invalid JSON boolean", "The literal true or false", null);
    }

    /**
     * Parst das JSON-Literal {@code null}.
     *
     * @throws LunarException (Lunar-Error) Wenn an aktueller Position kein "null"-Text steht.
     */
    private void parseNull() {
        if (json.startsWith("null", pos)) {
            pos += 4;
        } else {
            throw parseFailure("Invalid JSON null literal", "The literal null", null);
        }
    }

    /**
     * Prüft, ob an der aktuellen Leseposition das erwartete Trennzeichen steht, und rückt vor.
     *
     * @param c Das erwartete Zeichen.
     * @throws LunarException (Lunar-Error) Wenn das Zeichen nicht übereinstimmt.
     */
    private void expect(char c) {
        skipWhitespace();
        if (current() != c) {
            throw parseFailure("Required JSON delimiter is missing", "The character '" + c + "'", null);
        }
        pos++;
    }

    /**
     * Gibt das aktuelle Zeichen an der Leseposition zurück.
     *
     * @return Das Zeichen oder {@code '\0'}, falls das Eingabeende erreicht ist.
     */
    private char current() {
        if (pos >= json.length()) return '\0';
        return json.charAt(pos);
    }

    /**
     * Überspringt alle nachfolgenden Leerzeichen, Tabulatoren und Zeilenumbrüche.
     */
    private void skipWhitespace() {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
    }

    /**
     * Erstellt eine detaillierte {@link LunarException} mit Angabe der aktuellen Zeichenposition im JSON-String.
     *
     * @param title    Kurzbeschreibung des Parsing-Fehlers.
     * @param expected Erwartetes Token oder Zeichen.
     * @param cause    Ursprüngliche Exception oder {@code null}.
     * @return Die erzeugte {@link LunarException}.
     */
    private LunarException parseFailure(String title, String expected, Throwable cause) {
        return Errors.lunar(ErrorDetails.of("SimpleJsonParser", title)
                .withActual("position=" + pos + ", inputLength=" + json.length())
                .withExpected(expected)
                .withExplanation("The JSON input does not match the supported syntax or numeric range at the reported character position"), cause);
    }
}