package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.math.Maths;
import LunarAxis.ui.core.UIElement;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code UIProgressBar} repräsentiert einen visuellen Fortschrittsbalken auf der Benutzeroberfläche.
 * <p>
 * Sie eignet sich ideal für Lebensbalken (HP), Manaanzeigen, Ausdauer, Erfahrungspunkte oder Ladezeiten.
 * Die Füllung wird automatisch basierend auf dem aktuellen Wert und dem Maximalwert prozentual berechnet und gezeichnet.
 * </p>

 * <pre>
 * # Beispiel TigerJython:
 * healthBar = UIProgressBar(20, 20, 200, 20, 100, Colors.RED)
 * healthBar.setValue(75) # Setzt den aktuellen Wert auf 75%
 * </pre>
 *
 * @version 1.2.6
 */
public class UIProgressBar extends UIElement {
    private float currentValue;
    private float maxValue;
    private Colors bgColor;
    private Colors fillColor;
    private Colors borderColor;

    /**
     * Erstellt einen neuen Fortschrittsbalken mit Position, Dimensionen, Maximalwert und Füllfarbe.
     * Der initiale Wert entspricht standardmässig dem Maximalwert.
     *
     * @param x         Die X-Koordinate in Screen-Koordinaten.
     * @param y         Die Y-Koordinate in Screen-Koordinaten.
     * @param width     Die Gesamtbreite des Balkens in Pixeln.
     * @param height    Die Gesamthöhe des Balkens in Pixeln.
     * @param maxValue  Der Maximalwert des Fortschritts (muss endlich und &gt; 0 sein).
     * @param fillColor Die {@link Colors}-Farbe für den gefüllten Fortschrittsbereich.
     * @throws RuntimeException (Lunar-Error) Wenn {@code maxValue} ungültig (NaN, negativ oder 0) ist.
     */
    public UIProgressBar(float x, float y, int width, int height, float maxValue, Colors fillColor) {
        super(x, y, width, height);

        if (!Float.isFinite(maxValue) || maxValue <= 0.0f) {
            throw Errors.argument(ErrorDetails.of("UIProgressBar", "Progress maximum must be finite and positive")
                    .withActual(maxValue)
                    .withExpected("A finite maximum greater than 0")
                    .withExplanation("The progress fraction is calculated by dividing the current value by the maximum"), null);
        }

        this.currentValue = maxValue;
        this.maxValue = maxValue;
        this.bgColor = Colors.DARK_GRAY;
        this.fillColor = fillColor;
        this.borderColor = Colors.WHITE;
    }

    /**
     * Aktualisiert die Logik des Fortschrittsbalkens.
     * <p>
     * Ein Fortschrittsbalken benötigt keine laufenden Frame-Updates.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Eingabeabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
    }

    /**
     * Zeichnet den Hintergrund, die prozentuale Füllung und den Rahmen des Fortschrittsbalkens.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        // 1. Hintergrund
        g.setColor(bgColor);
        g.fillRect((int) x, (int) y, width, height);

        // 2. Füllung (prozentual)
        float percentage = Maths.max(0f, Maths.min(1f, currentValue / maxValue));
        int fillWidth = (int) (width * percentage);
        
        g.setColor(fillColor);
        g.fillRect((int) x, (int) y, fillWidth, height);

        // 3. Rahmen
        g.setColor(borderColor);
        g.drawRect((int) x, (int) y, width, height);
    }

    /**
     * Setzt den aktuellen Fortschrittswert (wird automatisch auf den Bereich [0, maxValue] begrenzt).
     *
     * @param value Der neue Fortschrittswert.
     * @throws RuntimeException (Lunar-Error) Wenn {@code value} den Wert {@code Float.NaN} hat.
     */
    public void setValue(float value) {
        if (Float.isNaN(value)) {
            throw Errors.argument(ErrorDetails.of("UIProgressBar", "Progress value must not be NaN")
                    .withActual(value)
                    .withExpected("A numeric value that can be clamped to the progress range")
                    .withExplanation("NaN cannot represent a progress amount"), null);
        }

        this.currentValue = Maths.max(0f, Maths.min(maxValue, value));
    }

    /**
     * Setzt den Maximalwert des Fortschrittsbalkens.
     *
     * @param maxValue Der neue Maximalwert (wird auf mindestens 1.0f begrenzt).
     * @throws RuntimeException (Lunar-Error) Wenn {@code maxValue} NaN oder unendlich ist.
     */
    public void setMaxValue(float maxValue) {
        if (Float.isNaN(maxValue) || maxValue == Float.POSITIVE_INFINITY) {
            throw Errors.argument(ErrorDetails.of("UIProgressBar", "Progress maximum cannot be NaN or positive Infinity")
                    .withActual(maxValue)
                    .withExpected("A maximum that becomes finite after applying the minimum value of 1")
                    .withExplanation("The stored maximum must remain finite so the progress fraction can be calculated"), null);
        }

        this.maxValue = Maths.max(1f, maxValue);
    }

    /**
     * Gibt den aktuellen Fortschrittswert zurück.
     *
     * @return Der aktuelle Wert.
     */
    public float getValue() {
        return currentValue;
    }

    /**
     * Gibt den Maximalwert zurück.
     *
     * @return Der Maximalwert.
     */
    public float getMaxValue() {
        return maxValue;
    }

    /**
     * Setzt die Hintergrundfarbe des ungefüllten Bereichs.
     *
     * @param bgColor Die neue {@link Colors}-Farbe.
     */
    public void setBgColor(Colors bgColor) {
        this.bgColor = bgColor;
    }

    /**
     * Setzt die Füllfarbe des Fortschrittsbalkens.
     *
     * @param fillColor Die neue {@link Colors}-Farbe.
     */
    public void setFillColor(Colors fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Setzt die Rahmenfarbe des Fortschrittsbalkens.
     *
     * @param borderColor Die neue {@link Colors}-Farbe.
     */
    public void setBorderColor(Colors borderColor) {
        this.borderColor = borderColor;
    }
}