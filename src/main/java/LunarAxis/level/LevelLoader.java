package LunarAxis.level;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.LunarException;
import LunarAxis.diagnostics.Validation;
import LunarAxis.grid.Grid;
import LunarAxis.grid.Tile;
import LunarAxis.math.Maths;
import LunarAxis.math.Rect;
import LunarAxis.math.Vec2;
import LunarAxis.util.SimpleJsonParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Die Klasse {@code LevelLoader} ist dafür zuständig, Level-Daten aus externen Dateien
 * (.txt, .json oder .png) zu laden und daraus ein vollständiges {@link Level}-Objekt zu erstellen.
 * <p>
 * Das System arbeitet vollständig datengesteuert: Der Entwickler bzw. Nutzer registriert im Vorfeld selbst,
 * welche numerischen IDs, Textzeichen oder RGB-Farben welchen Tiles, Spawn-Punkten oder Triggern entsprechen.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class LevelLoader {

    private final Map<Integer, Tile> idMap;      //json (numerische IDs)
    private final Map<Character, Tile> charMap;    //txt (Zeichen)
    private final Map<Integer, Tile> colorMap;   //png (RGB-Farben)

    // Mapping von Zeichen/Farben zu Objekttypen (z.B. 'E' -> "enemy")
    private final Map<Character, String> objectTypeCharMap;
    private final Map<Integer, String> objectTypeColorMap;
    private final Map<Integer, String> triggerColorMap;

    private Tile fallbackTile;

    private Character spawnChar = null;     // für txt
    private Integer spawnColor = null;      // für png
    private Vec2 spawnPoint;     // für json

    /**
     * Erstellt einen neuen, leeren LevelLoader und initialisiert alle Registrierungs-Maps.
     */
    public LevelLoader() {
        this.idMap = new HashMap<>();
        this.charMap = new HashMap<>();
        this.colorMap = new HashMap<>();

        this.fallbackTile = null;
        this.spawnChar = null;
        this.spawnColor = null;

        this.objectTypeCharMap = new HashMap<>();
        this.objectTypeColorMap = new HashMap<>();
        this.triggerColorMap = new LinkedHashMap<>();
    }

    /**
     * Legt ein Fallback-Tile fest, das verwendet wird, wenn für ein Zeichen, eine ID
     * oder eine Farbe kein spezifisches Tile registriert wurde.
     *
     * @param tile Das Standard-Fallback-{@link Tile}.
     */
    public void setFallbackTile(Tile tile) {
        this.fallbackTile = tile;
    }

    /**
     * Registriert ein Tile für eine numerische ID (verwendet beim JSON-Laden).
     *
     * @param id   Die numerische Tile-ID.
     * @param tile Das zugehörige {@link Tile}.
     * @throws RuntimeException (Lunar-Error) wenn das Tile {@code null} ist.
     */
    public void registerTile(int id, Tile tile) {
        Validation.notNull(tile, "Tile cannot be null");
        idMap.put(id, tile);
    }

    /**
     * Registriert ein Tile für ein bestimmtes Textzeichen (verwendet beim TXT-Laden).
     *
     * @param c    Das Textzeichen im Level-Grid.
     * @param tile Das zugehörige {@link Tile}.
     * @throws RuntimeException (Lunar-Error) wenn das Tile {@code null} ist.
     */
    public void registerChar(char c, Tile tile) {
        Validation.notNull(tile, "Tile cannot be null");
        charMap.put(c, tile);
    }

    /**
     * Registriert ein Tile für einen RGB-Farbwert (verwendet beim PNG-Laden).
     *
     * @param rgb  Der RGB-Farbwert.
     * @param tile Das zugehörige {@link Tile}.
     * @throws RuntimeException (Lunar-Error) wenn das Tile {@code null} ist.
     */
    public void registerColor(int rgb, Tile tile) {
        Validation.notNull(tile, "Tile cannot be null");
        colorMap.put(rgb & 0xFFFFFF, tile);
    }

    /**
     * Registriert ein Textzeichen als Spawn-Marker für den Spieler (verwendet im TXT-Format).
     *
     * @param c Das Zeichen, das den Spieler-Spawn markiert.
     */
    public void registerSpawnChar(char c) {
        this.spawnChar = c;
    }

    /**
     * Registriert eine RGB-Farbe als Spawn-Marker für den Spieler (verwendet im PNG-Format).
     *
     * @param rgb Der RGB-Farbwert des Spawn-Pixels.
     */
    public void registerSpawnColor(int rgb) {
        int normalized = rgb & 0xFFFFFF;
        ensureNotTriggerColor(normalized, "spawn marker");
        this.spawnColor = normalized;
    }

    /**
     * Registriert ein Zeichen als Objekt-Spawn-Marker (z.B. für Gegner oder Items in Text-Leveln).
     * Das Zeichen wird einem frei wählbaren Objekttypnamen zugeordnet.
     *
     * <pre>
     * loader.registerObjectType('E', "enemy");
     * </pre>
     *
     * @param character Das Zeichen im Level.
     * @param type      Der frei wählbare Objekttyp als String.
     */
    public void registerObjectType(char character, String type) {
        Validation.notBlank(type, "Object type cannot be blank");

        if (objectTypeCharMap.containsKey(character)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "Character is already registered as an object marker")
                    .withActual("character=" + character + ", existingType=" + objectTypeCharMap.get(character) + ", requestedType=" + type)
                    .withExpected("A character without an existing object marker registration")
                    .withExplanation("Registering the character again would overwrite its existing object type"), null);
        }
        objectTypeCharMap.put(character, type.trim());
    }

    /**
     * Registriert eine Farbe als Objekt-Spawn-Marker in PNG-Leveln.
     * <p>
     * Der RGB-Wert wird dabei auf 24 Bit normalisiert.
     * <i>(Hinweis zu {@code & 0xFFFFFF}: Dies ist eine bitweise Und-Verknüpfung (Bitwise AND),
     * die den Alpha-Kanal bzw. die obersten 8 Bytes eines ARGB-Integers abschneidet, sodass nur der reine RGB-Farbcode übrig bleibt).</i>
     * </p>
     *
     * <pre>
     * loader.registerObjectType(0xFF0000, "enemy");
     * </pre>
     *
     * @param rgb  Der RGB-Farbwert.
     * @param type Der frei wählbare Objekttyp als String.
     */
    public void registerObjectType(int rgb, String type) {
        Validation.notBlank(type, "Object type cannot be blank");
        int normalized = rgb & 0xFFFFFF;
        ensureNotTriggerColor(normalized, "object marker");

        if (objectTypeColorMap.containsKey(normalized)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "RGB color is already registered as an object marker")
                    .withActual(String.format("rgb=#%06X, existingType=%s, requestedType=%s", normalized, objectTypeColorMap.get(normalized), type))
                    .withExpected("An RGB color without an existing object marker registration")
                    .withExplanation("Registering the color again would overwrite its existing object type"), null);
        }
        objectTypeColorMap.put(normalized, type.trim());
    }

    /**
     * Registriert eine PNG-Farbe als Trigger-Kennung.
     * <p>
     * Dieselbe Farbe darf zusätzlich mit {@code registerColor()} einem Tile zugeordnet werden,
     * sie darf jedoch nicht zugleich Spawn- oder Objektmarker sein.
     * </p>
     *
     * @param rgb Der RGB-Farbwert.
     * @param id  Die ID des Triggers.
     */
    public void registerTriggerColor(int rgb, String id) {
        Validation.notBlank(id, "Trigger ID cannot be blank");
        int normalized = rgb & 0xFFFFFF;
        String normalizedId = id.trim();

        if (spawnColor != null && spawnColor.equals(normalized)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "RGB color is already registered as a spawn marker")
                    .withActual(String.format("rgb=#%06X, requestedTrigger=%s", normalized, normalizedId))
                    .withExpected("A trigger color that is not the player spawn color")
                    .withExplanation("A color cannot identify both a trigger and a player spawn marker"), null);
        }
        if (objectTypeColorMap.containsKey(normalized)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "RGB color is already registered as an object marker")
                    .withActual(String.format("rgb=#%06X, objectType=%s, requestedTrigger=%s", normalized, objectTypeColorMap.get(normalized), normalizedId))
                    .withExpected("A trigger color without an object marker registration")
                    .withExplanation("A color cannot identify both a trigger and an object spawn marker"), null);
        }

        String existingId = triggerColorMap.get(normalized);
        if (existingId != null && !existingId.equals(normalizedId)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "RGB color is already registered with another trigger ID")
                    .withActual(String.format("rgb=#%06X, existingTrigger=%s, requestedTrigger=%s", normalized, existingId, normalizedId))
                    .withExpected("An unused trigger color or the same normalized trigger ID")
                    .withExplanation("A single RGB color cannot identify two different triggers"), null);
        }
        triggerColorMap.put(normalized, normalizedId);
    }

    /**
     * Interne Hilfsmethode, um sicherzustellen, dass eine Farbe nicht doppelt als Trigger belegt ist.
     *
     * @param rgb  Der RGB-Wert.
     * @param role Die angeforderte Rolle.
     */
    private void ensureNotTriggerColor(int rgb, String role) {
        if (triggerColorMap.containsKey(rgb)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "RGB color is already registered as a trigger")
                    .withActual(String.format("rgb=#%06X, trigger=%s, requestedRole=%s", rgb, triggerColorMap.get(rgb), role))
                    .withExpected("A color without a trigger registration")
                    .withExplanation("A trigger color cannot also be used as a " + role), null);
        }
    }

    /**
     * Lädt ein Level aus einer Datei.
     * Das Dateiformat (.png, .json oder .txt) wird automatisch anhand der Dateiendung erkannt.
     *
     * @param filePath Pfad zur Level-Datei.
     * @param cellSize Die Grösse einer Grid-Zelle in Pixeln.
     * @return Das fertig geladene {@link Level}-Objekt.
     */
    public Level load(String filePath, int cellSize) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "Level file path cannot be empty")
                    .withActual(filePath)
                    .withExpected("A non-blank path to a level file")
                    .withExplanation("The loader needs a file path to select and read the level data"), null);
        }
        if (cellSize <= 0) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "Cell size must be greater than 0")
                    .withActual(cellSize)
                    .withExpected("A positive cell size in pixels")
                    .withExplanation("The level grid and its world coordinates require a positive cell size"), null);
        }

        File file = new File(filePath);
        if (!file.exists()) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "Level file was not found")
                    .withActual(filePath)
                    .withExpected("A path to an existing level file")
                    .withExplanation("Check the file path and the working directory used by the game"), null);
        }

        String name = file.getName().toLowerCase();
        try {
            if (name.endsWith(".png")) {
                return loadPng(file, cellSize);
            }
            if (name.endsWith(".json")) {
                return loadJson(file, cellSize);
            }
            if (name.endsWith(".txt")) {
                return loadTxt(file, cellSize);
            }
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "Unsupported level file format")
                    .withActual(filePath)
                    .withExpected("A file ending in .png, .json or .txt")
                    .withExplanation("The loader selects its parser from the file extension"), null);
        } catch (LunarException e) {
            throw e;
        } catch (Exception e) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "The level could not be loaded")
                    .withActual("filePath=" + filePath + ", cellSize=" + cellSize)
                    .withExpected("Readable level data matching the selected file format")
                    .withExplanation("Loading failed; the original exception is preserved as the cause"), e);
        }
    }

    /**
     * Lädt ein Level aus einer PNG-Bilddatei, bei der jedes Pixel einer Grid-Zelle entspricht.
     *
     * @param file     Die PNG-Datei.
     * @param cellSize Die Zellgrösse in Pixeln.
     * @return Das geladene {@link Level}.
     * @throws IOException Wenn beim Lesen der Datei ein I/O-Fehler auftritt.
     */
    private Level loadPng(File file, int cellSize) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "The level image could not be decoded")
                    .withActual(file.getPath())
                    .withExpected("An image supported by the available ImageIO readers")
                    .withExplanation("ImageIO returned no image for the supplied file"), null);
        }

        int cols = image.getWidth();
        int rows = image.getHeight();
        Grid grid = new Grid(rows, cols, cellSize);
        Vec2 foundSpawn = null;

        /* [NEU]
         * Object-Spawns werden während desselben Pixel-Durchlaufs gesammelt, in dem auch die Tiles verarbeitet werden
         */
        Map<String, List<Vec2>> objectSpawns = new HashMap<>();
        Map<String, List<Rect>> triggerBounds = new LinkedHashMap<>();

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int argb = image.getRGB(x, y);

                // Transparente Pixel werden als leer (null) behandelt
                int alpha = (argb >> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }

                // Alpha-Kanal entfernen für RGB-Mapping
                int rgb = argb & 0xFFFFFF;
                float worldX = (long) x * cellSize;
                float worldY = (long) y * cellSize;

                if (spawnColor != null && rgb == spawnColor) {
                    foundSpawn = new Vec2(x * cellSize, y * cellSize);
                }

                /*
                 * Object-Spawn.
                 */
                String objectType = objectTypeColorMap.get(rgb);
                if (objectType != null) {
                    objectSpawns.computeIfAbsent(objectType, key -> new ArrayList<>()).add(new Vec2(x * cellSize, y * cellSize));
                }

                String triggerId = triggerColorMap.get(rgb);
                if (triggerId != null) {
                    triggerBounds.computeIfAbsent(triggerId, key -> new ArrayList<>()).add(new Rect(x * cellSize, y * cellSize, cellSize, cellSize));
                }

                Tile tile = colorMap.get(rgb);
                if (tile == null) {
                    if (fallbackTile != null) {
                        tile = fallbackTile;
                    } else {
                        throw Errors.lunar(ErrorDetails.of("LevelLoader", "No tile is registered for the level pixel")
                                .withActual(String.format("file=%s, x=%d, y=%d, rgb=#%06X, alpha=%d", file.getPath(), x, y, rgb, alpha))
                                .withExpected("A registered RGB tile mapping or a fallback tile")
                                .withExplanation("The pixel is not fully transparent and no tile can be selected for its RGB color"), null);
                    }
                }
                grid.setTile(y, x, tile);
            }
        }

        Level level = new Level(file.getName(), grid, foundSpawn != null ? foundSpawn : new Vec2(0, 0), new HashMap<>());
        addObjectSpawns(level, objectSpawns);
        addTriggers(level, triggerBounds);
        return level;
    }

    /**
     * Lädt ein Level aus einer Textdatei (.txt), bei der jedes Zeichen einer Grid-Zelle entspricht.
     *
     * @param file     Die TXT-Datei.
     * @param cellSize Die Zellgrösse in Pixeln.
     * @return Das geladene {@link Level}.
     * @throws IOException Wenn beim Lesen der Datei ein I/O-Fehler auftritt.
     */
    private Level loadTxt(File file, int cellSize) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());

        // Leere Zeilen am Anfang und Ende entfernen
        while (!lines.isEmpty() && lines.get(0).trim().isEmpty()) {
            lines.remove(0);
        }

        while (!lines.isEmpty() && lines.get(lines.size() - 1).trim().isEmpty()) {
            lines.remove(lines.size() - 1);
        }

        if (lines.isEmpty()) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "Text level contains no usable rows")
                    .withActual(file.getPath())
                    .withExpected("At least one row after removing blank leading and trailing lines")
                    .withExplanation("An empty text level cannot define a grid"), null);
        }

        int rows = lines.size();
        int cols = lines.get(0).length();
        Grid grid = new Grid(rows, cols, cellSize);

        Vec2 foundSpawn = null;
        Map<String, List<Vec2>> objectSpawns = new HashMap<>();


        for (int r = 0; r < rows; r++) {
            String rowStr = lines.get(r);

            if (rowStr.length() != cols) {
                throw Errors.lunar(ErrorDetails.of("LevelLoader", "Text level rows have different lengths")
                        .withActual("file=" + file.getPath() + ", gridRow=" + r + ", length=" + rowStr.length())
                        .withExpected(cols + " characters in every grid row")
                        .withExplanation("The grid must be rectangular; gridRow is zero-based after removing blank boundary lines"), null);
            }

            for (int c = 0; c < cols; c++) {
                char character = rowStr.charAt(c);
                // Space ist reserviert für leere Zellen
                if (character == ' ') {
                    continue; // Leerzeichen als leeres Tile
                }

                float worldX = (long) c * cellSize;
                float worldY = (long) r * cellSize;


                // Spawn-Punkt erkennen, aber NICHT abbrechen (kein continue)
                if (spawnChar != null && character == spawnChar) {
                    foundSpawn = new Vec2(worldX, worldY);
                }

                /*
                 * Object-Spawn
                 */
                String objectType = objectTypeCharMap.get(character);

                if (objectType != null) {
                    objectSpawns.computeIfAbsent(objectType, key -> new ArrayList<>()).add(new Vec2(worldX, worldY));
                }

                Tile tile = charMap.get(character);
                if (tile == null) {
                    if (fallbackTile != null) {
                        tile = fallbackTile;
                    } else {
                        throw Errors.lunar(ErrorDetails.of("LevelLoader", "No tile is registered for the level character")
                                .withActual("file=" + file.getPath() + ", gridRow=" + r + ", column=" + c + ", character=" + character)
                                .withExpected("A registered character mapping or a fallback tile")
                                .withExplanation("The character is not a reserved empty cell and no tile can be selected; grid indices are zero-based"), null);
                    }
                }
                grid.setTile(r, c, tile);
            }
        }

        Level level = new Level(file.getName(), grid, foundSpawn != null ? foundSpawn : new Vec2(0, 0), new HashMap<>());
        addObjectSpawns(level, objectSpawns);
        return level;
    }

    /**
     * Lädt ein Level aus einer JSON-Datei, die Gitterdaten, Metadaten und Objekte strukturiert beschreibt.
     *
     * @param file     Die JSON-Datei.
     * @param cellSize Die Zellgrösse in Pixeln.
     * @return Das geladene {@link Level}.
     * @throws IOException Wenn beim Lesen der Datei ein I/O-Fehler auftritt.
     */
    private Level loadJson(File file, int cellSize) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        SimpleJsonParser parser = new SimpleJsonParser(content);

        Map<String, Object> root;
        try {
            root = parser.parseObject();
        } catch (LunarException e) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "The JSON level could not be parsed")
                    .withActual(file.getPath())
                    .withExpected("A JSON object accepted by the level parser")
                    .withExplanation("The parser rejected the file contents; the original parser exception is preserved as the cause"), e);
        }

        // Metadaten mit Validierung extrahieren
        String name = extractString(root, "name", file.getName());

        Vec2 jsonSpawn = null;
        if (root.containsKey("spawnX") && root.containsKey("spawnY")) {
            jsonSpawn = new Vec2(extractFiniteFloat(root.get("spawnX"), "spawnX"),
                    extractFiniteFloat(root.get("spawnY"), "spawnY"));
        }

        Map<String, String> properties = new HashMap<>();
        Object propertiesObject = root.get("properties");

        if (propertiesObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> propertiesMap = (Map<String, Object>) propertiesObject;

            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                properties.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        // Tiles-Array mit Validierung extrahieren
        Object tilesObject = root.get("tiles");
        if (tilesObject == null) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "Required JSON tiles array is missing")
                    .withActual("file=" + file.getPath() + ", tiles=null")
                    .withExpected("A non-empty tiles array containing non-empty rows")
                    .withExplanation("The tiles field defines the level grid"), null);
        }
        if (!(tilesObject instanceof List)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON tiles field must be an array")
                    .withActual(tilesObject.getClass().getName())
                    .withExpected("A JSON array")
                    .withExplanation("The tiles field must contain a sequence of grid rows"), null);
        }

        @SuppressWarnings("unchecked")
        List<Object> tilesData = (List<Object>) tilesObject;

        if (tilesData.isEmpty()) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON tiles array is empty")
                    .withActual("rowCount=0")
                    .withExpected("At least one grid row")
                    .withExplanation("An empty tiles array cannot define a level grid"), null);
        }

        int rows = tilesData.size();
        int cols = -1;
        Grid grid = null;

        Map<String, List<Vec2>> objectSpawns = new HashMap<>();
        Object objectsObject = root.get("objects");

        if (objectsObject instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> objectsData = (List<Object>) objectsObject;

            for (int i = 0; i < objectsData.size(); i++) {
                Object object = objectsData.get(i);
                if (!(object instanceof Map)) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> objectData = (Map<String, Object>) object;

                Object typeObject = objectData.get("type");
                Object xObject = objectData.get("x");
                Object yObject = objectData.get("y");

                if (!(typeObject instanceof String) || !(xObject instanceof Number) || !(yObject instanceof Number)) {
                    throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON object spawn has invalid required fields")
                            .withActual("index=" + i + ", type=" + typeObject + ", x=" + xObject + ", y=" + yObject)
                            .withExpected("A string type and numeric x and y fields")
                            .withExplanation("Each object spawn entry needs an object type and two coordinates"), null);
                }

                String type = ((String) typeObject).trim();
                if (type.isEmpty()) {
                    throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON object type cannot be empty")
                            .withActual("index=" + i + ", type=" + typeObject)
                            .withExpected("A non-blank object type")
                            .withExplanation("The object type is used to group the loaded spawn positions"), null);
                }

                float x = extractFiniteFloat(xObject, "objects[" + i + "].x");
                float y = extractFiniteFloat(yObject, "objects[" + i + "].y");

                objectSpawns.computeIfAbsent(type, key -> new ArrayList<>()).add(new Vec2(x, y));
            }
        } else if (objectsObject != null) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON objects field must be an array")
                    .withActual(objectsObject.getClass().getName())
                    .withExpected("An array, null or an omitted objects field")
                    .withExplanation("Object spawn entries must be supplied as a sequence"), null);
        }

        Map<String, List<Rect>> triggerBounds = readJsonTriggers(root.get("events"));

        for (int r = 0; r < rows; r++) {
            Object rowObj = tilesData.get(r);
            if (!(rowObj instanceof List)) {
                throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON tile row must be an array")
                        .withActual("row=" + r + ", value=" + rowObj)
                        .withExpected("An array of numeric tile IDs")
                        .withExplanation("Each entry in tiles represents one grid row"), null);
            }

            @SuppressWarnings("unchecked")
            List<Object> rowData = (List<Object>) rowObj;

            if (cols == -1) {
                cols = rowData.size();

                if (cols <= 0) {
                    throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON tile row cannot be empty")
                            .withActual("row=" + r + ", columnCount=" + cols)
                            .withExpected("At least one tile ID per row")
                            .withExplanation("The first row determines the width of the level grid"), null);
                }

                grid = new Grid(rows, cols, cellSize);
            } else if (rowData.size() != cols) {
                throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON tile rows have different lengths")
                        .withActual("row=" + r + ", columnCount=" + rowData.size())
                        .withExpected(cols + " tile IDs in every row")
                        .withExplanation("The level grid must be rectangular"), null);
            }

            for (int c = 0; c < cols; c++) {
                Object tileIdObj = rowData.get(c);
                if (!(tileIdObj instanceof Number)) {
                    throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON tile ID must be numeric")
                            .withActual("row=" + r + ", column=" + c + ", value=" + tileIdObj)
                            .withExpected("A numeric tile ID")
                            .withExplanation("Tile IDs are looked up in the numeric tile registry"), null);
                }

                // Prüfen ob es eine Ganzzahl ist
                Number tileIdNumber = (Number) tileIdObj;
                double numericId = tileIdNumber.doubleValue();

                if (!Double.isFinite(numericId) || numericId != Maths.floor(numericId) || numericId < Integer.MIN_VALUE || numericId > Integer.MAX_VALUE) {
                    throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON tile ID must be a finite integer in the int range")
                            .withActual("row=" + r + ", column=" + c + ", value=" + tileIdObj)
                            .withExpected("An integer between " + Integer.MIN_VALUE + " and " + Integer.MAX_VALUE)
                            .withExplanation("Fractional, non-finite or out-of-range IDs cannot be converted without changing their meaning"), null);
                }

                int tileId = tileIdNumber.intValue();
                Tile tile = idMap.get(tileId);

                if (tile == null) {
                    if (fallbackTile != null) {
                        tile = fallbackTile;
                    } else {
                        throw Errors.lunar(ErrorDetails.of("LevelLoader", "No tile is registered for the JSON tile ID")
                                .withActual("file=" + file.getPath() + ", row=" + r + ", column=" + c + ", tileId=" + tileId)
                                .withExpected("A registered tile ID or a fallback tile")
                                .withExplanation("The loader cannot select a tile for this grid cell"), null);
                    }
                }
                grid.setTile(r, c, tile);
            }
        }

        Level level = new Level(name, grid, jsonSpawn != null ? jsonSpawn : new Vec2(0, 0), properties);
        addObjectSpawns(level, objectSpawns);
        addTriggers(level, triggerBounds);
        return level;
    }

    /**
     * Überträgt gesammelte Objekt-Spawn-Positionen in das {@link Level}-Objekt.
     *
     * @param level        Das Ziel-Level.
     * @param objectSpawns Die Map mit Objekt-Typen und deren Spawn-Positionen.
     */
    private void addObjectSpawns(Level level, Map<String, List<Vec2>> objectSpawns) {
        for (Map.Entry<String, List<Vec2>> entry : objectSpawns.entrySet()) {
            for (Vec2 position : entry.getValue()) {
                level.addObjectSpawn(entry.getKey(), position);
            }
        }
    }

    /**
     * Liest Event-Trigger aus JSON-Daten ein.
     * <p>
     * <i>Unterschied LinkedHashMap vs. HashMap:</i> Eine normale {@link HashMap} garantiert keine feste Reihenfolge
     * der gespeicherten Elemente beim Durchlaufen. Eine {@link LinkedHashMap} behält dagegen strikt die
     * <b>Einfügereihenfolge (Insertion Order)</b> bei, was wichtig ist, wenn Trigger in einer bestimmten,
     * deterministischen Reihenfolge verarbeitet werden müssen.
     * </p>
     *
     * @param eventsObject Das rohe Events-Objekt aus dem JSON-Parser.
     * @return Eine Map mit Trigger-IDs und deren Rechteck-Begrenzungen.
     */
    private Map<String, List<Rect>> readJsonTriggers(Object eventsObject) {
        Map<String, List<Rect>> triggerBounds = new LinkedHashMap<>();

        if (eventsObject == null) {
            return triggerBounds;
        }
        Validation.isTrue(eventsObject instanceof List, "Level JSON: 'events' must be an Array!");

        @SuppressWarnings("unchecked")
        List<Object> events = (List<Object>) eventsObject;

        for (int i = 0; i < events.size(); i++) {
            Object eventObject = events.get(i);
            Validation.isTrue(eventObject instanceof Map, "Level JSON: event at index " + i + " must be an Object");

            @SuppressWarnings("unchecked")
            Map<String, Object> event = (Map<String, Object>) eventObject;

            Object idObject = event.get("id");
            Object xObject = event.get("x");
            Object yObject = event.get("y");
            Object widthObject = event.get("width");
            Object heightObject = event.get("height");

            Validation.isTrue(idObject instanceof String
                            && xObject instanceof Number
                            && yObject instanceof Number
                            && widthObject instanceof Number
                            && heightObject instanceof Number,
                    "Level JSON: each event requires string 'id' and numeric 'x', 'y', 'width' and 'height'");

            String id = ((String) idObject).trim();
            Validation.notBlank(id, "Level JSON: event ID cannot be empty");

            float x = extractFiniteFloat(xObject, "event x");
            float y = extractFiniteFloat(yObject, "event y");
            float width = extractFiniteFloat(widthObject, "event width");
            float height = extractFiniteFloat(heightObject, "event height");

            Validation.positive(width, "Level JSON: event width must be greater than 0");
            Validation.positive(height, "Level JSON: event height must be greater than 0");

            triggerBounds.computeIfAbsent(id, key -> new ArrayList<>()).add(new Rect(x, y, width, height));
        }
        return triggerBounds;
    }

    /**
     * Fügt eingelesene Trigger-Grenzen dem Level hinzu.
     *
     * @param level         Das Ziel-Level.
     * @param triggerBounds Die Map mit den Trigger-Bereichen.
     */
    private void addTriggers(Level level, Map<String, List<Rect>> triggerBounds) {
        for (Map.Entry<String, List<Rect>> entry : triggerBounds.entrySet()) {
            level.addTrigger(new LevelTrigger(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Hilfsfunktion für die typsichere Extraktion von Strings aus einer JSON-Map.
     *
     * @param map          Die Map.
     * @param key          Der Schlüssel.
     * @param defaultValue Der Standardwert bei fehlendem Feld.
     * @return Der gefundene String oder der Standardwert.
     */
    private String extractString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof String)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON field '" + key + "' must be a string")
                    .withActual(value)
                    .withExpected("A string, null or an omitted field")
                    .withExplanation("Missing or null values use the supplied default; other values must already be strings"), null);
        }

        return (String) value;
    }

    /**
     * Hilfsfunktion für die Extraktion und Validierung endlicher Fließkommazahlen aus JSON-Daten.
     *
     * @param value     Das zu prüfende Objekt.
     * @param fieldName Der Name des Feldes für Fehlermeldungen.
     * @return Der extrahierte {@code float}-Wert.
     */
    private float extractFiniteFloat(Object value, String fieldName) {
        if (!(value instanceof Number)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON field '" + fieldName + "' must be a number")
                    .withActual(value)
                    .withExpected("A numeric value representable as a finite float")
                    .withExplanation("This field is used as a level coordinate or rectangle dimension"), null);
        }

        double doubleValue = ((Number) value).doubleValue();
        float floatValue = ((Number) value).floatValue();

        if (!Double.isFinite(doubleValue) || !Float.isFinite(floatValue)) {
            throw Errors.lunar(ErrorDetails.of("LevelLoader", "JSON field '" + fieldName + "' must be finite")
                    .withActual(value)
                    .withExpected("A numeric value that remains finite when converted to float")
                    .withExplanation("NaN, infinity and values overflowing the float range cannot define level geometry"), null);
        }

        return floatValue;
    }
}