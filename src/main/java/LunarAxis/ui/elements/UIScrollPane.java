package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.containers.UIPanel;
import LunarAxis.ui.core.UIAnchor;
import LunarAxis.ui.core.UIElement;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.Graphics2D;
import java.awt.Shape;

/**
 * Die Klasse {@code UIScrollPane} repräsentiert einen Container-UI-Element, das seinen Inhalt vertikal scrollen kann.
 * <p>
 * Sie eignet sich besonders für Inventare, lange Optionslisten, Dialogverläufe oder Chat-Logs.
 * Mithilfe von AWT-Clipping wird sichergestellt, dass der enthaltene Inhalt nur innerhalb der definierten
 * Fenstergrenzen gezeichnet wird.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * listPanel = UIPanel(0, 0, 300, 800)
 * scrollPane = UIScrollPane(50, 50, 320, 400, listPanel)
 * </pre>
 *
 * @version 1.2.6
 */
public class UIScrollPane extends UIElement {
    private UIElement content;
    private float scrollY, maxScrollY;

    private enum DragMode {
        NONE, CONTENT, THUMB
    }

    private DragMode dragMode = DragMode.NONE;

    private static final int THUMB_WIDTH = 8;
    private static final int MIN_THUMB_HEIGHT = 20;

    private float lastMouseY;

    private Colors backgroundColor;
    private Colors borderColor;
    private Colors scrollBarColor;

    /**
     * Erstellt eine neue ScrollPane mit Position, Abmessungen und einem zugewiesenen Inhalt-Element.
     *
     * @param x       Die X-Koordinate in Screen-Koordinaten.
     * @param y       Die Y-Koordinate in Screen-Koordinaten.
     * @param width   Die Breite der ScrollPane in Pixeln.
     * @param height  Die Höhe der ScrollPane in Pixeln.
     * @param content Das anzuzeigende {@link UIElement} (z.B. ein {@link UIPanel}).
     */
    public UIScrollPane(float x, float y, int width, int height, UIElement content) {
        super(x, y, width, height);
        this.content = content;
        this.scrollY = 0;
        this.backgroundColor = Colors.DARK_GRAY;
        this.borderColor = Colors.WHITE;
        this.scrollBarColor = Colors.GRAY;
        updateMaxScroll();
    }

    /**
     * Berechnet den maximalen vertikalen Scroll-Weg neu basierend auf der Höhe des Inhalts.
     */
    private void updateMaxScroll() {
        maxScrollY = computeMaxScroll();
        scrollY = clampScroll(scrollY, maxScrollY);
    }

    /**
     * Berechnet den maximal zulässigen vertikalen Scrollweg in Pixeln basierend auf der Inhaltshöhe.
     *
     * @return Der maximale Offset-Wert.
     */
    private float computeMaxScroll() {
        return content == null || height <= 0 ? 0f : Math.max(0f, (float) content.getHeight() - height);
    }

    /**
     * Begrenzt den Scroll-Offset auf den gültigen Wertebereich [0, maximum].
     *
     * @param offset  Der zu begrenzende Offset-Wert.
     * @param maximum Der maximale Scrollweg.
     * @return Der begrenzte Scroll-Wert.
     */
    private static float clampScroll(float offset, float maximum) {
        return Math.max(0f, Math.min(maximum, offset));
    }

    /**
     * Berechnet die Höhe des Scrollbar-Anfassers (Thumb) unter Berücksichtigung der Mindesthöhe.
     *
     * @return Die Höhe des Anfassers in Pixeln.
     */
    private int thumbHeight() {
        int trackHeight = height - 2;
        int proportionalHeight = (int) Math.round(trackHeight * (height / (double) content.getHeight()));
        return Math.min(trackHeight - 1, Math.max(MIN_THUMB_HEIGHT, proportionalHeight));
    }

    /**
     * Berechnet die X-Position der Scrollleiste im Fenster.
     *
     * @return Die absolute X-Koordinate der Scrollbar.
     */
    private int thumbX() {
        return (int) x + width - THUMB_WIDTH - 2;
    }

    /**
     * Berechnet die aktuelle Y-Position des Scrollbar-Anfassers basierend auf dem Scroll-Fortschritt.
     *
     * @param offset      Der aktuelle Scroll-Offset.
     * @param maximum     Der maximale Scrollweg.
     * @param thumbHeight Die Höhe des Anfassers.
     * @return Die absolute Y-Koordinate des Anfassers.
     */
    private int thumbY(float offset, float maximum, int thumbHeight) {
        int travel = height - 2 - thumbHeight;
        return (int) y + 1 + Math.round(offset / maximum * travel);
    }

