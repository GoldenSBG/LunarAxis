package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;
import java.util.List;

/**
 * Die Klasse {@code UIDropdown} repräsentiert ein ausklappbares Auswahlmenü (Dropdown).
 * <p>
 * Sie erlaubt dem Benutzer, aus einer Liste von vorgegebenen Optionen ein Element auszuwählen.
 * Über ein {@link Runnable}-Callback kann bei einer neuen Auswahl eine entsprechende Aktion ausgelöst werden.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * options = ["Leicht", "Normal", "Schwer"]
 * dropdown = UIDropdown(100, 100, 150, options)
 * dropdown.setOnSelect(lambda: print("Ausgewählt: " + dropdown.getSelectedOption()))
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UIDropdown extends UIElement {
    private List<String> options;
    private int selectedIndex;
    private boolean isOpen;
    private Colors buttonColor;
    private Colors hoverColor;
    private Colors textColor;
    private Colors dropdownColor;
    private int itemHeight;
    private int fontSize;
    private String fontName = "SansSerif";

    private boolean explicitFontName;
    private boolean explicitFontSize;
    private boolean explicitTextColor;

    private Runnable onSelect;

    /**
     * Erstellt ein neues Dropdown-Menü mit Position, Breite und einer Liste von Optionen.
     *
     * @param x       Die X-Koordinate auf dem Bildschirm.
     * @param y       Die Y-Koordinate auf dem Bildschirm.
     * @param width   Die Breite des Dropdown-Headers in Pixeln.
     * @param options Eine {@link List} von auswählbaren String-Optionen.
     * @throws RuntimeException (Lunar-Error) Wenn {@code options} {@code null} ist.
     */
    public UIDropdown(float x, float y, int width, List<String> options) {
        super(x, y, width, 30);

        if (options == null) {
            throw Errors.nullValue(ErrorDetails.of("UIDropdown", "Dropdown options must not be null")
                    .withActual(null)
                    .withExpected("A list of dropdown options")
                    .withExplanation("The dropdown requires a list to read and display its options"), null);
        }

        this.options = options;
        this.selectedIndex = 0;
        this.isOpen = false;
        this.buttonColor = Colors.DARK_GRAY;
        this.hoverColor = Colors.GRAY;
        this.textColor = Colors.WHITE;
        this.dropdownColor = Colors.BLACK;
        this.itemHeight = 25;
        this.fontSize = 16;
    }

    /**
     * Setzt die Aktion (Callback), die bei Auswahl eines neuen Eintrags ausgeführt werden soll.
     *
     * @param onSelect Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnSelect(Runnable onSelect) {
        this.onSelect = onSelect;
    }

    /**
     * Aktualisiert den Aufklapp- und Auswahlzustand des Dropdown-Menüs basierend auf Mauseingaben.
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

        if (isOpen) {
            // Prüfen ob auf den Header geklickt wurde (zum Schliessen)
            if (containsMouse(input) && input.isMouseReleased(1)) {
                isOpen = false;
                return;
            }

            // Prüfen ob auf ein Item geklickt wurde
            for (int i = 0; i < options.size(); i++) {
                int itemY = (int) y + height + (i * itemHeight);
                if (mouseX >= x && mouseX <= x + width &&
                    mouseY >= itemY && mouseY <= itemY + itemHeight) {
                    if (input.isMouseReleased(1)) {
                        selectedIndex = i;
                        isOpen = false;
                        if (onSelect != null) {
                            onSelect.run();
                        }
                        return;
                    }
                }
            }

            // Dropdown schliessen wenn woanders geklickt
            if (input.isMouseReleased(1)) {
                isOpen = false;
            }
        } else {
            // Dropdown öffnen
            if (containsMouse(input) && input.isMouseReleased(1)) {
                isOpen = true;
            }
        }
    }

    /**
     * Zeichnet den Header des Dropdowns sowie im geöffneten Zustand die ausgeklappte Optionsliste.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }
        String currentText = getSelectedOption();
        Font font = resolveThemeFont(theme.defaultFont, fontName, fontSize, explicitFontName, explicitFontSize);
        Colors resolvedTextColor = resolveThemeColor(textColor, theme.textColor, explicitTextColor);
        Colors resolvedBorder = resolveThemeColor(Colors.WHITE, theme.borderColor, false);
        int baseline = (int) (y + height / 2 + font.getSize() / 3);

        // Button zeichnen
        g.setColor(buttonColor);
        g.fillRect((int) x, (int) y, width, height);
        g.setColor(resolvedBorder);
        g.drawRect((int) x, (int) y, width, height);

        // Aktueller Wert
        g.setColor(resolvedTextColor);
        drawStyledText(g, currentText, (int) (x + 8), baseline, font);

        // Dropdown-Pfeil ▼
        drawStyledText(g, "▼", (int) (x + width - 20), baseline, font);

        // Dropdown-Liste wenn geöffnet
        if (isOpen) {
            g.setColor(dropdownColor);
            g.fillRect((int) x, (int) y + height, width, options.size() * itemHeight);
            g.setColor(resolvedBorder);
            g.drawRect((int) x, (int) y + height, width, options.size() * itemHeight);

            for (int i = 0; i < options.size(); i++) {
                int itemY = (int) y + height + i * itemHeight;

                // Vereinfacht: immer grau zeichnen
                g.setColor(i == selectedIndex ? hoverColor : dropdownColor);
                g.fillRect((int) x, itemY, width, itemHeight);

                g.setColor(resolvedTextColor);
                drawStyledText(g, options.get(i), (int) (x + 8), itemY + itemHeight / 2 + font.getSize() / 3, font);
            }
        }
    }

    /**
     * Gibt den Index der aktuell ausgewählten Option zurück.
     *
     * @return Der 0-basierte Index der Auswahl.
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Gibt den Text der aktuell ausgewählten Option zurück.
     *
     * @return Der ausgewählte Text-String.
     * @throws RuntimeException (Lunar-Error) Wenn der selektierte Index ausserhalb der Liste liegt.
     */
    public String getSelectedOption() {
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            throw Errors.index(ErrorDetails.of("UIDropdown", "Dropdown selection no longer refers to an option")
                    .withActual("selectedIndex=" + selectedIndex + ", optionCount=" + options.size())
                    .withExpected("A selected index within the current options list")
                    .withExplanation("The options list may be empty or may have changed since the selection was assigned"), null);
        }

        return options.get(selectedIndex);
    }

    /**
     * Setzt den Auswahlindex des Dropdowns manuell.
     *
     * @param index Der festzulegende Index.
     * @throws RuntimeException (Lunar-Error) Wenn der Index ausserhalb des gültigen Bereichs liegt.
     */
    public void setSelectedIndex(int index) {
        if (index < 0 || index >= options.size()) {
            throw Errors.index(ErrorDetails.of("UIDropdown", "Dropdown selection index is out of bounds")
                    .withActual(index)
                    .withExpected("0 <= index < " + options.size())
                    .withExplanation("The selected index must refer to an option currently present in the dropdown"), null);
        }

        this.selectedIndex = index;
    }

    /**
     * Gibt die Liste der verfügbaren Optionen zurück.
     *
     * @return Die {@link List} der Strings.
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Gibt die Hintergrundfarbe des Dropdown-Buttons zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getButtonColor() {
        return buttonColor;
    }

    /**
     * Setzt die Hintergrundfarbe des Dropdown-Buttons.
     *
     * @param buttonColor Die neue {@link Colors}-Farbe.
     */
    public void setButtonColor(Colors buttonColor) {
        this.buttonColor = buttonColor;
    }

    /**
     * Gibt die Hervorhebungsfarbe für ausgewählte oder gehoverte Einträge zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getHoverColor() {
        return hoverColor;
    }

    /**
     * Setzt die Hervorhebungsfarbe für ausgewählte oder gehoverte Einträge.
     *
     * @param hoverColor Die neue {@link Colors}-Farbe.
     */
    public void setHoverColor(Colors hoverColor) {
        this.hoverColor = hoverColor;
    }

    /**
     * Gibt die Schriftfarbe des Dropdowns zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getTextColor() {
        return textColor;
    }

    /**
     * Setzt die Schriftfarbe des Dropdowns.
     *
     * @param textColor Die neue {@link Colors}-Farbe.
     */
    public void setTextColor(Colors textColor) {
        this.textColor = textColor;
        explicitTextColor = true;
    }

    /**
     * Gibt die Hintergrundfarbe des ausgeklappten Menüs zurück.
     *
     * @return Die {@link Colors}-Farbe.
     */
    public Colors getDropdownColor() {
        return dropdownColor;
    }

    /**
     * Setzt die Hintergrundfarbe des ausgeklappten Menüs.
     *
     * @param dropdownColor Die neue {@link Colors}-Farbe.
     */
    public void setDropdownColor(Colors dropdownColor) {
        this.dropdownColor = dropdownColor;
    }

    /**
     * Gibt die Höhe eines einzelnen Listeneintrags in Pixeln zurück.
     *
     * @return Die Zeilenhöhe.
     */
    public int getItemHeight() {
        return itemHeight;
    }

    /**
     * Setzt die Höhe eines einzelnen Listeneintrags in Pixeln.
     *
     * @param itemHeight Die neue Zeilenhöhe.
     */
    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    /**
     * Gibt die Schriftgrösse der Textanzeige zurück.
     *
     * @return Die Schriftgrösse in Punkten.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Setzt die Schriftgrösse der Textanzeige.
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
     * Setzt den Namen der verwendeten Schriftart.
     *
     * @param fontName Der neue Schriftartname.
     */
    public void setFontName(String fontName) {
        this.fontName = fontName;
        explicitFontName = true;
    }
}