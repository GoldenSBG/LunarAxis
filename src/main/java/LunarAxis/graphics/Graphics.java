package LunarAxis.graphics;

import LunarAxis.math.Maths;
import LunarAxis.math.Rect;
import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 * Die Klasse {@code Graphics} ist das universelle Zeichen-Werkzeug (der "Pinsel") der LunarAxis-Engine.
 * <p>
 * Sie fungiert als sichere Hülle (Wrapper) um das komplizierte Java-eigene {@link Graphics2D}-System
 * und bietet Schülern eine einfache, intuitive Schnittstelle, um Formen, Bilder, Linien und Text
 * auf den Bildschirm zu zeichnen.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Formen und Text zeichnen
 * def render(graphics):
 *     # Farbe auf Rot setzen
 *     graphics.setColor(Colors.RED)
 *
 *     # Ein gefülltes Rechteck zeichnen (x, y, breite, höhe)
 *     graphics.fillRect(100, 50, 80, 40)
 *
 *     # Text auf dem Bildschirm ausgeben
 *     graphics.setColor(Colors.WHITE)
 *     graphics.drawString("Punkte: 10", 20, 30)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Graphics {

    /**
     * Das Graphics2D-Objekt, das für die Zeichnungen verwendet wird
     */
    private final Graphics2D graphics2D;
    private final AffineTransform affineTransform;
    private Camera2D activeCamera;

    /**
     * Erstellt einen neuen Grafik-Wrapper basierend auf einem bestehenden Java-Zeichenkontext.
     *
     * @param graphics2D Das originale Java {@link Graphics2D}-Objekt.
     * @throws RuntimeException (Lunar-Error) Wenn der übergebene Grafikkontext {@code null} ist.
     */
    public Graphics(Graphics2D graphics2D) {
        if (graphics2D == null) {
            throw Errors.lunar(ErrorDetails.of("Graphics", "Graphics2D must not be null")
                    .withActual(null)
                    .withExpected("An existing Graphics2D drawing context")
                    .withExplanation("The graphics wrapper delegates drawing operations to this context and stores its initial transformation"), null);
        }

        this.graphics2D = graphics2D;
        this.affineTransform = new AffineTransform(graphics2D.getTransform());
    }

    /**
     * Legt die aktuelle Zeichenfarbe fest. Alle nachfolgenden Formen und Linien
     * werden in dieser Farbe gezeichnet.
     *
     * @param color Das {@link Colors}-Objekt (z.B. {@code Colors.RED}).
     */
    public void setColor(Colors color) {
        if (color == null) {
            return;
        }
        graphics2D.setColor(color.asAwt());
    }

    /**
     * Zeichnet ein Bild (Sprite) an einer bestimmten Position auf den Bildschirm.
     *
     * @param image Das zu zeichnende {@link Image}.
     * @param x     Die X-Koordinate der oberen linken Ecke.
     * @param y     Die Y-Koordinate der oberen linken Ecke.
     */
    public void drawImage(Image image, int x, int y) {
        if (image == null) {
            return;
        }
        graphics2D.drawImage(image, x, y, null);
    }

    /**
     * Zeichnet ein Bild und skaliert es dabei direkt auf eine bestimmte Breite und Höhe.
     *
     * @param image  Das zu zeichnende {@link Image}.
     * @param x      Die X-Koordinate der oberen linken Ecke.
     * @param y      Die Y-Koordinate der oberen linken Ecke.
     * @param width  Die Ziel-Breite des Bildes in Pixeln.
     * @param height Die Ziel-Höhe des Bildes in Pixeln.
     */
    public void drawImage(Image image, int x, int y, int width, int height) {
        if (image == null) {
            return;
        }
        graphics2D.drawImage(image, x, y, width, height, null);
    }

    /**
     * Zeichnet ein Bild gedreht um seinen eigenen Mittelpunkt.
     *
     * @param image Das zu zeichnende {@link Image}.
     * @param x     Die X-Koordinate der oberen linken Ecke.
     * @param y     Die Y-Koordinate der oberen linken Ecke.
     * @param angle Der Drehwinkel in Grad (z.B. 90.0 für eine Vierteldrehung).
     */
    public void drawImageRotated(Image image, int x, int y, float angle) {
        if (image == null) {
            return;
        }
        // Speichern der alten Transformation
        AffineTransform old = graphics2D.getTransform();

        // Rotation um den Mittelpunkt des Bildes
        int centerX = x + (image.getWidth(null) / 2);
        int centerY = y + (image.getHeight(null) / 2);

        try {
            graphics2D.rotate(Maths.toRadians(angle), centerX, centerY);
            graphics2D.drawImage(image, x, y, null);
        } finally {
            graphics2D.setTransform(old);
        }
    }

    /**
     * Legt einen halbtransparenten Schleier (Overlay) über den gesamten Bildschirm
     * (z.B. für einen Abdunklungs-Effekt im Pausenmenü).
     *
     * @param width  Die Breite des Bildschirms.
     * @param height Die Höhe des Bildschirms.
     * @param alpha  Die Transparenz als Wert von 0.0f (vollständig unsichtbar) bis 1.0f (vollständig schwarz).
     */
    public void fillScreenRectWithAlpha(int width, int height, float alpha) {
        float safeAlpha = Maths.max(0.0f, Maths.min(1.0f, alpha));
        if (width <= 0 || height <= 0 || safeAlpha <= 0.0f) {
            return;
        }

        Graphics2D overlay = (Graphics2D) graphics2D.create();
        try {
            overlay.setTransform(affineTransform);
            overlay.setComposite(AlphaComposite.SrcOver);
            overlay.setColor(new Color(0, 0, 0, Math.round(safeAlpha * 255.0f)));
            overlay.fillRect(0, 0, width, height);
        } finally {
            overlay.dispose();
        }
    }

    /**
     * Zeichnet ein ausgefülltes (schonendes) Rechteck.
     *
     * @param x      X-Koordinate der linken oberen Ecke.
     * @param y      Y-Koordinate der linken oberen Ecke.
     * @param width  Breite des Rechtecks.
     * @param height Höhe des Rechtecks.
     */
    public void fillRect(int x, int y, int width, int height) {
        graphics2D.fillRect(x, y, width, height);
    }

    /**
     * Zeichnet ein ausgefülltes Rechteck, wobei die Position als {@link Vec2}-Vektor übergeben wird.
     *
     * @param position Die Position (X und Y als Vektor).
     * @param width    Breite des Rechtecks.
     * @param height   Höhe des Rechtecks.
     */
    public void fillRect(Vec2 position, int width, int height) {
        if (position == null) {
            return;
        }
        graphics2D.fillRect((int) position.x, (int) position.y, width, height);
    }

    /**
     * Zeichnet nur den Rand (die Umrandung) eines Rechtecks.
     *
     * @param x X-Koordinate der linken oberen Ecke.
     * @param y Y-Koordinate der linken oberen Ecke.
     * @param w Breite des Rechtecks.
     * @param h Höhe des Rechtecks.
     */
    public void drawRect(int x, int y, int w, int h) {
        graphics2D.drawRect(x, y, w, h);
    }

    /**
     * Zeichnet den Rand eines Rechtecks mit präzisen Kommazahlen (Gleitkommazahlen).
     *
     * @param x      X-Koordinate.
     * @param y      Y-Koordinate.
     * @param width  Breite.
     * @param height Höhe.
     */
    public void drawRect(float x, float y, float width, float height) {
        graphics2D.draw(new Rectangle2D.Float(x, y, width, height));
    }

    /**
     * Zeichnet den Rand eines Rechtecks basierend auf einem {@link Rect}-Objekt.
     *
     * @param rect Das Rechteck-Datenobjekt.
     */
    public void drawRect(Rect rect) {
        if (rect == null) {
            return;
        }
        drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Zeichnet den Rand eines Rechtecks, wobei die Position als {@link Vec2} übergeben wird.
     *
     * @param position Position als Vektor.
     * @param width    Breite.
     * @param height   Höhe.
     */
    public void drawRect(Vec2 position, int width, int height) {
        if (position == null) {
            return;
        }
        graphics2D.drawRect((int) position.x, (int) position.y, width, height);
    }

    /**
     * Zeichnet einen ausgefüllten Kreis oder eine Ellipse.
     *
     * @param x X-Koordinate der oberen linken Ecke.
     * @param y Y-Koordinate der oberen linken Ecke.
     * @param w Breite (Durchmesser X).
     * @param h Höhe (Durchmesser Y).
     */
    public void fillCircle(int x, int y, int w, int h) {
        graphics2D.fillOval(x, y, w, h);
    }

    /**
     * Zeichnet die Umrandung eines Kreises oder einer Ellipse.
     *
     * @param x X-Koordinate.
     * @param y Y-Koordinate.
     * @param w Breite.
     * @param h Höhe.
     */
    public void drawCircle(int x, int y, int w, int h) {
        graphics2D.drawOval(x, y, w, h);
    }

    /**
     * Zeichnet einen ausgefüllten Kreis basierend auf seinem echten Mittelpunkt und Radius.
     *
     * @param centerX X-Koordinate des Mittelpunkts.
     * @param centerY Y-Koordinate des Mittelpunkts.
     * @param radius  Der Radius (Abstand vom Mittelpunkt zum Rand).
     */
    public void fillCircle(int centerX, int centerY, int radius) {
        int x = centerX - radius;
        int y = centerY - radius;
        int diameter = radius * 2;

        graphics2D.fillOval(x, y, diameter, diameter);
    }

    /**
     * Zeichnet einen ausgefüllten Kreis über einen Mittelpunkt-Vektor und einen Radius.
     *
     * @param position Der Mittelpunkt als Vektor.
     * @param radius   Der Radius des Kreises.
     */
    public void fillCircle(Vec2 position, float radius) {
        if (position == null) {
            return;
        }
        fillCircle((int) position.x, (int) position.y, (int) radius);
    }

    /**
     * Zeichnet den Umriss eines Kreises basierend auf Mittelpunkt und Radius.
     *
     * @param centerX X-Koordinate des Mittelpunkts.
     * @param centerY Y-Koordinate des Mittelpunkts.
     * @param radius  Der Radius des Kreises.
     */
    public void drawCircle(int centerX, int centerY, int radius) {
        // Umrechnung von Mittelpunkt/Radius auf obere linke Ecke für Java2D
        int x = centerX - radius;
        int y = centerY - radius;
        int diameter = radius * 2;

        graphics2D.drawOval(x, y, diameter, diameter);
    }

    /**
     * Zeichnet den Umriss eines Kreises mit fliessenden Kommazahlen (präziser).
     *
     * @param centerX X-Koordinate des Mittelpunkts.
     * @param centerY Y-Koordinate des Mittelpunkts.
     * @param radius  Radius des Kreises.
     */
    public void drawCircle(float centerX, float centerY, float radius) {
        if (radius < 0.0f) {
            return;
        }
        graphics2D.draw(new Ellipse2D.Float(centerX - radius, centerY - radius, radius * 2.0f, radius * 2.0f));
    }

    /**
     * Zeichnet Text linksbündig auf den Bildschirm.
     *
     * @param text Der anzuzeigende Textstring.
     * @param x    Die X-Koordinate der linken Textkante.
     * @param y    Die Y-Koordinate der Grundlinie (Baseline) des Textes.
     */
    public void drawString(String text, int x, int y) {
        if (text == null) {
            return;
        }
        graphics2D.drawString(text, x, y);
    }

    /**
     * Zeichnet Text linksbündig mit einer spezifischen Schriftart und Schriftgrösse.
     *
     * @param text     Der Text.
     * @param x        X-Koordinate der linken Kante.
     * @param y        Y-Koordinate der Grundlinie.
     * @param fontSize Die gewünschte Schriftgrösse in Pixeln.
     * @param fontName Der Name der Schriftart (z.B. aus der {@link Fonts}-Klasse).
     */
    public void drawString(String text, int x, int y, int fontSize, String fontName) {
        if (text == null) {
            return;
        }
        /*
        if (fontName == null) {
            return;
        }
         */
        Font font = Fonts.getFont(fontName, Font.PLAIN, fontSize);
        graphics2D.setFont(font);
        graphics2D.drawString(text, x, y);
    }

    /**
     * Zeichnet Text so, dass die Angabe {@code centerX} exakt den horizontalen Mittelpunkt
     * des Textes markiert (perfekt für Menü-Einträge oder zentrierte Überschriften).
     *
     * @param text    Der Text.
     * @param centerX Die X-Koordinate der horizontalen Mitte.
     * @param y       Die Y-Koordinate der Grundlinie.
     */
    public void drawCenteredString(String text, int centerX, int y) {
        if (text == null) {
            return;
        }

        FontMetrics metrics = graphics2D.getFontMetrics();
        int x = centerX - metrics.stringWidth(text) / 2;
        graphics2D.drawString(text, x, y);
    }

    /**
     * Zeichnet zentrierten Text mit einer spezifischen Schriftart und Schriftgrösse.
     *
     * @param text     Der Text.
     * @param centerX  Die X-Koordinate der horizontalen Mitte.
     * @param y        Die Y-Koordinate der Grundlinie.
     * @param fontSize Schriftgrösse in Pixeln.
     * @param fontName Name der Schriftart.
     */
    public void drawCenteredString(String text, int centerX, int y, int fontSize, String fontName) {
        if (text == null) {
            return;
        }

        Font font = Fonts.getFont(fontName, Font.PLAIN, fontSize);
        Font previousFont = graphics2D.getFont();

        try {
            graphics2D.setFont(font);
            FontMetrics metrics = graphics2D.getFontMetrics(font);
            int x = centerX - metrics.stringWidth(text) / 2;
            graphics2D.drawString(text, x, y);
        } finally {
            graphics2D.setFont(previousFont);
        }
    }

    /**
     * Zeichnet eine gerade Linie zwischen zwei Punkten (mit ganzzahligen Koordinaten).
     *
     * @param x1 Startpunkt X.
     * @param y1 Startpunkt Y.
     * @param x2 Endpunkt X.
     * @param y2 Endpunkt Y.
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        graphics2D.drawLine(x1, y1, x2, y2);
    }

    /**
     * Zeichnet eine präzise Linie mit Kommazahlen.
     *
     * @param x1 Startpunkt X.
     * @param y1 Startpunkt Y.
     * @param x2 Endpunkt X.
     * @param y2 Endpunkt Y.
     */
    public void drawLine(float x1, float y1, float x2, float y2) {
        graphics2D.draw(new Line2D.Float(x1, y1, x2, y2));
    }

    /**
     * Zeichnet eine Linie zwischen zwei Vektoren.
     *
     * @param start Startpunkt als {@link Vec2}.
     * @param end   Endpunkt als {@link Vec2}.
     */
    public void drawLine(Vec2 start, Vec2 end) {
        if (start == null || end == null) {
            return;
        }
        graphics2D.drawLine((int) start.x, (int) start.y, (int) end.x, (int) end.y);
    }

    /**
     * Zeichnet ein ausgefülltes Polygon (viereckige oder vielseitige geometrische Form).
     *
     * @param xPoints Array mit allen X-Punkten.
     * @param yPoints Array mit allen Y-Punkten.
     * @param nPoints Die Gesamtzahl der Ecken.
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        graphics2D.fillPolygon(xPoints, yPoints, nPoints);
    }

    /**
     * Zeichnet den Umriss eines Polygons.
     *
     * @param xPoints Array mit allen X-Punkten.
     * @param yPoints Array mit allen Y-Punkten.
     * @param nPoints Die Gesamtzahl der Ecken.
     */
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        graphics2D.drawPolygon(xPoints, yPoints, nPoints);
    }

    /**
     * Aktiviert eine 2D-Kamera ({@link Camera2D}). Ab diesem Moment werden alle nachfolgenden
     * Zeichenbefehle automatisch durch das Objektiv dieser Kamera verschoben und skaliert.
     *
     * @param camera2D Das zu aktivierende {@link Camera2D}-Objekt.
     */
    public void applyCamera(Camera2D camera2D) {
        if (camera2D == null) {
            return;
        }

        /*
        Guard 1:
        - falls bereits die gleiche Kamera aktiv ist soll es nichts machen
        - verhindert doppelte Transformation bei kombinierter Nutzung von engine.setCamera() und manueller g.applyCamera() in render()
         */
        if (activeCamera == camera2D) {
            return;
        }

        /*
        Guard 2:
        - falls eine andere Kamera aktiv ist doese zuerst zurücksetzen
        - stellt sicher, dass die Transformationskette nicht korrumpiert wird
         */

        if (activeCamera != null) {
            activeCamera.reset(graphics2D);
        }

        camera2D.setViewport(getWidth(), getHeight());
        activeCamera = camera2D;
        camera2D.apply(graphics2D);
    }

    /**
     * Schaltet die aktive Kamera wieder ab. Nach diesem Aufruf wird wieder direkt
     * in normalen Bildschirmkoordinaten gezeichnet.
     */
    public void resetCamera() {
        if (activeCamera != null) {
            activeCamera.reset(graphics2D);
            activeCamera = null;
        }
    }

    /**
     * Zeichnet ein Rechteck direkt in festen Bildschirmkoordinaten, völlig unabhängig davon,
     * ob gerade eine Kamera aktiv ist oder wohin die Kamera schaut (ideal für HUD/UI).
     *
     * @param x      X-Koordinate auf dem Bildschirm.
     * @param y      Y-Koordinate auf dem Bildschirm.
     * @param width  Breite.
     * @param height Höhe.
     */
    public void drawScreenRect(float x, float y, float width, float height) {
        runInScreenSpace(() ->
                graphics2D.draw(new Rectangle2D.Float(x, y, width, height))
        );
    }

    /**
     * Zeichnet eine Linie direkt in festen Bildschirmkoordinaten (kameraunabhängig).
     *
     * @param x1 Start X.
     * @param y1 Start Y.
     * @param x2 End X.
     * @param y2 End Y.
     */
    public void drawScreenLine(float x1, float y1, float x2, float y2) {
        runInScreenSpace(() ->
                graphics2D.draw(new Line2D.Float(x1, y1, x2, y2))
        );
    }

    /**
     * Zeichnet Text linksbündig in festen Bildschirmkoordinaten (kameraunabhängig).
     *
     * @param text Der Text.
     * @param x    Bildschirm-X.
     * @param y    Bildschirm-Y (Grundlinie).
     */
    public void drawScreenString(String text, int x, int y) {
        if (text == null) {
            return;
        }
        runInScreenSpace(() -> graphics2D.drawString(text, x, y));
    }

    /**
     * Zeichnet Text linksbündig in festen Bildschirmkoordinaten mit eigener Schriftgrösse.
     *
     * @param text     Der Text.
     * @param x        Bildschirm-X.
     * @param y        Bildschirm-Y.
     * @param fontSize Schriftgrösse.
     * @param fontName Schriftart-Name.
     */
    public void drawScreenString(String text, int x, int y, int fontSize, String fontName) {
        if (text == null) {
            return;
        }
        runInScreenSpace(() -> {
            Font font = Fonts.getFont(fontName, Font.PLAIN, fontSize);
            graphics2D.setFont(font);
            graphics2D.drawString(text, x, y);
        });
    }

    /**
     * Zeichnet zentrierten Text in festen Bildschirmkoordinaten (kameraunabhängig).
     *
     * @param text    Der Text.
     * @param centerX Horizontale Mitte auf dem Bildschirm.
     * @param y       Grundlinie auf dem Bildschirm.
     */
    public void drawScreenCenteredString(String text, int centerX, int y) {
        if (text == null) {
            return;
        }

        runInScreenSpace(() -> {
            FontMetrics metrics = graphics2D.getFontMetrics();
            int x = centerX - metrics.stringWidth(text) / 2;
            graphics2D.drawString(text, x, y);
        });
    }

    /**
     * Zeichnet zentrierten Text in festen Bildschirmkoordinaten mit spezifischer Schriftart.
     *
     * @param text     Der Text.
     * @param centerX  Horizontale Mitte.
     * @param y        Grundlinie.
     * @param fontSize Schriftgrösse.
     * @param fontName Schriftart.
     */
    public void drawScreenCenteredString(String text, int centerX, int y, int fontSize, String fontName) {
        if (text == null) {
            return;
        }

        runInScreenSpace(() -> {
            Font font = Fonts.getFont(fontName, Font.PLAIN, fontSize);
            Font previousFont = graphics2D.getFont();

            try {
                graphics2D.setFont(font);
                FontMetrics metrics = graphics2D.getFontMetrics(font);
                int x = centerX - metrics.stringWidth(text) / 2;
                graphics2D.drawString(text, x, y);
            } finally {
                graphics2D.setFont(previousFont);
            }
        });
    }

    /**
     * Interne Hilfsmethode, die kurzzeitig eventuell aktive Kamera-Verschiebungen ausblendet,
     * um Code garantiert im reinen Bildschirmraum (Screen Space) auszuführen.
     *
     * @param drawing Ein Code-Block (Lambda) mit den Zeichenbefehlen.
     */
    private void runInScreenSpace(Runnable drawing) {
        AffineTransform previous = graphics2D.getTransform();
        try {
            graphics2D.setTransform(affineTransform);
            drawing.run();
        } finally {
            graphics2D.setTransform(previous);
        }
    }

    /**
     * Gibt die aktuelle Breite der Zeichenfläche in Pixeln zurück.
     *
     * @return Die Breite in Pixeln.
     */
    public int getWidth() {
        Rectangle rectangle = graphics2D.getClipBounds();
        return rectangle != null ? rectangle.width : 0;
    }

    /**
     * Gibt die aktuelle Höhe der Zeichenfläche in Pixeln zurück.
     *
     * @return Die Höhe in Pixeln.
     */
    public int getHeight() {
        Rectangle rectangle = graphics2D.getClipBounds();
        return rectangle != null ? rectangle.height : 0;
    }

    /**
     * Gibt das rohe Java-{@link Graphics2D}-Objekt zurück.
     * Dient als Fallback für fortgeschrittene Engine-Operationen.
     *
     * @return Das originale Java-Grafikobjekt.
     */
    public Graphics2D raw() {
        return graphics2D;
    }

    /**
     * Rotiert (dreht) das gesamte nachfolgende Zeichen-Koordinatensystem um einen Winkel.
     *
     * @param angle Der Winkel in Grad.
     */
    public void rotate(float angle) {
        graphics2D.rotate(Maths.toRadians(angle));
    }

    /**
     * Verschiebt (translatiert) das gesamte nachfolgende Zeichen-Koordinatensystem um X und Y.
     *
     * @param x Verschiebung auf der X-Achse.
     * @param y Verschiebung auf der Y-Achse.
     */
    public void translate(float x, float y) {
        graphics2D.translate(x, y);
    }
}

