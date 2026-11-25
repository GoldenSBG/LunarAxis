package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.containers.UIPanel;
import LunarAxis.ui.core.UIAnchor;
import LunarAxis.ui.core.UIElement;

/**
 * Die Klasse {@code UIWindow} repräsentiert ein verschiebbares Fenster mit Titelleiste auf der Benutzeroberfläche.
 * <p>
 * Sie erweitert {@link UIPanel} und dient als Basis für Menüs, Inventar-Fenster, Einstellungen
 * oder Dialogstrukturen. Über die Titelleiste kann das Fenster per Drag-and-Drop interaktiv
 * auf dem Bildschirm verschoben werden.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * window = UIWindow("Einstellungen", 100, 100, 400, 300)
 * window.addChild(UIButton("Speichern", 20, 40, 120, 30, Colors.BLUE, Colors.WHITE))
 * </pre>
 *
 * @version 1.2.6
 */
public class UIWindow extends UIPanel {
    private String title;
    private Colors titleBarColor;
    private String fontName = "SansSerif";

    private float dragOffsetX, dragOffsetY;

    private int titleBarHeight;
    private int fontSize = 16;

    private boolean isDragging;
    private boolean draggable = true;

    /**
     * Erstellt ein neues Fenster mit Titel, Position und Dimensionen.
     *
     * @param title  Der in der Titelleiste angezeigte Text.
     * @param x      Die X-Koordinate auf dem Bildschirm.
     * @param y      Die Y-Koordinate auf dem Bildschirm.
     * @param width  Die Breite des Fensters in Pixeln.
     * @param height Die Gesamthöhe des Fensters (inklusive Titelleiste) in Pixeln.
     */
    public UIWindow(String title, float x, float y, int width, int height) {
        super(x, y, width, height);
        this.title = title;
        this.titleBarHeight = 30;
        this.titleBarColor = new Colors(60, 60, 80);
        this.setDrawBackground(false);
    }

    /**
     * Aktualisiert den Interaktionszustand des Fensters (Drag-Logik über die Titelleiste)
     * und leitet das Update an alle Kind-Elemente weiter.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Mausabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible) {
            return;
        }

        if (draggable) {
            int mouseX = input.getMouseX();
            int mouseY = input.getMouseY();

            // Drag-Logik für die Titelleiste
            boolean isInTitleBar = mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + titleBarHeight;

            if (isInTitleBar && input.isMouseDown(1) && !isDragging) {
                isDragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                // Anchor auf ABSOLUTE setzen, damit applyAnchor() nicht dagegen arbeitet
                setAnchor(UIAnchor.ABSOLUTE);
            }

            // Drag beenden wenn Maus losgelassen wird
            if (!input.isMouseDown(1)) {
                isDragging = false;
            }

            if (isDragging) {
                x = mouseX - dragOffsetX;
                y = mouseY - dragOffsetY;
            }

        }
        // Kinder updaten
        super.update(deltaTime, input);
    }

    /**
     * Zeichnet den Fenster-Hintergrund, den Rahmen, die Titelleiste mit Titeltext
     * sowie alle enthaltenen Kind-Elemente.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        // Haupt-Hintergrund
        g.setColor(getBackgroundColor());
        g.fillRect((int) x, (int) y + titleBarHeight, width, height - titleBarHeight);
        g.setColor(getBorderColor());
        g.drawRect((int) x, (int) y, width, height);

        // Titelleiste
        g.setColor(titleBarColor);
        g.fillRect((int) x, (int) y, width, titleBarHeight);
        
        // Titel-Text
        g.setColor(resolveThemeColor(Colors.WHITE, theme.textColor, false));
        g.drawString(title, (int) (x + 10), (int) (y + titleBarHeight - 8), fontSize, fontName);

        drawChildren(g);
    }

    /**
     * Gibt den Fenster-Titel zurück.
     *
     * @return Der Titel-String.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setzt den Fenster-Titel.
     *
     * @param title Der neue Titel-String.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gibt die Höhe der Titelleiste in Pixeln zurück.
     *
     * @return Die Höhe der Titelleiste.
     */
    public int getTitleBarHeight() {
        return titleBarHeight;
    }

    /**
     * Setzt die Höhe der Titelleiste in Pixeln.
     *
     * @param titleBarHeight Die neue Höhe.
     */
    public void setTitleBarHeight(int titleBarHeight) {
        this.titleBarHeight = titleBarHeight;
    }

    /**
     * Gibt die Farbe der Titelleiste zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getTitleBarColor() {
        return titleBarColor;
    }

    /**
     * Setzt die Farbe der Titelleiste.
     *
     * @param titleBarColor Die neue {@link Colors}-Farbe.
     */
    public void setTitleBarColor(Colors titleBarColor) {
        this.titleBarColor = titleBarColor;
    }

    /**
     * Gibt den Schriftart-Namen des Titels zurück.
     *
     * @return Der Name der Schriftart.
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Setzt den Schriftart-Namen für den Titel.
     *
     * @param fontName Der neue Schriftart-Name.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    /**
     * Gibt die Schriftgrösse des Titeltexts zurück.
     *
     * @return Die Schriftgrösse in Punkten.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Setzt die Schriftgrösse des Titeltexts.
     *
     * @param fontSize Die neue Schriftgrösse in Punkten.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Prüft, ob das Fenster verschiebbar ist.
     *
     * @return {@code true}, wenn verschiebbar, sonst {@code false}.
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * Legt fest, ob das Fenster per Drag-and-Drop verschiebbar ist.
     *
     * @param draggable {@code true} für verschiebbar, sonst {@code false}.
     */
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }
}
