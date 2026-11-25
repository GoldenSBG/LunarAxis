package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;

import java.awt.*;

/**
 * Die Klasse {@code UILabel} repräsentiert ein UI-Element zur Anzeige von einfachem Text.
 * <p>
 * Sie eignet sich ideal für Score-Anzeigen, Lebenspunkte, Menü-Titel oder einfache Beschriftungen.
 * Die Darstellung erfolgt direkt auf Screen-Koordinaten und nutzt das {@link Graphics}-System der Engine.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * label = UILabel("Score: 100", 20, 30, 20, Colors.WHITE)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UILabel extends UIElement {
    private String text;
    private Colors color;
    private int fontSize;
    private String fontName;
    private boolean explicitFontName;

    /**
     * Erstellt ein neues Text-Label mit Standard-Schriftart ("SansSerif").
     *
     * @param text     Der anzuzeigende Text.
     * @param x        Die X-Koordinate in Screen-Koordinaten.
     * @param y        Die Y-Koordinate in Screen-Koordinaten.
     * @param fontSize Die Schriftgrösse in Punkten/Pixeln.
     * @param color    Die Farbe des Textes als {@link Colors}.
     */
    public UILabel(String text, float x, float y, int fontSize, Colors color) {
        super(x, y, 0, 0);
        this.text = text;
        this.fontSize = fontSize;
        this.color = color;
        this.fontName = "SansSerif";
    }

    /**
     * Erstellt ein neues Text-Label mit einem spezifischen Schriftart-Namen.
     *
     * @param text     Der anzuzeigende Text.
     * @param x        Die X-Koordinate in Screen-Koordinaten.
     * @param y        Die Y-Koordinate in Screen-Koordinaten.
     * @param fontSize Die Schriftgrösse in Punkten/Pixeln.
     * @param color    Die Farbe des Textes als {@link Colors}.
     * @param fontName Der Name der zu verwendenden Schriftart.
     */
    public UILabel(String text, float x, float y, int fontSize, Colors color, String fontName) {
        this(text, x, y, fontSize, color);
        this.fontName = fontName;
        explicitFontName = true;
    }

    /**
     * Aktualisiert die Logik des Text-Labels.
     * <p>
     * Ein statisches Text-Label benötigt keine laufenden Frame-Updates.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Eingabeabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
    }

    /**
     * Zeichnet den Button (Hintergrund je nach Zustand, Rahmen und zentrierten Text).
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (visible && text != null && !text.isEmpty()) {
            Font font = resolveThemeFont(theme.defaultFont, fontName, fontSize, explicitFontName, true);
            g.setColor(color);
            drawStyledText(g, text, (int) x, (int) y, font);
        }
    }

    /**
     * Gibt den aktuellen Text des Labels zurück.
     *
     * @return Der Text-String.
     */
    public String getText() {
        return text;
    }

    /**
     * Setzt den anzuzeigenden Text.
     *
     * @param text Der neue Text-String.
     */
    public void setText(String text) { 
        this.text = text; 
    }

    /**
     * Gibt die Textfarbe zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getColor() {
        return color;
    }

    /**
     * Setzt die Textfarbe.
     *
     * @param color Die neue {@link Colors}-Farbe.
     */
    public void setColor(Colors color) { 
        this.color = color; 
    }

    /**
     * Gibt die Textfarbe zurück (Alias für {@link #getColor()}).
     *
     * @return Die {@link Colors}-Farbe des Textes.
     */
    public Colors getTextColor() {
        return getColor();
    }

    /**
     * Setzt die Textfarbe (Alias für {@link #setColor(Colors)}).
     *
     * @param textColor Die neue {@link Colors}-Farbe des Textes.
     */
    public void setTextColor(Colors textColor) {
        setColor(textColor);
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
     * Setzt den Namen der zu verwendenden Schriftart.
     *
     * @param fontName Der neue Schriftartname.
     */
    public void setFontName(String fontName) { 
        this.fontName = fontName;
        explicitFontName = true;
    }
}