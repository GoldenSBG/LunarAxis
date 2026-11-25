package LunarAxis.graphics.particle;

import LunarAxis.math.Maths;
import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code ParticleEmitter} fungiert als kontinuierlicher Quellpunkt für Partikel
 * (vergleichbar mit einem Schornstein, der dauerhaft Rauch ausstösst, oder einem Zauberstab).
 * <p>
 * Im Gegensatz zu einmaligen Explosionen kümmert sich der Emitter im Game-Loop selbstständig darum,
 * in regelmässigen Abständen (abhängig von der eingestellten Rate) neue Partikel über einen
 * {@link ParticleBuilder} zu erzeugen und in das {@link ParticleSystem} einzuspeisen.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Einen dauerhaften Feuer-Emitter erstellen und an den Spieler heften
 * fireEmitter = ParticleEmitter(myParticleSystem, firePreset)
 * fireEmitter.setEmissionRate(30.0) # 30 Partikel pro Sekunde
 *
 * # In der update(deltaTime)-Methode:
 * fireEmitter.setPosition(player.getX(), player.getY())
 * fireEmitter.update(deltaTime)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticleEmitter {

    private final ParticleSystem particleSystem;

    private float x, y;
    private float emissionRate;
    private float emissionTimer;

    private boolean active;

    private ParticlePreset preset;

    /**
     *  Der initiale Z-Index für neu erzeugte Partikel dieses Emitters.
     */
    private long zIndex = Particle.DEFAULT_Z_INDEX;

    /**
     * Erstellt einen neuen Partikel-Emitter, der an ein Partikel-System angebunden ist
     * und eine feste Vorlage (Preset) verwendet.
     *
     * @param particleSystem Das {@link ParticleSystem}, in dem die Partikel verwaltet werden.
     * @param preset         Das {@link ParticlePreset}, das als Vorlage für jedes erzeugte Partikel dient.
     *
     * @throws RuntimeException (Lunar-Error) Wenn das Partikel-System oder das Preset {@code null} sind.
     */
    public ParticleEmitter(ParticleSystem particleSystem, ParticlePreset preset) {
        if (particleSystem == null) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Particle system cannot be null")
                    .withActual(null)
                    .withExpected("An existing ParticleSystem")
                    .withExplanation("The emitter needs a particle system to allocate and register particles"), null);
        }
        if (preset == null) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Preset cannot be null")
                    .withActual(null)
                    .withExpected("An existing ParticlePreset")
                    .withExplanation("The emitter uses the preset when creating each particle"), null);
        }

        this.particleSystem = particleSystem;
        x = 0f;
        y = 0f;
        emissionRate = 10f;
        emissionTimer = 0f;
        active = true;
        this.preset = preset;
    }

    /**
     * Aktualisiert den Emitter und erzeugt bei Bedarf (abhängig von der verstrichenen Zeit
     * und der Emissionsrate) automatisch neue Partikel.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @throws RuntimeException (Lunar-Error) Wenn {@code deltaTime} ungültig ist oder zu Timings-Überläufen führt.
     */
    public void update(float deltaTime) {
        if (!active) {
            return;
        }
        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Update duration must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite duration in seconds")
                    .withExplanation("NaN corrupts the emission timer and an infinite duration can prevent the emission loop from terminating"), null);
        }

        if (deltaTime <= 0f) {
            return;
        }

        float nextTimer = emissionTimer + deltaTime;
        if (!Float.isFinite(nextTimer)) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Update duration overflows the emission timer")
                    .withActual("deltaTime=" + deltaTime + ", emissionTimer=" + emissionTimer)
                    .withExpected("A duration that keeps the accumulated timer finite")
                    .withExplanation("The update cannot be processed because the accumulated time exceeds the float range"), null);
        }

        float interval = 1f / emissionRate;
        if (nextTimer >= interval && nextTimer - interval == nextTimer) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Emission interval is too small for the accumulated time")
                    .withActual("deltaTime=" + deltaTime + ", emissionTimer=" + emissionTimer + ", emissionRate=" + emissionRate)
                    .withExpected("Timing values for which subtracting one emission interval reduces the timer")
                    .withExplanation("Float rounding would leave the timer unchanged and prevent the emission loop from terminating"), null);
        }

        emissionTimer = nextTimer;

        while (emissionTimer >= interval) {
            emitParticle();
            emissionTimer -= interval;
        }
    }

    /**
     * Erzeugt ein einzelnes Partikel an der aktuellen Emitter-Position basierend auf dem gesetzten Preset.
     */
    protected void emitParticle() {
        Particle particle = ParticleBuilder.create(particleSystem).position(x, y).preset(preset).emit();
        particle.setZIndex(zIndex);
    }

    /**
     * Legt den Z-Index fest, den neu emittierte Partikel nach diesem Aufruf erhalten sollen.
     *
     * @param zIndex Der zuzuweisende Z-Index.
     */
    public void setZIndex(long zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * Setzt die Welt-Position des Emitters über separate X- und Y-Koordinaten.
     *
     * @param x X-Koordinate.
     * @param y Y-Koordinate.
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Setzt die Welt-Position des Emitters über einen Vektor.
     *
     * @param position Die Position als {@link Vec2}-Vektor.
     * @throws RuntimeException (Lunar-Error) Wenn der Vektor {@code null} ist.
     */
    public void setPosition(Vec2 position) {
        if (position == null) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Position cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the emitter position")
                    .withExplanation("The emitter must read both coordinates from the supplied position"), null);
        }

        this.x = position.x;
        this.y = position.y;
    }

    /**
     * Legt fest, wie viele Partikel pro Sekunde vom Emitter erzeugt werden sollen.
     *
     * @param particlesPerSecond Die Ausstoss-Rate (Partikel pro Sekunde). Werte &le; 0 schalten die Emission ab.
     * @throws RuntimeException (Lunar-Error) Wenn die Rate ungültig (NaN) ist.
     */
    public void setEmissionRate(float particlesPerSecond) {
        if (!Float.isFinite(particlesPerSecond)) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Emission rate must be finite")
                    .withActual(particlesPerSecond)
                    .withExpected("A finite rate; values at or below 0 disable emission")
                    .withExplanation("NaN prevents valid timing calculations and an infinite rate can prevent the emission loop from terminating"), null);
        }
        if (particlesPerSecond <= 0f) {
            this.emissionRate = 0f;
            return;
        }

        this.emissionRate = Maths.max(0.1f, particlesPerSecond);
    }

    /**
     * Ändert das Vorlagen-Preset, das der Emitter für zukünftige Partikel verwendet.
     *
     * @param preset Ein neues {@link ParticlePreset}.
     * @throws RuntimeException (Lunar-Error) Wenn das Preset {@code null} ist.
     */
    public void setPreset(ParticlePreset preset) {
        if (preset == null) {
            throw Errors.argument(ErrorDetails.of("ParticleEmitter", "Preset cannot be null")
                    .withActual(null)
                    .withExpected("An existing ParticlePreset")
                    .withExplanation("The emitter requires a preset for subsequent particle emissions"), null);
        }
        this.preset = preset;
    }

    /**
     * Gibt das aktuell verwendete Partikel-Preset zurück.
     *
     * @return Das {@link ParticlePreset}.
     */
    public ParticlePreset getPreset() {
        return preset;
    }

    /**
     * Gibt die aktuelle X-Koordinate des Emitters zurück.
     *
     * @return X-Position.
     */
    public float getX() {
        return x;
    }

    /**
     * Gibt die aktuelle Y-Koordinate des Emitters zurück.
     *
     * @return Y-Position.
     */
    public float getY() {
        return y;
    }

    /**
     * Gibt die aktuelle Ausstoss-Rate zurück.
     *
     * @return Partikel pro Sekunde.
     */
    public float getEmissionRate() {
        return emissionRate;
    }

    /**
     * Aktiviert den Emitter, sodass er wieder Partikel ausstösst.
     */
    public void start() {
        active = true;
    }

    /**
     * Pausiert den Emitter, sodass vorübergehend keine neuen Partikel mehr erzeugt werden.
     */
    public void stop() {
        active = false;
    }

    /**
     * Prüft, ob der Emitter aktuell aktiv ist.
     *
     * @return {@code true}, wenn er aktiv ist, sonst {@code false}.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gibt das zugehörige Partikel-System zurück.
     *
     * @return Das {@link ParticleSystem}.
     */
    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }
}
