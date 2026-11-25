package LunarAxis.graphics;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.image.BufferedImage;

/**
 * Die Klasse {@code SpriteSheet} dient dazu, eine grosse Bilddatei (ein sogenanntes Sprite-Sheet),
 * die viele kleine Einzelbilder (Sprites) in einem Raster enthält, in ihre einzelnen Teile zu zerlegen.
 * <p>
 * In der 2D-Spieleentwicklung packt man Grafiken (wie verschiedene Lauf-Animationen eines Charakters
 * oder ein Set aus Items) oft zusammen in ein einziges Bild, um die Dateiverwaltung zu vereinfachen.
 * Der {@code SpriteSheet}-Manager schneidet daraus automatisch die gewünschten Ausschnitte heraus.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Ein SpriteSheet laden und Einzelbilder ausschneiden
 * from LunarAxis.graphics import SpriteSheet
 *
 * # Lädt das Bild und teilt es in kleine Kacheln (Cells) mit je 32x32 Pixeln auf
 * sheetImage = engine.loadImage("assets/images/characters.png")
 * sheet = SpriteSheet(sheetImage, 32, 32)
 *
 * # Holt das erste Bild aus der ersten Spalte (0), ersten Reihe (0)
 * playerIcon = sheet.getSprite(0, 0)
 *
 * # Holt eine ganze Reihe als Array (z.B. für eine Lauf-Animation)
 * walkFrames = sheet.getRow(0)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class SpriteSheet {

    private final BufferedImage bufferedImageSheet;
    private final int cellWidth, cellHeight;
    private final int columns, rows;

    /**
     * Erstellt ein neues SpriteSheet aus einem grossen Gesamtbild und teilt es
     * in ein festes Raster aus gleich grossen Zellen auf.
     *
     * @param bufferedImageSheet Das grosse Gesamtbild ({@link BufferedImage}), das alle Sprites enthält.
     * @param cellWidth          Die Breite einer einzelnen Kachel (Sprite) in Pixeln.
     * @param cellHeight         Die Höhe einer einzelnen Kachel (Sprite) in Pixeln.
     *
     * @throws RuntimeException (Lunar-Error) Wenn das Bild fehlt oder die Kachel-Grösse ungültig (0 oder negativ) ist.
     */
    public SpriteSheet(BufferedImage bufferedImageSheet, int cellWidth, int cellHeight) {
        if (bufferedImageSheet == null) {
            throw Errors.argument(ErrorDetails.of("SpriteSheet", "Sprite sheet image cannot be null")
                    .withActual(null)
                    .withExpected("An existing BufferedImage")
                    .withExplanation("Sprite extraction requires an image containing the sprite cells"), null);
        }

        if (cellWidth <= 0 || cellHeight <= 0) {
            throw Errors.argument(ErrorDetails.of("SpriteSheet", "Sprite dimensions must be greater than 0")
                    .withActual("cellWidth=" + cellWidth + ", cellHeight=" + cellHeight)
                    .withExpected("Positive cell width and height")
                    .withExplanation("The image dimensions are divided by the cell dimensions to determine the available sprites"), null);
        }

        this.bufferedImageSheet = bufferedImageSheet;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;

        this.columns = bufferedImageSheet.getWidth() / cellWidth;
        this.rows = bufferedImageSheet.getHeight() / cellHeight;

        if (columns <= 0 || rows <= 0) {
            throw Errors.argument(ErrorDetails.of("SpriteSheet", "Sprite sheet is smaller than one sprite")
                    .withActual("imageWidth=" + bufferedImageSheet.getWidth() + ", imageHeight=" + bufferedImageSheet.getHeight() + ", cellWidth=" + cellWidth + ", cellHeight=" + cellHeight)
                    .withExpected("An image containing at least one complete sprite cell")
                    .withExplanation("Both image dimensions must be at least as large as the corresponding cell dimensions"), null);
        }
    }

    /**
     * Schneidet ein spezifisches Einzelbild anhand seiner Spalten- und Reihen-Nummer aus.
     *
     * @param col Die Spalte (von links nach rechts, beginnend bei 0).
     * @param row Die Reihe (von oben nach unten, beginnend bei 0).
     * @return Das ausgeschnittene {@link BufferedImage} oder {@code null}, wenn die Koordinaten ausserhalb liegen.
     */
    public BufferedImage getSprite(int col, int row) {
        if (col < 0 || col >= columns || row < 0 || row >= rows) {
            return null;
        }
        return bufferedImageSheet.getSubimage(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
    }

    /**
     * Schneidet ein Einzelbild anhand einer fortlaufenden Nummer (Index) aus.
     * Dabei wird zeilenweise von links nach rechts gezählt.
     *
     * @param index Die laufende Nummer des Sprites (beginnend bei 0).
     * @return Das ausgeschnittene {@link BufferedImage} oder {@code null}, wenn der Index ungültig ist.
     */
    public BufferedImage getSprite(int index) {
        if (index < 0 || index >= getTotalSprites()) {
            return null;
        }
        int col = index % columns;
        int row = index / columns;

        return getSprite(col, row);
    }

    /**
     * Schneidet eine komplette waagrechte Reihe aus (z.B. alle Bilder einer Animationsrichtung).
     *
     * @param row Die Nummer der gewünschten Reihe (beginnend bei 0).
     * @return Ein Array von {@link BufferedImage}-Objekten, die der Reihe entsprechen.
     */
    public BufferedImage[] getRow(int row) {
        if (row < 0 || row >= rows) {
            return new BufferedImage[0];
        }

        BufferedImage[] sprites = new BufferedImage[columns];

        for (int col = 0; col < columns; col++) {
            sprites[col] = getSprite(col, row);
        }
        return sprites;
    }

    /**
     * Schneidet eine bestimmte Anzahl an Bildern aus einer horizontalen Reihe aus.
     * Nützlich, wenn eine Reihe im Bild mehr Sprites enthält, als die Animation tatsächlich braucht.
     *
     * @param row   Die Nummer der Reihe (beginnend bei 0).
     * @param count Die Anzahl der Sprites, die von links beginnend ausgeschnitten werden sollen.
     * @return Ein Array der ausgeschnittenen Bilder.
     */
    public BufferedImage[] getRow(int row, int count) {
        if (row < 0 || row >= rows || count <= 0) {
            return new BufferedImage[0];
        }

        count = Math.min(count, columns);
        BufferedImage[] sprites = new BufferedImage[count];

        for (int col = 0; col < count; col++) {
            sprites[col] = getSprite(col, row);
        }

        return sprites;
    }

    /**
     * Gibt die Anzahl der Spalten (Sprite-Anzahl pro Zeile) auf diesem Sheet zurück.
     *
     * @return Die Spaltenanzahl.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gibt die Anzahl der Reihen auf diesem Sheet zurück.
     *
     * @return Die Reihenanzahl.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Berechnet die Gesamtzahl aller Einzelbilder, die in diesem SpriteSheet enthalten sind.
     *
     * @return Die Gesamtanzahl der Sprites.
     */
    public int getTotalSprites() {
        return columns * rows;
    }

    /**
     * Gibt die feste Pixel-Breite einer einzelnen Kachel (Cell) zurück.
     *
     * @return Die Kachel-Breite in Pixeln.
     */
    public int getSpriteWidth() {
        return cellWidth;
    }

    /**
     * Gibt die feste Pixel-Höhe einer einzelnen Kachel (Cell) zurück.
     *
     * @return Die Kachel-Höhe in Pixeln.
     */
    public int getSpriteHeight() {
        return cellHeight;
    }

    /**
     * Gibt das zugrundeliegende, ungeschnittene Gesamtbild zurück.
     *
     * @return Das originale {@link BufferedImage} des Sheets.
     */
    public BufferedImage getSheetImage() {
        return bufferedImageSheet;
    }
}