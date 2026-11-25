package LunarAxis.ui.core;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Fonts;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.theme.UITheme;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;

/**
 * Die abstrakte Basisklasse {@code UIElement} bildet das Fundament für alle Elemente der Benutzeroberfläche (UI).
 * <p>
 * Jedes UI-Element besitzt eine Position, Grösse, Sichtbarkeit, einen Aktivierungszustand sowie einen Z-Index.
 * UI-Elemente nutzen absolute Bildschirm-Koordinaten (Pixel) und werden unabhängig von der Spielkamera gerendert.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public abstract class UIElement {
    protected float x, y;
    protected int width, height;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected int zIndex = 0;
    protected UIAnchor anchor = UIAnchor.ABSOLUTE;
    protected UITheme theme;
    private boolean themeStyleEnabled;
    protected UIElement parent;

    /**
     * Erstellt ein neues UI-Element mit Position, Grösse und dem dunklen Standard-Theme.
     *
     * @param x      Die X-Koordinate auf dem Bildschirm.
     * @param y      Die Y-Koordinate auf dem Bildschirm.
     * @param width  Die Breite des Elements in Pixeln.
     * @param height Die Höhe des Elements in Pixeln.
     */
    public UIElement(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.theme = UITheme.createDarkTheme();
    }

    /**
     * Wird in jedem Frame vom {@code UIManager} aufgerufen, um den Zustand und die Eingaben des Elements zu aktualisieren.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Maus- und Tastaturabfragen.
     */
    public abstract void update(float deltaTime, InputManager input);

    /**
     * Zeichnet das UI-Element auf den Bildschirm.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    public abstract void draw(Graphics g);

    /**
     * Prüft, ob sich die aktuelle Mausposition innerhalb der Grenzen dieses UI-Elements befindet.
     *
     * @param input Der {@link InputManager} zur Abfrage der Mauskoordinaten.
     * @return {@code true}, wenn die Maus über dem Element liegt, sonst {@code false}.
     */
    public boolean containsMouse(InputManager input) {
        int mx = input.getMouseX();
        int my = input.getMouseY();
        return mx >= x && mx <= (x + width) && my >= y && my <= (y + height);
    }

    /**
     * Berechnet die tatsächliche Bildschirmposition basierend auf dem zugewiesenen {@link UIAnchor}.
     * Wird vom {@code UIManager} automatisch vor dem Rendervorgang aufgerufen.
     *
     * @param screenWidth  Die aktuelle Gesamtbreite des Bildschirms.
     * @param screenHeight Die aktuelle Gesamthöhe des Bildschirms.
     */
    public void applyAnchor(int screenWidth, int screenHeight) {
        if (anchor == UIAnchor.ABSOLUTE || parent != null) {
            return; // Absolute Position oder innerhalb eines Panels
        }

        switch (anchor) {
            case TOP_LEFT:
                x = theme.padding;
                y = theme.padding;
                break;
            case TOP_CENTER:
                x = (screenWidth - width) / 2f;
                y = theme.padding;
                break;
            case TOP_RIGHT:
                x = screenWidth - width - theme.padding;
                y = theme.padding;
                break;
            case CENTER_LEFT:
                x = theme.padding;
                y = (screenHeight - height) / 2f;
                break;
            case CENTER:
                x = (screenWidth - width) / 2f;
                y = (screenHeight - height) / 2f;
                break;
            case CENTER_RIGHT:
                x = screenWidth - width - theme.padding;
                y = (screenHeight - height) / 2f;
                break;
            case BOTTOM_LEFT:
                x = theme.padding;
                y = screenHeight - height - theme.padding;
                break;
            case BOTTOM_CENTER:
                x = (screenWidth - width) / 2f;
                y = screenHeight - height - theme.padding;
                break;
            case BOTTOM_RIGHT:
                x = screenWidth - width - theme.padding;
                y = screenHeight - height - theme.padding;
                break;
        }
    }

    /**
     * Gibt die X-Position des Elements zurück.
     * @return X-Koordinate.
     */
    public float getX() {
        return x;
    }

    /**
     * Setzt die X-Position und schaltet den Anker-Modus auf {@link UIAnchor#ABSOLUTE} um.
     * @param x Neue X-Koordinate.
     */
    public void setX(float x) {
        this.x = x; this.anchor = UIAnchor.ABSOLUTE;
    }

    /**
     * Gibt die Y-Position des Elements zurück.
     * @return Y-Koordinate.
     */
    public float getY() {
        return y;
    }

    /**
     * Setzt die Y-Position und schaltet den Anker-Modus auf {@link UIAnchor#ABSOLUTE} um.
     * @param y Neue Y-Koordinate.
     */
    public void setY(float y) {
        this.y = y; this.anchor = UIAnchor.ABSOLUTE;
    }

    /**
     * Setzt sowohl X- als auch Y-Position und schaltet den Anker-Modus auf {@link UIAnchor#ABSOLUTE} um.
     *
     * @param x Neue X-Koordinate.
     * @param y Neue Y-Koordinate.
     */
    public void setPosition(float x, float y) {
        this.x = x; this.y = y; this.anchor = UIAnchor.ABSOLUTE;
    }

    /**
     * Gibt die Breite des Elements zurück.
     * @return Breite in Pixeln.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Setzt die Breite des Elements.
     * @param width Neue Breite in Pixeln.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Gibt die Höhe des Elements zurück.
     * @return Höhe in Pixeln.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Setzt die Höhe des Elements.
     * @param height Neue Höhe in Pixeln.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Prüft, ob das Element sichtbar ist.
     * @return {@code true}, wenn sichtbar.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Legt die Sichtbarkeit des Elements fest.
     * @param visible {@code true} für sichtbar, sonst {@code false}.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Prüft, ob das Element interaktiv/aktiviert ist.
     * @return {@code true}, wenn aktiviert.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Legt den Aktivierungszustand des Elements fest.
     * @param enabled {@code true} zum Aktivieren, sonst {@code false}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gibt den Z-Index (die Zeichenebene) des Elements zurück.
     * @return Der Z-Index.
     */
    public int getZIndex() {
        return zIndex;
    }

    /**
     * Setzt den Z-Index (die Zeichenebene) des Elements.
     * @param zIndex Neuer Z-Index.
     */
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * Gibt den aktuellen Positionierungs-Anker zurück.
     * @return Der {@link UIAnchor}.
     */
    public UIAnchor getAnchor() {
        return anchor;
    }

    /**
     * Setzt den Positionierungs-Anker für die automatische Ausrichtung.
     *
     * @param anchor Der neue {@link UIAnchor}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code anchor} {@code null} ist.
     */
    public void setAnchor(UIAnchor anchor) {
        if (anchor == null) {
            throw Errors.nullValue(ErrorDetails.of("UIElement", "UI anchor must not be null")
                    .withActual(null)
                    .withExpected("A UIAnchor value, including ABSOLUTE for manual positioning")
                    .withExplanation("Anchor calculation requires a valid positioning mode"), null);
        }

        this.anchor = anchor;
    }

    /**
     * Gibt das zugewiesene {@link UITheme} zurück.
     * @return Das Visuelle Theme.
     */
    public UITheme getTheme() {
        return theme;
    }

    /**
     * Weist dem Element ein visuelles Theme zu.
     *
     * @param theme Das zu verwendende {@link UITheme}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code theme} {@code null} ist.
     */
    public void setTheme(UITheme theme) {
        if (theme == null) {
            throw Errors.nullValue(ErrorDetails.of("UIElement", "UI theme must not be null")
                    .withActual(null)
                    .withExpected("A UITheme instance")
                    .withExplanation("Anchor positioning reads layout values from the assigned theme"), null);
        }

        this.theme = theme;
        themeStyleEnabled = true;
    }

    /**
     * Ermittelt die anzuwendende Farbe unter Berücksichtigung des aktivierten {@link UITheme}s.
     * Manuell gesetzte Farben behalten ihren Vorrang vor Theme-Standards.
     *
     * @param color      Die aktuell im Element gespeicherte Standardfarbe.
     * @param themeColor Die Farbe aus dem aktuell zugewiesenen {@link UITheme}.
     * @param explicit   {@code true}, wenn die Farbe vom Entwickler explizit überschrieben wurde.
     * @return Die aufzulösende {@link Colors}-Instanz.
     */
    protected final Colors resolveThemeColor(Colors color, Colors themeColor, boolean explicit) {
        return themeStyleEnabled && !explicit && themeColor != null ? themeColor : color;
    }

    /**
     * Löst Schriftart und Schriftgrösse unabhängig voneinander auf, basierend auf dem {@link UITheme}.
     *
     * @param themeFont    Die Schriftart aus dem {@link UITheme}.
     * @param name         Der Name der Fallback-Schriftart.
     * @param size         Die Ziel-Schriftgrösse.
     * @param explicitName {@code true}, wenn der Schriftname explizit überschrieben wurde.
     * @param explicitSize {@code true}, wenn die Schriftgrösse explizit überschrieben wurde.
     * @return Die berechnete {@link Font}-Instanz.
     */
    protected final Font resolveThemeFont(Font themeFont, String name, int size, boolean explicitName, boolean explicitSize) {
        if (!themeStyleEnabled || themeFont == null) {
            return Fonts.getFont(name, Font.PLAIN, size);
        }
        if (explicitName) {
            return Fonts.getFont(name, themeFont.getStyle(), explicitSize ? size : themeFont.getSize2D());
        }
        if (explicitSize) {
            return themeFont.getSize2D() == size ? themeFont : themeFont.deriveFont((float) size);
        }
        return themeFont;
    }

    /**
     * Zeichnet den angegebenen Text mit der aufgelösten Schriftart und stellt anschliessend
     * die zuvor im {@link Graphics}-Objekt aktivierte Schriftart wieder her.
     *
     * @param g    Das {@link Graphics}-Zeichenwerkzeug der Engine.
     * @param text Der zu zeichnende Text.
     * @param x    X-Koordinate für den Text.
     * @param y    Y-Koordinate für den Text.
     * @param font Die zu verwendende {@link Font}-Schriftart.
     */
    protected final void drawStyledText(Graphics g, String text, int x, int y, Font font) {
        if (text == null) {
            return;
        }

        Graphics2D raw = g.raw();
        Font previous = raw.getFont();
        try {
            raw.setFont(font);
            g.drawString(text, x, y);
        } finally {
            raw.setFont(previous);
        }
    }

    /**
     * Gibt das Eltern-Element (z.B. ein Panel) zurück.
     * @return Das übergeordnete {@link UIElement} oder {@code null}.
     */
    public UIElement getParent() {
        return parent;
    }

    /**
     * Setzt das übergeordnete Eltern-Element.
     * @param parent Das übergeordnete {@link UIElement}.
     */
    public void setParent(UIElement parent) {
        this.parent = parent;
    }
}
