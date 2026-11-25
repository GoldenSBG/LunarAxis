package LunarAxis.physics;

import LunarAxis.math.Maths;
import LunarAxis.math.Vec2;
import LunarAxis.objects.Actor;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Utility-Klasse {@code PhysicsUtils} bündelt mathematische und physikalische Hilfsfunktionen.
 * <p>
 * Sie stellt häufig benötigte Funktionen bereit, um den Code in den Game-Loops der
 * TigerJython-Nutzer sauber und performant zu halten.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class PhysicsUtils {

    /**
     * Privater Konstruktor, um die Instanziierung dieser Utility-Klasse zu verhindern.
     */
    private PhysicsUtils() {
    }

    /**
     * Berechnet die euklidische Distanz zwischen zwei 2D-Vektoren.
     *
     * @param a Erster Punkt als {@link Vec2}
     * @param b Zweiter Punkt als {@link Vec2}
     * @return Die Distanz als Float (0f bei {@code null}-Werten)
     */
    public static float calculateDistance(Vec2 a, Vec2 b) {
        if (a == null || b == null) {
            return 0f;
        }
        return a.distance(b);
    }

    /**
     * Führt eine lineare Interpolation (Lerp) zwischen zwei Werten durch.
     * Sehr nützlich für weiche Kamerafahrten oder sanftes Abbremsen (Friction).
     *
     * @param start Startwert
     * @param end   Zielwert
     * @param t     Interpolations-Faktor (wird automatisch auf 0.0 bis 1.0 begrenzt)
     * @return Der interpolierte Wert.
     */
    public static float lerp(float start, float end, float t) {
        t = clamp(t, 0.0f, 1.0f);
        return start + t * (end - start);
    }

    /**
     * Limitiert (clampet) einen Wert auf einen bestimmten Bereich [min, max].
     * Nützlich, um zu verhindern, dass Objekte aus dem Bildschirm fliegen oder Geschwindigkeiten
     * ein Maximum überschreiten.
     *
     * @param value Der aktuelle Wert
     * @param min   Das absolute Minimum
     * @param max   Das absolute Maximum
     * @return Der limitierte Wert
     * @throws RuntimeException (Lunar-Error) wenn {@code min} oder {@code max} NaN sind oder {@code min > max}
     */
    public static float clamp(float value, float min, float max) {
        if (Float.isNaN(min) || Float.isNaN(max) || min > max) {
            throw Errors.argument(ErrorDetails.of("PhysicsUtils", "Clamp range is invalid")
                    .withActual("min=" + min + ", max=" + max)
                    .withExpected("Bounds without NaN, with min <= max")
                    .withExplanation("Clamping requires an ordered interval with a valid lower and upper boundary"), null);
        }

        return Maths.max(min, Maths.min(max, value));
    }

    /**
     * Berechnet den Winkel (in Radiant) von einem Punkt zu einem anderen.
     * Nützlich, um ein Objekt (z.B. einen Turm oder Spieler) in Richtung eines Ziels auszurichten.
     *
     * <pre>
     * # Verwendung in TigerJython:
     * angle = PhysicsUtils.getAngleTowards(player, mousePos)
     * turret.setRotation(Maths.toDegrees(angle))
     * </pre>
     *
     * @param from Startpunkt (z.B. Position des Turms) als {@link Vec2}
     * @param to   Zielpunkt (z.B. Position der Maus) als {@link Vec2}
     * @return Winkel in Radiant
     */
    public static float getAngleTowards(Vec2 from, Vec2 to) {
        if (from == null || to == null) {
            return 0f;
        }
        return (float) Maths.atan2(to.getY() - from.getY(), to.getX() - from.getX());
    }

    /**
     * Berechnet den Winkel von der Mitte eines {@link Actor} zu einem {@link Vec2}-Punkt.
     *
     * @param from Der Quell-Actor
     * @param to   Der Zielpunkt als {@link Vec2}
     * @return Winkel in Radiant
     */
    public static float getAngleTowards(Actor from, Vec2 to) {
        if (from == null || to == null) return 0f;
        Vec2 fromCenter = new Vec2(from.getX() + from.getWidth() / 2f, from.getY() + from.getHeight() / 2f);
        return getAngleTowards(fromCenter, to);
    }

    /**
     * Berechnet den Winkel von der Mitte eines {@link Actor} zu einem anderen {@link Actor}.
     * Nutzt automatisch die Zentren beider Actors.
     *
     * @param from Quell-Actor
     * @param to   Ziel-Actor
     * @return Winkel in Radiant
     */
    public static float getAngleTowards(Actor from, Actor to) {
        if (from == null || to == null) return 0f;
        Vec2 fromCenter = new Vec2(from.getX() + from.getWidth() / 2f, from.getY() + from.getHeight() / 2f);
        Vec2 toCenter = new Vec2(to.getX() + to.getWidth() / 2f, to.getY() + to.getHeight() / 2f);
        return getAngleTowards(fromCenter, toCenter);
    }

    /**
     * Wandelt einen Winkel (in Radiant) in einen normierten Richtungsvektor um.
     * Nützlich, um Projektile oder Bewegungen exakt in eine bestimmte Richtung zu senden.
     *
     * <pre>
     * # Verwendung in TigerJython:
     * direction = PhysicsUtils.getDirectionVector(angle)
     * bulletVelocity = direction.multiply(500) # 500 Pixel pro Sekunde
     * </pre>
     *
     * @param angle Winkel in Radiant
     * @return Normierter Richtungsvektor (Länge = 1.0) als {@link Vec2}
     */
    public static Vec2 getDirectionVector(float angle) {
        return new Vec2((float) Maths.cos(angle), (float) Maths.sin(angle));
    }
}
