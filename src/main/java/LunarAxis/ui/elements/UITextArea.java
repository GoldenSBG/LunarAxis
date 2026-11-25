package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;

/**
 * Die Klasse {@code UITextArea} repräsentiert ein UI-Element zur Anzeige von mehrzeiligem Text.
 * <p>
 * Sie eignet sich besonders für Logbücher, Aufgabenbeschreibungen, lange Dialogfenster
 * oder Konsolenausgaben. Der Text wird anhand von Zeilenumbrüchen ({@code \n}) aufgeteilt
 * und mit einem automatischen Vertikal-Clipping innerhalb der zugewiesenen Höhe gerendert.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * textArea = UITextArea("Zeile 1\nZeile 2", 50, 50, 200, 100, 14, Colors.WHITE)
 * textArea.appendText("Zeile 3 hinzugefügt")
 * </pre>
 *
 * @version 1.2.6
 */
public class UITextArea extends UIElement {
    private String text;
    private Colors textColor;
    private int fontSize;
    private String fontName;
    private int lineHeight;

    /**
     * Erstellt eine neue mehrzeilige Textfläche mit Position, Abmessungen, Schriftgrösse und Textfarbe.
     *
     * @param text     Der anfängliche Text-Inhalt (kann {@code \n} für Zeilenumbrüche enthalten).
     * @param x        Die X-Koordinate in Screen-Koordinaten.
     * @param y        Die Y-Koordinate in Screen-Koordinaten.
     * @param width    Die Breite der Textfläche in Pixeln.
     * @param height   Die Höhe der Textfläche in Pixeln.
     * @param fontSize Die Schriftgrösse in Punkten/Pixeln.
     * @param color    Die {@link Colors}-Farbe des Textes.
     */
    public UITextArea(String text, float x, float y, int width, int height, int fontSize, Colors color) {
        super(x, y, width, height);
        this.text = text;
        this.textColor = color;
        this.fontSize = fontSize;
        this.fontName = "SansSerif";
        this.lineHeight = fontSize + 6; // Etwas Abstand zwischen Zeilen
    }

    /**
     * Aktualisiert die Logik der Textfläche.
     * <p>
     * Eine statische Textfläche benötigt keine laufenden Frame-Updates.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Eingabeabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
    }

    /**
     * Zeichnet die einzelnen Zeilen des Textes unter Berücksichtigung der vertikalen Höhe.
     * Zeilen ausserhalb des Höhenbereichs werden nicht gezeichnet (Clipping).
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible || text == null) {
            return;
        }

        g.setColor(textColor);
        String[] lines = text.split("\n");
        int currentY = (int) y + fontSize;

        for (String line : lines) {
            // Einfaches Clipping: Nicht zeichnen wenn ausserhalb der Höhe
            if (currentY >= y && currentY <= y + height) {
                g.drawString(line, (int) x, currentY, fontSize, fontName);
            }
            currentY += lineHeight;
        }
    }

    /**
     * Gibt den aktuellen Text der Textfläche zurück.
     *
     * @return Der gesamte Text-String.
     */
    public String getText() {
        return text;
    }

    /**
     * Setzt den Text der Textfläche zurück und überschreibt den bisherigen Inhalt.
     *
     * @param text Der neue Text-String.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Hängt eine neue Zeile an den bestehenden Text an.
     *
     * @param text Der anzuhängende Text-String.
     */
    public void appendText(String text) {
        String previousText = this.text == null ? "" : this.text;
        this.text = previousText + (previousText.isEmpty() ? "" : "\n") + text;
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
}