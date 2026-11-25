package LunarAxis.physics;

import LunarAxis.math.Maths;
import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code RigidBody2D} implementiert eine einfache, zweidimensionale Physik-Simulation für einen {@link LunarAxis.objects.Actor}.
 * <p>
 * Sie verwaltet Position, Geschwindigkeit, Beschleunigung, Kräfte sowie physikalische Eigenschaften
 * wie Masse, Reibung (Friction), Luftwiderstand (Drag), maximale Geschwindigkeit und Bewegungsgrenzen (Bounds).
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class RigidBody2D {

    /**
     * Aktuelle Position des Körpers in der Welt.
     */
    public Vec2 position = new Vec2(0, 0);

    /**
     * Aktueller Geschwindigkeits-Vektor.
     */
    public Vec2 velocity = new Vec2(0, 0);

    /**
     * Aktueller Beschleunigungs-Vektor.
     */
    public Vec2 acceleration = new Vec2(0, 0);

    /**
     * Die Masse des Körpers (Werte <= 0.001 repräsentieren einen statischen Körper).
     */
    public float mass = 1.0f;

    /**
     * Reibungskoeffizient.
     */
    public float friction = 0.95f;

    /**
     *  Luftwiderstandskoeffizient.
     */
    public float drag = 0.01f;

    /**
     * Skalierungsfaktor für Schwerkraft.
     */
    public float gravityScale = 1.0f;

    /**
     * Maximale Geschwindigkeitsbegrenzung.
     */
    public float maxSpeed = 100.0f;

    // Grenzen für Bewegung
    /**
     *  Gibt an, ob Bewegungsgrenzen (Bounds) aktiv sind.
     */
    public boolean useBounds = false;

    /**
     * Minimale X-Grenze.
     */
    public float minX = -Float.MAX_VALUE;

    /**
     * Maximale X-Grenze.
     */
    public float maxX = Float.MAX_VALUE;

    /**
     * Minimale Y-Grenze.
     */
    public float minY = -Float.MAX_VALUE;

    /**
     * Maximale Y-Grenze.
     */
    public float maxY = Float.MAX_VALUE;

    // Akkumulierte Kräfte (wird bei jedem Frame zurückgesetzt)
    public Vec2 accumulatedForces = new Vec2(0, 0);

    /**
     * Erstellt einen neuen Standard-RigidBody2D mit Masse 1.0.
     */
    public RigidBody2D() {
    }

    /**
     * Erstellt einen neuen RigidBody2D mit einer spezifischen Masse.
     *
     * @param mass Die Masse des Körpers.
     * @throws RuntimeException (Lunar-Error) wenn die Masse nicht endlich ist.
     */
    public RigidBody2D(float mass) {
        if (!Float.isFinite(mass)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Body mass must be finite")
                    .withActual(mass)
                    .withExpected("A finite mass; values at or below 0.001 represent a static body")
                    .withExplanation("NaN and infinite masses cannot be used consistently in force and energy calculations"), null);
        }

        this.mass = mass;
    }

    /**
     * Erstellt einen neuen RigidBody2D mit Masse, Reibung und Luftwiderstand.
     *
     * @param mass     Die Masse.
     * @param friction Die Reibung.
     * @param drag     Der Luftwiderstand.
     * @throws RuntimeException (Lunar-Error) wenn einer der Parameter nicht endlich ist.
     */
    public RigidBody2D(float mass, float friction, float drag) {
        if (!Float.isFinite(mass)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Body mass must be finite")
                    .withActual(mass)
                    .withExpected("A finite mass; values at or below 0.001 represent a static body")
                    .withExplanation("NaN and infinite masses cannot be used consistently in force and energy calculations"), null);
        }
        if (!Float.isFinite(friction)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Body friction must be finite")
                    .withActual(friction)
                    .withExpected("A finite friction value")
                    .withExplanation("This constructor stores friction directly, so NaN and Infinity can produce invalid velocity calculations"), null);
        }
        if (!Float.isFinite(drag)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Body drag must be finite")
                    .withActual(drag)
                    .withExpected("A finite drag value")
                    .withExplanation("This constructor stores drag directly, so NaN and Infinity can produce invalid velocity calculations"), null);
        }

        this.mass = mass;
        this.friction = friction;
        this.drag = drag;
    }

    /**
     * Fügt eine Kraft zum Körper hinzu.
     *
     * <p>
     * Statische Bodies ({@code mass <= 0.001}) nehmen keine Kräfte auf.
     * Das entspricht ihrem Verhalten in {@code update()}.
     * </p>
     *
     * <p>
     * Formel nach Newton ($F = m \cdot a \implies a = F / m$).
     * </p>
     *
     * @param force Anzuwendende Kraft als {@link Vec2}; {@code null} wird ignoriert.
     * @throws RuntimeException (Lunar-Error) wenn die Masse oder die Kraftkomponenten ungültig sind.
     */
    public void addForce(Vec2 force) {
        if (!Float.isFinite(mass)) {
            throw Errors.state(ErrorDetails.of("RigidBody2D", "Body mass is not finite")
                    .withActual(mass)
                    .withExpected("A finite mass; values at or below 0.001 represent a static body")
                    .withExplanation("The body mass must be valid before a force can be converted into acceleration"), null);
        }
        if (mass <= 0.001f) {
            return;
        }
        if (!Float.isFinite(force.getX()) || !Float.isFinite(force.getY())) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Force components must be finite")
                    .withActual("x=" + force.getX() + ", y=" + force.getY())
                    .withExpected("Finite X and Y force components")
                    .withExplanation("NaN and infinite force components would invalidate the accumulated acceleration"), null);
        }


        Vec2 forceCopy = force.copy().divide(mass);
        accumulatedForces = accumulatedForces.add(forceCopy);
    }

    /**
     * Fügt eine lineare Kraft an einem bestimmten Punkt hinzu.
     * Der Punkt wird derzeit nicht zur Berechnung eines Drehmoments verwendet.
     *
     * @param force Die Kraft
     * @param point Der Angriffspunkt
     */
    public void addForceAt(Vec2 force, Vec2 point) {
        addForce(force);
    }

    /**
     * Setzt die Geschwindigkeit direkt.
     *
     * @param velocity Der neue Geschwindigkeits-Vektor
     */
    public void setVelocity(Vec2 velocity) {
        if (velocity != null) {
            this.velocity.set(velocity);
        }
    }

    /**
     * Setzt die Position direkt.
     *
     * @param position Die neue Position
     */
    public void setPosition(Vec2 position) {
        if (position != null) {
            this.position.set(position);
        }
    }

    /**
     * Aktualisiert den physikalischen Zustand des Körpers (wird jeden Frame aufgerufen).
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @throws RuntimeException (Lunar-Error) wenn Zeitintervall oder physikalische Parameter ungültig sind.
     */
    public void update(float deltaTime) {
        if (!Float.isFinite(mass)) {
            throw Errors.state(ErrorDetails.of("RigidBody2D", "Body mass is not finite")
                    .withActual(mass)
                    .withExpected("A finite mass; values at or below 0.001 represent a static body")
                    .withExplanation("The mass must identify a valid static or dynamic body before updating"), null);
        }

        // Early-Exit für statische Bodies (mass <= 0.001f)
        // Statische Bodies sollen sich nicht bewegen und keine Kräfte akkumulieren
        if (this.mass <= 0.001f) {
            accumulatedForces.set(0, 0);
            return;
        }

        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Physics update time must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite time interval")
                    .withExplanation("NaN and infinite time intervals would invalidate velocity and position calculations"), null);
        }
        if (!Float.isFinite(friction) || !Float.isFinite(drag)) {
            throw Errors.state(ErrorDetails.of("RigidBody2D", "Body damping parameters are not finite")
                    .withActual("friction=" + friction + ", drag=" + drag)
                    .withExpected("Finite friction and drag values")
                    .withExplanation("The public damping fields must contain finite values before updating velocity"), null);
        }
        if (Float.isNaN(maxSpeed) || maxSpeed < 0.0f) {
            throw Errors.state(ErrorDetails.of("RigidBody2D", "Maximum speed is invalid")
                    .withActual(maxSpeed)
                    .withExpected("A non-negative maximum speed or positive Infinity to disable the limit")
                    .withExplanation("A negative speed limit reverses the velocity during limiting, while NaN prevents a meaningful comparison"), null);
        }
        if (useBounds && (Float.isNaN(minX) || Float.isNaN(maxX) || Float.isNaN(minY) || Float.isNaN(maxY) || minX > maxX || minY > maxY)) {
            throw Errors.state(ErrorDetails.of("RigidBody2D", "Active movement bounds are invalid")
                    .withActual("minX=" + minX + ", maxX=" + maxX + ", minY=" + minY + ", maxY=" + maxY)
                    .withExpected("Bounds without NaN, with minX <= maxX and minY <= maxY")
                    .withExplanation("Each active movement interval must have an ordered lower and upper boundary"), null);
        }

        // 1. Beschleunigung zur Geschwindigkeit addieren (F = m * a)
        velocity = velocity.add(accumulatedForces.x * deltaTime, accumulatedForces.y * deltaTime);

        // 2. Luftwiderstand anwenden (Drag)
        if (drag > 0) {
            velocity = velocity.multiply(1.0f - Maths.min(drag * deltaTime, 0.5f));
        }

        /**
         *  3. Reibung anwenden (Friction) [bug done]
         *  Setter erlaubt friction == 1
         *  -> Der Grenzwert muss wie die übrigen positiven Reibungswerte angewendet werden
         */
        if (friction > 0) {
            velocity = velocity.multiply(1.0f - Maths.min(friction * deltaTime, 0.5f));
        }

        // 4. Geschwindigkeit begrenzen (Max Speed)
        float currentSpeed = velocity.length();
        if (currentSpeed > maxSpeed) {
            velocity = velocity.normalize().multiply(maxSpeed);
        }

        // 5. Position aktualisieren
        position = position.add(velocity.getX() * deltaTime, velocity.getY() * deltaTime);

        // 6. Begrenzungen anwenden (Bounds)
        if (useBounds) {
            if (position.getX() < minX) {
                position.setX(minX);
                velocity.setX(-velocity.getX() * 0.8f); // Abprallen mit Energieverlust
            } else if (position.getX() > maxX) {
                position.setX(maxX);
                velocity.setX(-velocity.getX() * 0.8f);
            }
            if (position.getY() < minY) {
                position.setY(minY);
                velocity.setY(-velocity.getY() * 0.8f);
            } else if (position.getY() > maxY) {
                position.setY(maxY);
                velocity.setY(-velocity.getY() * 0.8f);
            }
        }

        // 7. Akkumulierte Kräfte für den nächsten Frame zurücksetzen
        accumulatedForces.set(0, 0);
    }

    /**
     * Wendet eine konstante Kraft an (z.B. Gravitation).
     * Diese Kraft wird jeden Frame automatisch angewendet.
     *
     * @param force Die anzuwendende Kraft
     */
    public void applyConstantForce(Vec2 force) {
        addForce(force);
    }

    /**
     * Gibt die kinetische Energie des Körpers zurück (E = 0.5 * m * v²).
     *
     * @return Die kinetische Energie
     */
    public float getKineticEnergy() {
        return 0.5f * mass * velocity.lengthSquared();
    }

    /**
     * Bremst den Körper abrupt ab (z.B. bei einer Kollision).
     */
    public void stop() {
        velocity.set(0, 0);
        accumulatedForces.set(0, 0);
    }

    /**
     * Setzt den Körper komplett zurück (Position, Geschwindigkeit und Kräfte).
     *
     * @param position Die neue Reset-Position
     */
    public void reset(Vec2 position) {
        if (position != null) {
            this.position.set(position);
        }
        this.velocity.set(0, 0);
        this.acceleration.set(0, 0);
        this.accumulatedForces.set(0, 0);
    }

    /**
     * Gibt die Masse zurück.
     * @return Die Masse
     */
    public float getMass() {
        return mass;
    }

    /**
     * Setzt die Masse des Körpers.
     *
     * @param mass Die neue Masse
     * @throws RuntimeException (Lunar-Error) wenn die Masse nicht endlich ist
     */
    public void setMass(float mass) {
        if (!Float.isFinite(mass)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Body mass must be finite")
                    .withActual(mass)
                    .withExpected("A finite mass")
                    .withExplanation("The mass must be finite before applying the existing minimum mass limit"), null);
        }

        this.mass = Maths.max(0.001f, mass);
    }

    /**
     * Gibt den Reibungskoeffizienten zurück.
     * @return Reibung
     */
    public float getFriction() {
        return friction;
    }

    /**
     * Setzt den Reibungskoeffizienten (wird auf [0, 1] begrenzt).
     *
     * @param friction Die Reibung
     * @throws RuntimeException (Lunar-Error) wenn der Wert NaN ist
     */
    public void setFriction(float friction) {
        if (Float.isNaN(friction)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Body friction must not be NaN")
                    .withActual(friction)
                    .withExpected("A numeric friction value that can be clamped to the range [0, 1]")
                    .withExplanation("NaN cannot be ordered or converted into a meaningful friction coefficient"), null);
        }

        this.friction = Maths.max(0, Maths.min(1, friction));
    }

    /**
     * Gibt den Luftwiderstandskoeffizienten zurück.
     * @return Luftwiderstand (Drag)
     */
    public float getDrag() {
        return drag;
    }

    /**
     * Setzt den Luftwiderstand (wird auf [0, 1] begrenzt).
     *
     * @param drag Der Drag-Wert
     * @throws RuntimeException (Lunar-Error) wenn der Wert NaN ist
     */
    public void setDrag(float drag) {
        if (Float.isNaN(drag)) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Body drag must not be NaN")
                    .withActual(drag)
                    .withExpected("A numeric drag value that can be clamped to the range [0, 1]")
                    .withExplanation("NaN cannot be ordered or converted into a meaningful drag coefficient"), null);
        }

        this.drag = Maths.max(0, Maths.min(1, drag));
    }

    /**
     * Gibt den Geschwindigkeits-Vektor zurück.
     * @return {@link Vec2} der Geschwindigkeit
     */
    public Vec2 getVelocity() {
        return velocity;
    }

    /**
     * Gibt den Positions-Vektor zurück.
     * @return {@link Vec2} der Position
     */
    public Vec2 getPosition() {
        return position;
    }

    /**
     * Berechnet die aktuelle Bewegungsgeschwindigkeit (Betrag der Geschwindigkeit).
     * @return Die Speed als Float
     */
    public float getSpeed() {
        return velocity.length();
    }

    /**
     * Aktiviert Bewegungsgrenzen (Bounds), innerhalb derer sich der Körper bewegen kann.
     *
     * @MinX Minimale X-Koordinate
     * @MaxX Maximale X-Koordinate
     * @MinY Minimale Y-Koordinate
     * @MaxY Maximale Y-Koordinate
     * @throws RuntimeException (Lunar-Error) bei ungültigen oder NaN-Grenzen
     */
    public void setBounds(float minX, float maxX, float minY, float maxY) {
        if (Float.isNaN(minX) || Float.isNaN(maxX) || Float.isNaN(minY) || Float.isNaN(maxY) || minX > maxX || minY > maxY) {
            throw Errors.argument(ErrorDetails.of("RigidBody2D", "Movement bounds are invalid")
                    .withActual("minX=" + minX + ", maxX=" + maxX + ", minY=" + minY + ", maxY=" + maxY)
                    .withExpected("Bounds without NaN, with minX <= maxX and minY <= maxY")
                    .withExplanation("Each movement interval requires an ordered lower and upper boundary before bounds can be enabled"), null);
        }

        this.useBounds = true;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * Deaktiviert die Bewegungsgrenzen.
     */
    public void disableBounds() {
        this.useBounds = false;
    }

    /**
     * Gibt eine textuelle Darstellung des RigidBody mit Position, Geschwindigkeit und Speed zurück.
     *
     * @return Ein formatierter String
     */
    @Override
    public String toString() {
        return String.format("RigidBody2D[pos=(%.2f, %.2f), vel=(%.2f, %.2f), speed=%.2f]",
                position.getX(), position.getY(), velocity.getX(), velocity.getY(), getSpeed());
    }
}
