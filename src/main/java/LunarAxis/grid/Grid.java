package LunarAxis.grid;

import LunarAxis.math.Maths;
import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet ein 2D-Gitter von Tiles und stellt Koordinaten-Umrechnung bereit.
 *
 * <p>Das Grid arbeitet in Weltkoordinaten: Die Zelle (0,0) befindet sich bei
 * Weltkoordinate (0,0), und jede Zelle hat die Grösse cellSize x cellSize Pixel.</p>
 *
 * <h3>Koordinaten-System:</h3>
 * <pre>
 * Grid-Koordinaten: (row, col) - Indizes im Array
 * Welt-Koordinaten: (x, y) - Pixel in der Spielwelt
 *
 * Umrechnung:
 *   Welt → Grid: col = worldX / cellSize, row = worldY / cellSize
 *   Grid → Welt: worldX = col * cellSize, worldY = row * cellSize
 * </pre>
 *
 * <h3>Beispiel (TigerJython):</h3>
 * <pre>
 * grid = Grid(10, 15, 40)  # 10 Zeilen, 15 Spalten, 40px pro Zelle
 * grid.setTile(0, 0, wall)
 * tile = grid.getTile(0, 0)
 * pos = grid.getWorldPosition(0, 0)  # Vec2(0, 0)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 * @see Tile
 * @see TileMap
 */
public class Grid {

    private final int rows;
    private final int cols;
    private final int cellSize;
    private final Tile[][] tiles;

    /**
     * Erstellt ein neues Grid mit der angegebenen Grösse.
     *
     * @param rows     Anzahl der Zeilen
     * @param cols     Anzahl der Spalten
     * @param cellSize Grösse jeder Zelle in Pixeln
     * @throws RuntimeException (Lunar-Error) wenn rows, cols oder cellSize kleiner oder gleich 0 sind
     */
    public Grid(int rows, int cols, int cellSize) {
        if (rows <= 0 || cols <= 0 || cellSize <= 0) {
            throw Errors.argument(ErrorDetails.of("Grid", "Grid dimensions and cellSize must be positive")
                    .withActual("rows=" + rows + ", cols=" + cols + ", cellSize=" + cellSize)
                    .withExpected("Rows, columns and cell size greater than 0")
                    .withExplanation("A grid requires at least one row and column, and coordinate conversion requires a positive cell size"), null);
        }

        this.rows = rows;
        this.cols = cols;
        this.cellSize = cellSize;
        this.tiles = new Tile[rows][cols];
    }

