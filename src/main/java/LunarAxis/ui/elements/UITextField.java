package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.inputs.Key;
import LunarAxis.math.Maths;
import LunarAxis.ui.core.UIElement;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;

/**
 * Die Klasse {@code UITextField} repräsentiert ein einzeiliges Texteingabefeld auf der Benutzeroberfläche.
 * <p>
 * Sie verwaltet den Fokuszustand bei Mausklicks, liest getippte Zeichen über den {@link InputManager} ein
 * und zeichnet den Platzhalter sowie einen blinkenden Text-Cursor.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * inputField = UITextField("Spielername eingeben...", 100, 100, 200, 32)
 * inputField.setOnTextChange(lambda: print("Neuer Text: " + inputField.getText()))
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UITextField extends UIElement {
    private String text;
    private String placeholder;
    private String fontName;

    private boolean focused;

    private Colors backgroundColor;
    private Colors textColor;
    private Colors placeholderColor;
    private Colors borderColor;
    private Colors focusColor;

    private int fontSize;
    private float cursorBlinkTimer;
    private int maxChars;

    private boolean explicitFontName;
    private boolean explicitFontSize;
    private boolean explicitTextColor;

    private Runnable onTextChange;

    /**
     * Erstellt ein neues Texteingabefeld mit Platzhalter-Text, Position und Dimensionen.
     *
     * @param placeholder Der ausgegraute Hinweis-Text, wenn das Feld leer ist.
     * @param x           Die X-Koordinate in Screen-Koordinaten.
     * @param y           Die Y-Koordinate in Screen-Koordinaten.
     * @param width       Die Breite des Eingabefelds in Pixeln.
     * @param height      Die Höhe des Eingabefelds in Pixeln.
     */
    public UITextField(String placeholder, float x, float y, int width, int height) {
        super(x, y, width, height);
        this.placeholder = placeholder;
        this.text = "";
        this.focused = false;
        this.backgroundColor = Colors.DARK_GRAY;
        this.textColor = Colors.WHITE;
        this.placeholderColor = Colors.GRAY;
        this.borderColor = Colors.WHITE;
        this.focusColor = Colors.BLUE;
        this.fontSize = 16;
        this.fontName = "SansSerif";
        this.maxChars = 50;
    }

    /**
     * Setzt die Aktion (Callback), die bei jeder Textänderung ausgeführt werden soll.
     *
     * @param onTextChange Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnTextChange(Runnable onTextChange) {
        this.onTextChange = onTextChange;
    }

    /**
     * Aktualisiert den Fokuszustand und verarbeitet Tastatureingaben bei aktivem Fokus.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Maus- und Tastaturabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible || !enabled) {
            return;
        }

        boolean wasFocused = focused;
        if (input.isMouseReleased(1)) {
            focused = containsMouse(input);
        }

        if (!focused || !wasFocused) {
            cursorBlinkTimer = 0f;
        } else if (Float.isFinite(deltaTime) && deltaTime > 0f) {
            cursorBlinkTimer = (cursorBlinkTimer + deltaTime % 1f) % 1f;
        }

        if (!focused) {
            return;
        }

        if (input.isKeyPressed(Key.BACK_SPACE) && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            cursorBlinkTimer = 0f;
            if (onTextChange != null) {
                onTextChange.run();
            }
        }

        // A listener can hide or disable the field.
        if (!focused || !visible || !enabled) {
            return;
        }

        // Getippte Zeichen verarbeiten
        String typed = input.consumeTypedCharacters();
        if (!typed.isEmpty()) {
            int previousLength = text.length();
            for (int i = 0; i < typed.length(); i++) {
                // Nur druckbare ASCII-Zeichen zulassen (verhindert Steuerzeichen wie Enter/Tab im Text)
                char c = typed.charAt(i);
                if (text.length() < maxChars && c >= 32 && c < 127) {
                    text += c;
                }
            }

            if (text.length() != previousLength) {
                cursorBlinkTimer = 0f;
            }
            if (onTextChange != null) {
                onTextChange.run();
            }
        }
    }

    /**
     * Zeichnet den Hintergrund, den Rahmen (in Fokusfarbe bei Aktivierung), den Text/Platzhalter sowie den blinkenden Cursor.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        // Hintergrund
        g.setColor(backgroundColor);
        g.fillRect((int) x, (int) y, width, height);

        // Rahmen
        g.setColor(focused ? focusColor : borderColor);
        g.drawRect((int) x, (int) y, width, height);

        Font font = resolveThemeFont(theme.defaultFont, fontName, fontSize, explicitFontName, explicitFontSize);
        Colors resolvedTextColor = resolveThemeColor(textColor, theme.textColor, explicitTextColor);

        boolean empty = text.isEmpty();

        g.setColor(empty ? placeholderColor : resolvedTextColor);
        drawStyledText(g, empty ? placeholder : text, (int) (x + 8), (int) (y + height / 2 + font.getSize() / 3), font);

        // Blinkender Cursor wenn fokussiert
        if (enabled && focused && cursorBlinkTimer < 0.5f && width > 16 && height > 8) {
            int textWidth = g.raw().getFontMetrics(font).stringWidth(text);
            int cursorX = (int) (x + 8) + Maths.min(textWidth, width - 16);
            g.setColor(resolvedTextColor);
            g.fillRect(cursorX, (int) y + 4, 2, height - 8);
        }
    }

    /**
     * Gibt den getippten Text zurück.
     *
     * @return Der aktuelle Eingabetext.
     */
    public String getText() { return text; }

    /**
     * Setzt den Inhalt des Eingabefeldes manuell.
     *
     * @param text Der festzulegende Text (darf das eingestellte Zeichenlimit nicht überschreiten).
     * @throws RuntimeException (Lunar-Error) Wenn {@code text} {@code null} ist.
     */
    public void setText(String text) {
        if (text == null) {
            throw Errors.nullValue(ErrorDetails.of("UITextField", "Text field content must not be null")
                    .withActual(null)
                    .withExpected("A string; use an empty string to clear the field")
                    .withExplanation("Text editing requires a string whose length and characters can be inspected"), null);
        }

        if (text.length() <= maxChars) {
            this.text = text;
        }
    }

    /**
     * Prüft, ob das Eingabefeld aktuell den Fokus besitzt.
     *
     * @return {@code true}, wenn fokussiert, sonst {@code false}.
     */
    public boolean isFocused() {
        return focused;
    }

    /**
     * Legt die Sichtbarkeit des Eingabefelds fest.
     * Beim Ausblenden wird der Fokus automatisch entzogen und der Cursor-Blink-Timer zurückgesetzt.
     *
     * @param visible {@code true} für sichtbar, sonst {@code false}.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            focused = false;
            cursorBlinkTimer = 0f;
        }
    }

    /**
     * Legt den Aktivierungszustand des Eingabefelds fest.
     * Beim Deaktivieren wird der Fokus automatisch entzogen und der Cursor-Blink-Timer zurückgesetzt.
     *
     * @param enabled {@code true} zum Aktivieren, sonst {@code false}.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            focused = false;
            cursorBlinkTimer = 0f;
        }
    }
    /**
     * Setzt die maximale Anzahl an erlaubten Zeichen.
     *
     * @param maxChars Das Zeichenlimit (muss &ge; 0 sein).
     * @throws RuntimeException (Lunar-Error) Wenn {@code maxChars} negativ ist.
     */
    public void setMaxChars(int maxChars) {
        if (maxChars < 0) {
            throw Errors.argument(ErrorDetails.of("UITextField", "Maximum character count must not be negative")
                    .withActual(maxChars)
                    .withExpected("A character count greater than or equal to 0")
                    .withExplanation("A text length limit cannot be negative; use 0 to prevent new characters"), null);
        }

        this.maxChars = maxChars;
    }

    /**
     * Gibt den Platzhalter-Text zurück.
     *
     * @return Der Platzhalter-Text.
     */
    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Setzt den Platzhalter-Text.
     *
     * @param placeholder Der neue Platzhalter-Text.
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * Gibt die Hintergrundfarbe zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Setzt die Hintergrundfarbe.
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
        explicitTextColor = true;
    }

    /**
     * Gibt die Farbe des Platzhalter-Textes zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getPlaceholderColor() {
        return placeholderColor;
    }

    /**
     * Setzt die Farbe des Platzhalter-Textes.
     *
     * @param placeholderColor Die neue {@link Colors}-Farbe.
     */
    public void setPlaceholderColor(Colors placeholderColor) {
        this.placeholderColor = placeholderColor;
    }

    /**
     * Gibt die Rahmenfarbe im normalen Zustand zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getBorderColor() {
        return borderColor;
    }

    /**
     * Setzt die Rahmenfarbe im normalen Zustand.
     *
     * @param borderColor Die neue {@link Colors}-Farbe.
     */
    public void setBorderColor(Colors borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Gibt die Rahmenfarbe im fokussierten Zustand zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getFocusColor() {
        return focusColor;
    }

    /**
     * Setzt die Rahmenfarbe im fokussierten Zustand.
     *
     * @param focusColor Die neue {@link Colors}-Farbe.
     */
    public void setFocusColor(Colors focusColor) {
        this.focusColor = focusColor;
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
        explicitFontSize = true;
    }

    /**
     * Gibt den Namen der Schriftart zurück.
     *
     * @return Der Schriftartname.
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * Setzt den Namen der Schriftart.
     *
     * @param fontName Der neue Schriftartname.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
        explicitFontName = true;
    }
}

