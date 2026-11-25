package LunarAxis.core.game;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.Point;

/**
 * Maps a fixed logical image to a centered rectangle in Swing panel coordinates.
 *
 * @author Patrick André
 * @version 1.2.7
 */
public final class ViewportGeometry {

    private final int logicalWidth;
    private final int logicalHeight;
    private final int panelWidth;
    private final int panelHeight;
    private final double scale;
    private final double offsetX;
    private final double offsetY;
    private final double right;
    private final double bottom;

    private ViewportGeometry(int logicalWidth, int logicalHeight, int panelWidth, int panelHeight) {
        this.logicalWidth = logicalWidth;
        this.logicalHeight = logicalHeight;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        scale = Math.min((double) panelWidth / logicalWidth, (double) panelHeight / logicalHeight);
        offsetX = Math.max(0.0, (panelWidth - logicalWidth * scale) / 2.0);
        offsetY = Math.max(0.0, (panelHeight - logicalHeight * scale) / 2.0);
        right = offsetX + logicalWidth * scale;
        bottom = offsetY + logicalHeight * scale;
    }

    /**
     * Fits the logical image without changing its aspect ratio.
     *
     * @return the fitted geometry, or null if the panel has no drawable area
     */
    public static ViewportGeometry fit(int logicalWidth, int logicalHeight, int panelWidth, int panelHeight) {
        if (logicalWidth <= 0 || logicalHeight <= 0) {
            throw Errors.argument(ErrorDetails.of("ViewportGeometry", "Logical dimensions must be positive")
                    .withActual("width=" + logicalWidth + ", height=" + logicalHeight)
                    .withExpected("A positive logical width and height")
                    .withExplanation("The logical dimensions define the source image for viewport fitting"), null);
        }

        if (panelWidth <= 0 || panelHeight <= 0) {
            return null;
        }

        return new ViewportGeometry(logicalWidth, logicalHeight, panelWidth, panelHeight);
    }

    /**
     * Converts a panel position to a logical pixel.
     *
     * @return the logical pixel, or null outside the half-open image bounds
     */
    public Point mapToLogical(int panelX, int panelY) {
        if (panelX < 0 || panelY < 0 || panelX >= panelWidth || panelY >= panelHeight
                || panelX < offsetX || panelX >= right || panelY < offsetY || panelY >= bottom) {
            return null;
        }

        int x = Math.min(logicalWidth - 1, (int) ((panelX - offsetX) / scale));
        int y = Math.min(logicalHeight - 1, (int) ((panelY - offsetY) / scale));
        return new Point(x, y);
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public int getPanelHeight() {
        return panelHeight;
    }

    public double getScale() {
        return scale;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }
}