    /**
     * Prüft, ob die angegebene Position innerhalb des Grids liegt.
     *
     * @param row Zeilen-Index
     * @param col Spalten-Index
     * @return true wenn die Position gültig ist, false sonst
     */
    public boolean isValid(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Setzt ein Tile an der angegebenen Position.
     *
     * @param row  Zeilen-Index
     * @param col  Spalten-Index
     * @param tile Das zu setzende Tile (kann null sein für leere Zelle)
     */
    public void setTile(int row, int col, Tile tile) {
        if (isValid(row, col)) {
            tiles[row][col] = tile;
        }
    }

    /**
     * Gibt das Tile an der angegebenen Position zurück.
     *
     * @param row Zeilen-Index
     * @param col Spalten-Index
     * @return Das Tile oder null, wenn die Position ungültig ist oder keine Tile gesetzt wurde
     */
    public Tile getTile(int row, int col) {
        if (isValid(row, col)) {
            return tiles[row][col];
        }
        return null;
    }

    /**
     * Setzt alle Zellen auf null (leer).
     */
    public void clear() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = null;
            }
        }
    }

    /**
     * Füllt das gesamte Grid mit dem angegebenen Tile.
     *
     * @param tile Das Tile, das in alle Zellen gesetzt werden soll
     */
    public void fill(Tile tile) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = tile;
            }
        }
    }

    /**
     * Konvertiert eine Welt-X-Koordinate in eine Grid-Spalte.
     *
     * @param worldX X-Koordinate in der Spielwelt
     * @return Spalten-Index (kann ausserhalb des Grids liegen)
     * @throws RuntimeException (Lunar-Error) wenn worldX nicht endlich ist (NaN oder unendlich)
     */
    public int getGridCol(float worldX) {
        if (!Float.isFinite(worldX)) {
            throw Errors.argument(ErrorDetails.of("Grid", "World X coordinate must be finite")
                    .withActual(worldX)
                    .withExpected("A finite world X coordinate")
                    .withExplanation("NaN and infinite coordinates cannot identify a meaningful grid column"), null);
        }

        return (int) Maths.floor((worldX / cellSize));
    }

    /**
     * Konvertiert eine Welt-Y-Koordinate in eine Grid-Zeile.
     *
     * @param worldY Y-Koordinate in der Spielwelt
     * @return Zeilen-Index (kann ausserhalb des Grids liegen)
     * @throws RuntimeException (Lunar-Error) wenn worldY nicht endlich ist (NaN oder unendlich)
     */
    public int getGridRow(float worldY) {
        if (!Float.isFinite(worldY)) {
            throw Errors.argument(ErrorDetails.of("Grid", "World Y coordinate must be finite")
                    .withActual(worldY)
                    .withExpected("A finite world Y coordinate")
                    .withExplanation("NaN and infinite coordinates cannot identify a meaningful grid row"), null);
        }

        return (int) Maths.floor((worldY / cellSize));
    }

    /**
     * Gibt die Welt-Position der linken oberen Ecke einer Zelle zurück.
     *
     * @param row Zeilen-Index
     * @param col Spalten-Index
     * @return Vec2 mit den Welt-Koordinaten der Zellen-Ecke
     */
    public Vec2 getWorldPosition(int row, int col) {
        float worldX = col * cellSize;
        float worldY = row * cellSize;
        return new Vec2(worldX, worldY);
    }

    /**
     * Gibt die Welt-Position der Mitte einer Zelle zurück.
     * Nützlich für die Positionierung von Actors in der Zellen-Mitte.
     *
     * @param row Zeilen-Index
     * @param col Spalten-Index
     * @return Vec2 mit den Welt-Koordinaten der Zellen-Mitte
     */
    public Vec2 getWorldCenter(int row, int col) {
        float worldX = col * cellSize + cellSize / 2.0f;
        float worldY = row * cellSize + cellSize / 2.0f;
        return new Vec2(worldX, worldY);
    }

    /**
     * Gibt die Tiles der 8 Nachbarzellen zurück (inklusive Diagonalen).
     *
     * @param row Zeilen-Index der Zentrum-Zelle
     * @param col Spalten-Index der Zentrum-Zelle
     * @return Liste der Nachbar-Tiles (null-Werte werden übersprungen)
     */
    public List<Tile> getNeighbors(int row, int col) {
        List<Tile> neighbors = new ArrayList<>();
        List<int[]> coords = getNeighborCoords(row, col, true);

        for (int[] coord : coords) {
            Tile tile = getTile(coord[0], coord[1]);
            if (tile != null) {
                neighbors.add(tile);
            }
        }
        return neighbors;
    }

    /**
     * Gibt die Koordinaten der Nachbarzellen zurück.
     *
     * @param row              Zeilen-Index der Zentrum-Zelle
     * @param col              Spalten-Index der Zentrum-Zelle
     * @param includeDiagonals true für 8 Nachbarn, false für 4 Nachbarn (nur orthogonal)
     * @return Liste von [row, col]-Arrays der Nachbarzellen
     */
    public List<int[]> getNeighborCoords(int row, int col, boolean includeDiagonals) {
        List<int[]> neighbors = new ArrayList<>();

        int[] dRows = includeDiagonals ?
                new int[]{-1, -1, -1, 0, 0, 1, 1, 1} :
                new int[]{-1, 1, 0, 0};
        int[] dCols = includeDiagonals ?
                new int[]{-1, 0, 1, -1, 1, -1, 0, 1} :
                new int[]{0, 0, -1, 1};

        for (int i = 0; i < dRows.length; i++) {
            int nRow = row + dRows[i];
            int nCol = col + dCols[i];

            if (isValid(nRow, nCol)) {
                neighbors.add(new int[]{nRow, nCol});
            }
        }
        return neighbors;
    }

    /**
     * Gibt die Anzahl der Zeilen zurück.
     * @return Anzahl Zeilen
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gibt die Anzahl der Spalten zurück.
     * @return Anzahl Spalten
     */
    public int getCols() {
        return cols;
    }

    /**
     * Gibt die Grösse einer Zelle in Pixeln zurück.
     * @return Zellgrösse in Pixeln
     */
    public int getCellSize() {
        return cellSize;
    }

    /**
     * Gibt die Gesamtbreite des Grids in Pixeln zurück.
     * @return Breite = cols * cellSize
     */
    public int getWidth() {
        return cols * cellSize;
    }

    /**
     * Gibt die Gesamthöhe des Grids in Pixeln zurück.
     * @return Höhe = rows * cellSize
     */
    public int getHeight() {
        return rows * cellSize;
    }

    /**
     * Gibt eine textuelle Darstellung des Grids mit Zeilen, Spalten, Zellgrösse
     * und Gesamtpixel-Dimensionen zurück (ideal für Debugging-Zwecke).
     *
     * @return Ein formatierter String mit den Gitter-Eigenschaften.
     */
    @Override
    public String toString() {
        return String.format("Grid[rows=%d, cols=%d, cellSize=%dpx, dimensions=%dx%d]",
                rows, cols, cellSize, getWidth(), getHeight());
    }
}
