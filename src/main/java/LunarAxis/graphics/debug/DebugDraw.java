package LunarAxis.graphics.debug;

import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.math.Maths;
import LunarAxis.math.Rect;
import LunarAxis.math.Vec2;
import LunarAxis.objects.Actor;
import LunarAxis.physics.RigidBody2D;
import LunarAxis.physics.CollisionResult;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code DebugDraw} stellt eine Sammlung von zustandslosen (statischen) Hilfsmethoden
 * zur Verfügung, um visuelle Debug-Hilfen (wie Hitboxen, Geschwindigkeitsvektoren oder Gitterlinien)
 * auf den Bildschirm zu zeichnen.
 * <p>
 * Nichts wird hierbei automatisch im Hintergrund aktiviert. Du entscheidest als Entwickler
 * ganz gezielt im Render-Durchlauf deines Spiels, welche Debug-Informationen du gerade sehen möchtest.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Hitbox und Geschwindigkeit eines Spielers anzeigen
 * def render(graphics):
 *     # Zeichnet die grüne Umrandung der Kollisionsbox (Hitbox) um den Spieler
 *     DebugDraw.drawActorBounds(graphics, player)
 *
 *     # Zeichnet einen blauen Vektor für die aktuelle Bewegungsrichtung / Geschwindigkeit
 *     DebugDraw.drawActorVelocity(graphics, player)
 *
 *     # Zeigt Text fest auf dem Bildschirm an (kameraunabhängig)
 *     DebugDraw.drawScreenText(graphics, "FPS: " + str(engine.getFpsString()), 20, 30)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class DebugDraw {

    private static final float DEFAULT_VECTOR_SCALE = 1.0f;
    private static final float DEFAULT_VELOCITY_SCALE = 0.1f;
    private static final float DEFAULT_GRAVITY_SCALE = 0.1f;
    private static final float MIN_ARROW_LENGTH = 3.0f;
    private static final float ARROW_HEAD_ANGLE = (float) (Maths.PI / 6.0);

    /**
     * Privater Konstruktor, um zu verhindern, dass Instanzen dieser reinen Hilfsklasse erstellt werden.
     */
    private DebugDraw() {
    }

    /**
     * Zeichnet die physikalische Umrandung (Hitbox/Bounds) eines {@link Actor}s
     * in der Standardfarbe (Grün).
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der {@link Actor}, dessen Grenzen gezeichnet werden sollen.
     */
    public static void drawActorBounds(Graphics graphics, Actor actor) {
        drawActorBounds(graphics, actor, Colors.GREEN);
    }

    /**
     * Zeichnet die physikalische Umrandung eines {@link Actor}s in einer frei wählbaren Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der Actor.
    3     * @param colors   Die gewünschte Farbe für die Umrandung.
     */
    public static void drawActorBounds(Graphics graphics, Actor actor, Colors colors) {
        if (graphics == null || actor == null) {
            return;
        }
        drawRect(graphics, actor.getPhysicsBounds(), colors);
    }

    /**
     * Zeichnet ein einzelnes Rechteck ({@link Rect}) in einer bestimmten Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param rect     Das zu zeichnende Rechteck.
     * @param color    Die Farbe des Rechtecks.
     */
    public static void drawRect(Graphics  graphics, Rect rect, Colors color) {
        if (graphics == null || rect == null) {
            return;
        }
        graphics.setColor(color != null ? color : Colors.GREEN);
        graphics.drawRect(rect);
    }

    /**
     * Markiert den exakten Ursprung (die obere linke Ecke, x und y) eines {@link Actor}s
     * mit einem kleinen Fadenkreuz in Gelb.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der Actor.
     */
    public static void drawActorOrigin(Graphics graphics, Actor actor) {
        drawActorOrigin(graphics, actor, Colors.YELLOW);
    }

    /**
     * Markiert den Ursprung eines {@link Actor}s mit einer frei wählbaren Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der Actor.
     * @param color    Die Farbe des Fadenkreuzes.
     */
    public static void drawActorOrigin(Graphics graphics, Actor actor, Colors color) {
        if (graphics == null || actor == null) {
            return;
        }
        float x = actor.getX();
        float y = actor.getY();
        float markerSize = 5.0f;

        graphics.setColor(color != null ? color : Colors.YELLOW);
        graphics.drawLine(x - markerSize, y, x + markerSize, y);
        graphics.drawLine(x, y - markerSize, x, y + markerSize);
    }

    /**
     * Markiert den exakten Mittelpunkt der tatsächlichen Kollisions-Hitbox eines {@link Actor}s
     * mit einem kleinen Kreis in Cyan.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der Actor.
     */
    public static void drawActorCenter(Graphics graphics, Actor actor) {
        drawActorCenter(graphics, actor, Colors.CYAN);
    }

    /**
     * Markiert den Mittelpunkt der Hitbox eines Actors mit einer frei wählbaren Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der Actor.
     * @param color    Die Farbe des Zentrums-Punktes.
     */
    public static void drawActorCenter(Graphics graphics, Actor actor, Colors color) {
        if (graphics == null || actor == null) {
            return;
        }

        Rect bounds = actor.getPhysicsBounds();
        graphics.setColor(color != null ? color : Colors.CYAN);
        graphics.drawCircle(bounds.getCenterX(), bounds.getCenterY(), 3.0f);
    }

    /**
     * Zeichnet ein vollständiges Koordinaten-Gitter (Grid) in der Welt.
     * Nützlich, um die Orientierung in einem grossen Level zu behalten.
     *
     * @param graphics  Das Zeichen-Werkzeug.
     * @param x         Start-X-Koordinate des Gitters.
     * @param y         Start-Y-Koordinate des Gitters.
     * @param width     Gesamtbreite des Gitters.
     * @param height    Gesamthöhe des Gitters.
     * @param cellSize  Die Grösse einer einzelnen Kachel (Zelle) in Pixeln.
     * @param color     Die Linienfarbe des Gitters.
     *
     * @throws RuntimeException (Lunar-Error) Wenn unendliche oder ungültige Werte übergeben werden, die eine Endlosschleife verursachen könnten.
     */
    public static void drawGrid(Graphics graphics, float x, float y, float width, float height, float cellSize, Colors color) {
        if (graphics == null || width <= 0.0f || height <= 0.0f || cellSize <= 0.0f) {
            return;
        }

        if (!Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(width)
                || !Float.isFinite(height) || !Float.isFinite(cellSize)) {
            throw Errors.argument(ErrorDetails.of("DebugDraw", "Grid coordinates and dimensions must be finite")
                    .withActual("x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", cellSize=" + cellSize)
                    .withExpected("Finite coordinates, dimensions and cell size")
                    .withExplanation("Non-finite values cannot define a drawable grid and may prevent the drawing loops from terminating"), null);
        }

        float right = x + width;
        float bottom = y + height;

        if (!Float.isFinite(right) || !Float.isFinite(bottom)) {
            throw Errors.argument(ErrorDetails.of("DebugDraw", "Grid boundaries exceed the float range")
                    .withActual("x=" + x + ", y=" + y + ", width=" + width + ", height=" + height)
                    .withExpected("Finite results for x + width and y + height")
                    .withExplanation("An infinite boundary would prevent the grid loop from terminating"), null);
        }

        graphics.setColor(color != null ? color : Colors.DARK_GRAY);

        for (float gridX = x; gridX <= right;) {
            graphics.drawLine(gridX, y, gridX, bottom);
            if (gridX == right) {
                break;
            }

            float nextX = gridX + cellSize;
            if (nextX <= gridX) {
                throw Errors.argument(ErrorDetails.of("DebugDraw", "Grid cell size cannot advance the horizontal coordinate")
                        .withActual("gridX=" + gridX + ", cellSize=" + cellSize)
                        .withExpected("A cell size that produces a larger float coordinate")
                        .withExplanation("Float rounding leaves the coordinate unchanged and would cause an endless drawing loop"), null);
            }
            gridX = nextX;
        }

        for (float gridY = y; gridY <= bottom;) {
            graphics.drawLine(x, gridY, right, gridY);
            if (gridY == bottom) {
                break;
            }

            float nextY = gridY + cellSize;
            if (nextY <= gridY) {
                throw Errors.argument(ErrorDetails.of("DebugDraw", "Grid cell size cannot advance the vertical coordinate")
                        .withActual("gridY=" + gridY + ", cellSize=" + cellSize)
                        .withExpected("A cell size that produces a larger float coordinate")
                        .withExplanation("Float rounding leaves the coordinate unchanged and would cause an endless drawing loop"), null);
            }
            gridY = nextY;
        }
    }

    /**
     * Zeichnet ein Kreuz aus horizontalen und vertikalen Hauptachsen um einen bestimmten Ursprung.
     *
     * @param graphics   Das Zeichen-Werkzeug.
     * @param originX    X-Mittelpunkt der Achsen.
     * @param originY    Y-Mittelpunkt der Achsen.
     * @param halfLength Die halbe Länge der Achsen-Arme in Pixeln.
     * @param color      Die Farbe der Achsen.
     */
    public static void drawAxes(Graphics graphics, float originX, float originY, float halfLength, Colors color) {
        if (graphics == null || halfLength <= 0.0f) {
            return;
        }

        graphics.setColor(color != null ? color : Colors.WHITE);
        graphics.drawLine(originX - halfLength, originY, originX + halfLength, originY);
        graphics.drawLine(originX, originY - halfLength, originX, originY + halfLength);
    }

    /**
     * Zeichnet einen einfachen Kreis mit Standardfarbe (Orange).
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param centerX  X-Koordinate des Mittelpunkts.
     * @param centerY  Y-Koordinate des Mittelpunkts.
     * @param radius   Der Radius des Kreises.
     */
    public static void drawCircle(Graphics graphics, float centerX, float centerY, float radius) {
        drawCircle(graphics, centerX, centerY, radius, Colors.ORANGE);
    }

    /**
     * Zeichnet einen Kreis mit anpassbarer Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param centerX  X-Mittelpunkt.
     * @param centerY  Y-Mittelpunkt.
     * @param radius   Radius.
     * @param color    Farbe.
     */
    public static void drawCircle(Graphics graphics, float centerX, float centerY, float radius, Colors color) {
        if (graphics == null || radius <= 0.0f) {
            return;
        }

        graphics.setColor(color != null ? color : Colors.ORANGE);
        graphics.drawCircle(centerX, centerY, radius);
    }

    /**
     * Zeichnet einen Vektor-Pfeil ab einem bestimmten Startpunkt mit Standard-Skalierung (1.0) und Gelb.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param start    Der Startpunkt ({@link Vec2}) des Pfeils in der Welt.
     * @param vector   Der Vektor (Richtung und Länge).
     */
    public static void drawVectorArrow(Graphics graphics, Vec2 start, Vec2 vector) {
        drawVectorArrow(graphics, start, vector, DEFAULT_VECTOR_SCALE, Colors.YELLOW, null, null);
    }

    /**
     * Zeichnet einen Vektor-Pfeil mit benutzerdefinierter Skalierung und Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param start    Startpunkt.
     * @param vector   Vektor.
     * @param scale    Skalierungsfaktor (um den Vektor auf dem Bildschirm grösser/kleiner darzustellen).
     * @param color    Pfeilfarbe.
     */
    public static void drawVectorArrow(Graphics graphics, Vec2 start, Vec2 vector, float scale, Colors color) {
        drawVectorArrow(graphics, start, vector, scale, color, null, null);
    }

    /**
     * Zeichnet einen Vektor-Pfeil inklusive eines optionalen Text-Labels nahe seiner Spitze.
     *
     * @param graphics   Das Zeichen-Werkzeug.
     * @param start      Startpunkt des Pfeils.
     * @param vector     Der darzustellende Vektor.
     * @param scale      Skalierungsfaktor.
     * @param color      Farbe des Pfeils.
     * @param label      Ein optionaler Text, der an der Pfeilspitze angezeigt wird (kann {@code null} sein).
     * @param labelColor Die Farbe des Text-Labels.
     */
    public static void drawVectorArrow(Graphics graphics, Vec2 start, Vec2 vector, float scale, Colors color, String label, Colors labelColor) {
        if (graphics == null || start == null || vector == null || scale < 0.0f) {
            return;
        }

        float dx = vector.getX() * scale;
        float dy = vector.getY() * scale;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (isInvalid(start.getX()) || isInvalid(start.getY()) || isInvalid(dx)
                || isInvalid(dy) || Double.isNaN(length)
                || Double.isInfinite(length) || length < MIN_ARROW_LENGTH) {
            return;
        }

        float endX = start.getX() + dx;
        float endY = start.getY() + dy;
        Colors arrowColor = color != null ? color : Colors.YELLOW;

        graphics.setColor(arrowColor);
        graphics.drawLine(start.getX(), start.getY(), endX, endY);

        double angle = Math.atan2(dy, dx);
        float headLength = (float) Math.min(10.0, length * 0.35);

        float leftX = (float) (endX - headLength * Math.cos(angle - ARROW_HEAD_ANGLE));
        float leftY = (float) (endY - headLength * Math.sin(angle - ARROW_HEAD_ANGLE));
        float rightX = (float) (endX - headLength * Math.cos(angle + ARROW_HEAD_ANGLE));
        float rightY = (float) (endY - headLength * Math.sin(angle + ARROW_HEAD_ANGLE));

        graphics.drawLine(endX, endY, leftX, leftY);
        graphics.drawLine(endX, endY, rightX, rightY);

        if (label != null && !label.trim().isEmpty()) {
            graphics.setColor(labelColor != null ? labelColor : arrowColor);
            graphics.drawString(label, Math.round(endX + 5.0f), Math.round(endY - 5.0f));
        }
    }

    /**
     * Zeichnet die aktuelle Geschwindigkeit (Velocity) eines {@link Actor}s als Pfeil,
     * ausgehend von der Mitte seiner Hitbox. Wenn der Actor einen {@link RigidBody2D} besitzt,
     * wird dessen physikalische Geschwindigkeit verwendet.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der Actor, dessen Geschwindigkeit visualisiert werden soll.
     */
    public static void drawActorVelocity(Graphics graphics, Actor actor) {
        drawActorVelocity(graphics, actor, DEFAULT_VELOCITY_SCALE, Colors.CYAN, null, null);
    }

    /**
     * Zeichnet die Actor-Geschwindigkeit mit eigener Skalierung und Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param actor    Der Actor.
     * @param scale    Skalierungsfaktor für den Pfeil.
     * @param color    Farbe des Pfeils.
     */
    public static void drawActorVelocity(Graphics graphics, Actor actor, float scale, Colors color) {
        drawActorVelocity(graphics, actor, scale, color, null, null);
    }

    /**
     * Zeichnet die Actor-Geschwindigkeit mit Skalierung, Farbe und optionalem Text-Label.
     *
     * @param graphics   Das Zeichen-Werkzeug.
     * @param actor      Der Actor.
     * @param scale      Skalierung.
     * @param color      Pfeilfarbe.
     * @param label      Optionaler Text (z.B. "Speed").
     * @param labelColor Farbe des Textes.
     */
    public static void drawActorVelocity(Graphics graphics, Actor actor, float scale, Colors color, String label, Colors labelColor) {
        if (graphics == null || actor == null) {
            return;
        }

        Vec2 velocity;
        RigidBody2D body = actor.getRigidBody();

        if (body != null) {
            velocity = body.getVelocity();
        } else {
            velocity = actor.getVelocity();
        }

        if (velocity == null) {
            return;
        }

        Rect bounds = actor.getPhysicsBounds();
        Vec2 start = new Vec2(bounds.getCenterX(), bounds.getCenterY());

        drawVectorArrow(graphics, start, velocity, scale, color != null ? color : Colors.CYAN, label, labelColor);
    }

    /**
     * Zeichnet einen vom Aufrufer übergebenen Gravitätsvektor als Pfeil.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param start    Der Startpunkt des Vektors in der Welt.
     * @param gravity  Der Gravitationsvektor.
     */
    public static void drawGravity(Graphics graphics, Vec2 start, Vec2 gravity) {
        drawGravity(graphics, start, gravity, DEFAULT_GRAVITY_SCALE, Colors.ORANGE, null, null);
    }

    /**
     * Zeichnet einen Gravitätsvektor mit erweiterten Anpassungsoptionen.
     *
     * @param graphics   Das Zeichen-Werkzeug.
     * @param start      Startpunkt.
     * @param gravity    Gravitationsvektor.
     * @param scale      Skalierung.
     * @param color      Farbe.
     * @param label      Optionales Text-Label.
     * @param labelColor Farbe des Labels.
     */
    public static void drawGravity(Graphics graphics, Vec2 start, Vec2 gravity, float scale, Colors color, String label, Colors labelColor) {
        drawVectorArrow(graphics, start, gravity, scale, color != null ? color : Colors.ORANGE, label, labelColor);
    }

    /**
     * Visualisiert die Details einer physikalischen Kollision (MTV - Minimum Translation Vector,
     * Korrekturrichtung und exakter Kontaktpunkt).
     *
     * @param graphics    Das Zeichen-Werkzeug.
     * @param firstBounds Die Grenzen des ersten beteiligten Objekts.
     * @param result      Das {@link CollisionResult}-Ergebnis der Kollisionsberechnung.
     */
    public static void drawCollisionResult(Graphics graphics, Rect firstBounds, CollisionResult result) {
        drawCollisionResult(graphics, firstBounds, result, Colors.ORANGE, Colors.RED, Colors.MAGENTA);
    }

    /**
     * Visualisiert ein Kollisionsergebnis mit individuell anpassbaren Farben für Vektor, Normale und Kontaktpunkt.
     *
     * @param graphics    Das Zeichen-Werkzeug.
     * @param firstBounds Die Grenzen des ersten Objekts.
     * @param result      Das Kollisionsergebnis.
     * @param mtvColor    Farbe für den Verschiebe-Vektor (MTV).
     * @param normalColor Farbe für die Kollisionsnormale.
     * @param contactColor Farbe für den Kontaktpunkt.
     */
    public static void drawCollisionResult(Graphics graphics, Rect firstBounds, CollisionResult result, Colors mtvColor, Colors normalColor, Colors contactColor) {
        if (graphics == null || firstBounds == null || result == null) {
            return;
        }

        Vec2 depth = result.getDepth();
        if (depth == null) {
            return;
        }

        Vec2 center = new Vec2(firstBounds.getCenterX(), firstBounds.getCenterY());
        drawVectorArrow(graphics, center, depth, 1.0f, mtvColor != null ? mtvColor : Colors.ORANGE);

        Vec2 contactPoint = result.getContactPoint();
        if (contactPoint != null) {
            graphics.setColor(contactColor != null ? contactColor : Colors.MAGENTA);
            graphics.drawCircle(contactPoint.getX(), contactPoint.getY(), 3.0f);

            Vec2 normal = result.getNormal();
            drawVectorArrow(graphics, contactPoint, normal, 24.0f, normalColor != null ? normalColor : Colors.RED);
        }
    }

    /**
     * Zeichnet Text, der fest auf dem Bildschirm klebt (HUD/UI-Elemente).
     * Die Welt-Kamera hat keinen Einfluss auf seine Position.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param text     Der anzuzeigende Text.
     * @param x        Bildschirm-X-Koordinate in Pixeln.
     * @param y        Bildschirm-Y-Koordinate in Pixeln.
     */
    public static void drawScreenText(Graphics graphics, String text, int x, int y) {
        drawScreenText(graphics, text, x, y, Colors.WHITE);
    }

    /**
     * Zeichnet bildschirmfesten Text in einer bestimmten Farbe.
     *
     * @param graphics Das Zeichen-Werkzeug.
     * @param text     Der Text.
     * @param x        Bildschirm-X.
     * @param y        Bildschirm-Y.
     * @param color    Textfarbe.
     */
    public static void drawScreenText(Graphics graphics, String text, int x, int y, Colors color) {
        if (graphics == null || text == null) {
            return;
        }

        graphics.setColor(color != null ? color : Colors.WHITE);
        graphics.drawScreenString(text, x, y);
    }

    /**
     * Interne Hilfsmethode, die prüft, ob eine Fliesskommazahl ungültig ist (NaN oder Unendlich).
     *
     * @param value Der zu prüfende Wert.
     * @return {@code true}, falls der Wert ungültig ist.
     */
    private static boolean isInvalid(float value) {
        return Float.isNaN(value) || Float.isInfinite(value);
    }
}
