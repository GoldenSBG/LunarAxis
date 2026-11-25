package LunarAxis.graphics;

import LunarAxis.math.Maths;
import LunarAxis.math.Vec2;
import LunarAxis.objects.Actor;
import LunarAxis.physics.PhysicsUtils;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Die Klasse {@code Camera2D} simuliert ein Kamera-Objektiv für deine Spielwelt.
 * <p>
 * In grösseren Spielen (wie Super Mario oder GTA) ist die Spielwelt viel grösser
 * als das kleine Fenster, in das du hineinschaust. Anstatt die ganze Welt zu bewegen,
 * bewegst du diese Kamera über die Welt.
 * Sie ermöglicht sanftes Scrollen, Zoomen und weiches Verfolgen von Objekten (z.B. dem Spieler).
 * </p>
 * <p>
 * <b>Achtung Koordinatensysteme:</b> Wenn du eine Kamera benutzt, gibt es einen Unterschied zwischen
 * <i>Weltkoordinaten</i> (wo ein Baum im riesigen Level steht) und <i>Bildschirmkoordinaten</i>
 * (wo du mit der Maus auf den echten Monitor klickst). Die Kamera hilft dir, diese umzurechnen.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Kamera erstellen und Spieler folgen lassen
 * camera = Camera2D()
 *
 * # Dem Spieler weich folgen (0.1 = leicht träge, 1.0 = sofort)
 * camera.follow(player, 0.1)
 *
 * # Auf 150% Grösse zoomen
 * camera.setZoom(1.5)
 *
 * # Beim Zeichnen (in der render-Methode):
 * graphics.applyCamera(camera)
 * # ... ab hier alles zeichnen, was in der Welt steht (Gegner, Bäume)
 * graphics.resetCamera()
 * # ... ab hier alles zeichnen, was fest am Bildschirm klebt (Lebensanzeige, Punkte)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Camera2D {

    private float x, y;

    /**
     * Zoom-Faktor
     * 1.0 = normale Grösse
     * 2.0 = doppelte Grösse
     * 0.5 = halbe Grösse
     */
    private float zoom = 1.0f;

    // Grösse sichtbaren bereichs
    private int viewportWidth, viewportHeight;

    private float rotation = 0.0f;

    /**
     * Begrenzung des Kamerabereichs
     */
    private float minX = Float.NEGATIVE_INFINITY;
    private float maxX = Float.POSITIVE_INFINITY;
    private float minY = Float.NEGATIVE_INFINITY;
    private float maxY = Float.POSITIVE_INFINITY;

    /**
     * Gespeicherte Transformation für Reset
     */
    private AffineTransform savedTransform;

    /**
     * Erstellt eine neue Kamera, die standardmässig exakt auf den Welt-Nullpunkt (0, 0) blickt.
     */
    public Camera2D() {
        this(0, 0);
    }

    /**
     * Erstellt eine neue Kamera auf einer spezifischen Position.
     *
     * @param x Die X-Koordinate in der Welt, auf die die Kamera mittig blickt.
     * @param y Die Y-Koordinate in der Welt, auf die die Kamera mittig blickt.
     */
    public Camera2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Erstellt eine neue Kamera auf einer spezifischen Position mit einem vorgegebenen Zoom.
     *
     * @param x    Die X-Koordinate in der Welt.
     * @param y    Die Y-Koordinate in der Welt.
     * @param zoom Der Zoomfaktor (1.0 = normal, 2.0 = ran gezoomt).
     */
    public Camera2D(float x, float y, float zoom) {
        this.x = x;
        this.y = y;
        setZoom(zoom);
    }


    /**
     * Wendet die Kamera-Verschiebung (und den Zoom) auf das Java-Zeichensystem an.
     * Alles, was nach dem Aufruf dieser Methode gezeichnet wird, verschiebt sich
     * passend zur Kameraposition.
     *
     * <p>
     * <b>Wichtig:</b> Schülerinnen und Schüler sollten diese Methode nicht direkt nutzen,
     * sondern stattdessen den komfortableren Weg über
     * {@link Graphics#applyCamera(Camera2D)} in ihrer Render-Methode gehen.
     * </p>
     *
     * @param g2d Der interne Zeichenkontext von Java.
     */
    public void apply(Graphics2D g2d) {
        if (g2d == null) {
            return;
        }

        //aktuelle transformation speichern
        savedTransform = g2d.getTransform();

        /*
         * Der Kameramittelpunkt wird auf die Bildschirmmitte gelegt
         * Anschliessend wird die Welt relativ zur Kamera verschoben und skaliert
         */
        g2d.translate(viewportWidth / 2.0, viewportHeight / 2.0);
        g2d.scale(zoom, zoom);
        g2d.rotate(Maths.toRadians(rotation));
        g2d.translate(-x, -y);
    }

    /**
     * Setzt die Verschiebung wieder zurück, sodass danach wieder ganz normal
     * fixiert am Fensterrand gezeichnet wird (z.B. für Menüs oder Punktzahlen).
     *
     * <pre>
     * # Verwendung in TigerJython (indirekt über Graphics):
     * graphics.applyCamera(camera)
     * # ... Welt zeichnen ...
     * graphics.resetCamera()
     * </pre>
     *
     * @param g2d Der interne Zeichenkontext.
     */
    public void reset(Graphics2D g2d) {
        if (g2d == null || savedTransform == null) {
            return;
        }
        g2d.setTransform(savedTransform);
        savedTransform = null;
    }

    /**
     * Teilt der Kamera mit, wie gross das Spielfenster ist.
     * Wird normalerweise automatisch von der Engine beim Setup aufgerufen.
     *
     * @param width  Die Breite des Fensters in Pixeln.
     * @param height Die Höhe des Fensters in Pixeln.
     */
    public void setViewport(int width, int height) {
        if (width < 0 || height < 0) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Viewport size must not be negative")
                    .withActual("width=" + width + ", height=" + height)
                    .withExpected("Width and height greater than or equal to 0")
                    .withExplanation("The viewport dimensions describe the available drawing area and cannot be negative"), null);
        }

        viewportWidth = width;
        viewportHeight = height;
        clampPosition();
    }

    /**
     * Lässt die Kamera einem Zielpunkt in der Welt folgen.
     * Wenn der lerpFactor klein ist, zieht die Kamera weich hinterher,
     * anstatt ruckartig an die neue Position zu springen.
     *
     * @param targetX    Die X-Koordinate des Ziels.
     * @param targetY    Die Y-Koordinate des Ziels.
     * @param lerpFactor Wie schnell die Kamera aufholt (0.0 = bewegt sich gar nicht, 1.0 = springt sofort hin). Ein guter Wert ist oft z.B. 0.05.
     */
    public void follow(float targetX, float targetY, float lerpFactor) {
        lerpFactor = PhysicsUtils.clamp(lerpFactor, 0.0f, 1.0f);
        x = PhysicsUtils.lerp(x, targetX, lerpFactor);
        y = PhysicsUtils.lerp(y, targetY, lerpFactor);

        clampPosition();
    }

    /**
     * Lässt die Kamera bequem einem Spielfigur-Objekt ({@link Actor}) folgen.
     * Die Kamera zielt dabei automatisch auf den Mittelpunkt des Actors.
     *
     * <pre>
     * # Beispiel Tigerjython
     * camera.follow(player, 0.05)
     * </pre>
     *
     * @param target     Der zu verfolgende Actor.
     * @param lerpFactor Wie weich die Kamera folgt (0.0 bis 1.0).
     */
    public void follow(Actor target, float lerpFactor) {
        if (target == null) {
            return;
        }
        follow(target.getX() + target.getWidth() / 2.0f, target.getY() + target.getHeight() / 2.0f, lerpFactor);
    }

    /**
     * Lässt die Kamera einem Vektor (Punkt im Raum) folgen.
     *
     * @param target     Der Punkt ({@link Vec2}), auf den die Kamera zielen soll.
     * @param lerpFactor Die Weichheit der Verfolgung.
     */
    public void follow(Vec2 target, float lerpFactor) {
        if (target == null) {
            return;
        }
        follow(target.getX(), target.getY(), lerpFactor);
    }

    /**
     * Setzt die Kamera sofort und ohne weiche Zwischenschritte auf eine neue Position.
     *
     * @param targetX Die neue X-Position in der Welt.
     * @param targetY Die neue Y-Position in der Welt.
     */
    public void snapTo(float targetX, float targetY) {
        this.x = targetX;
        this.y = targetY;
        clampPosition();
    }

    /**
     * Lässt die Kamera sofort und ohne weiche Zwischenschritte auf die Mitte
     * eines bestimmten Actors springen.
     *
     * @param target Der Actor, auf den die Kamera gerichtet werden soll.
     */
    public void snapTo(Actor target) {
        if (target == null) {
            return;
        }
        snapTo(target.getX() + target.getWidth() / 2.0f, target.getY() + target.getHeight() / 2.0f);
    }

    /**
     * Rechnet eine Klick-Position auf dem echten Bildschirm (Monitor) in eine
     * Position innerhalb deiner grossen Spielwelt um.
     * <p>
     * <b>Beispiel:</b> Wenn du in einem Strategiespiel auf dem Bildschirm auf
     * X=100 klickst, die Kamera aber schon 5000 Pixel nach rechts gefahren ist,
     * rechnet diese Methode automatisch aus, dass du in der Spielwelt eigentlich
     * auf X=5100 geklickt hast.
     * </p>
     *
     * <pre>
     * # Verwendung in TigerJython:
     * mouseX = input.getMouseX()
     * mouseY = input.getMouseY()
     * worldPos = camera.screenToWorld(mouseX, mouseY)
     * # Jetzt weisst du, wo der Spieler in der Welt hingeklickt hat
     * </pre>
     *
     * @param screenX Die X-Koordinate des Mauszeigers auf dem Bildschirm.
     * @param screenY Die Y-Koordinate des Mauszeigers auf dem Bildschirm.
     * @return Ein {@link Vec2}-Vektor mit den berechneten Koordinaten in der Spielwelt.
     */
    public Vec2 screenToWorld(float screenX, float screenY) {
        //Verschiebung um Viewport-mitte und Entskalierung
        float dx = (screenX - viewportWidth / 2.0f) / zoom;
        float dy = (screenY - viewportHeight / 2.0f) / zoom;

        //inverse rotation
        float rad = (float) Maths.toRadians(rotation);
        float cos = (float) Maths.cos(rad);
        float sin = (float) Maths.sin(rad);

        //Verschiebung um Kameraposition
        float worldX = x + dx * cos + dy * sin;
        float worldY = y - dx * sin + dy * cos;

        return new Vec2(worldX, worldY);
    }

    /**
     * Das Gegenteil von {@link #screenToWorld(float, float)}.
     * Nimmt einen Punkt aus der riesigen Spielwelt und rechnet aus, wo genau
     * (auf welchen Pixel) er aktuell in deinem kleinen Spielfenster gezeichnet werden würde.
     * <p>
     * Nützlich z.B. wenn man einen Lebensbalken, der immer am Bildschirmrand klebt,
     * genau über dem Kopf eines flüchtenden Gegners (der in der Welt steht) anzeigen will.
     * </p>
     *
     * @param worldX Die X-Position in der Welt.
     * @param worldY Die Y-Position in der Welt.
     * @return Die X/Y-Position als Pixel auf dem Monitor (Fenster).
     */
    public Vec2 worldToScreen(float worldX, float worldY) {
        //Verschiebung um Kameraposition
        float dx = worldX - x;
        float dy = worldY - y;

        //rotation
        float rad = (float) Maths.toRadians(rotation);
        float cos = (float) Maths.cos(rad);
        float sin = (float) Maths.sin(rad);

        //skalierung und verschiebung um viewport-mitte
        float screenX = viewportWidth / 2.0f + zoom * (dx * cos - dy * sin);
        float screenY = viewportHeight / 2.0f + zoom * (dx * sin + dy * cos);

        return new Vec2(screenX, screenY);
    }


    /**
     * Macht exakt dasselbe wie {@link #worldToScreen(float, float)},
     * akzeptiert als Eingabe aber direkt ein Vektor-Objekt.
     *
     * @param worldPos Die Weltposition als {@link Vec2}.
     * @return Die Bildschirmposition.
     */
    public Vec2 worldToScreen(Vec2 worldPos) {
        if (worldPos == null) {
            return new Vec2(0, 0);
        }
        return worldToScreen(worldPos.getX(), worldPos.getY());
    }

    /**
     * Prüft, ob ein einzelner Punkt in der Welt durch das Kameraobjektiv momentan
     * überhaupt auf dem Monitor sichtbar ist.
     *
     * @param worldX X-Koordinate in der Welt.
     * @param worldY Y-Koordinate in der Welt.
     * @return {@code true} wenn er im sichtbaren Bildausschnitt liegt.
     */
    public boolean isVisible(float worldX, float worldY) {
        float halfWidth = viewportWidth / (2.0f * zoom);
        float halfHeight = viewportHeight / (2.0f * zoom);

        return worldX >= x - halfWidth && worldX <= x + halfWidth && worldY >= y - halfHeight && worldY <= y + halfHeight;
    }

    /**
     * Prüft, ob ein Weltpunkt im sichtbaren Bereich liegt, erlaubt aber einen
     * zusätzlichen Toleranz-Rand (Margin) ausserhalb des sichtbaren Bereichs.
     * <p>
     * Das ist in der professionellen Spieleentwicklung eine Technik namens "Culling":
     * Objekte, die die Kamera gerade eh nicht sieht, werden erst gar nicht gezeichnet.
     * </p>
     *
     * @param worldX X-Koordinate in der Welt.
     * @param worldY Y-Koordinate in der Welt.
     * @param margin Wie viele Pixel ausserhalb des Kamerabildes noch als "sichtbar" gewertet werden.
     * @return {@code true} wenn der Punkt (inkl. Rand) im Blickfeld ist.
     */
    public boolean isVisible(float worldX, float worldY, float margin) {
        float halfW = viewportWidth / (2.0f * zoom) + margin;
        float halfH = viewportHeight / (2.0f * zoom) + margin;
        return worldX > x - halfW && worldX < x + halfW &&
                worldY > y - halfH && worldY < y + halfH;
    }

    /**
     * Prüft, ob ein gesamter Actor (Spielfigur/Objekt) zumindest teilweise
     * durch das Kameraobjektiv sichtbar ist.
     *
     * @param actor Der zu überprüfende {@link Actor}.
     * @return {@code true}, wenn sich die Rechtecke (Hitboxen) von Kamera und Actor überlappen.
     */
    public boolean isVisible(Actor actor) {
        if (actor == null) {
            return false;
        }
        float left = actor.getX();
        float right = actor.getX() + actor.getWidth();
        float top = actor.getY();
        float bottom = actor.getY() + actor.getHeight();
        float halfWidth = viewportWidth / (2.0f * zoom);
        float halfHeight = viewportHeight / (2.0f * zoom);

        return right >= x - halfWidth &&
                left <= x + halfWidth &&
                bottom >= y - halfHeight &&
                top <= y + halfHeight;

    }

    /**
     * Setzt unsichtbare Grenzen für die Kamera, ähnlich wie Wände in einem Level.
     * Die Kamera wird diesen Bereich niemals verlassen, selbst wenn du ihr sagst,
     * sie soll noch weiter dem Spieler folgen.
     *
     * <pre>
     * # Beispiel Tigerjython - Das Level ist 1600x1200 gross
     * camera.setBounds(0, 0, 1600, 1200)
     * </pre>
     *
     * @param minX Der am weitesten links liegende erlaubte Punkt.
     * @param minY Der am weitesten oben liegende erlaubte Punkt.
     * @param maxX Der am weitesten rechts liegende erlaubte Punkt.
     * @param maxY Der am weitesten unten liegende erlaubte Punkt.
     */
    public void setBounds(float minX, float minY, float maxX, float maxY) {
        if (Float.isNaN(minX) || Float.isNaN(minY) || Float.isNaN(maxX) || Float.isNaN(maxY)) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Camera bounds must not contain NaN")
                    .withActual("minX=" + minX + ", minY=" + minY + ", maxX=" + maxX + ", maxY=" + maxY)
                    .withExpected("Ordered numeric bounds without NaN")
                    .withExplanation("NaN cannot define a camera boundary because comparisons with NaN are always false"), null);
        }
        if (minX > maxX || minY > maxY) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Minimum bounds must not be greater than maximum bounds")
                    .withActual("minX=" + minX + ", minY=" + minY + ", maxX=" + maxX + ", maxY=" + maxY)
                    .withExpected("minX <= maxX and minY <= maxY")
                    .withExplanation("Each minimum boundary must precede or equal its corresponding maximum boundary"), null);
        }


        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        clampPosition();
    }

    /**
     * Löscht alle mit {@link #setBounds(float, float, float, float)} gesetzten Grenzen wieder,
     * sodass die Kamera sich ins Unendliche bewegen kann.
     */
    public void clearBounds() {
        minX = Float.NEGATIVE_INFINITY;
        minY = Float.NEGATIVE_INFINITY;
        maxX = Float.POSITIVE_INFINITY;
        maxY = Float.POSITIVE_INFINITY;
    }

    /**
     * Interne Hilfsmethode. Sie prüft bei jeder Bewegung, ob die Kamera gerade versucht,
     * über die gesetzten Grenzen (Bounds) hinauszufahren, und schiebt sie notfalls
     * hart zurück an den Rand.
     * <p>
     * Besonderheit: Wenn das Level (die Bounds) kleiner ist als das Kamerafenster,
     * wird das Level stattdessen exakt in die Bildschirmmitte zentriert.
     * </p>
     */
    private void clampPosition() {
        if (viewportWidth == 0 || viewportHeight == 0) {
            return;
        }

        //halbe Breite & Höhe von sichtbarem Bereich (in Weltkoord)
        float halfWidth = viewportWidth / (2.0f * zoom);
        float halfHeight = viewportHeight / (2.0f * zoom);

        //Grenze für Kameramittelpunkt
        float effectiveMinX = minX + halfWidth;
        float effectiveMaxX = maxX - halfWidth;
        float effectiveMinY = minY + halfHeight;
        float effectiveMaxY = maxY - halfHeight;

        //Level ist kleiner als Viewport => Kamera zentrieren
        if (effectiveMinX > effectiveMaxX) {
            x = (minX + maxX) / 2.0f;
        } else {
            x = PhysicsUtils.clamp(x, effectiveMinX, effectiveMaxX);
        }

        if (effectiveMinY > effectiveMaxY) {
            y = (minY + maxY) / 2.0f;
        } else {
            y = PhysicsUtils.clamp(y, effectiveMinY, effectiveMaxY);
        }
    }

    /**
     * Ändert den Zoomfaktor nicht schlagartig, sondern zoomt über Zeit "weich" hinein oder heraus.
     *
     * <pre>
     * # Beispiel Tigerjython
     * camera.zoomTo(2.0, 0.05)  # Weich auf 200% Grösse zoomen
     * </pre>
     *
     * @param targetZoom Der gewünschte finale Zoomfaktor (z.B. 2.0).
     * @param lerpFactor Wie schnell der Zoom sich anpasst (0.0 bis 1.0).
     */
    public void zoomTo(float targetZoom, float lerpFactor) {
        if (!Float.isFinite(targetZoom) || targetZoom <= 0f) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Target zoom must be finite and greater than 0")
                    .withActual(targetZoom)
                    .withExpected("A finite target zoom greater than 0")
                    .withExplanation("Zoom interpolation requires a valid target scale"), null);
        }
        if (Float.isNaN(lerpFactor)) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Zoom interpolation factor must not be NaN")
                    .withActual(lerpFactor)
                    .withExpected("A numeric interpolation factor that can be clamped to the range from 0 to 1")
                    .withExplanation("NaN would propagate through interpolation and invalidate the camera zoom"), null);
        }

        lerpFactor = PhysicsUtils.clamp(lerpFactor, 0.0f, 1.0f);
        float nextZoom = PhysicsUtils.lerp(zoom, targetZoom, lerpFactor);

        if (!Float.isFinite(nextZoom) || nextZoom <= 0f) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Zoom interpolation produced an invalid result")
                    .withActual("zoom=" + zoom + ", targetZoom=" + targetZoom + ", lerpFactor=" + lerpFactor + ", result=" + nextZoom)
                    .withExpected("A finite interpolation result greater than 0")
                    .withExplanation("The calculated value cannot be stored as a valid camera zoom"), null);
        }
        zoom = nextZoom;
    }

    /**
     * Multipliziert den aktuellen Zoom der Kamera mit dem übergebenen Faktor.
     * Wenn der aktuelle Zoom z.B. 2.0 ist und du {@code zoomBy(2.0)} aufrufst,
     * wird der neue Zoom 4.0.
     *
     * @param factor Der Multiplikator für den Zoom.
     */
    public void zoomBy(float factor) {
        if (!Float.isFinite(factor) || factor <= 0f) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Zoom factor must be finite and greater than 0")
                    .withActual(factor)
                    .withExpected("A finite multiplication factor greater than 0")
                    .withExplanation("The factor must preserve a positive finite camera zoom"), null);
        }
        setZoom(zoom * factor);
    }

    /**
     * Gibt die aktuelle X-Koordinate in der Welt zurück, auf die die Kamera momentan genau mittig schaut.
     *
     * @return Die X-Position.
     */
    public float getX() {
        return x;
    }

    /**
     * Setzt die Kamera hart auf eine neue X-Position.
     *
     * @param x Die neue X-Position.
     */
    public void setX(float x) {
        this.x = x;
        clampPosition();
    }

    /**
     * Gibt die aktuelle Y-Koordinate in der Welt zurück, auf die die Kamera momentan genau mittig schaut.
     *
     * @return Die Y-Position.
     */
    public float getY() {
        return y;
    }

    /**
     * Setzt die Kamera hart auf eine neue Y-Position.
     *
     * @param y Die neue Y-Position.
     */
    public void setY(float y) {
        this.y = y;
        clampPosition();
    }

    /**
     * Gibt den aktuellen Zoomfaktor der Kamera zurück.
     *
     * @return Der Zoom (1.0 ist Standard).
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Setzt den Zoomfaktor der Kamera sofort auf einen neuen Wert.
     *
     * @param zoom Der neue Zoomfaktor (muss grösser als 0 sein).
     */
    public void setZoom(float zoom) {
        if (!Float.isFinite(zoom) || zoom <= 0f) {
            throw Errors.argument(ErrorDetails.of("Camera2D", "Zoom must be finite and greater than 0")
                    .withActual(zoom)
                    .withExpected("A finite zoom greater than 0")
                    .withExplanation("Camera scaling and inverse coordinate conversion require a finite non-zero zoom"), null);
        }

        this.zoom = zoom;
        clampPosition();
    }

    /**
     * Gibt zurück, um wie viel Grad das Kamera-Bild aktuell gedreht ist.
     *
     * @return Die Drehung in Grad.
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Rotiert (dreht) das gesamte sichtbare Kamerabild.
     *
     * @param rotation Der gewünschte Drehwinkel in Grad (z.B. 90, 180).
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     * Gibt die eingestellte innere Breite des Fensters zurück.
     *
     * @return Fensterbreite in Pixeln.
     */
    public int getViewportWidth() {
        return viewportWidth;
    }

    /**
     * Gibt die eingestellte innere Höhe des Fensters zurück.
     *
     * @return Fensterhöhe in Pixeln.
     */
    public int getViewportHeight() {
        return viewportHeight;
    }


    /**
     * Gibt eine einfache Textdarstellung der Kamera zurück (hilfreich bei der Fehlersuche / Print).
     */
    @Override
    public String toString() {
        return String.format("Camera2D(x=%.1f, y=%.1f, zoom=%.2f)", x, y, zoom);
    }
}
