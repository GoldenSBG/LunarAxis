package LunarAxis.grid;

import LunarAxis.graphics.Colors;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.Image;

/**
 * Beschreibt die visuellen Eigenschaften einer einzelnen Grid-Zelle.
 *
 * <p>Ein Tile ist ein leichtgewichtiges Datenobjekt, das wiederverwendet werden kann.
 * Mehrere Grid-Zellen können auf dieselbe Tile-Instanz verweisen, um Speicher zu sparen.</p>
 *
 * <h3>Beispiel (TigerJython):</h3>
 * <pre>
 * grass = Tile(0, Colors.GREEN)
 * wall = Tile(1, Colors.GRAY, wallImage)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 * @see Grid
 * @see TileMap
 */
public class Tile {

    private final int id;
    private final Colors color;
    private final Image image;

    private boolean solid = false;

    /**
     * Erstellt ein neues Tile mit ID und Farbe.
     *
     * @param id    Eindeutige Identifikationsnummer des Tiles (z.B. 0 = Leer, 1 = Wand)
     * @param color Fallback-Farbe, falls kein Bild geladen ist
     * @throws RuntimeException (Lunar-Error) wenn id negativ ist
     */
    public Tile(int id, Colors color) {
        this(id, color, null);
    }

    /**
     * Erstellt ein neues Tile mit ID, Farbe und optionalem Bild.
     *
     * @param id    Eindeutige Identifikationsnummer des Tiles
     * @param color Fallback-Farbe, falls kein Bild geladen ist
     * @param image Optionales Bild für das Tile (kann null sein)
     * @throws RuntimeException (Lunar-Error) wenn id negativ ist
     */
    public Tile(int id, Colors color, Image image) {
        if (id < 0) {
            throw Errors.argument(ErrorDetails.of("Tile", "Tile ID must not be negative")
                    .withActual(id)
                    .withExpected("A tile ID greater than or equal to 0")
                    .withExplanation("Tile identifiers use non-negative integers; 0 is a valid identifier"), null);
        }

        this.id = id;
        this.color = color;
        this.image = image;
    }

    /**
     * Gibt die eindeutige ID des Tiles zurück.
     * @return Tile-ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gibt die Fallback-Farbe des Tiles zurück.
     * @return Farbe oder null, falls keine Farbe gesetzt ist
     */
    public Colors getColor() {
        return color;
    }

    /**
     * Gibt das optionale Bild des Tiles zurück.
     * @return Bild oder null, falls kein Bild gesetzt ist
     */
    public Image getImage() {
        return image;
    }

    /**
     * Prüft, ob dieses Tile ein Bild hat.
     * @return true wenn ein Bild gesetzt ist, false sonst
     */
    public boolean hasImage() {
        return image != null;
    }

    /**
     * Prüft, ob dieses Tile als festes Hindernis (Collider) behandelt werden soll.
     * @return true wenn solid, false sonst
     */
    public boolean isSolid() {
        return solid;
    }

    /**
     * Legt fest, ob dieses Tile als festes Hindernis behandelt werden soll.
     * Da Tiles wiederverwendet werden, betrifft dies alle Grid-Zellen, die dieses Tile referenzieren.
     * @param solid true für Hindernis, false für begehbar
     */
    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    /**
     * Gibt eine textuelle Darstellung des Tiles mit ID, Solid-Status und Bild-Status zurück
     * (ideal für Debugging-Zwecke).
     *
     * @return Ein formatierter String mit den Tile-Eigenschaften.
     */
    @Override
    public String toString() {
        return String.format("Tile[id=%d, solid=%b, hasImage=%b]", id, solid, hasImage());
    }
}