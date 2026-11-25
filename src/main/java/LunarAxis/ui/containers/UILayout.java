package LunarAxis.ui.containers;

import LunarAxis.ui.core.UIElement;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.util.List;

/**
 * Die Klasse {@code UILayout} unterstützt die automatische Anordnung und Positionierung
 * von Kind-UI-Elementen innerhalb eines {@link UIPanel}s.
 * <p>
 * Sie bietet verschiedene Layout-Modi: Vertikal (untereinander), Horizontal (nebeneinander)
 * sowie Grid (in einem gleichmässigen Raster).
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UILayout {

    /**
     * Enumeration der unterstützten Layout-Modi für die Positionierung von UI-Elementen.
     */
    public enum Mode {
        /**
         * Elemente werden vertikal untereinander angeordnet.
         */
        VERTICAL,

        /**
         * Elemente werden horizontal nebeneinander angeordnet.
         */
        HORIZONTAL,

        /**
         * Elemente werden in einem Spalten-Raster (Grid) angeordnet.
         */
        GRID
    }

    private Mode mode;
    private int spacing;
    private int padding;
    private int gridColumns;

    /**
     * Erstellt ein neues {@code UILayout} mit dem angegebenen Modus, Abstand und Innenabstand.
     *
     * @param mode    Der zu verwendende {@link Mode} (VERTICAL, HORIZONTAL oder GRID).
     * @param spacing Der Abstand zwischen den einzelnen UI-Elementen in Pixeln.
     * @param padding Der innere Rand (Padding) zum Container in Pixeln.
     * @throws RuntimeException (Lunar-Error) Wenn {@code mode} {@code null} ist.
     */
    public UILayout(Mode mode, int spacing, int padding) {
        if (mode == null) {
            throw Errors.nullValue(ErrorDetails.of("UILayout", "Layout mode must not be null")
                    .withActual(null)
                    .withExpected("VERTICAL, HORIZONTAL or GRID")
                    .withExplanation("Applying a layout requires a valid arrangement mode"), null);
        }

        this.mode = mode;
        this.spacing = spacing;
        this.padding = padding;
        this.gridColumns = 3; // Standard für Grid
    }

    /**
     * Factory-Methode zur Erstellung eines vertikalen Layouts.
     *
     * @param spacing Abstand zwischen den Elementen.
     * @param padding Innenabstand des Containers.
     * @return Ein neues {@link UILayout} im Modus {@link Mode#VERTICAL}.
     */
    public static UILayout vertical(int spacing, int padding) {
        return new UILayout(Mode.VERTICAL, spacing, padding);
    }

    /**
     * Factory-Methode zur Erstellung eines horizontalen Layouts.
     *
     * @param spacing Abstand zwischen den Elementen.
     * @param padding Innenabstand des Containers.
     * @return Ein neues {@link UILayout} im Modus {@link Mode#HORIZONTAL}.
     */
    public static UILayout horizontal(int spacing, int padding) {
        return new UILayout(Mode.HORIZONTAL, spacing, padding);
    }

    /**
     * Factory-Methode zur Erstellung eines Raster-Layouts (Grid).
     *
     * @param columns Anzahl der Spalten.
     * @param spacing Abstand zwischen den Elementen.
     * @param padding Innenabstand des Containers.
     * @return Ein neues {@link UILayout} im Modus {@link Mode#GRID}.
     */
    public static UILayout grid(int columns, int spacing, int padding) {
        UILayout layout = new UILayout(Mode.GRID, spacing, padding);
        layout.setGridColumns(columns);
        return layout;
    }

    /**
     * Wendet die berechneten Koordinaten und Dimensionen des Layouts
     * auf alle Kind-Elemente des übergebenen {@link UIPanel}s an.
     *
     * @param panel Das {@link UIPanel}, dessen Kinder positioniert werden sollen.
     */
    public void apply(UIPanel panel) {
        List<UIElement> children = panel.getChildren();
        int panelWidth = panel.getWidth();
        int panelHeight = panel.getHeight();

        switch (mode) {
            case VERTICAL:
                applyVertical(children, 0f, 0f, panelWidth);
                break;
            case HORIZONTAL:
                applyHorizontal(children, 0f, 0f, panelHeight);
                break;
            case GRID:
                applyGrid(children, 0f, 0f, panelWidth, panelHeight);
                break;
        }
    }

    /**
     * Positioniert die Kind-Elemente vertikal untereinander.
     *
     * @param children   Die Liste der zu positionierenden UI-Elemente.
     * @param panelX     Die Start-X-Koordinate des Containers.
     * @param panelY     Die Start-Y-Koordinate des Containers.
     * @param panelWidth Die Gesamtbreite des Containers.
     */
    private void applyVertical(List<UIElement> children, float panelX, float panelY, int panelWidth) {
        float currentY = panelY + padding;
        
        for (UIElement child : children) {
            child.setX(panelX + padding);
            child.setY(currentY);
            
            // Falls das Child keine feste Breite hat, nutze Panel-Breite minus Padding
            if (child.getWidth() == 0) {
                child.setWidth(panelWidth - (padding * 2));
            }
            
            currentY += child.getHeight() + spacing;
        }
    }

    /**
     * Positioniert die Kind-Elemente horizontal nebeneinander.
     *
     * @param children    Die Liste der zu positionierenden UI-Elemente.
     * @param panelX      Die Start-X-Koordinate des Containers.
     * @param panelY      Die Start-Y-Koordinate des Containers.
     * @param panelHeight Die Gesamthöhe des Containers.
     */
    private void applyHorizontal(List<UIElement> children, float panelX, float panelY, int panelHeight) {
        float currentX = panelX + padding;
        
        for (UIElement child : children) {
            child.setX(currentX);
            child.setY(panelY + padding);
            
            // Falls das Kind keine feste Höhe hat, nutze Panel-Höhe minus Padding
            if (child.getHeight() == 0) {
                child.setHeight(panelHeight - (padding * 2));
            }
            
            currentX += child.getWidth() + spacing;
        }
    }

    /**
     * Positioniert die Child-Elemente in einem quadratischen Spaltenraster (Grid).
     *
     * @param children    Die Liste der zu positionierenden UI-Elemente.
     * @param panelX      Die Start-X-Koordinate des Containers.
     * @param panelY      Die Start-Y-Koordinate des Containers.
     * @param panelWidth  Die Gesamtbreite des Containers.
     * @param panelHeight Die Gesamthöhe des Containers.
     */
    private void applyGrid(List<UIElement> children, float panelX, float panelY, int panelWidth, int panelHeight) {
        int cellWidth = (panelWidth - (padding * 2) - (spacing * (gridColumns - 1))) / gridColumns;
        int cellHeight = cellWidth; // Quadratische Zellen
        
        int col = 0;
        int row = 0;
        
        for (UIElement child : children) {
            float x = panelX + padding + (col * (cellWidth + spacing));
            float y = panelY + padding + (row * (cellHeight + spacing));
            
            child.setX(x);
            child.setY(y);
            child.setWidth(cellWidth);
            child.setHeight(cellHeight);
            
            col++;
            if (col >= gridColumns) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Gibt den aktuellen Layout-Modus zurück.
     *
     * @return Der {@link Mode}.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Setzt den Layout-Modus.
     *
     * @param mode Der neue {@link Mode}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code mode} {@code null} ist.
     */
    public void setMode(Mode mode) {
        if (mode == null) {
            throw Errors.nullValue(ErrorDetails.of("UILayout", "Layout mode must not be null")
                    .withActual(null)
                    .withExpected("VERTICAL, HORIZONTAL or GRID")
                    .withExplanation("Applying a layout requires a valid arrangement mode"), null);
        }

        this.mode = mode;
    }

    /**
     * Gibt den Abstand zwischen den Elementen zurück.
     *
     * @return Abstand in Pixeln.
     */
    public int getSpacing() {
        return spacing;
    }

    /**
     * Setzt den Abstand zwischen den Elementen.
     *
     * @param spacing Neuer Abstand in Pixeln.
     */
    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    /**
     * Gibt den Innenabstand (Padding) zum Container zurück.
     *
     * @return Padding in Pixeln.
     */
    public int getPadding() {
        return padding;
    }

    /**
     * Setzt den Innenabstand (Padding) zum Container.
     *
     * @param padding Neues Padding in Pixeln.
     */
    public void setPadding(int padding) {
        this.padding = padding;
    }

    /**
     * Gibt die Anzahl der Spalten im Grid-Modus zurück.
     *
     * @return Anzahl der Spalten.
     */
    public int getGridColumns() {
        return gridColumns;
    }

    /**
     * Setzt die Anzahl der Spalten für den Grid-Modus.
     *
     * @param gridColumns Anzahl der Spalten (muss grösser als 0 sein).
     * @throws RuntimeException (Lunar-Error) Wenn die Anzahl 0 oder negativ ist.
     */
    public void setGridColumns(int gridColumns) {
        if (gridColumns == 0) {
            throw Errors.arithmetic(ErrorDetails.of("UILayout", "Grid column count must not be zero")
                    .withActual(gridColumns)
                    .withExpected("A column count greater than 0")
                    .withExplanation("Grid cell width is calculated by dividing the available width by the column count"), null);
        }
        if (gridColumns < 0) {
            throw Errors.argument(ErrorDetails.of("UILayout", "Grid column count must not be negative")
                    .withActual(gridColumns)
                    .withExpected("A column count greater than 0")
                    .withExplanation("A grid cannot arrange elements into a negative number of columns"), null);
        }

        this.gridColumns = gridColumns;
    }
}