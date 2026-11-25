package LunarAxis.graphics;

import java.awt.Font;

import LunarAxis.util.ResourceManager;

/**
 * Die Klasse {@code Fonts} ist eine praktische Hilfsklasse (Utility-Klasse) für die Verwaltung
 * von Schriftarten in der LunarAxis-Engine.
 * <p>
 * Sie stellt Standard-Schriftfamilien zur Verfügung und greift im Hintergrund auf den
 * {@link ResourceManager} zu, um eigene Schriftarten (z.B. Retro-Pixel-Schriften als {@code .ttf}-Datei)
 * zu laden und bereitzustellen.
 * </p>
 * <p>
 * Da dies eine reine Hilfsklasse ist, muss sie nicht mit {@code new Fonts()} instanziiert werden.
 * Alle Methoden können direkt statisch aufgerufen werden.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Eigene Schriftart laden und verwenden
 *
 * # 1. Eine TrueType-Schriftart (.ttf) aus dem assets-Ordner registrieren
 * Fonts.loadFont("retro_font", "assets/fonts/arcade.ttf")
 *
 * # 2. Die geladene Schrift in einer Grösse von 24 Pixeln abrufen (Font.BOLD für Fett)
 * myFont = Fonts.getFont("retro_font", Font.BOLD, 24)
 *
 * # 3. Dem Grafik-Tool zuweisen
 * graphics.setFont(myFont)
 * graphics.drawString("Game Over", 100, 200)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Fonts {

    /**
     * Voreingestellte Standard-Schriftart (SansSerif)
     */
    public static final String DEFAULT = "SansSerif";

    /**
     * Monospaced-Schriftart, bei der jeder Buchstabe exakt gleich breit ist (ideal für Tabellen oder Timer)
     */
    public static final String MONO = "Monospaced";

    /**
     * Dialog-Schriftart für Benutzeroberflächen
     */
    public static final String DIALOG = "Dialog";

    /**
     * Privater Konstruktor, um zu verhindern, dass von dieser Hilfsklasse
     * aus Versehen Instanzen erstellt werden.
     */
    private Fonts() {
    }

    /**
     * Lädt eine benutzerdefinierte Schriftarten-Datei (z.B. im {@code .ttf}-Format) von der Festplatte
     * oder aus den Projekt-Ressourcen und registriert sie unter einem Namen.
     *
     * @param name Der eindeutige Name, unter dem die Schrift intern gespeichert werden soll (z.B. "pixel").
     * @param path Der Dateipfad zur Schriftart-Datei (z.B. "fonts/font.ttf").
     * @return Das fertig geladene Java-{@link Font}-Objekt.
     */
    public static Font loadFont(String name, String path) {
        return ResourceManager.getInstance().loadFont(name, path);
    }

    /**
     * Ruft eine bereits geladene Schriftart in einer bestimmten Stil-Art und Grösse ab.
     *
     * @param name  Der Name, unter dem die Schrift registriert wurde (oder eine Standard-Konstante wie {@link #DEFAULT}).
     * @param style Der Stil der Schrift (z.B. {@code Font.PLAIN} für normal, {@code Font.BOLD} für fett, {@code Font.ITALIC} für kursiv).
     * @param size  Die Schriftgrösse in Pixeln als Kommazahl.
     * @return Das angepasste {@link Font}-Objekt, bereit zum Zeichnen.
     */
    public static Font getFont(String name, int style, float size) {
        return ResourceManager.getInstance().getFont(name, style, size);
    }

    /**
     * Prüft, ob eine bestimmte Schriftart bereits erfolgreich geladen und registriert wurde.
     *
     * @param name Der Name der zu prüfenden Schriftart.
     * @return {@code true}, wenn die Schrift bereitsteht, andernfalls {@code false}.
     */
    public static boolean isFontLoaded(String name) {
        return ResourceManager.getInstance().isFontLoaded(name);
    }
}
