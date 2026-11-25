package LunarAxis.graphics.particle;

import LunarAxis.math.Maths;
import LunarAxis.math.Vec2;
import LunarAxis.util.LunarRandom;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code ParticleUtils} ist eine mathematische Hilfsklasse (Utility-Klasse)
 * voller nützlicher Werkzeuge für die Partikel-Berechnung.
 * <p>
 * Sie stellt Methoden zur Verfügung, um zufällige Richtungen zu bestimmen, Vektoren aus Winkeln
 * zu errechnen oder zufällige Startpunkte innerhalb von geometrischen Formen (wie Kreisen oder Rechtecken)
 * für Partikel-Emissionen zu generieren.
 * </p>
 * <p>
 * Da dies eine reine Hilfsklasse ist, muss sie nicht instanziiert werden. Alle Methoden sind statisch.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticleUtils {

    /**
     * Privater Konstruktor, um zu verhindern, dass Instanzen dieser Hilfsklasse erstellt werden.
     */
    private ParticleUtils() {
    }

    /**
     * Erzeugt einen zufälligen Richtungsvektor mit einer Länge von 1.0 (Einheitsvektor)
     * in einen beliebigen Winkel von 0 bis 360 Grad.
     *
     * @return Ein zufälliger Richtungsvektor als {@link Vec2}.
     */
    public static Vec2 randomDirection() {
        float angle = LunarRandom.global().nextFloat(0f, 360f);
        return directionFromAngle(angle);
    }

    /**
     * Wandelt einen spezifischen Winkel (in Grad) in einen Richtungsvektor um.
     *
     * @param angle Der Winkel in Grad.
     * @return Der entsprechende Richtungsvektor als {@link Vec2}.
     */
    public static Vec2 directionFromAngle(float angle) {
        double radians = Maths.toRadians(angle);
        return new Vec2((float) Maths.cos(radians), (float) Maths.sin(radians));
    }

    /**
     * Erzeugt einen zufälligen Punkt gleichmässig verteilt <i>innerhalb</i> einer Kreisfläche.
     * <p>
     * <i>Mathematischer Trick:</i> Die Verwendung der Quadratwurzel (sqrt) bei der Distanzberechnung
     * verhindert, dass sich alle Punkte in der Mitte knäueln, und sorgt für eine echte, gleichmässige Flächenverteilung.
     * </p>
     *
     * @param center Der Mittelpunkt des Kreises als {@link Vec2}.
     * @param radius Der Radius des Kreises.
     * @return Eine zufällige Position innerhalb des Kreises.
     * @throws RuntimeException (Lunar-Error) Wenn der Mittelpunkt {@code null} ist.
     */
    public static Vec2 randomPointInCircle(Vec2 center, float radius) {
        if (center == null) {
            throw Errors.argument(ErrorDetails.of("ParticleUtils", "Circle center cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the circle center")
                    .withExplanation("The generated offset must be added to an existing center position"), null);
        }

        radius = Maths.max(0f, radius);
        float angle = LunarRandom.global().nextFloat(0f, 360f);

        /*
         * sqrt sorgt dafür, dass die Punkte gleichmässig über die Fläche verteilt werden
         */
        float distance = (float) Maths.sqrt(LunarRandom.global().nextFloat(0f, 1f)) * radius;
        Vec2 direction = directionFromAngle(angle);

        return new Vec2(center.x + direction.x * distance, center.y + direction.y * distance);
    }


    /**
     * Erzeugt einen zufälligen Punkt direkt auf der äusseren Kreislinie (dem Rand eines Kreises).
     *
     * @param center Der Mittelpunkt des Kreises als {@link Vec2}.
     * @param radius Der Radius des Kreises.
     * @return Eine zufällige Position auf dem Kreisrand.
     * @throws RuntimeException (Lunar-Error) Wenn der Mittelpunkt {@code null} ist.
     */
    public static Vec2 randomPointOnCircle(Vec2 center, float radius) {
        if (center == null) {
            throw Errors.argument(ErrorDetails.of("ParticleUtils", "Circle center cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the circle center")
                    .withExplanation("The generated point on the circumference is positioned relative to the circle center"), null);
        }

        radius = Maths.max(0f, radius);

        float angle = LunarRandom.global().nextFloat(0f, 360f);
        Vec2 direction = directionFromAngle(angle);

        return new Vec2(center.x + direction.x * radius, center.y + direction.y * radius);
    }


    /**
     * Erzeugt einen zufälligen Punkt innerhalb einer rechteckigen Fläche.
     * <p>
     * Der übergebene Vektor repräsentiert dabei den Mittelpunkt des Rechtecks.
     * </p>
     *
     * @param center Der Mittelpunkt des Rechtecks als {@link Vec2}.
     * @param width  Die Gesamtbreite des Rechtecks.
     * @param height Die Gesamthöhe des Rechtecks.
     * @return Eine zufällige Position innerhalb des Rechtecks.
     * @throws RuntimeException (Lunar-Error) Wenn der Mittelpunkt {@code null} ist.
     */
    public static Vec2 randomPointInArea(Vec2 center, float width, float height) {
        if (center == null) {
            throw Errors.argument(ErrorDetails.of("ParticleUtils", "Area center cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the rectangle center")
                    .withExplanation("The random coordinate ranges are calculated relative to the rectangle center"), null);
        }

        width = Maths.max(0f, width);
        height = Maths.max(0f, height);

        float x = LunarRandom.global().nextFloat(center.x - width / 2f, center.x + width / 2f);
        float y = LunarRandom.global().nextFloat(center.y - height / 2f, center.y + height / 2f);

        return new Vec2(x, y);
    }

    /**
     * Multipliziert einen Richtungsvektor mit einer Geschwindigkeit,
     * um einen fertigen Geschwindigkeits-Vektor zu erhalten.
     *
     * @param direction Der Richtungsvektor (idealerweise ein Einheitsvektor).
     * @param speed     Die gewünschte Geschwindigkeit (Skalar).
     * @return Der berechnete Geschwindigkeitsvektor als {@link Vec2}.
     * @throws RuntimeException (Lunar-Error) Wenn die Richtung {@code null} ist.
     */
    public static Vec2 velocityFromDirection(Vec2 direction, float speed) {
        if (direction == null) {
            throw Errors.argument(ErrorDetails.of("ParticleUtils", "Direction cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the direction")
                    .withExplanation("Velocity is calculated by multiplying both direction components by the supplied speed"), null);
        }
        return new Vec2(direction.x * speed, direction.y * speed);
    }

    /**
     * Begrenzt (clampet) einen Zahlenwert auf ein bestimmtes Minimum und Maximum.
     * Falls der Wert kleiner als {@code min} ist, wird {@code min} zurückgegeben;
     * falls er größer als {@code max} ist, wird {@code max} zurückgegeben.
     *
     * @param value Der zu prüfende Wert.
     * @param min   Die erlaubte Untergrenze.
     * @param max   Die erlaubte Obergrenze.
     * @return Der begrenzte Wert.
     */
    public static float clamp(float value, float min, float max) {
        return Maths.max(min, Maths.min(max, value));
    }
}
