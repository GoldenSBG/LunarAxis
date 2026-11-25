package LunarAxis.level;

import LunarAxis.grid.Grid;
import LunarAxis.grid.Tile;
import LunarAxis.grid.TileMap;
import LunarAxis.math.Vec2;
import LunarAxis.objects.Actor;
import LunarAxis.physics.RigidBody2D;
import LunarAxis.scene.BaseScene;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.Validation;

import java.util.*;

/**
 * Die Klasse {@code Level} repräsentiert ein vollständiges, logisches Level in der LunarAxis-Engine.
 * <p>
 * Sie kapselt das zugrundeliegende {@link Grid} (Gitter), wichtige Metadaten (Eigenschaften),
 * den Start-Spawn-Punkt, Objekt-Spawn-Positionen sowie Level-Trigger.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Level {

    private final String name;
    private final Grid grid;
    private final Vec2 spawnPoint;
    private final Map<String, String> properties;
    private final Map<String, List<Vec2>> objectSpawnPoints;
    private final List<LevelTrigger> triggers;

    /**
     * Erstellt ein neues Level mit Namen und Grid (beginnend bei Spawn-Punkt (0,0) und ohne Eigenschaften).
     *
     * @param name Der Name des Levels.
     * @param grid Das {@link Grid} des Levels.
     */
    public Level(String name, Grid grid) {
        this(name, grid, new Vec2(0, 0), new HashMap<>());
    }

    /**
     * Erstellt ein neues Level mit allen Hauptattributen.
     *
     * @param name       Der Name des Levels.
     * @param grid       Das {@link Grid} des Levels.
     * @param spawnPoint Der Standard-Spawn-Punkt für den Spieler als {@link Vec2}.
     * @param properties Eine Map mit zusätzlichen Level-Eigenschaften (Metadaten).
     * @throws RuntimeException (Lunar-Error) wenn das Grid {@code null} ist.
     */
    public Level(String name, Grid grid, Vec2 spawnPoint, Map<String, String> properties) {
        this.name = name != null ? name : "Unnamed Level";
        Validation.notNull(grid, "Grid cannot be null");

        this.grid = grid;
        this.spawnPoint = spawnPoint != null ? spawnPoint : new Vec2(0, 0);
        this.properties = properties != null ? new HashMap<>(properties) : new HashMap<String, String>();
        this.objectSpawnPoints = new HashMap<>();
        this.triggers = new ArrayList<>();
    }

    /**
     * Gibt den Namen des Levels zurück.
     *
     * @return Der Levelname als String.
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt das zum Level gehörende Grid zurück.
     *
     * @return Das {@link Grid}.
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Gibt den Standard-Spawn-Punkt des Spielers zurück.
     *
     * @return Der Spawn-Punkt als {@link Vec2}.
     */
    public Vec2 getSpawnPoint() {
        return spawnPoint;
    }

    /**
     * Ruft eine spezifische Level-Eigenschaft über ihren Schlüssel ab.
     *
     * @param key Der Schlüssel der Eigenschaft.
     * @return Der Wert als String oder {@code null}, wenn der Schlüssel nicht existiert.
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Ruft eine Level-Eigenschaft ab oder gibt einen Standardwert zurück, falls der Schlüssel fehlt.
     *
     * @param key          Der Schlüssel.
     * @param defaultValue Der Standardwert, falls keine Eigenschaft gefunden wird.
     * @return Der Wert oder der Standardwert.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * Gibt eine unveränderbare Ansicht aller Level-Eigenschaften zurück.
     *
     * @return Eine unmodifizierbare {@link Map}.
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Fügt eine Spawn-Position für einen bestimmten Objekttyp (z. B. "Enemy", "Coin") hinzu.
     * <p>
     * <b>Technischer Hinweis zu {@code computeIfAbsent}:</b>
     * Diese Methode prüft, ob der Schlüssel (der Objekttyp) bereits in der {@link HashMap} existiert.
     * Falls nein, wird der Lambda-Ausdruck {@code k -> new ArrayList<>()} ausgeführt, der automatisch
     * eine neue {@link ArrayList} für diesen Typ anlegt und in die Map einfügt.
     * Danach wird die Position direkt an die Liste angehängt.
     * <i>(Unterschied HashMap vs ArrayList: Eine HashMap speichert Schlüssel-Wert-Paare für schnelles Suchen nach Namen,
     * während eine ArrayList eine rein sequenzielle Liste von Elementen speichert).</i>
     * </p>
     *
     * @param type     Der Typ des Objekts als String.
     * @param position Die Welt-Position als {@link Vec2}.
     */
    public void addObjectSpawn(String type, Vec2 position) {
        Validation.notBlank(type, "Object type cannot be blank");
        Validation.notNull(position, "Position cannot be null");
        objectSpawnPoints.computeIfAbsent(type, k -> new ArrayList<>()).add(position);
    }

    /**
     * Gibt alle Spawn-Positionen für einen bestimmten Objekttyp zurück.
     * <p>
     * <b>Technischer Hinweis:</b>
     * {@code getOrDefault} sucht nach dem Typ und gibt die Liste zurück. Falls der Typ nicht existiert,
     * wird dank {@code Collections.emptyList()} keine {@code null}-Referenz (und damit eine spätere NullPointerException)
     * riskiert, sondern sicher eine leere, unveränderbare Liste übergeben.
     * </p>
     *
     * @param type Der Objekttyp.
     * @return Eine {@link List} von Positionen oder eine leere Liste, wenn kein Typ gefunden wurde.
     */
    public List<Vec2> getObjectSpawns(String type) {
        return objectSpawnPoints.getOrDefault(type, Collections.emptyList());
    }

    /**
     * Gibt eine unmodifizierbare Ansicht aller Objekt-Spawns des gesamten Levels zurück.
     *
     * @return Eine Map mit allen Objekttypen und ihren jeweiligen Spawn-Listen.
     */
    public Map<String, List<Vec2>> getAllObjectSpawns() {
        return Collections.unmodifiableMap(objectSpawnPoints);
    }

    /**
     * Fügt dem Level Trigger-Daten hinzu.
     * Die Methode führt keine räumliche Prüfung oder Spiellogik aus.
     *
     * @param trigger Das hinzuzufügende {@link LevelTrigger}-Objekt.
     */
    public void addTrigger(LevelTrigger trigger) {
        Validation.notNull(trigger, "Trigger cannot be null");
        triggers.add(trigger);
    }

    /**
     * Gibt alle im Level registrierten Trigger zurück.
     *
     * @return Eine unmodifizierbare Liste von {@link LevelTrigger}-Objekten.
     */
    public List<LevelTrigger> getTriggers() {
        return Collections.unmodifiableList(triggers);
    }

    /**
     * Erzeugt unsichtbare physische Collider für die unveränderte Level-Weltgeometrie
     * (Standard: Offset 0, Scale 1).
     *
     * @param scene Die {@link BaseScene}, in der die Collider als Actors registriert werden.
     */
    public void createSolidColliders(BaseScene scene) {
        Validation.notNull(scene, "Scene cannot be null");
        createSolidColliders(scene, 0, 0, grid.getCellSize());
    }

    /**
     * Erzeugt physische Collider passend zu einer {@link TileMap} unter Berücksichtigung von Offset oder Skalierung.
     *
     * <p>Die TileMap muss dasselbe Grid wie dieses Level verwenden.
     * Positionen und Zellgrösse entsprechen der Berechnung in {@code TileMap.draw()}.</p>
     *
     * @param scene   Die {@link BaseScene}.
     * @param tileMap Die zugehörige {@link TileMap}.
     * @throws RuntimeException (Lunar-Error) wenn die TileMap ein anderes Grid verwendet oder die Dimensionen das Limit überschreiten.
     */
    public void createSolidColliders(BaseScene scene, TileMap tileMap) {
        Validation.notNull(scene, "Scene cannot be null");
        Validation.notNull(tileMap, "TileMap cannot be null");
        Validation.isTrue(tileMap.getGrid() == grid, "TileMap must use this Level's Grid");

        float scaledSize = grid.getCellSize() * tileMap.getScale();
        if (!Float.isFinite(scaledSize) || scaledSize < 0f || (double) scaledSize > Integer.MAX_VALUE) {
            throw Errors.arithmetic(ErrorDetails.of("Level", "Scaled collider size exceeds the supported range")
                    .withActual("cellSize=" + grid.getCellSize() + ", scale=" + tileMap.getScale() + ", scaledSize=" + scaledSize)
                    .withExpected("A finite scaled cell size between 0 and " + Integer.MAX_VALUE)
                    .withExplanation("Collider width and height use integer dimensions and cannot represent this scaled size"), null);
        }

        int scaledCellSize = (int) scaledSize;
        createSolidColliders(scene, tileMap.getOffsetX(), tileMap.getOffsetY(), scaledCellSize);
    }

    /**
     * Interne Hilfsmethode zur Erstellung und Registrierung der soliden Wand-Collider im Grid.
     *
     * @param scene    Die Szene.
     * @param offsetX  Der X-Versatz.
     * @param offsetY  Der Y-Versatz.
     * @param cellSize Die effektive Zellgrösse in Pixeln.
     */
    private void createSolidColliders(BaseScene scene, int offsetX, int offsetY, int cellSize) {
        if (cellSize <= 0) {
            /*
             * TileMap.draw() zeichnet bei einer effektiven Zellgrösse von null ebenfalls keine sichtbare Tilefläche.
             */
            return;
        }

        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                Tile tile = grid.getTile(row, col);
                if (tile == null || !tile.isSolid()) {
                    continue;
                }

                float worldX = (long) offsetX + (long) col * cellSize;
                float worldY = (long) offsetY + (long) row * cellSize;


                Actor collider = new Actor();
                collider.setX(worldX);
                collider.setY(worldY);
                collider.setWidth(cellSize);
                collider.setHeight(cellSize);
                collider.setVisible(false);
                collider.setRigidBody(new RigidBody2D(0.0f));
                scene.addActor(collider);
            }
        }
    }
}