package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;

/**
 * Die Klasse {@code UIButton} repräsentiert eine klickbare Schaltfläche (Button) in der Benutzeroberfläche.
 * <p>
 * Sie unterstützt visuelle Rückmeldungen für Hover- und Klick-Zustände sowie Callback-Aktionen
 * über {@link Runnable} beim Auslösen eines Klicks.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * button = UIButton("Start", 100, 100, 150, 40, Colors.BLUE, Colors.WHITE)
 * button.setOnClick(lambda: print("Geklickt!"))
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UIButton extends UIElement {
    private String text;
    private Colors bgColor;
    private Colors hoverColor;
    private Colors clickColor;
    private Colors textColor;
    private int fontSize;
    private String fontName = "SansSerif";

    private boolean explicitFontName;
    private boolean explicitFontSize;
    private boolean explicitHoverColor;
    private boolean explicitClickColor;
    
    private Runnable onClickAction;
    private boolean isHovered = false;
    private boolean isPressed = false;

    /**
     * Erstellt einen neuen Button mit Text, Position, Dimensionen und Farbwerten.
     *
     * @param text      Der auf dem Button angezeigte Text.
     * @param x         Die X-Koordinate auf dem Bildschirm.
     * @param y         Die Y-Koordinate auf dem Bildschirm.
     * @param width     Die Breite des Buttons in Pixeln.
     * @param height    Die Höhe des Buttons in Pixeln.
     * @param bgColor   Die Standard-Hintergrundfarbe als {@link Colors}.
     * @param textColor Die Textfarbe als {@link Colors}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code bgColor} {@code null} ist.
     */
    public UIButton(String text, float x, float y, int width, int height, Colors bgColor, Colors textColor) {
        super(x, y, width, height);

        if (bgColor == null) {
            throw Errors.nullValue(ErrorDetails.of("UIButton", "Button background color must not be null")
                    .withActual(null)
                    .withExpected("A background color")
                    .withExplanation("Hover and pressed colors are derived from the background color"), null);
        }

        this.text = text;
        this.bgColor = bgColor;
        this.hoverColor = bgColor.brighter();
        this.clickColor = bgColor.darker();
        this.textColor = textColor;
        this.fontSize = 20;
    }

    /**
     * Setzt die Aktion (Callback), die beim Klicken auf den Button ausgeführt werden soll.
     *
     * @param action Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnClick(Runnable action) {
        this.onClickAction = action;
    }

    /**
     * Aktualisiert den Interaktionszustand des Buttons (Hover, Gedrückt, Klick-Event).
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Mausabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible || !enabled) {
            return;
        }

        isHovered = containsMouse(input);

        // Mausklick starten
        if (isHovered && input.isMousePressed(1)) {
            isPressed = true;
        }

        // Mausklick beenden → Aktion auslösen
        if (isPressed && input.isMouseReleased(1)) {
            try {
                if (isHovered && onClickAction != null) {
                    onClickAction.run();
                }
            } finally {
                isPressed = false;
            }
        }

        // Wenn Maus nicht mehr gedrückt ist (z.B. ausserhalb losgelassen)
        if (!input.isMouseDown(1)) {
            isPressed = false;
        }
    }

    /**
     * Zeichnet den Button (Hintergrund je nach Zustand, Rahmen und zentrierten Text).
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }
        g.setColor(isPressed ? clickColor : isHovered ? hoverColor : bgColor);
        g.fillRect((int) x, (int) y, width, height);

        g.setColor(resolveThemeColor(Colors.WHITE, theme.borderColor, false));
        g.drawRect((int) x, (int) y, width, height);

        if (text == null || text.isEmpty()) {
            return;
        }

        Font font = resolveThemeFont(theme.defaultFont, fontName, fontSize, explicitFontName, explicitFontSize);
        FontMetrics metrics = g.raw().getFontMetrics(font);
        int textX = (int) Math.round(x + (width - metrics.stringWidth(text)) / 2.0);
        int textY = (int) Math.round(y + (height - metrics.getHeight()) / 2.0 + metrics.getAscent());

        g.setColor(textColor);
        drawStyledText(g, text, textX, textY, font);
    }

    /**
     * Gibt den Text des Buttons zurück.
     * @return Der Button-Text.
     */
    public String getText() { return text; }

    /**
     * Setzt den Text des Buttons.
     * @param text Der neue Text.
     */
    public void setText(String text) { 
        this.text = text; 
    }

    /**
     * Gibt die Standard-Hintergrundfarbe zurück.
     * @return {@link Colors} der Hintergrundfarbe.
     */
    public Colors getBgColor() { return bgColor; }

    /**
     * Setzt die Hintergrundfarbe des Buttons und berechnet automatisch neue Hover- und Klick-Farben.
     *
     * @param bgColor Die neue {@link Colors}-Hintergrundfarbe.
     * @throws RuntimeException (Lunar-Error) Wenn {@code bgColor} {@code null} ist.
     */
    public void setBgColor(Colors bgColor) {
        if (bgColor == null) {
            throw Errors.nullValue(ErrorDetails.of("UIButton", "Button background color must not be null")
                    .withActual(null)
                    .withExpected("A background color")
                    .withExplanation("Hover and pressed colors are derived from the background color"), null);
        }

        this.bgColor = bgColor;
        if (!explicitHoverColor) {
            hoverColor = bgColor.brighter();
        }
        if (!explicitClickColor) {
            clickColor = bgColor.darker();
        }
    }

    /**
     * Gibt die Textfarbe zurück.
     * @return {@link Colors} des Textes.
     */
    public Colors getTextColor() {
        return textColor;
    }

    /**
     * Setzt die Textfarbe des Buttons.
     * @param textColor Die neue {@link Colors}-Textfarbe.
     */
    public void setTextColor(Colors textColor) {
        this.textColor = textColor; 
    }

    /**
     * Gibt die Schriftgrösse zurück.
     * @return Schriftgrösse in Punkten.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Setzt die Schriftgrösse des Button-Textes.
     * @param fontSize Die neue Schriftgrösse.
     */
    public void setFontSize(int fontSize) { 
        this.fontSize = fontSize;
        explicitFontSize = true;
    }

    /**
     * Gibt den Namen der Schriftart zurück.
     * @return Name der Schriftart.
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Setzt den Namen der Schriftart.
     * @param fontName Der Name der Schriftart.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
        explicitFontName = true;
    }

    /**
     * Gibt die Hintergrundfarbe bei Maus-Hover zurück.
     * @return {@link Colors} der Hover-Farbe.
     */
    public Colors getHoverColor() {
        return hoverColor;
    }

    /**
     * Setzt eine spezifische Farbe für den Maus-Hover-Zustand.
     * @param hoverColor Die neue Hover-Farbe.
     */
    public void setHoverColor(Colors hoverColor) {
        this.hoverColor = hoverColor;
        explicitHoverColor = true;
    }

    /**
     * Gibt die Hintergrundfarbe im gedrückten Zustand zurück.
     * @return {@link Colors} der Klick-Farbe.
     */
    public Colors getClickColor() {
        return clickColor;
    }

    /**
     * Setzt eine spezifische Farbe für den gedrückten Zustand.
     * @param clickColor Die neue Klick-Farbe.
     */
    public void setClickColor(Colors clickColor) {
        this.clickColor = clickColor;
        explicitClickColor = true;
    }
}