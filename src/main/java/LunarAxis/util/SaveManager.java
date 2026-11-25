package LunarAxis.util;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.LunarException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Die Klasse {@code SaveManager} stellt ein einfaches, Schlüssel-Wert-basiertes Speicher-System
 * für die persistente Speicherung von Spieldaten bereit.
 * <p>
 * Die Daten werden im strukturierten JSON-Format auf der Festplatte abgelegt und können
 * typsicher als primitive Datentypen ({@code Integer}, {@code Float}, {@code Double}, {@code Boolean})
 * oder {@code String} ausgelesen werden. Der Zielordner lässt sich flexibel anpassen.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * save = SaveManager.getInstance()
 * save.setDirectory("my_saves")
 * save.set("score", 500)
 * save.set("playerName", "Player")
 * save.save("slot1")
 *
 * save.load("slot1")
 * score = save.getInt("score")
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class SaveManager {

    private static SaveManager instance;

    private final Map<String, Object> data;
    private Path saveDirectory;

    /**
     * Privater Konstruktor zur Durchsetzung des Singleton-Musters.
     * Initialisiert den In-Memory-Datenspeicher und setzt das Standard-Speicherverzeichnis auf "saves".
     */
    private SaveManager() {
        this.data = new HashMap<>();
        this.saveDirectory = Paths.get("saves");
        //ensureSaveDirectoryExists();
    }

    /**
     * Gibt die globale Singleton-Instanz des {@code SaveManager}s zurück.
     *
     * @return Die zentrale {@link SaveManager}-Instanz.
     */
    public static synchronized SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    /**
     * Setzt das Zielverzeichnis, in dem Speicherdateien (Savegames) abgelegt und geladen werden.
     * <p>
     * Es werden sowohl relative als auch absolute Pfade unterstützt. Falls der angegebene Ordner
     * noch nicht existiert, wird er automatisch im Dateisystem angelegt.
     * </p>
     *
     * @param path Der Pfad zum Zielverzeichnis (z. B. "my_saves" oder "C:/saves").
     * @throws LunarException (Lunar-Error) Wenn der Pfad leer ist, kein Ordner ist oder nicht erstellt werden kann.
     */
    public synchronized void setDirectory(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Save directory path must not be blank")
                    .withActual(path)
                    .withExpected("A non-empty directory path")
                    .withExplanation("A directory path is required before selecting the save location"), null);
        }

        Path newDirectory = Paths.get(path);
        try {
            if (!Files.exists(newDirectory)) {
                Files.createDirectories(newDirectory);
            }
            if (!Files.isDirectory(newDirectory)) {
                throw Errors.lunar(ErrorDetails.of("SaveManager", "Save path is not a directory")
                        .withActual(path)
                        .withExpected("A directory")
                        .withExplanation("Save files must be stored inside a directory rather than inside another file"), null);
            }
            this.saveDirectory = newDirectory;
        }  catch (IOException e) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Could not create the save directory")
                    .withActual(path)
                    .withExpected("A writable directory location")
                    .withExplanation("The filesystem operation failed while preparing the save directory"), e);
        }
    }

    /**
     * Gibt den aktuell eingestellten Pfad des Speicher-Verzeichnisses als String zurück.
     *
     * @return Der Verzeichnispfad.
     */
    public String getDirectory() {
        return saveDirectory.toString();
    }

    /**
     * Überprüft, ob ein übergebener Schlüssel gültig (nicht {@code null}) ist.
     *
     * @param key Der zu prüfende Schlüssel.
     * @throws LunarException (Lunar-Error) Wenn {@code key} {@code null} ist.
     */
    private void validateKey(String key) {
        if (key == null) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Save key must not be null")
                    .withActual(null)
                    .withExpected("A string key")
                    .withExplanation("Stored values are addressed by a non-null key"), null);
        }
    }

    /**
     * Liest einen Wert aus dem internen Datenspeicher und überprüft dessen Datentyp.
     *
     * @param key          Der Schlüssel des Werts.
     * @param expectedType Die erwartete Java-Klasse des Werts.
     * @param required     Legt fest, ob der Schlüssel zwingend existieren muss.
     * @return Das ausgelesene Objekt oder {@code null}, falls nicht erforderlich und nicht vorhanden.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel fehlt (bei {@code required = true}) oder der Typ abweicht.
     */
    private Object readTypedValue(String key, Class<?> expectedType, boolean required) {
        validateKey(key);
        Object value = data.get(key);

        if (value == null) {
            if (required) {
                throw Errors.lunar(ErrorDetails.of("SaveManager", "Required save key is missing")
                        .withActual(key)
                        .withExpected("An existing key containing " + expectedType.getSimpleName())
                        .withExplanation("Set or load the value before reading it, or use the getter overload with a default value"), null);
            }
            return null;
        }

        if (!expectedType.isInstance(value)) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Saved value has an unexpected type")
                    .withActual("key=" + key + ", type=" + value.getClass().getSimpleName())
                    .withExpected(expectedType.getSimpleName())
                    .withExplanation("Typed getters do not implicitly convert stored values to another type"), null);
        }

        return value;
    }

    /**
     * Speichert einen Wert unter dem angegebenen Schlüssel im Arbeitsspeicher.
     *
     * @param key   Der Eindeutige Schlüssel (darf nicht {@code null} sein).
     * @param value Der zu speichernde Wert (muss {@code Integer}, {@code Float}, {@code Double}, {@code Boolean} oder {@code String} sein).
     * @throws LunarException (Lunar-Error) Wenn Schlüssel oder Wert ungültig sind, der Typ nicht unterstützt wird oder Zahlen nicht endlich sind.
     */
    public void set(String key, Object value) {
        validateKey(key);

        if (value == null) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Saved value must not be null")
                    .withActual(null)
                    .withExpected("Integer, Float, Double, Boolean or String")
                    .withExplanation("The save manager stores non-null primitive values and strings"), null);
        }

        if (!(value instanceof Integer) && !(value instanceof Float) && !(value instanceof Double)
                && !(value instanceof Boolean) && !(value instanceof String)) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Saved value type is not supported")
                    .withActual(value.getClass().getName())
                    .withExpected("Integer, Float, Double, Boolean or String")
                    .withExplanation("The save format supports primitive scalar values and strings rather than arbitrary objects"), null);
        }

        if ((value instanceof Float && !Float.isFinite((Float) value))
                || (value instanceof Double && !Double.isFinite((Double) value))) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Saved number must be finite")
                    .withActual(value)
                    .withExpected("A finite numeric value")
                    .withExplanation("JSON does not define numeric literals for NaN or Infinity"), null);
        }

        data.put(key, value);
    }

    /**
     * Ruft den ungetypten Wert für den angegebenen Schlüssel ab.
     *
     * @param key Der Schlüssel.
     * @return Das gespeicherte {@link Object} oder {@code null}, falls der Schlüssel nicht existiert.
     */
    public Object get(String key) {
        validateKey(key);
        return data.get(key);
    }

    /**
     * Prüft, ob ein Schlüssel im aktuellen Datenspeicher vorhanden ist.
     *
     * @param key Der zu prüfende Schlüssel.
     * @return {@code true}, wenn der Schlüssel existiert, sonst {@code false}.
     */
    public boolean contains(String key) {
        validateKey(key);
        return data.containsKey(key);
    }

    /**
     * Entfernt einen Schlüssel und seinen zugewiesenen Wert aus den Daten.
     *
     * @param key Der zu entfernende Schlüssel.
     */
    public void remove(String key) {
        validateKey(key);
        data.remove(key);
    }

    /**
     * Löscht alle im Arbeitsspeicher befindlichen Daten.
     */
    public void clear() {
        data.clear();
    }

    /**
     * Ruft den Wert für den angegebenen Schlüssel als {@code int} ab.
     *
     * @param key Der Schlüssel.
     * @return Der Ganzzahl-Wert.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel fehlt oder der Wert kein {@code Integer} ist.
     */
    public int getInt(String key) {
        return (Integer) readTypedValue(key, Integer.class, true);
    }

    /**
     * Ruft den Wert für den Schlüssel als {@code int} ab oder gibt den Standardwert zurück, falls der Schlüssel fehlt.
     *
     * @param key          Der Schlüssel.
     * @param defaultValue Der Fallback-Wert, falls der Schlüssel nicht existiert.
     * @return Der gespeicherte Wert oder {@code defaultValue}.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel existiert, aber kein {@code Integer} ist.
     */
    public int getInt(String key, int defaultValue) {
        Object value = readTypedValue(key, Integer.class, false);
        return value == null ? defaultValue : (Integer) value;
    }

    /**
     * Ruft den Wert für den angegebenen Schlüssel als {@code float} ab.
     *
     * @param key Der Schlüssel.
     * @return Der Gleitkommazahl-Wert.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel fehlt oder der Wert kein {@code Float} ist.
     */
    public float getFloat(String key) {
        return (Float) readTypedValue(key, Float.class, true);
    }

    /**
     * Ruft den Wert für den Schlüssel als {@code float} ab oder gibt den Standardwert zurück, falls der Schlüssel fehlt.
     *
     * @param key          Der Schlüssel.
     * @param defaultValue Der Fallback-Wert, falls der Schlüssel nicht existiert.
     * @return Der gespeicherte Wert oder {@code defaultValue}.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel existiert, aber kein {@code Float} ist.
     */
    public float getFloat(String key, float defaultValue) {
        Object value = readTypedValue(key, Float.class, false);
        return value == null ? defaultValue : (Float) value;
    }

    /**
     * Ruft den Wert für den angegebenen Schlüssel als {@code double} ab.
     *
     * @param key Der Schlüssel.
     * @return Der doppeltgenaue Gleitkommazahl-Wert.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel fehlt oder der Wert kein {@code Double} ist.
     */
    public double getDouble(String key) {
        return (Double) readTypedValue(key, Double.class, true);
    }

    /**
     * Ruft den Wert für den Schlüssel als {@code double} ab oder gibt den Standardwert zurück, falls der Schlüssel fehlt.
     *
     * @param key          Der Schlüssel.
     * @param defaultValue Der Fallback-Wert, falls der Schlüssel nicht existiert.
     * @return Der gespeicherte Wert oder {@code defaultValue}.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel existiert, aber kein {@code Double} ist.
     */
    public double getDouble(String key, double defaultValue) {
        Object value = readTypedValue(key, Double.class, false);
        return value == null ? defaultValue : (Double) value;
    }

    /**
     * Ruft den Wert für den angegebenen Schlüssel als {@code boolean} ab.
     *
     * @param key Der Schlüssel.
     * @return Der Wahrheitswert.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel fehlt oder der Wert kein {@code Boolean} ist.
     */
    public boolean getBoolean(String key) {
        return (Boolean) readTypedValue(key, Boolean.class, true);
    }

    /**
     * Ruft den Wert für den Schlüssel als {@code boolean} ab oder gibt den Standardwert zurück, falls der Schlüssel fehlt.
     *
     * @param key          Der Schlüssel.
     * @param defaultValue Der Fallback-Wert, falls der Schlüssel nicht existiert.
     * @return Der gespeicherte Wert oder {@code defaultValue}.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel existiert, aber kein {@code Boolean} ist.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = readTypedValue(key, Boolean.class, false);
        return value == null ? defaultValue : (Boolean) value;
    }

    /**
     * Ruft den Wert für den angegebenen Schlüssel als {@code String} ab.
     *
     * @param key Der Schlüssel.
     * @return Der Text-String.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel fehlt oder der Wert kein {@code String} ist.
     */
    public String getString(String key) {
        return (String) readTypedValue(key, String.class, true);
    }

    /**
     * Ruft den Wert für den Schlüssel als {@code String} ab oder gibt den Standardwert zurück, falls der Schlüssel fehlt.
     *
     * @param key          Der Schlüssel.
     * @param defaultValue Der Fallback-Wert, falls der Schlüssel nicht existiert.
     * @return Der gespeicherte Wert oder {@code defaultValue}.
     * @throws LunarException (Lunar-Error) Wenn der Schlüssel existiert, aber kein {@code String} ist.
     */
    public String getString(String key, String defaultValue) {
        Object value = readTypedValue(key, String.class, false);
        return value == null ? defaultValue : (String) value;
    }

    /**
     * Serialisiert die aktuellen Speicher-Daten und schreibt sie als JSON-Datei im Speicherordner ab.
     *
     * @param name Der Dateiname ohne Dateiendung (z. B. "savegame" wird zu "savegame.json").
     * @throws LunarException (Lunar-Error) Bei unzulässigem Namen oder E/A-Schreibfehlern.
     */
    public void save(String name) {
        validateSaveName(name);
        Path filePath = saveDirectory.resolve(name + ".json");

        try {
            ensureSaveDirectoryExists();
            StringBuilder json = new StringBuilder();
            json.append("{\n");

            int count = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (count > 0) {
                    json.append(",\n");
                }
                json.append("  \"").append(escapeJsonString(entry.getKey())).append("\": ");
                json.append(formatJsonValue(entry.getValue()));
                count++;
            }

            json.append("\n}");
            Files.writeString(filePath, json.toString());
        } catch (IOException e) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Could not write the save file")
                    .withActual(filePath)
                    .withExpected("A writable save file")
                    .withExplanation("The filesystem operation failed while writing the serialized save data"), e);
        }
    }

    /**
     * Liest Daten aus einer JSON-Datei ein und ersetzt den aktuellen Speicherinhalt damit.
     *
     * @param name Der Dateiname ohne Dateiendung (z. B. "savegame").
     * @throws LunarException (Lunar-Error) Bei ungültigem Namen, fehlender Datei oder Parsing-Fehlern.
     */
    public void load(String name) {
        validateSaveName(name);
        Path filePath = saveDirectory.resolve(name + ".json");

        if (!Files.exists(filePath)) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Save file was not found")
                    .withActual(filePath)
                    .withExpected("An existing save file")
                    .withExplanation("Create the save first or select the directory containing the requested save"), null);
        }

        try {
            String content = Files.readString(filePath);
            SimpleJsonParser parser = new SimpleJsonParser(content);
            Map<String, Object> loadedData = parser.parseObject();
            Map<String, Object> normalizedData = new HashMap<>();

            for (Map.Entry<String, Object> entry : loadedData.entrySet()) {
                Object value = normalizeJsonValue(entry.getValue());
                if (value != null) {
                    normalizedData.put(entry.getKey(), value);
                }
            }

            data.clear();
            data.putAll(normalizedData);
        } catch (LunarException e) {
            throw e;
        } catch (Exception e) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Could not load the save file")
                    .withActual(filePath)
                    .withExpected("A readable file containing supported JSON save data")
                    .withExplanation("Reading or processing the save file failed"), e);
        }
    }

    /**
     * Konvertiert und normalisiert eingelesene JSON-Werte in die vom {@code SaveManager} unterstützten Datentypen.
     *
     * @param value Der geparste JSON-Wert.
     * @return Der konvertierte Wert (z. B. {@code Integer}, {@code Float}, {@code Double}, {@code Boolean}, {@code String}).
     * @throws LunarException (Lunar-Error) Wenn unendliche Fließkommazahlen geladen werden.
     */
    private Object normalizeJsonValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean || value instanceof String || value instanceof Integer) {
            return value;
        }
        if (value instanceof Long) {
            long longValue = (Long) value;
            if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                return (int) longValue;
            }
            return (double) longValue;
        }
        if (value instanceof Double) {
            double doubleValue = (Double) value;
            if (!Double.isFinite(doubleValue)) {
                throw Errors.lunar(ErrorDetails.of("SaveManager", "Loaded number must be finite")
                        .withActual(doubleValue)
                        .withExpected("A finite JSON number")
                        .withExplanation("Non-finite values cannot be stored and written back as valid JSON"), null);
            }

            float floatValue = (float) doubleValue;
            if ((double) floatValue == doubleValue) {
                return floatValue;
            }
            return doubleValue;
        }
        return null;
    }

    /**
     * Formatiert ein gegebenes Java-Objekt in einen gültigen JSON-Wert-String.
     *
     * @param value Das zu formatierende Objekt.
     * @return Der JSON-String-Wert.
     */
    private String formatJsonValue(Object value) {
        if (value instanceof String) {
            return "\"" + escapeJsonString((String) value) + "\"";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        return "null";
    }

    /**
     * Maskiert Sonderzeichen in Texten für die valide Ausgabe im JSON-Format.
     *
     * @param str Der Ursprungstext.
     * @return Der JSON-konform maskierte String.
     */
    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\': escaped.append("\\\\"); break;
                case '"': escaped.append("\\\""); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    /**
     * Prüft den angegebenen Dateinamen auf Pfad-Traversierungsversuche (z. B. Slashes oder "..") und leere Eingaben.
     *
     * @param name Der zu prüfende Savegame-Name.
     * @throws LunarException (Lunar-Error) Wenn der Name unzulässige Pfadkomponenten oder Leerzeichen enthält.
     */
    private void validateSaveName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Save name must not be blank")
                    .withActual(name)
                    .withExpected("A non-empty save name without a path")
                    .withExplanation("A save name identifies a file inside the configured save directory"), null);
        }

        if (name.contains("/") || name.contains("\\") || name.contains("..")) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Save name contains forbidden path components")
                    .withActual(name)
                    .withExpected("A name without slashes, backslashes or consecutive dots")
                    .withExplanation("Select the directory through setDirectory instead of embedding path components in the save name"), null);
        }
    }

    /**
     * Stellt sicher, dass das konfigurierte Zielverzeichnis existiert und beschreibbar ist.
     *
     * @throws LunarException (Lunar-Error) Wenn der Ordner nicht angelegt werden kann oder durch eine Datei blockiert ist.
     */
    private void ensureSaveDirectoryExists() {
        try {
            if (!Files.exists(saveDirectory)) {
                Files.createDirectories(saveDirectory);
            }
            if (!Files.isDirectory(saveDirectory)) {
                throw Errors.lunar(ErrorDetails.of("SaveManager", "Configured save location is not a directory")
                        .withActual(saveDirectory)
                        .withExpected("A directory")
                        .withExplanation("The configured location must be a directory before a save file can be written"), null);
            }
        } catch (IOException e) {
            throw Errors.lunar(ErrorDetails.of("SaveManager", "Could not prepare the save directory")
                    .withActual(saveDirectory)
                    .withExpected("An existing directory or a location where directories can be created")
                    .withExplanation("The filesystem operation failed while preparing the save location"), e);
        }
    }
}