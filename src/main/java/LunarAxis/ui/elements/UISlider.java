package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.math.Maths;
import LunarAxis.ui.core.UIElement;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code UISlider} repräsentiert einen Schieberegler (Slider) auf der Benutzeroberfläche.
 * <p>
 * Sie erlaubt die stufenlose Auswahl von kontinuierlichen Zahlenwerten zwischen einem festgelegten Minimum
 * und Maximum (z. B. für Lautstärkeregelung, Helligkeit oder Sensitivität).
 * Änderungen des Werts können über ein {@link Runnable}-Callback in Echtzeit verarbeitet werden.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * volumeSlider = UISlider(100, 200, 200, 0.0, 1.0, 0.8)
 * volumeSlider.setOnChange(lambda: sound.setVolume(volumeSlider.getValue()))
 * </pre>
 *
 * @version 1.2.6
 */
public class UISlider extends UIElement {
    private float value;
    private float min, max;

    private int trackHeight, thumbSize;

    private Colors trackColor;
    private Colors fillColor;
    private Colors thumbColor;

    private boolean dragging;
    
    private Runnable onChange;

    /**
     * Erstellt einen neuen Schieberegler mit Position, Breite, Wertebereich und Initialwert.
     *
     * @param x            Die X-Koordinate in Screen-Koordinaten.
     * @param y            Die Y-Koordinate in Screen-Koordinaten.
     * @param width        Die Gesamtbreite der Reglerleiste in Pixeln.
     * @param min          Der minimale auswählbare Wert.
     * @param max          Der maximale auswählbare Wert (muss grösser als {@code min} sein).
     * @param initialValue Der anfänglich eingestellte Wert.
     * @throws RuntimeException (Lunar-Error) Wenn der Wertebereich ungültig oder der Initialwert NaN ist.
     */
    public UISlider(float x, float y, int width, float min, float max, float initialValue) {
        super(x, y, width, 30);

        if (!Float.isFinite(min) || !Float.isFinite(max) || min >= max || !Float.isFinite(max - min)) {
            throw Errors.argument(ErrorDetails.of("UISlider", "Slider range is invalid")
                    .withActual("min=" + min + ", max=" + max)
                    .withExpected("Finite bounds with min < max and a finite float difference")
                    .withExplanation("Slider positioning divides by the range and requires a positive finite range width"), null);
        }
        if (Float.isNaN(initialValue)) {
            throw Errors.argument(ErrorDetails.of("UISlider", "Initial slider value must not be NaN")
                    .withActual(initialValue)
                    .withExpected("A numeric value that can be clamped to the slider range")
                    .withExplanation("NaN cannot represent a slider position"), null);
        }

        this.min = min;
        this.max = max;
        this.value = Maths.max(min, Maths.min(max, initialValue));
        this.trackColor = Colors.DARK_GRAY;
        this.fillColor = Colors.BLUE;
        this.thumbColor = Colors.WHITE;
        this.trackHeight = 8;
        this.thumbSize = 20;
    }

