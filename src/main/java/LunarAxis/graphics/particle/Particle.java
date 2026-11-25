package LunarAxis.graphics.particle;

import LunarAxis.graphics.Colors;
import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;

/**
 * Die Klasse {@code Particle} repräsentiert ein einzelnes Partikel (z.B. für Funken, Rauch,
 * Explosionen oder Regentropfen) in der Spielwelt.
 * <p>
 * Ein Partikel besitzt eigene physikalische Eigenschaften wie Position, Geschwindigkeit,
 * Beschleunigung, Schwerkraft und Rotation, sowie eine begrenzte Lebensdauer (Life).
 * Wenn die Lebensdauer abgelaufen ist, stirbt das Partikel und wird vom System ausgeblendet.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Ein Partikel manuell erstellen und bewegen
 * from LunarAxis.graphics.particle import Particle
 * from LunarAxis.graphics import Colors
 *
 * # Erstellt ein Partikel bei x=100, y=200 mit Geschwindigkeit vX=5, vY=-10, Grösse 6, weisser Farbe und 2 Sekunden Leben
 * p = Particle(100, 200, 5, -10, 6, Colors.WHITE, 2.0)
 *
 * # In der update(deltaTime)-Methode deines Spiels:
 * p.update(deltaTime)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Particle {

    /**
     * Die aktuelle Position des Partikels in der Welt (X und Y)
     */
    public Vec2 position;

    /**
     * Die aktuelle Geschwindigkeit (Richtung und Tempo) des Partikels
     */
    public Vec2 velocity;

    /**
     *  Die Beschleunigung, welche die Geschwindigkeit kontinuierlich verändert
     */
    public Vec2 acceleration;

    /**
     * Die Grösse (Durchmesser oder Seitenlänge) des Partikels in Pixeln
     */
    public float size;

    /**
     * Die Farbe des Partikels
     */
    public Colors color;

    /**
     * Die geometrische Form des Partikels (z.B. Kreis oder Quadrat)
     */
    public ParticleShape shape;

    /**
     * Optionale Textur (Bild) für den {@link ParticleShape#TEXTURE}-Modus.
     * Wenn kein Bild zugewiesen ist, wird automatisch auf die Standard-Kreisform zurückgefallen.
     */
    public Image texture;

    /**
     * Die aktuelle Drehung (Rotation) des Partikels in Grad
     */
    public float rotation;

    /**
     * Die Drehgeschwindigkeit (wie schnell sich das Partikel um sich selbst dreht)
     */
    public float angularVelocity;

    /**
     * Die verbleibende Lebenszeit des Partikels in Sekunden
     */
    public float life;

    /**
     * Die maximale (ursprüngliche) Lebenszeit des Partikels in Sekunden
     */
    public float maxLife;

    /**
     * Gibt an, ob das Partikel aktuell aktiv ist und am Spielgeschehen teilnimmt
     */
    public boolean alive;

    /**
     * Die Stärke der Schwerkraft, die dieses Partikel nach unten zieht
     */
    public float gravity = 0f;

    /**
     *  Standard-Zeichentiefe (Z-Index), standardmässig über dem gesamten Bereich von Actor-Z-Indizes.
     */
    public static final long DEFAULT_Z_INDEX = Long.MAX_VALUE;

    /**
     * Der aktuelle Z-Index für das Rendering des Partikels.
     */
    private long zIndex = DEFAULT_Z_INDEX;

    /**
     * Erstellt ein neues, zunächst inaktives Partikel mit Standardwerten
     * (weisse Farbe, Grösse 4, 1 Sekunde Leben).
     */
    public Particle() {
        position = new Vec2();
        velocity = new Vec2();
        acceleration = new Vec2();

        size = 4f;
        color = new Colors(255, 255, 255);
        shape = ParticleShape.CIRCLE;

        rotation = 0f;
        angularVelocity = 0f;

        life = 1f;
        maxLife = 1f;

        alive = false;
        gravity = 0f;
    }

    /**
     * Erstellt ein neues, sofort aktives Partikel mit spezifischen Startwerten.
     *
     * @param x     Start-X-Koordinate in der Welt.
     * @param y     Start-Y-Koordinate in der Welt.
     * @param vX    Anfangsgeschwindigkeit auf der X-Achse.
     * @param vY    Anfangsgeschwindigkeit auf der Y-Achse.
     * @param size  Grösse des Partikels.
     * @param color Farbe des Partikels.
     * @param life  Lebensdauer in Sekunden.
     */
    public Particle(float x, float y, float vX, float vY, float size, Colors color, float life) {
        position = new Vec2(x, y);
        velocity = new Vec2(vX, vY);
        acceleration = new Vec2(0, 0);

        this.size = size;
        this.color = color;
        shape = ParticleShape.CIRCLE;

        rotation = 0f;
        angularVelocity = 0f;

        this.life = life;
        this.maxLife = life;

        alive = true;
        gravity = 0f;
    }

    /**
     * Aktualisiert den Zustand und die Physik des Partikels für das aktuelle Frame.
     * <p>
     * <b>Physik-Reihenfolge:</b> Beschleunigung beeinflusst die Geschwindigkeit,
     * Geschwindigkeit verändert die Position. Zudem wird die Schwerkraft addiert,
     * die Rotation angepasst und die verbleibende Lebenszeit heruntergezählt.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @throws RuntimeException (Lunar-Error) Wenn {@code deltaTime} ungültig (NaN oder unendlich) ist.
     */
    public void update(float deltaTime) {
        if (!alive) {
            return;
        }

        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("Particle", "Update duration must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite duration in seconds")
                    .withExplanation("NaN and infinite durations would corrupt particle movement and lifetime calculations"), null);
        }

        if (gravity != 0f) {
            velocity.y += gravity * deltaTime;
        }

        // geschwindigkeit verändern
        velocity.x += acceleration.x * deltaTime;
        velocity.y += acceleration.y * deltaTime;

        // position verändern
        position.x += velocity.x * deltaTime;
        position.y += velocity.y * deltaTime;

        // rotation updaten
        rotation += angularVelocity * deltaTime;

        // lebenszeit verkürzen
        life -= deltaTime;
        if (life <= 0) {
            life = 0;
            alive = false;
        }
    }

    /**
     * Berechnet den prozentualen Fortschritt der Lebensdauer des Partikels.
     *
     * @return Ein Wert zwischen {@code 0.0f} (frisch erstellt) und {@code 1.0f} (am Ende seiner Lebenszeit).
     */
    public float getLifeProgress() {
        if (maxLife <= 0f) {
            return 1f;
        }

        float progress = 1f - (life / maxLife);

        if (progress < 0f) {
            return 0f;
        }
        if (progress > 1f) {
            return 1f;
        }

        return progress;
    }

    /**
     * Gibt den Z-Index (die Zeichnungstiefe) dieses Partikels zurück.
     *
     * @return Der aktuelle Z-Index.
     */
    public long getZIndex() {
        return zIndex;
    }

    /**
     * Setzt den Z-Index (die Zeichnungstiefe) für das Rendering. Kleinere Werte werden zuerst gezeichnet.
     *
     * @param zIndex Der neue Z-Index.
     */
    public void setZIndex(long zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * Setzt das Partikel vollständig zurück, entfernt zugewiesene Texturen und schaltet es ab
     * ({@code alive = false}), sodass es sicher für den Partikel-Pool wiederverwendet werden kann.
     */
    public void reset() {
        position.set(0f, 0f);
        velocity.set(0f, 0f);
        acceleration.set(0f, 0f);

        size = 4f;
        color = new Colors(255, 255, 255);
        shape = ParticleShape.CIRCLE;
        texture = null;

        zIndex = DEFAULT_Z_INDEX;

        rotation = 0f;
        angularVelocity = 0f;

        life = 1f;
        maxLife = 1f;

        alive = false;
        gravity = 0f;
    }

    /**
     * Setzt das Partikel zurück, löscht alte Texturreferenzen, weist ihm neue Startwerte
     * zu und aktiviert es sofort wieder im System.
     *
     * @param x     Neue X-Koordinate in der Welt.
     * @param y     Neue Y-Koordinate in der Welt.
     * @param vX    Neue X-Geschwindigkeit.
     * @param vY    Neue Y-Geschwindigkeit.
     * @param size  Neue Grösse.
     * @param color Neue Farbe.
     * @param life  Neue Lebensdauer in Sekunden.
     */
    public void reset(float x, float y, float vX, float vY, float size, Colors color, float life) {
        position.set(x, y);
        velocity.set(vX, vY);
        acceleration.set(0f, 0f);

        this.size = size;
        this.color = color;
        shape = ParticleShape.CIRCLE;
        texture = null;

        zIndex = DEFAULT_Z_INDEX;

        rotation = 0f;
        angularVelocity = 0f;

        this.life = life;
        this.maxLife = life;

        alive = true;
        gravity = 0f;
    }

    /**
     * Prüft, ob das Partikel aktuell sichtbar ist (also lebt, eine sinnvolle Grösse hat
     * und nicht komplett durchsichtig ist).
     *
     * @return {@code true}, wenn es gezeichnet werden soll, sonst {@code false}.
     */
    public boolean isVisible() {
        return alive && size > 0.01f && color.getAlpha() > 0;
    }

    /**
     * Gibt eine kompakte Textdarstellung des Partikels zurück (für Debugging-Zwecke).
     */
    @Override
    public String toString() {
        return String.format("Particle[pos=%s, life=%.2f/%.2f, size=%.2f, alive=%s]", position, life, maxLife, size, alive);
    }
}