    /**
     * Aktualisiert die Drag-to-Scroll-Eingabelogik und transformiert die Position des Inhalts.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Mausabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible || !enabled || content == null) {
            dragMode = DragMode.NONE;
            return;
        }

        updateMaxScroll();

        boolean thumbGesture = dragMode == DragMode.THUMB;
        boolean hasThumb = maxScrollY > 0f && width >= THUMB_WIDTH + 4 && height > 3;
        int mouseX = input.getMouseX();
        int mouseY = input.getMouseY();

        if (!input.isMouseDown(1) || maxScrollY <= 0f || (thumbGesture && !hasThumb)) {
            dragMode = DragMode.NONE;
        } else if (input.isMousePressed(1) && containsMouse(input)) {
            dragMode = DragMode.CONTENT;
            if (hasThumb) {
                int barHeight = thumbHeight();
                int barX = thumbX();
                int barY = thumbY(scrollY, maxScrollY, barHeight);
                if (mouseX >= barX && mouseX < barX + THUMB_WIDTH && mouseY >= barY && mouseY < barY + barHeight) {
                    dragMode = DragMode.THUMB;
                    thumbGesture = true;
                }
            }
            lastMouseY = mouseY;
        }

        if (dragMode != DragMode.NONE) {
            float deltaY = mouseY - lastMouseY;
            if (dragMode == DragMode.THUMB) {
                int travel = height - 2 - thumbHeight();
                scrollY = clampScroll(scrollY + deltaY / travel * maxScrollY, maxScrollY);
            } else {
                scrollY = clampScroll(scrollY - deltaY, maxScrollY);
            }
            lastMouseY = mouseY;
        }

        if (thumbGesture) {
            return;
        }

        UIElement child = content;
        float previousX = child.getX();
        float previousY = child.getY();
        UIAnchor previousAnchor = child.getAnchor();

        // Content-Position aktualisieren (relativ zum ScrollPane)
        // Anchor sichern, da setPosition() ihn auf ABSOLUTE zurücksetzt
        try {
            child.setPosition(x, y - scrollY);
            child.setAnchor(previousAnchor);
            child.update(deltaTime, input);
        } finally {
            child.setPosition(previousX, previousY);
            child.setAnchor(previousAnchor);
        }
    }

    /**
     * Zeichnet den Hintergrund, den Inhalt mit abgeschnittenen Rändern (Clipping) und die vertikale Scrollbar.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        g.setColor(backgroundColor);
        g.fillRect((int) x, (int) y, width, height);
        g.setColor(borderColor);
        g.drawRect((int) x, (int) y, width, height);

        UIElement child = content;
        if (child == null || width <= 2 || height <= 2) {
            return;
        }

        float maximum = computeMaxScroll();
        float offset = clampScroll(scrollY, maximum);
        boolean hasThumb = maximum > 0f && width >= THUMB_WIDTH + 4 && height > 3;
        int barHeight = hasThumb ? thumbHeight() : 0;
        int barX = hasThumb ? thumbX() : 0;
        int barY = hasThumb ? thumbY(offset, maximum, barHeight) : 0;

        Graphics2D raw = g.raw();
        Shape previousClip = raw.getClip();
        float previousX = child.getX();
        float previousY = child.getY();
        UIAnchor previousAnchor = child.getAnchor();

        try {
            raw.clipRect((int) x + 1, (int) y + 1, width - 2, height - 2);
            child.setPosition(x, y - offset);
            child.setAnchor(previousAnchor);
            child.draw(g);
        } finally {
            raw.setClip(previousClip);
            child.setPosition(previousX, previousY);
            child.setAnchor(previousAnchor);
        }

        if (hasThumb && content == child) {
            g.setColor(scrollBarColor);
            g.fillRect(barX, barY, THUMB_WIDTH, barHeight);
        }
    }

    /**
     * Gibt den aktuellen Inhalt des ScrollPanes zurück.
     *
     * @return Das enthaltene {@link UIElement}.
     */
    public UIElement getContent() {
        return content;
    }

    /**
     * Setzt das Inhalts-Element des ScrollPanes und berechnet die Scroll-Grenzen neu.
     *
     * @param content Das neue {@link UIElement} für den Inhalt.
     */
    public void setContent(UIElement content) {
        this.content = content;
        dragMode = DragMode.NONE;
        updateMaxScroll();
    }

    /**
     * Gibt die aktuelle vertikale Scroll-Position zurück.
     *
     * @return Die Offset-Position in Pixeln.
     */
    public float getScrollY() {
        return clampScroll(scrollY, computeMaxScroll());
    }

    /**
     * Setzt die vertikale Scroll-Position manuell.
     *
     * @param scrollY Die neue Scroll-Position.
     * @throws RuntimeException (Lunar-Error) Wenn {@code scrollY} den Wert {@code Float.NaN} hat.
     */
    public void setScrollY(float scrollY) {
        if (Float.isNaN(scrollY)) {
            throw Errors.argument(ErrorDetails.of("UIScrollPane", "Scroll position must not be NaN")
                    .withActual(scrollY)
                    .withExpected("A numeric position that can be clamped to the scroll range")
                    .withExplanation("NaN cannot represent a vertical content offset"), null);
        }

        this.scrollY = clampScroll(scrollY, computeMaxScroll());
    }

    /**
     * Legt die Sichtbarkeit des ScrollPanes fest.
     * Beim Ausblenden wird ein eventuell aktiver Ziehvorgang (Drag) abgebrochen.
     *
     * @param visible {@code true} für sichtbar, sonst {@code false}.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            dragMode = DragMode.NONE;
        }
    }

    /**
     * Legt den Aktivierungszustand des ScrollPanes fest.
     * Beim Deaktivieren wird ein eventuell aktiver Ziehvorgang (Drag) abgebrochen.
     *
     * @param enabled {@code true} zum Aktivieren, sonst {@code false}.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            dragMode = DragMode.NONE;
        }
    }

    /**
     * Gibt die Hintergrundfarbe des Containers zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Setzt die Hintergrundfarbe des Containers.
     *
     * @param backgroundColor Die neue {@link Colors}-Farbe.
     */
    public void setBackgroundColor(Colors backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Gibt die Rahmenfarbe des Containers zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBorderColor() {
        return borderColor;
    }

    /**
     * Setzt die Rahmenfarbe des Containers.
     *
     * @param borderColor Die neue {@link Colors}-Farbe.
     */
    public void setBorderColor(Colors borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Gibt die Farbe der Scrollbar zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getScrollBarColor() {
        return scrollBarColor;
    }

    /**
     * Setzt die Farbe der Scrollbar.
     *
     * @param scrollBarColor Die neue {@link Colors}-Farbe.
     */
    public void setScrollBarColor(Colors scrollBarColor) {
        this.scrollBarColor = scrollBarColor;
    }
}