    /**
     * Setzt die Aktion (Callback), die bei einer Änderung des Slider-Wertes ausgeführt werden soll.
     *
     * @param onChange Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /**
     * Aktualisiert den Zustand des Schiebereglers und verarbeitet Drag-Eingaben mit der Maus.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Mausabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible || !enabled) {
            return;
        }

        int mouseX = input.getMouseX();
        int mouseY = input.getMouseY();

        // Drag starten
        if (containsMouse(input) && input.isMouseDown(1)) {
            dragging = true;
        }

        // Drag beenden
        if (!input.isMouseDown(1)) {
            dragging = false;
        }

        // Wert aktualisieren während Drag
        if (dragging) {
            float relativeX = mouseX - x;
            float percentage = Maths.max(0f, Maths.min(1f, relativeX / width));
            float newValue = min + (max - min) * percentage;
            
            if (Maths.abs(newValue - value) > 0.01f) {
                value = newValue;
                if (onChange != null) {
                    onChange.run();
                }
            }
        }
    }

    /**
     * Zeichnet die Reglerleiste (Track), den gefüllten Fortschrittsbereich (Fill) und den Reglerknopf (Thumb).
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        int centerY = (int) y + height / 2;

        // Track (Hintergrund)
        g.setColor(trackColor);
        g.fillRect((int) x, centerY - trackHeight / 2, width, trackHeight);

        // Fill (gefüllter Teil)
        float percentage = (value - min) / (max - min);
        int fillWidth = (int) (width * percentage);
        g.setColor(fillColor);
        g.fillRect((int) x, centerY - trackHeight / 2, fillWidth, trackHeight);

        // Thumb (Knopf)
        int thumbX = (int) x + fillWidth - thumbSize / 2;
        g.setColor(thumbColor);
        g.fillCircle(thumbX + thumbSize / 2, centerY, thumbSize / 2);
    }

    /**
     * Gibt den aktuell eingestellten Wert des Schiebereglers zurück.
     *
     * @return Der aktuelle Wert.
     */
    public float getValue() {
        return value;
    }

    /**
     * Setzt den Wert des Schiebereglers manuell (wird automatisch auf den Bereich [min, max] begrenzt).
     *
     * @param value Der neue einzustellende Wert.
     * @throws RuntimeException (Lunar-Error) Wenn {@code value} den Wert {@code Float.NaN} hat.
     */
    public void setValue(float value) {
        if (Float.isNaN(value)) {
            throw Errors.argument(ErrorDetails.of("UISlider", "Slider value must not be NaN")
                    .withActual(value)
                    .withExpected("A numeric value that can be clamped to the slider range")
                    .withExplanation("NaN cannot represent a slider position"), null);
        }

        this.value = Maths.max(min, Maths.min(max, value));
    }

    /**
     * Gibt den Minimalwert des Reglers zurück.
     *
     * @return Der Minimalwert.
     */
    public float getMin() {
        return min;
    }

    /**
     * Gibt den Maximalwert des Reglers zurück.
     *
     * @return Der Maximalwert.
     */
    public float getMax() {
        return max;
    }

    /**
     * Gibt die Hintergrundfarbe der Leiste zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getTrackColor() {
        return trackColor;
    }

    /**
     * Setzt die Hintergrundfarbe der Leiste.
     *
     * @param trackColor Die neue {@link Colors}-Farbe.
     */
    public void setTrackColor(Colors trackColor) {
        this.trackColor = trackColor;
    }

    /**
     * Gibt die Farbe des gefüllten Bereichs zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getFillColor() {
        return fillColor;
    }

    /**
     * Setzt die Farbe des gefüllten Bereichs.
     *
     * @param fillColor Die neue {@link Colors}-Farbe.
     */
    public void setFillColor(Colors fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * Gibt die Farbe des Reglerknopfs zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getThumbColor() {
        return thumbColor;
    }

    /**
     * Setzt die Farbe des Reglerknopfs.
     *
     * @param thumbColor Die neue {@link Colors}-Farbe.
     */
    public void setThumbColor(Colors thumbColor) {
        this.thumbColor = thumbColor;
    }

    /**
     * Gibt die Höhe der Reglerleiste in Pixeln zurück.
     *
     * @return Die Leistenhöhe.
     */
    public int getTrackHeight() {
        return trackHeight;
    }

    /**
     * Setzt die Höhe der Reglerleiste in Pixeln.
     *
     * @param trackHeight Die neue Leistenhöhe.
     */
    public void setTrackHeight(int trackHeight) {
        this.trackHeight = trackHeight;
    }

    /**
     * Gibt den Durchmesser des Reglerknopfs in Pixeln zurück.
     *
     * @return Der Durchmesser.
     */
    public int getThumbSize() {
        return thumbSize;
    }

    /**
     * Setzt den Durchmesser des Reglerknopfs in Pixeln.
     *
     * @param thumbSize Der neue Durchmesser.
     */
    public void setThumbSize(int thumbSize) {
        this.thumbSize = thumbSize;
    }
}