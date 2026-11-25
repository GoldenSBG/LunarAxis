package LunarAxis.graphics.particle;

import LunarAxis.graphics.Colors;
import LunarAxis.math.Maths;
import LunarAxis.math.Vec2;
import LunarAxis.util.LunarRandom;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Der {@code ParticleBuilder} ist ein Erbauer (Builder) für Partikel.
 * <p>
 * Er nutzt das sogenannte <i>Method Chaining</i> (das Aneinanderhängen von Methodenaufrufen),
 * um auf sehr lesbare Weise komplexe Partikel mit individuellen Eigenschaften (Position, Geschwindigkeit,
 * Farbe, Grösse, Presets) zu konfigurieren und schliesslich über ein {@link ParticleSystem} abzufeuern.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Eine Funken-Explosion mit dem Builder erzeugen
 * ParticleBuilder.create(myParticleSystem) \
 *     .position(player.getX(), player.getY()) \
 *     .velocity(10, -20) \
 *     .size(5.0) \
 *     .color(Colors.ORANGE) \
 *     .life(1.5) \
 *     .emit()
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticleBuilder {

    private final ParticleSystem particleSystem;

    private float x, y;
    private float velocityX, velocityY;
    private float size;
    private float life;
    private float rotation;
    private float angularVelocity;
    private float gravity;

    private Colors color;
    private ParticleShape shape;
    private ParticlePreset preset;

    /**
     * Erstellt einen neuen Partikel-Builder, der an ein spezifisches Partikel-System gekoppelt ist.
     *
     * @param particleSystem Das {@link ParticleSystem}, in dem das Partikel nach dem Erstellen verwaltet wird.
     * @throws RuntimeException (Lunar-Error) Wenn das übergebene Partikel-System {@code null} ist.
     */
    public ParticleBuilder(ParticleSystem particleSystem) {
        if (particleSystem == null) {
            throw Errors.argument(ErrorDetails.of("ParticleBuilder", "Particle system cannot be null")
                    .withActual(null)
                    .withExpected("An existing ParticleSystem")
                    .withExplanation("The builder needs a particle system to allocate and register emitted particles"), null);
        }
        this.particleSystem = particleSystem;

        reset();
    }

    /**
     * Statische Fabrik-Methode (Factory Method), um einen neuen Builder bequem zu starten.
     *
     * @param particleSystem Das zugehörige Partikel-System.
     * @return Ein frisch initialisierter {@code ParticleBuilder}.
     */
    public static ParticleBuilder create(ParticleSystem particleSystem) {
        return new ParticleBuilder(particleSystem);
    }

    /**
     * Legt die Startposition des Partikels über separate X- und Y-Koordinaten fest.
     *
     * @param x X-Koordinate in der Welt.
     * @param y Y-Koordinate in der Welt.
     * @return Der Builder selbst (für Method Chaining).
     */
    public ParticleBuilder position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Legt die Startposition des Partikels über einen {@link Vec2}-Vektor fest.
     *
     * @param position Die Position als Vektor.
     * @return Der Builder.
     * @throws RuntimeException (Lunar-Error) Wenn der Vektor {@code null} ist.
     */
    public ParticleBuilder position(Vec2 position) {
        if (position == null) {
            throw Errors.argument(ErrorDetails.of("ParticleBuilder", "Position cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the particle position")
                    .withExplanation("The builder must read both coordinates from the supplied position"), null);
        }
        this.x = position.x;
        this.y = position.y;
        return this;
    }

    /**
     * Legt die Anfangsgeschwindigkeit des Partikels (in X- und Y-Richtung) fest.
     *
     * @param x Geschwindigkeit auf der X-Achse.
     * @param y Geschwindigkeit auf der Y-Achse.
     * @return Der Builder.
     */
    public ParticleBuilder velocity(float x, float y) {
        this.velocityX = x;
        this.velocityY = y;
        return this;
    }

    /**
     * Legt die Anfangsgeschwindigkeit über einen Vektor fest.
     *
     * @param velocity Die Geschwindigkeit als {@link Vec2}.
     * @return Der Builder.
     * @throws RuntimeException (Lunar-Error) Wenn der Vektor {@code null} ist.
     */
    public ParticleBuilder velocity(Vec2 velocity) {
        if (velocity == null) {
            throw Errors.argument(ErrorDetails.of("ParticleBuilder", "Velocity cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the particle velocity")
                    .withExplanation("Use a zero vector for a stationary particle instead of null"), null);
        }
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        return this;
    }

    /**
     * Bestimmt die Grösse des Partikels in Pixeln. Negative Werte werden automatisch zu 0 korrigiert.
     *
     * @param size Die Grösse.
     * @return Der Builder.
     */
    public ParticleBuilder size(float size) {
        this.size = Maths.max(0f, size);
        return this;
    }

    /**
     * Bestimmt die Lebensdauer des Partikels in Sekunden.
     *
     * @param life Lebensdauer.
     * @return Der Builder.
     */
    public ParticleBuilder life(float life) {
        this.life = Maths.max(0f, life);
        return this;
    }

    /**
     * Bestimmt die Farbe des Partikels.
     *
     * @param color Ein {@link Colors}-Objekt.
     * @return Der Builder.
     * @throws RuntimeException (Lunar-Error) Wenn die Farbe {@code null} ist.
     */
    public ParticleBuilder color(Colors color) {
        if (color == null) {
            throw Errors.argument(ErrorDetails.of("ParticleBuilder", "Color cannot be null")
                    .withActual(null)
                    .withExpected("An existing Colors value")
                    .withExplanation("Particle visibility and rendering require a color with an alpha value"), null);
        }
        this.color = color;
        return this;
    }

    /**
     * Bestimmt die geometrische Form des Partikels (z.B. Kreis oder Quadrat).
     *
     * @param shape Ein {@link ParticleShape}-Wert.
     * @return Der Builder.
     * @throws RuntimeException (Lunar-Error) Wenn die Form {@code null} ist.
     */
    public ParticleBuilder shape(ParticleShape shape) {
        if (shape == null) {
            throw Errors.argument(ErrorDetails.of("ParticleBuilder", "Shape cannot be null")
                    .withActual(null)
                    .withExpected("A ParticleShape value")
                    .withExplanation("The particle renderer selects its drawing operation from the particle shape"), null);
        }
        this.shape = shape;
        return this;
    }

    /**
     * Legt den initialen Rotationswinkel des Partikels fest.
     *
     * @param rotation Winkel in Grad.
     * @return Der Builder.
     */
    public ParticleBuilder rotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Legt fest, wie schnell sich das Partikel um sich selbst dreht.
     *
     * @param angularVelocity Drehgeschwindigkeit in Grad pro Sekunde.
     * @return Der Builder.
     */
    public ParticleBuilder angularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
        return this;
    }

    /**
     * Bestimmt die Stärke der Schwerkraft, die auf dieses spezifische Partikel wirkt.
     *
     * @param gravity Schwerkraft-Wert.
     * @return Der Builder.
     */
    public ParticleBuilder gravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    /**
     * Wendet eine vordefinierte Vorlage (Preset) auf das Partikel an (z.B. für Standard-Rauch oder Funken).
     *
     * @param preset Ein {@link ParticlePreset}-Objekt.
     * @return Der Builder.
     * @throws RuntimeException (Lunar-Error) Wenn das Preset {@code null} ist.
     */
    public ParticleBuilder preset(ParticlePreset preset) {
        if (preset == null) {
            throw Errors.argument(ErrorDetails.of("ParticleBuilder", "Preset cannot be null")
                    .withActual(null)
                    .withExpected("An existing ParticlePreset")
                    .withExplanation("The builder copies particle properties from the supplied preset"), null);
        }
        this.preset = preset;

        size = preset.getSize();
        life = preset.getLife();
        color = preset.getColor();
        shape = preset.getShape();
        this.gravity = preset.getGravity();

        return this;
    }

    /**
     * Schliesst die Konfiguration ab, erzeugt das Partikel im zugehörigen {@link ParticleSystem}
     * und feuert es in die Spielwelt ab.
     *
     * @return Das fertig erstellte und aktive {@link Particle}-Objekt.
     */
    public Particle emit() {
        Particle particle = particleSystem.createParticle();

        particle.position.set(x, y);

        float finalVelocityX = velocityX;
        float finalVelocityY = velocityY;

        if (preset != null && velocityX == 0f && velocityY == 0f) {
            float angle = calculateSpreadAngle(preset.getBaseAngle(), preset.getSpread());
            float speed = preset.getSpeed();

            finalVelocityX = (float) Maths.cos(angle) * speed;
            finalVelocityY = (float) Maths.sin(angle) * speed;
        }

        particle.velocity.set(finalVelocityX, finalVelocityY);
        particle.acceleration.set(0f, 0f);


        particle.size = size;
        particle.life = life;
        particle.maxLife = life;

        particle.rotation = rotation;
        particle.angularVelocity = angularVelocity;

        particle.color = color;
        particle.shape = shape;
        particle.gravity = gravity;

        return particle;
    }

    /**
     * Interne Hilfsmethode, die den Abflugwinkel basierend auf dem Preset und einer zufälligen Streuung (Spread) berechnet.
     */
    private float calculateSpreadAngle(float baseAngle, float spread) {
        float randomOffset = LunarRandom.global().nextFloat(-spread / 2f, spread / 2f);
        float angleDegrees = baseAngle + randomOffset;
        return (float) Maths.toRadians(angleDegrees);
    }

    /**
     * Setzt alle Werte des Builders wieder auf die Standards zurück,
     * damit derselbe Builder für das nächste Partikel wiederverwendet werden kann.
     *
     * @return Der zurückgesetzte Builder.
     */
    public ParticleBuilder reset() {
        this.x = 0f;
        this.y = 0f;

        this.velocityX = 0f;
        this.velocityY = 0f;

        this.size = 4f;
        this.life = 1f;
        this.rotation = 0f;
        this.angularVelocity = 0f;

        this.color = Colors.WHITE;
        this.shape = ParticleShape.CIRCLE;
        this.preset = null;
        this.gravity = 0f;

        return this;
    }

    /**
     * Gibt das Partikel-System zurück, an das dieser Builder gebunden ist.
     *
     * @return Das {@link ParticleSystem}.
     */
    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }
}
