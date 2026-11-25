package LunarAxis.physics;

import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code Gravity} kapselt Konstanten und Logiken zur Simulation von Schwerkraft.
 * <p>
 * Sie ist ideal, um Objekte (Actors) mit einer realistischen Fallgeschwindigkeit auszustatten,
 * die framerate-unabhängig durch {@code deltaTime} berechnet wird.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Gravity {

    /**
     * Standard-Erdanziehungskraft (Meter pro Sekunde im Quadrat)
     */
    public static final float EARTH = 9.81f;

    /**
     * Privater Konstruktor, um zu verhindern, dass diese Hilfsklasse instanziiert wird.
     */
    private Gravity() {
    }

    /**
     * Wendet Schwerkraft auf einen 2D-Geschwindigkeits-Vektor ({@link Vec2}) an.
     * <p>
     * Ein positiver {@code gravityValue} zieht das Objekt nach unten auf der Y-Achse.
     * </p>
     *
     * @param velocity     Der 2D-Geschwindigkeits-Vektor des Objekts
     * @param gravityValue Die Stärke der Gravitation
     * @param deltaTime    Die vergangene Zeit seit dem letzten Frame in Sekunden
     * @throws RuntimeException (Lunar-Error) wenn {@code gravityValue} oder {@code deltaTime} ungültig sind
     */
    public static void apply(Vec2 velocity, float gravityValue, float deltaTime) {
        if (velocity == null) {
            return;
        }
        if (!Float.isFinite(gravityValue)) {
            throw Errors.argument(ErrorDetails.of("Gravity", "Gravity value must be finite")
                    .withActual(gravityValue)
                    .withExpected("A finite acceleration value")
                    .withExplanation("NaN and infinite acceleration values would invalidate the vertical velocity"), null);
        }
        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("Gravity", "Gravity update time must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite time interval")
                    .withExplanation("NaN and infinite time intervals cannot be used to integrate acceleration"), null);
        }

        float newY = velocity.getY() + (gravityValue * deltaTime);
        velocity.setY(newY);
    }
}
