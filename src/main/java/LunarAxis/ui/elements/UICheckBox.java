package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;

import java.awt.*;

/**
 * Die Klasse {@code UICheckBox} repräsentiert ein Umschaltfeld (Checkbox) mit integriertem Label.
 * <p>
 * Sie erlaubt das Aktivieren und Deaktivieren von Einstellungen im Spiel oder in Menüs
 * und unterstützt Callback-Benachrichtigungen über {@link Runnable} bei Zustandsänderungen.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * checkbox = UICheckBox("Vollbild", 50, 100, True)
 * checkbox.setOnChange(lambda: print("Zustand geändert!"))
 * </pre>
 *
 * @version 1.2.6
 */
public class UICheckBox extends UIElement {
    private String label;
    private boolean checked;
    private Colors boxColor;
    private Colors checkColor;
    private Colors labelColor;
    private int boxSize;
    private String fontName = "SansSerif";
    private int fontSize = 16;

    private boolean explicitFontName;
    private boolean explicitFontSize;
    private boolean explicitLabelColor;

    private Runnable onChange;

    /**
     * Erstellt eine neue Checkbox mit Label, Position und initialem Auswahlzustand.
     *
     * @param label   Der Text der Beschriftung neben dem Kästchen.
     * @param x       Die X-Koordinate auf dem Bildschirm.
     * @param y       Die Y-Koordinate auf dem Bildschirm.
     * @param checked {@code true}, wenn die Checkbox initial ausgewählt sein soll, sonst {@code false}.
     */
    public UICheckBox(String label, float x, float y, boolean checked) {
        super(x, y, 150, 24);
        this.label = label;
        this.checked = checked;
        this.boxColor = Colors.WHITE;
        this.checkColor = Colors.GREEN;
        this.labelColor = Colors.WHITE;
        this.boxSize = 20;
    }

    /**
     * Setzt die Aktion (Callback), die bei jeder Zustandsänderung (Umschalten) ausgeführt werden soll.
     *
     * @param onChange Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Aktualisiert den Zustand der Checkbox und registriert Mausklicks zum Umschalten.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Mausabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible || !enabled) {
            return;
        }

        // Klick erkennen
        if (containsMouse(input) && input.isMouseReleased(1)) {
            checked = !checked;
            if (onChange != null) {
                onChange.run();
            }
        }
    }

    /**
     * Zeichnet das Checkbox-Kästchen, den Haken (falls aktiv) und das zugehörige Label.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        // Checkbox-Kästchen zeichnen
        g.setColor(boxColor);
        g.drawRect((int) x, (int) y, boxSize, boxSize);

        // Häkchen zeichnen wenn aktiv
        if (checked) {
            g.setColor(checkColor);
            g.fillRect((int) x + 4, (int) y + 4, boxSize - 8, boxSize - 8);
        }

        Font font = resolveThemeFont(theme.defaultFont, fontName, fontSize, explicitFontName, explicitFontSize);
        g.setColor(resolveThemeColor(labelColor, theme.textColor, explicitLabelColor));
        drawStyledText(g, label, (int) (x + boxSize + 8), (int) (y + boxSize - 4), font);
    }

    /**
     * Prüft, ob die Checkbox aktuell angewählt ist.
     *
     * @return {@code true}, wenn ausgewählt, sonst {@code false}.
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Legt fest, ob die Checkbox ausgewählt sein soll.
     *
     * @param checked {@code true} zum Auswählen, {@code false} zum Abwählen.
     */
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    /**
     * Gibt den Text des Labels zurück.
     *
     * @return Der Beschriftungstext.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setzt den Text des Labels.
     *
     * @param label Der neue Beschriftungstext.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gibt die Rahmenfarbe des Kästchens zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBoxColor() {
        return boxColor;
    }

    /**
     * Setzt die Rahmenfarbe des Kästchens.
     *
     * @param boxColor Die neue {@link Colors}-Farbe.
     */
    public void setBoxColor(Colors boxColor) {
        this.boxColor = boxColor;
    }

    /**
     * Gibt die Farbe des Häkchens/Füllblocks zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getCheckColor() {
        return checkColor;
    }

    /**
     * Setzt die Farbe des Häkchens/Füllblocks.
     *
     * @param checkColor Die neue {@link Colors}-Farbe.
     */
    public void setCheckColor(Colors checkColor) {
        this.checkColor = checkColor;
    }

    /**
     * Gibt die Farbe der Textbeschriftung zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getLabelColor() {
        return labelColor;
    }

    /**
     * Setzt die Farbe der Textbeschriftung.
     *
     * @param labelColor Die neue {@link Colors}-Farbe.
     */
    public void setLabelColor(Colors labelColor) {
        this.labelColor = labelColor;
        explicitLabelColor = true;
    }

    /**
     * Gibt die Farbe der Textbeschriftung zurück (Alias für {@link #getLabelColor()}).
     *
     * @return Die {@link Colors}-Farbe des Labels.
     */
    public Colors getTextColor() {
        return getLabelColor();
    }

    /**
     * Setzt die Farbe der Textbeschriftung (Alias für {@link #setLabelColor(Colors)}).
     *
     * @param textColor Die neue {@link Colors}-Farbe des Labels.
     */
    public void setTextColor(Colors textColor) {
        setLabelColor(textColor);
    }

    /**
     * Gibt die Kantenlänge des Checkbox-Kästchens in Pixeln zurück.
     *
     * @return Die Grösse des Kästchens.
     */
    public int getBoxSize() {
        return boxSize;
    }

    /**
     * Setzt die Kantenlänge des Checkbox-Kästchens.
     *
     * @param boxSize Die neue Kantenlänge in Pixeln.
     */
    public void setBoxSize(int boxSize) {
        this.boxSize = boxSize;
    }

    /**
     * Gibt den Namen der verwendeten Schriftart zurück.
     *
     * @return Der Name der Schriftart.
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Setzt den Namen der Schriftart für das Label.
     *
     * @param fontName Der Name der Schriftart.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
        explicitFontName = true;
    }

    /**
     * Gibt die Schriftgrösse des Labels zurück.
     *
     * @return Die Schriftgrösse in Punkten.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Setzt die Schriftgrösse des Labels.
     *
     * @param fontSize Die neue Schriftgrösse.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        explicitFontSize = true;
    }
}