package LunarAxis.grid;

import LunarAxis.graphics.Camera2D;
import LunarAxis.graphics.Graphics;
import LunarAxis.math.Maths;
import LunarAxis.objects.Actor;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code TileMap} ist dafür zuständig, ein {@link Grid} (Gitter) in Weltkoordinaten zu rendern.
 * <p>Die TileMap zeichnet ausschliesslich in Weltkoordinaten.</p>
 *
 * <p>Die Kamera-Transformation ({@code apply}/{@code reset}) wird ausserhalb der TileMap angewendet.
 * Ein Offset und Scale verändern die Weltkoordinaten und die Zellgrösse der Darstellung.</p>
 *
 * <p>Die optionale {@link Camera2D}-Übergabe dient <strong>nur</strong> der Culling-Optimierung,
 * nicht der Transformation.</p>
 *
 * @author Patrick André
 * @version 1.2.6
 *
 * @see Grid
 * @see Tile
 * @see Camera2D
 */
public class TileMap {

    private final Grid grid;
    private boolean cullingEnabled = true;
    
    // Neue Features für Positionierung und Skalierung
    private int offsetX = 0;
    private int offsetY = 0;
    private float scale = 1.0f;

    /**
     * Erstellt eine neue TileMap für das angegebene Grid.
     *
     * @param grid Das zu rendernde {@link Grid}.
     * @throws RuntimeException (Lunar-Error) wenn das Grid {@code null} ist.
     */
    public TileMap(Grid grid) {
        if (grid == null) {
            throw Errors.lunar(ErrorDetails.of("TileMap", "Grid must not be null")
                    .withActual(null)
                    .withExpected("An existing Grid")
                    .withExplanation("The tile map reads its tiles, dimensions and cell size from the supplied grid"), null);
        }

        this.grid = grid;
    }

    /**
     * Zeichnet das Grid in Weltkoordinaten.
     *
     * <p>Diese Methode sollte zwischen {@code camera.apply()} und {@code camera.reset()} aufgerufen werden.</p>
     *
     * @param g Das {@link Graphics}-Objekt zum Zeichnen.
     */
    public void draw(Graphics g) {
        draw(g, null);
    }

