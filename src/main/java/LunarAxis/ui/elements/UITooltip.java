package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;

/**
 * Die Klasse {@code UITooltip} repräsentiert ein dynamisches Hinweis-Fenster (Tooltip) auf der Benutzeroberfläche.
 * <p>
 * Der Tooltip folgt automatisch der aktuellen Mausposition (unter Berücksichtigung eines Einstellungs-Offsets)
 * und wird in der Regel bei Hover-Ereignissen über anderen UI-Elementen ein- und ausgeblendet.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * tooltip = UITooltip(15, 15)
 * tooltip.show("Speichert den aktuellen Spielstand")
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UITooltip extends UIElement {
    private String text;
    private Colors backgroundColor;
    private Colors textColor;
    private Colors borderColor;

    private String fontName;

    private int padding, fontSize;
    private float offsetX, offsetY;

    /**
     * Erstellt einen neuen Tooltip mit relativen Offsets zur Mausposition.
     * Der Tooltip ist initial unsichtbar.
     *
     * @param offsetX Der horizontale Abstand (Offset) des Tooltips von der Mauszeiger-Position.
     * @param offsetY Der vertikale Abstand (Offset) des Tooltips von der Mauszeiger-Position.
     */
    public UITooltip(float offsetX, float offsetY) {
        super(0, 0, 0, 0);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.backgroundColor = new Colors(20, 20, 30, 240);
        this.textColor = Colors.WHITE;
        this.borderColor = Colors.GRAY;
        this.padding = 8;
        this.fontSize = 14;
        this.fontName = "SansSerif";
        this.visible = false;
    }

    /**
     * Setzt den anzuzeigenden Hinweistext.
     * <p>
     * Passt die Dimensionen des Tooltip-Hintergrunds automatisch an den Text an.
     * Wenn der übergebene Text {@code null} oder leer ist, wird der Tooltip automatisch ausgeblendet.
     * </p>
     *
     * @param text Der Hinweistext.
     */
    public void setText(String text) {
        this.text = text;
        this.visible = (text != null && !text.isEmpty());
        // Grobe Schätzung der Grösse für den Hintergrund
        if (text != null) {
            this.width = text.length() * 8 + (padding * 2);
            this.height = 24 + (padding * 2);
        }
    }

    /**
     * Aktualisiert die Position des Tooltips, sodass er der aktuellen Mausposition zuzüglich Offset folgt.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Mausabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible) {
            return;
        }
        // Tooltip folgt der Maus
        this.x = input.getMouseX() + offsetX;
        this.y = input.getMouseY() + offsetY;
    }

    /**
     * Zeichnet den Hintergrund, den Rahmen und den Hinweistext des Tooltips.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible || text == null) {
            return;
        }

        // Hintergrund
        g.setColor(backgroundColor);
        g.fillRect((int) x, (int) y, width, height);
        g.setColor(borderColor);
        g.drawRect((int) x, (int) y, width, height);

        // Text
        g.setColor(textColor);
        g.drawString(text, (int) (x + padding), (int) (y + height - padding - 4), 14, "SansSerif");
    }

    /**
     * Blendet den Tooltip mit dem angegebenen Text ein.
     *
     * @param text Der anzuzeigende Hinweistext.
     */
    public void show(String text) {
        setText(text);
    }

    /**
     * Verbirgt den Tooltip.
     */
    public void hide() {
        setVisible(false);
    }

    /**
     * Gibt den aktuellen Hinweistext zurück.
     *
     * @return Der Text-String.
     */
    public String getText() {
        return text;
    }

    /**
     * Gibt die Hintergrundfarbe des Tooltips zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Setzt die Hintergrundfarbe des Tooltips.
     *
     * @param bgColor Die neue {@link Colors}-Farbe.
     */
    public void setBackgroundColor(Colors bgColor) {
        this.backgroundColor = bgColor;
    }

    /**
     * Gibt die Textfarbe zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getTextColor() {
        return textColor;
    }

    /**
     * Setzt die Textfarbe.
     *
     * @param textColor Die neue {@link Colors}-Farbe.
     */
    public void setTextColor(Colors textColor) {
        this.textColor = textColor;
    }

    /**
     * Gibt die Rahmenfarbe zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBorderColor() {
        return borderColor;
    }

    /**
     * Setzt die Rahmenfarbe.
     *
     * @param borderColor Die neue {@link Colors}-Farbe.
     */
    public void setBorderColor(Colors borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Gibt den Innenabstand (Padding) in Pixeln zurück.
     *
     * @return Das Padding.
     */
    public int getPadding() {
        return padding;
    }

    /**
     * Setzt den Innenabstand (Padding) in Pixeln.
     *
     * @param padding Das neue Padding.
     */
    public void setPadding(int padding) {
        this.padding = padding;
    }

    /**
     * Gibt die Schriftgrösse zurück.
     *
     * @return Die Schriftgrösse in Punkten.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Setzt die Schriftgrösse.
     *
     * @param fontSize Die neue Schriftgrösse in Punkten.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Gibt den Namen der verwendeten Schriftart zurück.
     *
     * @return Der Schriftartname.
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Setzt den Namen der verwendeten Schriftart.
     *
     * @param fontName Der neue Schriftartname.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }
}