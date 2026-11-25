package LunarAxis.graphics.particle;

import LunarAxis.graphics.Colors;
import LunarAxis.math.Maths;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code ParticlePreset} stellt vordefinierte Vorlagen (Presets) für Partikel bereit.
 * <p>
 * Anstatt jedes Partikel mühsam einzeln mit Grösse, Farbe, Geschwindigkeit und Schwerkraft zu konfigurieren,
 * greift man hier auf fertige Stile wie Feuer, Rauch, Explosionen, Funken, Regen oder Schnee zurück.
 * Dies erleichtert die Erstellung visueller Effekte in Spielen erheblich.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Ein vordefiniertes Feuer-Preset in einem Emitter verwenden
 * fireEmitter = ParticleEmitter(myParticleSystem, ParticlePreset.FIRE)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticlePreset {

    private final ParticleShape shape;
    private final Colors color;

    private final float size;
    private final float life;
    private final float speed;
    private final float spread;
    private final float gravity;  // NEU [@version 1.0.9 - 18.08.26]: Schwerkraft-Einfluss (positiv = fällt, negativ = steigt)
    private final float baseAngle; // NEU: Basis-Richtung in Grad (0° = rechts, 90° = unten, -90° = oben)

    /**
     * Erstellt ein neues Partikel-Preset mit allen physikalischen und optischen Eigenschaften.
     *
     * @param shape     Die geometrische Form des Partikels ({@link ParticleShape}).
     * @param color     Die Farbe des Partikels ({@link Colors}).
     * @param size      Die Grösse in Pixeln.
     * @param life      Die Lebensdauer in Sekunden.
     * @param speed     Die Anfangsgeschwindigkeit.
     * @param spread    Die Streuung (Streuwinkel in Grad).
     * @param gravity   Der Schwerkraft-Faktor (positiv = zieht nach unten, negativ = steigt nach oben).
     * @param baseAngle Die Basis-Flugrichtung in Grad (0° = rechts, 90° = unten, -90° = oben).
     *
     * @throws RuntimeException (Lunar-Error) Wenn Form oder Farbe {@code null} sind.
     */
    public ParticlePreset(ParticleShape shape, Colors color, float size, float life, float speed, float spread, float gravity, float baseAngle) {
        if (shape == null) {
            throw Errors.argument(ErrorDetails.of("ParticlePreset", "Shape cannot be null")
                    .withActual(null)
                    .withExpected("A ParticleShape value")
                    .withExplanation("Particles created from this preset require a shape for rendering"), null);
        }
        if (color == null) {
            throw Errors.argument(ErrorDetails.of("ParticlePreset", "Color cannot be null")
                    .withActual(null)
                    .withExpected("An existing Colors value")
                    .withExplanation("Particles created from this preset require a color for visibility checks and rendering"), null);
        }

        this.shape = shape;
        this.color = color;
        this.size = Maths.max(0f, size);
        this.life = Maths.max(0f, life);
        this.speed = Maths.max(0f, speed);
        this.spread = Maths.max(0f, spread);
        this.gravity = gravity;
        this.baseAngle = baseAngle;
    }

    /**
     * Gibt die Form des Presets zurück.
     *
     * @return Das {@link ParticleShape}-Objekt.
     */
    public ParticleShape getShape() {
        return shape;
    }

    /**
     * Gibt die Farbe des Presets zurück.
     *
     * @return Das {@link Colors}-Objekt.
     */
    public Colors getColor() {
        return color;
    }

    /**
     * Gibt die Standard-Grösse der Partikel zurück.
     *
     * @return Die Grösse in Pixeln.
     */
    public float getSize() {
        return size;
    }

    /**
     * Gibt die Lebensdauer der Partikel zurück.
     *
     * @return Die Lebensdauer in Sekunden.
     */
    public float getLife() {
        return life;
    }

    /**
     * Gibt die Geschwindigkeit der Partikel zurück.
     *
     * @return Die Geschwindigkeit.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Gibt den Streuwinkel (Spread) zurück.
     *
     * @return Die Streuung in Grad.
     */
    public float getSpread() {
        return spread;
    }

    /**
     * Gibt den Schwerkraft-Einfluss zurück.
     *
     * @return Der Gravitationswert.
     */
    public float getGravity() {
            return gravity;
    }

    /**
     * Gibt die Basis-Richtung (Winkel) des Ausstosses zurück.
     *
     * @return Der Winkel in Grad.
     */
    public float getBaseAngle() {
        return baseAngle;
    }

    /**
     * Vordefiniertes Preset für aufsteigendes Feuer (steigt nach oben).
     */
    public static final ParticlePreset FIRE = new ParticlePreset(
            ParticleShape.CIRCLE, Colors.ORANGE, 4f, 1.0f, 80f, 25f, -50.0f, -90f);

    /**
     * Vordefiniertes Preset für dichten Rauch (steigt langsam auf).
     */
    public static final ParticlePreset SMOKE = new ParticlePreset(
            ParticleShape.CIRCLE, Colors.GRAY, 6f, 2.5f, 25f, 45f, -20.0f, -90f);

    // Damit fliegt es ca. 0.4s nach oben und fällt dann 1.1s wie ein Brunnen nach unten!
    /**
     * Vordefiniertes Preset für eine Explosion (schiesst nach oben und fällt herab).
     */
    public static final ParticlePreset EXPLOSION = new ParticlePreset(
            ParticleShape.CIRCLE, Colors.YELLOW, 5f, 1.5f, 100f, 360f, 250.0f, 0f);

    /**
     * Vordefiniertes Preset für helle Funken.
     */
    public static final ParticlePreset SPARK = new ParticlePreset(
            ParticleShape.PIXEL, Colors.YELLOW, 2f, 0.8f, 250f, 360f, 150.0f, 0f);

    /**
     * Vordefiniertes Preset für schnellen Regen (fällt zügig nach unten).
     */
    public static final ParticlePreset RAIN = new ParticlePreset(
            ParticleShape.LINE, Colors.CYAN, 3f, 1.5f, 220f, 10f, 50.0f, 90f);

    /**
     * Vordefiniertes Preset für sanft schwebenden Schnee.
     */
    public static final ParticlePreset SNOW = new ParticlePreset(
            ParticleShape.CIRCLE, Colors.WHITE, 4f, 4.0f, 20f, 40f, 10.0f, 90f);
}
