package LunarAxis.util;

import LunarAxis.graphics.SpriteSheet;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Die Klasse {@code ResourceManager} verwaltet das zentralisierte Laden, Cachen und Freigeben von Ressourcen innerhalb der LunarAxis-Engine.
 * <p>
 * Um doppeltes Laden aus dem Speicher zu verhindern, speichert der {@code ResourceManager} alle geladenen
 * Bilder, SpriteSheets und Schriftarten in einem internen Cache. Ressourcen werden primär über das Dateisystem
 * gesucht und bei Nichtauffinden automatisch über den Classpath (JAR-Ressourcen) nachgeladen.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * rm = ResourceManager.getInstance()
 * playerImage = rm.loadImage("assets/textures/player.png")
 * font = rm.getFont("SansSerif", Font.BOLD, 18f)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ResourceManager {

    private static final ResourceManager instance = new ResourceManager();

    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    private final Map<String, SpriteSheet> spriteSheetCache = new HashMap<>();
    private final Map<String, Font> fontCache = new HashMap<>();

    /**
     * Privater Konstruktor für das Singleton-Muster.
     * Initialisiert die vordefinierten Standard-Schriftarten des Systems.
     */
    private ResourceManager() {
        registerDefaultFonts();
    }

    /**
     * Lädt ein Bild aus der angegebenen Datei oder Classpath-Ressource und speichert es im Cache.
     * <p>
     * Ist das Bild bereits geladen, wird die vorhandene {@link BufferedImage}-Instanz direkt aus dem
     * Cache zurückgegeben.
     * </p>
     *
     * @param path Der Pfad zur Bilddatei (z. B. "assets/textures/player.png").
     * @return Das geladene {@link BufferedImage} oder {@code null}, falls das Bild nicht geladen werden konnte.
     */
    public BufferedImage loadImage(String path) {
        if (path == null || path.isEmpty()) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Image path is missing")
                    .withActual(path)
                    .withExpected("A non-empty image path")
                    .withExplanation("An image cannot be located without a path"), null);
            return null;
        }

        String key = normalizePath(path);
        BufferedImage cachedImage = imageCache.get(key);
        if (cachedImage != null) {
            return cachedImage;
        }

        BufferedImage image = null;
        IOException failure = null;

        try {
            image = loadImageFromFile(key);
        } catch (IOException e) {
            failure = e;
        }

        if (image == null) {
            try {
                image = loadImageFromResource(key);
            } catch (IOException e) {
                if (failure == null) {
                    failure = e;
                } else if (failure != e) {
                    failure.addSuppressed(e);
                }
            }
        }

        if (image == null) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Image could not be loaded")
                    .withActual(path)
                    .withExpected("A readable image file or classpath resource in a supported image format")
                    .withExplanation("Neither the filesystem lookup nor the classpath lookup produced an image"), failure);
            return null;
        }

        if (failure != null) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("ResourceManager", "Image loaded through fallback after a read failure")
                    .withActual(path)
                    .withExpected("The primary image source to be readable")
                    .withExplanation("A failed read was recovered by the fallback lookup and the image is available"), failure);
        }

        imageCache.put(key, image);
        return image;
    }

    /**
     * Prüft, ob ein Bild unter dem angegebenen Pfad aktuell im Cache geladen ist.
     *
     * @param path Der Pfad zum Bild.
     * @return {@code true}, wenn das Bild im Cache existiert, sonst {@code false}.
     */
    public boolean isImageLoaded(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return imageCache.containsKey(normalizePath(path));
    }

    /**
     * Entfernt ein Bild aus dem internen Cache, um Speicher freizugeben.
     *
     * @param path Der Pfad des zu entfernenden Bildes.
     */
    public void unloadImage(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        imageCache.remove(normalizePath(path));
    }

    /**
     * Lädt ein {@link SpriteSheet} basierend auf einem Bildpfad sowie den Zellendimensionen
     * und speichert es im Cache.
     *
     * @param path       Der Pfad zur Bilddatei.
     * @param cellWidth  Die Breite einer einzelnen Kachel/Frame in Pixeln (muss &gt; 0 sein).
     * @param cellHeight Die Höhe einer einzelnen Kachel/Frame in Pixeln (muss &gt; 0 sein).
     * @return Das erstellte {@link SpriteSheet} oder {@code null}, falls das zugrunde liegende Bild ungültig ist.
     */
    public SpriteSheet loadSpriteSheet(String path, int cellWidth, int cellHeight) {
        if (path == null || path.isEmpty()) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Sprite sheet path is missing")
                    .withActual(path)
                    .withExpected("A non-empty image path")
                    .withExplanation("A sprite sheet requires an image source"), null);
            return null;
        }
        if (cellWidth <= 0 || cellHeight <= 0) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Sprite sheet cell dimensions are invalid")
                    .withActual("cellWidth=" + cellWidth + ", cellHeight=" + cellHeight)
                    .withExpected("Cell width and height greater than 0")
                    .withExplanation("The source image cannot be divided into cells with zero or negative dimensions"), null);
            return null;
        }

        String key = normalizePath(path) + ": " + cellWidth + " x " + cellHeight;
        SpriteSheet cachedSheet = spriteSheetCache.get(key);

        if (cachedSheet != null) {
            return cachedSheet;
        }

        BufferedImage image = loadImage(path);
        if (image == null) {
            return null;
        }

        SpriteSheet sheet = new SpriteSheet(image, cellWidth, cellHeight);
        spriteSheetCache.put(key, sheet);
        return sheet;
    }

    /**
     * Prüft, ob ein SpriteSheet mit den spezifischen Zellendimensionen im Cache enthalten ist.
     *
     * @param path       Der Pfad zum Quellbild.
     * @param cellWidth  Die Zellbreite in Pixeln.
     * @param cellHeight Die Zellhöhe in Pixeln.
     * @return {@code true}, wenn das SpriteSheet geladen ist, sonst {@code false}.
     */
    public boolean isSpriteSheetLoaded(String path, int cellWidth, int cellHeight) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        String key = normalizePath(path) + ": " + cellWidth + " x " + cellHeight;
        return spriteSheetCache.containsKey(key);
    }

    /**
     * Entfernt ein SpriteSheet aus dem internen Cache.
     *
     * @param path       Der Pfad zum Quellbild.
     * @param cellWidth  Die Zellbreite in Pixeln.
     * @param cellHeight Die Zellhöhe in Pixeln.
     */
    public void unloadSpriteSheet(String path, int cellWidth, int cellHeight) {
        if (path == null || path.isEmpty()) {
            return;
        }
        String key = normalizePath(path) + ": " + cellWidth + " x " + cellHeight;
        spriteSheetCache.remove(key);
    }

    /**
     * Lädt eine TrueType-Schriftart (.ttf) aus einer Datei oder Classpath-Ressource, registriert sie im
     * {@link GraphicsEnvironment} des Systems und speichert sie unter einem logischen Namen im Cache.
     *
     * @param name Der Schlüsselname, unter dem die Schriftart im Cache hinterlegt wird.
     * @param path Der Pfad zur Font-Datei (.ttf).
     * @return Die geladene {@link Font}-Instanz oder {@code null}, falls das Laden fehlschlug.
     */
    public Font loadFont(String name, String path) {
        if (name == null || name.isEmpty()) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Font name is missing")
                    .withActual(name)
                    .withExpected("A non-empty font cache name")
                    .withExplanation("A loaded font requires a name for later retrieval"), null);
            return null;
        }
        if (path == null || path.isEmpty()) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Font path is missing")
                    .withActual(path)
                    .withExpected("A non-empty font path")
                    .withExplanation("A font cannot be located without a path"), null);
            return null;
        }

        Font cachedFont = fontCache.get(name);
        if (cachedFont != null) {
            return cachedFont;
        }

        String normalizedPath = normalizePath(path);
        Font font = null;
        Exception failure = null;

        try {
            font = loadFontFromFile(normalizedPath);
        } catch (Exception e) {
            failure = e;
        }

        if (font == null) {
            try {
                font = loadFontFromResource(normalizedPath);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                } else if (failure != e) {
                    failure.addSuppressed(e);
                }
            }
        }

        if (font == null) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Font could not be loaded")
                    .withActual("name=" + name + ", path=" + path)
                    .withExpected("A readable font file or classpath resource supported by the TrueType loader")
                    .withExplanation("Neither lookup produced a usable font"), failure);
            return null;
        }

        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            fontCache.put(name, font);
        } catch (Exception e) {
            if (failure != null && failure != e) {
                e.addSuppressed(failure);
            }
            Logger.report(LogLevel.ERROR, ErrorDetails.of("ResourceManager", "Loaded font could not be registered")
                    .withActual("name=" + name + ", path=" + path)
                    .withExpected("A font that can be registered and cached")
                    .withExplanation("Loading succeeded but registering or caching the font failed"), e);
            return null;
        }

        if (failure != null) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("ResourceManager", "Font loaded through fallback after a read failure")
                    .withActual("name=" + name + ", path=" + path)
                    .withExpected("The primary font source to be readable")
                    .withExplanation("The fallback lookup recovered the load operation and the font is available"), failure);
        }

        return font;
    }

    /**
     * Ruft eine registrierte Schriftart aus dem Cache ab und leitet daraus eine Instanz mit
     * dem angegebenen Stil und der angegebenen Grösse ab.
     * <p>
     * Falls die angeforderte Schriftart nicht existiert, wird automatisch auf "SansSerif" zurückgegriffen.
     * </p>
     *
     * @param name  Der logische Name der Schriftart (z. B. "SansSerif" oder ein benutzerspezifischer Name).
     * @param style Der Schriftstil (z. B. {@link Font#PLAIN}, {@link Font#BOLD} oder {@link Font#ITALIC}).
     * @param size  Die Schriftgrösse in Punkten.
     * @return Eine abgeleitete {@link Font}-Instanz.
     */
    public Font getFont(String name, int style, float size) {
        Font font = fontCache.get(name);
        if (font == null) {
            font = fontCache.get("SansSerif");
        }
        return font.deriveFont(style, size);
    }

    /**
     * Prüft, ob eine Schriftart unter dem Namen im Cache registriert ist.
     *
     * @param name Der Name der Schriftart.
     * @return {@code true}, wenn die Schriftart existiert, sonst {@code false}.
     */
    public boolean isFontLoaded(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return fontCache.containsKey(name);
    }

    /**
     * Entfernt eine Schriftart aus dem Cache. Standard-Systemschriften (wie "SansSerif") können
     * nicht entfernt werden.
     *
     * @param name Der Name der zu entfernenden Schriftart.
     */
    public void unloadFont(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        if (isDefaultFont(name)) {
            return;
        }
        fontCache.remove(name);
    }

    /**
     * Leert sämtliche Caches für Bilder, SpriteSheets und Schriftarten und stellt anschliessend
     * die Standard-Systemschriften wieder her.
     */
    public void clearCache() {
        imageCache.clear();
        spriteSheetCache.clear();
        fontCache.clear();
        registerDefaultFonts();
    }

    /**
     * Versucht, eine Bilddatei aus dem lokalen Dateisystem zu laden.
     *
     * @param path Der Dateipfad.
     * @return Das geladene {@link BufferedImage} oder {@code null}, wenn die Datei nicht existiert.
     * @throws IOException Wenn beim Lesevorgang ein Ein-/Ausgabefehler auftritt.
     */
    private BufferedImage loadImageFromFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        return ImageIO.read(file);
    }

    /**
     * Versucht, ein Bild aus dem Classpath (JAR / Resources) zu laden.
     *
     * @param path Der Ressourcenpfad.
     * @return Das geladene {@link BufferedImage} oder {@code null}, wenn die Ressource nicht gefunden wurde.
     * @throws IOException Wenn beim Lesevorgang ein Ein-/Ausgabefehler auftritt.
     */
    private BufferedImage loadImageFromResource(String path) throws IOException {
        try (InputStream inputStream = ResourceManager.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                return null;
            }
            return ImageIO.read(inputStream);
        }
    }

    /**
     * Registriert die Standard-Schriftarten der Java-Laufzeitumgebung im Cache.
     */
    private void registerDefaultFonts() {
        fontCache.put("SansSerif", new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        fontCache.put("Serif", new Font(Font.SERIF, Font.PLAIN, 12));
        fontCache.put("Monospaced", new Font(Font.MONOSPACED, Font.PLAIN, 12));
        fontCache.put("Dialog", new Font(Font.DIALOG, Font.PLAIN, 12));
        fontCache.put("DialogInput", new Font(Font.DIALOG_INPUT, Font.PLAIN, 12));
    }

    /**
     * Versucht, eine TrueType-Font-Datei aus dem Dateisystem zu laden.
     *
     * @param path Der Dateipfad.
     * @return Die geladene {@link Font}-Instanz oder {@code null}, wenn die Datei nicht existiert.
     * @throws IOException         Wenn ein E/A-Fehler auftritt.
     * @throws FontFormatException Wenn die Datei kein gültiges TrueType-Format besitzt.
     */
    private Font loadFontFromFile(String path) throws IOException, FontFormatException {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        return Font.createFont(Font.TRUETYPE_FONT, file);
    }

    /**
     * Versucht, eine TrueType-Font-Datei aus dem Classpath zu laden.
     *
     * @param path Der Ressourcenpfad.
     * @return Die geladene {@link Font}-Instanz oder {@code null}, wenn die Ressource nicht existiert.
     * @throws IOException         Wenn ein E/A-Fehler auftritt.
     * @throws FontFormatException Wenn die Ressource kein gültiges TrueType-Format besitzt.
     */
    private Font loadFontFromResource(String path) throws IOException, FontFormatException {
        try (InputStream inputStream = ResourceManager.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, inputStream);
        }
    }

    /**
     * Prüft, ob der angegebene Schriftartname zu den geschützten System-Standardfonts gehört.
     *
     * @param name Der Name der Schriftart.
     * @return {@code true}, wenn es sich um einen Standard-Font handelt, sonst {@code false}.
     */
    private boolean isDefaultFont(String name) {
        return name.equals("SansSerif") ||
                name.equals("Serif") ||
                name.equals("Monospaced") ||
                name.equals("Dialog") ||
                name.equals("DialogInput");
    }


    /**
     * Normalisiert Pfad-Strings für die eindeutige Schlüsselverwendung im Cache.
     * Ersetzt Backslashes durch Slashes und entfernt führende Slashes.
     *
     * @param path Der ursprüngliche Pfad-String.
     * @return Der bereinigte und normalisierte Pfad.
     */
    private String normalizePath(String path) {
        String normalized = path.replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    /**
     * Gibt die Anzahl der aktuell im Cache befindlichen Bilder zurück.
     *
     * @return Die Anzahl der geladenen Bilder.
     */
    public int getLoadedImageCount() {
        return imageCache.size();
    }

    /**
     * Gibt die Anzahl der aktuell im Cache befindlichen SpriteSheets zurück.
     *
     * @return Die Anzahl der geladenen SpriteSheets.
     */
    public int getLoadedSpriteSheetCount() {
        return spriteSheetCache.size();
    }

    /**
     * Gibt die Anzahl der aktuell im Cache registrierten Schriftarten zurück.
     *
     * @return Die Anzahl der geladenen Schriftarten (inkl. Systemschriften).
     */
    public int getLoadedFontCount() {
        return fontCache.size();
    }

    /**
     * Gibt die globale Singleton-Instanz des {@code ResourceManager}s zurück.
     *
     * @return Die {@link ResourceManager}-Instanz.
     */
    public static ResourceManager getInstance() {
        return instance;
    }
}
