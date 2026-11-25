package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Die Klasse {@code UIRadioButton} repräsentiert einen Radio-Button für Optionen-Gruppen auf der Benutzeroberfläche.
 * <p>
 * Innerhalb einer zugewiesenen {@link UIRadioGroup} kann immer nur genau ein Radio-Button gleichzeitig
 * selektiert sein. Bei Auswahl eines Buttons werden alle übrigen Buttons der Gruppe automatisch abgewählt.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * radioGroup = UIRadioGroup("DifficultyGroup")
 * btnEasy = UIRadioButton("Leicht", "DifficultyGroup", 50, 100, True)
 * btnHard = UIRadioButton("Schwer", "DifficultyGroup", 50, 130, False)
 * radioGroup.add(btnEasy)
 * radioGroup.add(btnHard)
 * </pre>
 *
 * @version 1.2.6
 */
public class UIRadioButton extends UIElement {
    private String label;
    private boolean selected;

    private Colors circleColor;
    private Colors selectedColor;
    private Colors labelColor;
    private Colors borderColor;

    private int circleSize;

    private int fontSize;
    private String fontName;

    private boolean explicitFontName;
    private boolean explicitFontSize;
    private boolean explicitLabelColor;
    
    private Runnable onSelect;
    private UIRadioGroup group;

    /**
     * Erstellt einen neuen Radio-Button mit Label, Gruppenzuweisung, Position und initialem Auswahlzustand.
     *
     * @param label     Der Beschriftungstext neben dem Radio-Button.
     * @param groupName Der Name der zugehörigen Optionen-Gruppe.
     * @param x         Die X-Koordinate in Screen-Koordinaten.
     * @param y         Die Y-Koordinate in Screen-Koordinaten.
     * @param selected  {@code true}, wenn dieser Button initial ausgewählt sein soll.
     */
    public UIRadioButton(String label, String groupName, float x, float y, boolean selected) {
        super(x, y, 150, 24);
        this.label = label;
        this.selected = selected;
        this.circleColor = Colors.WHITE;
        this.selectedColor = Colors.BLUE;
        this.labelColor = Colors.WHITE;
        this.borderColor = Colors.BLACK;
        this.circleSize = 20;
        this.fontSize = 16;
        this.fontName = "SansSerif";
    }

    /**
     * Weist dem Radio-Button die verwalterische {@link UIRadioGroup} zu.
     *
     * @param group Die umschliessende {@link UIRadioGroup}.
     */
    void setGroup(UIRadioGroup group) {
        this.group = group;
    }

    /**
     * Setzt die Aktion (Callback), die bei Auswahl dieses Radio-Buttons ausgeführt werden soll.
     *
     * @param onSelect Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnSelect(Runnable onSelect) {
        this.onSelect = onSelect;
    }

    /**
     * Aktualisiert den Interaktionszustand des Radio-Buttons und leitet Klick-Events an die Gruppe weiter.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Mausabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible || !enabled) {
            return;
        }

        if (containsMouse(input) && input.isMouseReleased(1)) {
            if (!selected && group != null) {
                // Gruppe soll dieses Button auswählen
                group.select(this);

                if (onSelect != null) {
                    onSelect.run();
                }
            }
        }
    }

    /**
     * Zeichnet den kreisförmigen Radio-Button, den inneren Auswahlpunkt (falls selektiert) und das Label.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        // Kreis zeichnen
        g.setColor(circleColor);
        g.fillCircle((int) x + circleSize / 2, (int) y + circleSize / 2, circleSize / 2);
        g.setColor(borderColor);
        g.drawCircle((int) x + circleSize / 2, (int) y + circleSize / 2, circleSize / 2);

        // Punkt in der Mitte wenn selektiert
        if (selected) {
            g.setColor(selectedColor);
            g.fillCircle((int) x + circleSize / 2, (int) y + circleSize / 2, circleSize / 4);
        }

        Font font = resolveThemeFont(theme.defaultFont, fontName, fontSize, explicitFontName, explicitFontSize);
        g.setColor(resolveThemeColor(labelColor, theme.textColor, explicitLabelColor));
        drawStyledText(g, label, (int) (x + circleSize + 8), (int) (y + circleSize - 4), font);
    }

    /**
     * Prüft, ob der Radio-Button aktuell ausgewählt ist.
     *
     * @return {@code true}, wenn ausgewählt, sonst {@code false}.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Legt den Auswahlzustand fest.
     *
     * @param selected {@code true} zum Auswählen, sonst {@code false}.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Gibt den Beschriftungstext des Radio-Buttons zurück.
     *
     * @return Das Label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gibt die zugewiesene Radio-Gruppe zurück.
     *
     * @return Die {@link UIRadioGroup} oder {@code null}.
     */
    public UIRadioGroup getGroup() {
        return group;
    }

    /**
     * Gibt die Hintergrundfarbe des Kreises zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getCircleColor() {
        return circleColor;
    }

    /**
     * Setzt die Hintergrundfarbe des Kreises.
     *
     * @param circleColor Die neue {@link Colors}-Farbe.
     */
    public void setCircleColor(Colors circleColor) {
        this.circleColor = circleColor;
    }

    /**
     * Gibt die Farbe des Auswahldot-Punktes zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getSelectedColor() {
        return selectedColor;
    }

    /**
     * Setzt die Farbe des Auswahldot-Punktes im selektierten Zustand.
     *
     * @param selectedColor Die neue {@link Colors}-Farbe.
     */
    public void setSelectedColor(Colors selectedColor) {
        this.selectedColor = selectedColor;
    }

    /**
     * Gibt die Farbe des Beschriftungstextes zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getLabelColor() {
        return labelColor;
    }

    /**
     * Setzt die Farbe des Beschriftungstextes.
     *
     * @param labelColor Die neue {@link Colors}-Farbe.
     */
    public void setLabelColor(Colors labelColor) {
        this.labelColor = labelColor;
        explicitLabelColor = true;
    }

    /**
     * Gibt die Rahmenfarbe des Kreises zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBorderColor() {
        return borderColor;
    }

    /**
     * Setzt die Rahmenfarbe des Kreises.
     *
     * @param borderColor Die neue {@link Colors}-Farbe.
     */
    public void setBorderColor(Colors borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Gibt den Durchmesser des Kreises in Pixeln zurück.
     *
     * @return Die Kreisgrösse.
     */
    public int getCircleSize() {
        return circleSize;
    }

    /**
     * Setzt den Durchmesser des Kreises in Pixeln.
     *
     * @param circleSize Die neue Kreisgrösse.
     */
    public void setCircleSize(int circleSize) {
        this.circleSize = circleSize;
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
     * @param fontSize Die neue Schriftgrösse in Punkten.
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        explicitFontSize = true;
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
     * Setzt den Namen der verwendeten Schriftart für das Label.
     *
     * @param fontName Der neue Schriftartname.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
        explicitFontName = true;
    }
}