package LunarAxis.physics;

import LunarAxis.math.Maths;
import LunarAxis.math.Rect;
import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code Collision} ist die Kernklasse für alle Kollisionsberechnungen in der Engine.
 * <p>
 * Sie abstrahiert die komplexe Mathematik, sodass TigerJython-Nutzer mit einfachen
 * Methodenaufrufen wie {@code checkAABB()} oder {@code checkSphere()} Kollisionen testen können.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Collision {

    /**
     * Privater Konstruktor, um zu verhindern, dass diese Hilfsklasse instanziiert wird.
     */
    private Collision() {
    }

    /**
     * Führt einen 2D AABB (Axis-Aligned Bounding Box) Kollisionstest durch.
     * Nutzt die {@link Rect}-Klasse für eine saubere Kapselung.
     *
     * @param a Das erste Rechteck (Bounding Box)
     * @param b Das zweite Rechteck (Bounding Box)
     * @return {@code true}, wenn eine Überschneidung stattfindet, sonst {@code false}.
     */
    public static boolean checkAABB(Rect a, Rect b) {
        if (a == null || b == null) {
            return false;
        }
        return a.intersects(b);
    }

    /**
     * Führt einen kreisbasierten Kollisionstest in 2D durch.
     *
     * @param centerA Zentrum des ersten Kreises als {@link Vec2}
     * @param radiusA Radius des ersten Kreises
     * @param centerB Zentrum des zweiten Kreises
     * @param radiusB Radius des zweiten Kreises
     * @return {@code true}, wenn sich die Kreise schneiden, sonst {@code false}.
     * @throws RuntimeException (Lunar-Error) wenn einer der Radien ungültig ist.
     */
    public static boolean checkCircle(Vec2 centerA, float radiusA, Vec2 centerB, float radiusB) {
        if (centerA == null || centerB == null) {
            return false;
        }
        if (!Float.isFinite(radiusA) || radiusA < 0.0f) {
            throw Errors.argument(ErrorDetails.of("Collision", "First circle radius is invalid")
                    .withActual(radiusA)
                    .withExpected("A finite radius greater than or equal to 0")
                    .withExplanation("A circle radius must describe a non-negative finite distance from its center"), null);
        }
        if (!Float.isFinite(radiusB) || radiusB < 0.0f) {
            throw Errors.argument(ErrorDetails.of("Collision", "Second circle radius is invalid")
                    .withActual(radiusB)
                    .withExpected("A finite radius greater than or equal to 0")
                    .withExplanation("A circle radius must describe a non-negative finite distance from its center"), null);
        }

        return centerA.distance(centerB) < radiusA + radiusB;
    }


    /**
     * Berechnet den Minimum Translation Vector (MTV) für zwei Rechtecke.
     * Gibt {@code null} zurück, wenn keine Kollision vorliegt.
     *
     * <pre>
     * # Verwendung in TigerJython:
     * depth = Collision.getIntersectionDepth(playerRect, wallRect)
     * if depth is not None:
     *     player.setX(player.getX() + depth.getX())
     *     player.setY(player.getY() + depth.getY())
     * </pre>
     *
     * @param a Erstes {@link Rect}
     * @param b Zweites {@link Rect}
     * @return Ein {@link Vec2}-Vektor mit der Eindringtiefe oder {@code null}.
     */
    public static Vec2 getIntersectionDepth(Rect a, Rect b) {
        if (a == null || b == null) {
            return null;
        }
        return a.getIntersectionDepth(b);
    }

    /**
     * Ermittelt die Kollisionsdetails inklusive Eindringtiefe und Kollisionsrichtung (wie {@code getIntersectionDepth}).
     *
     * <pre>
     * # Verwendung in TigerJython:
     * result = Collision.resolveCollision(playerRect, wallRect)
     * if result is not None:
     *     if result.isTop():
     *         print("Von Oben gekommen!")
     *     player.setX(player.getX() + result.getDepth().getX())
     *     player.setY(player.getY() + result.getDepth().getY())
     * </pre>
     *
     * @param a Erstes {@link Rect}
     * @param b Zweites {@link Rect}
     * @return Ein {@link CollisionResult}-Objekt oder {@code null}.
     */
    public static CollisionResult resolveCollision(Rect a, Rect b) {
        if (a == null || b == null) {
            return null;
        }
        return a.resolveCollision(b);
    }

    /**
     * Führt eine 2D-Kreis-Kollision mit direkter Auflösung (Push-Back) durch.
     * Berechnet und gibt den notwendigen Verschiebungsvektor zurück (oder {@code null} bei keiner Kollision).
     *
     * @param centerA Zentrum von Kreis A
     * @param radiusA Radius von Kreis A
     * @param centerB Zentrum von Kreis B
     * @param radiusB Radius von Kreis B
     * @return Ein {@link Vec2} zur Kollisionsauflösung oder {@code null}.
     */
    public static Vec2 resolveCircleCollision(Vec2 centerA, float radiusA, Vec2 centerB, float radiusB) {
        if (!checkCircle(centerA, radiusA, centerB, radiusB)) {
            return null;
        }

        //Richtung von A nach B
        float dx = centerB.getX() - centerA.getX();
        float dy = centerB.getY() - centerA.getY();
        float distance = (float) Maths.sqrt(dx * dx + dy * dy);
        float overlap = radiusA + radiusB - distance;

        if (distance == 0.0f) {
            return new Vec2(overlap, 0.0f);
        }

        //berechne Überlappung
        float pushX = (dx / distance) * overlap;
        float pushY = (dy / distance) * overlap;

        /*
         * Vorzeichenkonvention der bisherigen Implementierung:
         * Vektor zeigt von Mittelpunkt A in Richtung Mittelpunkt B
         */
        return new Vec2(pushX, pushY);
    }

}