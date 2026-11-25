package LunarAxis.ui.theme;

import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Fonts;

import java.awt.*;

/**
 * Die Klasse {@code UITheme} kapselt die zentrale Stil-Definition für alle UI-Elemente der Engine.
 * <p>
 * Sie stellt einheitliche Farben, Schriftarten (Typografie) und Standard-Abmessungen bereit
 * und ermöglicht das einfache Umschalten zwischen Themes (z.B. Dark Mode oder Light Mode).
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UITheme {

    /**
     * Die Haupt-Hintergrundfarbe für UI-Container und Bildschirme.
     */
    public Colors backgroundColor;

    /**
     * Die Vordergrundfarbe für UI-Komponenten.
     */
    public Colors foregroundColor;

    /**
     * Die Akzentfarbe für hervorgehobene Elemente.
     */
    public Colors accentColor;

    /**
     * Die Textfarbe für normalen Text.
     */
    public Colors textColor;

    /**
     * Die Farbe für deaktivierte (disabled) UI-Elemente.
     */
    public Colors disabledColor;

    /**
     * Die Rahmenfarbe von UI-Elementen.
     */
    public Colors borderColor;

    /**
     * Die Hintergrundfarbe bei Maus-Hover.
     */
    public Colors hoverColor;

    /**
     * Die Hintergrundfarbe bei gedrücktem Zustand.
     */
    public Colors pressedColor;

    /**
     * Die Standard-Schriftart für gewöhnlichen Text.
     */
    public Font defaultFont;

    /**
     * Die Schriftart für Titel und Überschriften.
     */
    public Font titleFont;

    /**
     * Die Schriftart für kleine Hinweistexte oder Beschriftungen.
     */
    public Font smallFont;

    // Abmessungen
    /**
     * Standardhöhe für Schaltflächen (Buttons) in Pixeln.
     */
    public int buttonHeight;

    /**
     * Standardhöhe für Eingabefelder in Pixeln.
     */
    public int inputHeight;

    /**
     * Standard-Innenabstand (Padding) in Pixeln.
     */
    public int padding;

    /**
     * Abrundungsradius für Ecken in Pixeln.
     */
    public int borderRadius;

    /**
     * Erstellt ein neues, leeres {@code UITheme}.
     */
    public UITheme() {
    }

    /**
     * Factory-Methode zur Erstellung eines dunklen Standard-Themes (Dark Theme).
     *
     * @return Ein konfigurierte {@link UITheme}-Objekt mit dunklen Farbwerten.
     */
    public static UITheme createDarkTheme() {
        UITheme theme = new UITheme();
        theme.backgroundColor = new Colors(30, 30, 35);
        theme.foregroundColor = new Colors(50, 50, 60);
        theme.accentColor = new Colors(80, 140, 220);
        theme.textColor = new Colors(240, 240, 245);
        theme.disabledColor = new Colors(80, 80, 90);
        theme.borderColor = new Colors(70, 70, 80);
        theme.hoverColor = new Colors(90, 150, 230);
        theme.pressedColor = new Colors(60, 120, 200);

        theme.defaultFont = Fonts.getFont("SansSerif", Font.PLAIN, 16);
        theme.titleFont = Fonts.getFont("SansSerif", Font.BOLD, 24);
        theme.smallFont = Fonts.getFont("SansSerif", Font.PLAIN, 12);


        theme.buttonHeight = 40;
        theme.inputHeight = 32;
        theme.padding = 10;
        theme.borderRadius = 4;
        return theme;
    }

    /**
     * Factory-Methode zur Erstellung eines hellen Standard-Themes (Light Theme).
     *
     * @return Ein konfiguriertes {@link UITheme}-Objekt mit hellen Farbwerten.
     */
    public static UITheme createLightTheme() {
        UITheme theme = new UITheme();
        theme.backgroundColor = new Colors(240, 240, 245);
        theme.foregroundColor = new Colors(255, 255, 255);
        theme.accentColor = new Colors(60, 120, 200);
        theme.textColor = new Colors(20, 20, 25);
        theme.disabledColor = new Colors(180, 180, 190);
        theme.borderColor = new Colors(190, 190, 200);
        theme.hoverColor = new Colors(220, 230, 245);
        theme.pressedColor = new Colors(200, 215, 235);

        theme.defaultFont = Fonts.getFont("SansSerif", Font.PLAIN, 16);
        theme.titleFont = Fonts.getFont("SansSerif", Font.BOLD, 24);
        theme.smallFont = Fonts.getFont("SansSerif", Font.PLAIN, 12);


        theme.buttonHeight = 40;
        theme.inputHeight = 32;
        theme.padding = 10;
        theme.borderRadius = 4;
        return theme;
    }

    /**
     * Copy-Konstruktor zur Erstellung einer tiefen Kopie eines bestehenden {@code UITheme}s.
     *
     * @param other Das zu kopierende {@link UITheme}.
     */
    public UITheme(UITheme other) {
        this.backgroundColor = new Colors(other.backgroundColor);
        this.foregroundColor = new Colors(other.foregroundColor);
        this.accentColor = new Colors(other.accentColor);
        this.textColor = new Colors(other.textColor);
        this.disabledColor = new Colors(other.disabledColor);
        this.borderColor = new Colors(other.borderColor);
        this.hoverColor = new Colors(other.hoverColor);
        this.pressedColor = new Colors(other.pressedColor);

        this.defaultFont = other.defaultFont;
        this.titleFont = other.titleFont;
        this.smallFont = other.smallFont;

        this.buttonHeight = other.buttonHeight;
        this.inputHeight = other.inputHeight;
        this.padding = other.padding;
        this.borderRadius = other.borderRadius;
    }
}