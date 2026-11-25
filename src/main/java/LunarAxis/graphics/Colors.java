package LunarAxis.graphics;


import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.Color;

/**
 * Die Klasse {@code Colors} repräsentiert eine Farbe in der LunarAxis-Engine.
 * <p>
 * Alle Farben am Computer werden aus den drei Grundfarben <b>R</b>ot, <b>G</b>rün und <b>B</b>lau (RGB)
 * zusammengemischt. Jeder dieser Werte reicht von 0 (gar nicht vorhanden) bis 255 (volle Leuchtkraft).
 * Zusätzlich gibt es den sogenannten <b>A</b>lpha-Wert (RGBA), der bestimmt, wie durchsichtig eine Farbe ist
 * (0 = komplett unsichtbar, 255 = komplett deckend).
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Farben verwenden und erstellen
 *
 * # 1. Vorgefertigte Farben nutzen:
 * graphics.setColor(Colors.RED)
 * graphics.fillRect(10, 10, 50, 50)
 *
 * # 2. Eigene Farbe mischen (RGB: viel Rot, wenig Grün, kein Blau = Orange)
 * myOrange = Colors(255, 100, 0)
 *
 * # 3. Halbtransparente Farbe (Alpha = 128)
 * ghostBlue = Colors(0, 0, 255, 128)
 *
 * # 4. Farbe aus einem Hex-Code (wie im Webdesign)
 * webColor = Colors("#336699")
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Colors {

    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public static final Colors BLACK = new Colors(0, 0, 0);
    public static final Colors WHITE = new Colors(255,255,255);
    public static final Colors RED = new Colors(255,0,0);
    public static final Colors GREEN = new Colors(0,255,0);
    public static final Colors BLUE = new Colors(0,0,255);
    public static final Colors YELLOW = new Colors(255,255,0);
    public static final Colors CYAN = new Colors(0,255,255);
    public static final Colors MAGENTA = new Colors(255,0,255);
    public static final Colors GRAY = new Colors(128,128,128);
    public static final Colors ORANGE = new Colors(255,165,0);
    public static final Colors DARK_GRAY = new Colors(64,64,64);
    public static final Colors LIGHT_GRAY = new Colors(192,192,192);
    public static final Colors PURPLE = new Colors(128,0,128);

    /**
     * Mischt eine komplett deckende Farbe aus Rot, Grün und Blau (RGB).
     * <p>
     * <i>Hinweis:</i> Solltest du versehentlich Zahlen unter 0 oder über 255 eingeben,
     * werden diese automatisch auf den erlaubten Bereich korrigiert (geclampt).
     * </p>
     *
     * @param r Der Rot-Anteil (0 bis 255).
     * @param g Der Grün-Anteil (0 bis 255).
     * @param b Der Blau-Anteil (0 bis 255).
     */
    public Colors(int r, int g, int b) {
        this(r, g, b,255);
    }

    /**
     * Mischt eine Farbe aus Rot, Grün, Blau und einem Transparenz-Wert (Alpha).
     *
     * @param r Der Rot-Anteil (0 bis 255).
     * @param g Der Grün-Anteil (0 bis 255).
     * @param b Der Blau-Anteil (0 bis 255).
     * @param a Der Alpha-Wert für die Durchsichtigkeit (0 = unsichtbar, 255 = deckend).
     */
    public Colors(int r, int g, int b, int a) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
        this.a = clamp(a);
    }

    /**
     * Erstellt eine Farbe aus einem klassischen Hexadezimal-Code, wie er oft im
     * Webdesign (HTML/CSS) verwendet wird.
     *
     * <p>
     * Erlaubte Formate sind z.B. {@code "#FF0000"} (Rot) oder {@code "00FF00"} (Grün, ohne Raute).
     * Optional kann auch die Transparenz am Ende angehängt werden: {@code "#FF000080"} (Halbtransparentes Rot).
     * </p>
     *
     * @param hexCode Der Hex-Farbcode (6 oder 8 Zeichen, optional mit vorangestelltem '#').
     * @throws RuntimeException (Lunar-Error) Wenn der Code das falsche Format hat oder ungültige Zeichen enthält.
     */
    public Colors(String hexCode) {
        if (hexCode == null) {
            throw Errors.nullValue(ErrorDetails.of("Colors", "Hex color cannot be null")
                    .withActual(null)
                    .withExpected("RRGGBB or RRGGBBAA, optionally prefixed with #")
                    .withExplanation("A color string is required to read the red, green, blue and optional alpha components"), null);
        }

        String suppliedCode = hexCode;
        if (hexCode.startsWith("#")) {
            hexCode = hexCode.substring(1);
        }

        if (hexCode.length() != 6 && hexCode.length() != 8) {
            throw Errors.argument(ErrorDetails.of("Colors", "Hex color must be RRGGBB or RRGGBBAA")
                    .withActual(suppliedCode)
                    .withExpected("Exactly 6 or 8 hexadecimal characters after an optional #")
                    .withExplanation("Each color component occupies two hexadecimal characters"), null);
        }

        try {
            this.r = Integer.parseInt(hexCode.substring(0, 2), 16);
            this.g = Integer.parseInt(hexCode.substring(2, 4), 16);
            this.b = Integer.parseInt(hexCode.substring(4, 6), 16);

            if (hexCode.length() == 8) {
                this.a = Integer.parseInt(hexCode.substring(6, 8), 16);
            } else {
                this.a = 255;
            }
        } catch (NumberFormatException exception) {
            throw Errors.describe(exception, ErrorDetails.of("Colors", "Hex color contains an unreadable component")
                    .withActual(suppliedCode)
                    .withExpected("RRGGBB or RRGGBBAA using hexadecimal color components")
                    .withExplanation("At least one two-character component cannot be parsed as a base-16 integer"));
        }
    }

    /**
     * Copy-Konstruktor: Erstellt eine exakte Kopie einer bereits existierenden Farbe.
     *
     * @param other Die Vorlage-Farbe, die kopiert werden soll.
     * @throws RuntimeException (Lunar-Error) Wenn die Vorlage {@code null} ist.
     */
    public Colors(Colors other) {
        if (other == null) {
            throw Errors.nullValue(ErrorDetails.of("Colors", "Source color cannot be null")
                    .withActual(null)
                    .withExpected("An existing Colors instance")
                    .withExplanation("The copy constructor reads all four components from the source color"), null);
        }

        this.r = other.r;
        this.g = other.g;
        this.b = other.b;
        this.a = other.a;
    }

    /**
     * Interne Hilfsmethode. Stellt sicher, dass ein Farbwert niemals unter 0 oder über 255 fällt.
     *
     * @param value Der zu prüfende Wert.
     * @return Der korrigierte Wert zwischen 0 und 255.
     */
    private int clamp(int value) {
        return Math.max(0, Math.min(255,value));
    }

    /**
     * Gibt den Rot-Anteil dieser Farbe zurück.
     * @return Wert zwischen 0 und 255.
     */
    public int getRed() {
        return r;
    }

    /**
     * Gibt den Grün-Anteil dieser Farbe zurück.
     * @return Wert zwischen 0 und 255.
     */
    public int getGreen() {
        return g;
    }

    /**
     * Gibt den Blau-Anteil dieser Farbe zurück.
     * @return Wert zwischen 0 und 255.
     */
    public int getBlue() {
        return b;
    }

    /**
     * Gibt die Transparenz (Alpha) dieser Farbe zurück.
     * @return Wert zwischen 0 (unsichtbar) und 255 (deckend).
     */
    public int getAlpha() {
        return a;
    }

    /**
     * Konvertiert diese LunarAxis-Farbe in eine Standard-Java-Farbe ({@link java.awt.Color}).
     * Dies wird intern von der Engine benötigt, um Formen auf den Bildschirm zeichnen zu können.
     *
     * @return Das entsprechende AWT-Farbobjekt.
     */
    public Color asAwt() {
        return new Color(r, g, b, a);
    }

    /**
     * Gibt eine textuelle Zusammenfassung der Farbwerte zurück (nützlich für Print-Ausgaben).
     */
    @Override
    public String toString() {
        return String.format("Colors[r=%d,g=%d,b=%d,a=%d]", r,g,b,a);
    }

    /**
     * Erzeugt eine leicht hellere Version dieser Farbe und gibt sie als neues Objekt zurück.
     * (Die Transparenz / der Alpha-Wert bleibt dabei unverändert).
     *
     * @return Eine hellere {@link Colors}-Instanz.
     */
    public Colors brighter() {
        Color awtColor = this.asAwt().brighter();
        return new Colors(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue(), this.a);
    }

    /**
     * Erzeugt eine leicht dunklere Version dieser Farbe und gibt sie als neues Objekt zurück.
     * (Die Transparenz / der Alpha-Wert bleibt dabei unverändert).
     *
     * @return Eine dunklere {@link Colors}-Instanz.
     */
    public Colors darker() {
        Color awtColor = this.asAwt().darker();
        return new Colors(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue(), this.a);
    }

}