    /**
     * Zeichnet das Grid in Weltkoordinaten mit optionaler {@link Camera2D} für das Culling.
     *
     * <p>Wenn eine Kamera übergeben wird und Culling aktiviert ist, werden nur
     * sichtbare Tiles gezeichnet. Dies verbessert die Performance bei grossen Grids.</p>
     *
     * @param g      Das {@link Graphics}-Objekt zum Zeichnen.
     * @param camera Optionale {@link Camera2D} für die Culling-Optimierung (kann {@code null} sein).
     */
    public void draw(Graphics g, Camera2D camera) {
        if (g == null) {
            return;
        }

        int cellSize = grid.getCellSize();
        int scaledCellSize = (int) (cellSize * scale);

        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getCols(); col++) {
                Tile tile = grid.getTile(row, col);

                if (tile == null) {
                    continue;
                }

                // Tile zeichnen mit Offset und Skalierung
                float worldX = offsetX + col * scaledCellSize;
                float worldY = offsetY + row * scaledCellSize;

                // Culling: Prüfen ob Tile sichtbar ist (unter Berücksichtigung von Offset und Scale)
                if (cullingEnabled && camera != null && !camera.isVisible(worldX, worldY, scaledCellSize)) {
                    continue;
                }

                if (tile.hasImage()) {
                    g.drawImage(tile.getImage(), (int) worldX, (int) worldY, scaledCellSize, scaledCellSize);
                } else if (tile.getColor() != null) {
                    g.setColor(tile.getColor());
                    g.fillRect((int) worldX, (int) worldY, scaledCellSize, scaledCellSize);
                }
            }
        }
    }

    /**
     * Zentriert die TileMap automatisch in einem gegebenen Viewport.
     *
     * @deprecated Verwende {@link Camera2D#snapTo(float, float)} oder {@link Camera2D#follow(Actor, float)} stattdessen,
     * um die Welt zu zentrieren, ohne die Physik-Koordinaten von der visuellen Darstellung zu entkoppeln.
     *
     * @param viewportWidth  Die Breite des Fensters/Viewports.
     * @param viewportHeight Die Höhe des Fensters/Viewports.
     */
    @Deprecated
    public void centerIn(int viewportWidth, int viewportHeight) {
        int scaledCellSize = (int) (grid.getCellSize() * scale);
        int levelWidth = grid.getCols() * scaledCellSize;
        int levelHeight = grid.getRows() * scaledCellSize;
        
        this.offsetX = (viewportWidth - levelWidth) / 2;
        this.offsetY = (viewportHeight - levelHeight) / 2;
    }

    /**
     * Setzt einen manuellen Offset für das Rendering der TileMap.
     *
     * @deprecated Verwende {@link Camera2D#snapTo(float, float)} oder {@link Camera2D#follow(Actor, float)} stattdessen,
     * um die Welt zu zentrieren, ohne die Physik-Koordinaten von der visuellen Darstellung zu entkoppeln.
     *
     * @param offsetX Verschiebung auf der X-Achse.
     * @param offsetY Verschiebung auf der Y-Achse.
     */
    @Deprecated
    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Setzt die Skalierung der TileMap.
     * Standard ist 1.0f. Werte &gt; 1.0 vergrössern das Level, Werte &lt; 1.0 verkleinern es.
     *
     * @deprecated Verwende {@link Camera2D#snapTo(float, float)} oder {@link Camera2D#follow(Actor, float)} stattdessen,
     * um die Welt zu zentrieren, ohne die Physik-Koordinaten von der visuellen Darstellung zu entkoppeln.
     *
     * @param scale Der Skalierungsfaktor (muss &gt; 0 sein).
     * @throws RuntimeException (Lunar-Error) wenn die Skalierung nicht endlich oder kleiner/gleich 0 ist.
     */
    @Deprecated
    public void setScale(float scale) {
        if (!Float.isFinite(scale)) {
            throw Errors.lunar(ErrorDetails.of("TileMap", "Scale must be finite")
                    .withActual(scale)
                    .withExpected("A finite scale greater than 0")
                    .withExplanation("NaN and infinite scales cannot define a valid rendered cell size"), null);
        }
        if (scale <= 0f) {
            throw Errors.lunar(ErrorDetails.of("TileMap", "Scale must be greater than zero")
                    .withActual(scale)
                    .withExpected("A scale greater than 0")
                    .withExplanation("The tile map requires a positive scale to calculate its rendered cell dimensions"), null);
        }

        this.scale = scale;
    }

    /**
     * Skaliert die TileMap automatisch, damit sie optimal ins Fenster passt.
     * Gibt den verwendeten Scale-Faktor zurück, damit andere Objekte (z.B. Spieler)
     * ihre Grösse entsprechend anpassen können.
     *
     * @deprecated Verwende {@link Camera2D#snapTo(float, float)} oder {@link Camera2D#follow(Actor, float)} stattdessen,
     * um die Welt zu zentrieren, ohne die Physik-Koordinaten von der visuellen Darstellung zu entkoppeln.
     *
     * @param windowWidth  Die Breite des Fensters.
     * @param windowHeight Die Höhe des Fensters.
     * @return Der berechnete und angewendete Skalierungsfaktor.
     */
    @Deprecated
    public float fitToWindow(int windowWidth, int windowHeight) {
        if (windowWidth <= 0) {
            throw Errors.lunar(ErrorDetails.of("TileMap", "Window width must be greater than zero")
                    .withActual(windowWidth)
                    .withExpected("A window width greater than 0")
                    .withExplanation("Automatic fitting requires a positive horizontal drawing area"), null);
        }
        if (windowHeight <= 0) {
            throw Errors.lunar(ErrorDetails.of("TileMap", "Window height must be greater than zero")
                    .withActual(windowHeight)
                    .withExpected("A window height greater than 0")
                    .withExplanation("Automatic fitting requires a positive vertical drawing area"), null);
        }

        float levelWidth = grid.getCols() * (float) grid.getCellSize();
        float levelHeight = grid.getRows() * (float) grid.getCellSize();

        float scaleX = windowWidth / levelWidth;
        float scaleY = windowHeight / levelHeight;

        // Kleineren Faktor, damit alles reinpasst (Seitenverhältnis bleibt)
        scale = Maths.min(scaleX, scaleY);

        int scaledCellSize = (int) (grid.getCellSize() * scale);
        int drawWidth = grid.getCols() * scaledCellSize;
        int drawHeight = grid.getRows() * scaledCellSize;

        offsetX = (windowWidth - drawWidth) / 2;
        offsetY = (windowHeight - drawHeight) / 2;

        return scale;
    }

    /**
     * Gibt den aktuellen X-Offset zurück.
     * @return Der X-Offset.
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * Gibt den aktuellen Y-Offset zurück.
     * @return Der Y-Offset.
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Gibt den aktuellen Skalierungsfaktor zurück.
     * @return Der Skalierungsfaktor.
     */
    public float getScale() {
        return scale;
    }

    /**
     * Aktiviert oder deaktiviert die Culling-Optimierung.
     *
     * @param enabled {@code true} um Culling zu aktivieren, {@code false} um es zu deaktivieren.
     */
    public void setCullingEnabled(boolean enabled) {
        this.cullingEnabled = enabled;
    }

    /**
     * Prüft, ob Culling aktiviert ist.
     * @return {@code true}, wenn Culling aktiviert ist.
     */
    public boolean isCullingEnabled() {
        return cullingEnabled;
    }

    /**
     * Gibt das zugehörige Grid zurück.
     * @return Das {@link Grid}.
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Gibt eine textuelle Darstellung der TileMap mit Grid, Culling-Status, Offset und Skalierung zurück 
     * (ideal für Debugging-Zwecke).
     *
     * @return Ein formatierter String mit den TileMap-Eigenschaften.
     */
    @Override
    public String toString() {
        return String.format("TileMap[grid=%s, culling=%b, offset=(%.2f, %.2f), scale=%.2f]", grid, cullingEnabled, offsetX, offsetY, scale);
    }
}