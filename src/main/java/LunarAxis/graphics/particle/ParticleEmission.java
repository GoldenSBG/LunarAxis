package LunarAxis.graphics.particle;

import LunarAxis.math.Vec2;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

/**
 * Die Klasse {@code ParticleEmission} beschreibt einen einmaligen oder wiederkehrenden
 * Ausstoss-Vorgang (Emission) von Partikeln.
 * <p>
 * Sie bündelt alle nötigen Informationen an einem Ort: Welches Preset (Vorlage) soll verwendet werden ({@link ParticlePreset}),
 * an welcher Position ({@link Vec2}) soll gespawnt werden, wie viele Partikel ({@code count}) sollen erzeugt werden
 * und nach welchem geometrischen Muster ({@link ParticleSpawnMode}, z.B. als einzelner Punkt, in einem Kreis oder in einem Rechteck)
 * sollen sie verteilt werden.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Eine Emissions-Konfiguration für eine Explosion erstellen
 * emission = ParticleEmission(explosionPreset, player.getPosition(), 50, ParticleSpawnMode.CIRCLE, 25.0, 0.0, 0.0)
 * particleSystem.emit(emission)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticleEmission {

    private final ParticlePreset preset;
    private final Vec2 position;
    private final int count;
    private final ParticleSpawnMode spawnMode;

    private final float radius, width, height;

    /**
     * Erstellt eine einfache Punkt-Emission an einer festen Position.
     *
     * @param preset   Das {@link ParticlePreset}, das das Aussehen und Verhalten der Partikel bestimmt.
     * @param position Die Position ({@link Vec2}) in der Welt, an der emittiert wird.
     * @param count    Die Anzahl der Partikel, die erzeugt werden sollen.
     */
    public ParticleEmission(ParticlePreset preset, Vec2 position, int count) {
        this(preset, position, count, ParticleSpawnMode.POINT, 0f, 0f, 0f);
    }

    /**
     * Erstellt eine erweiterte Partikel-Emission mit spezifischem Spawnmuster und Ausdehnungs-Parametern.
     *
     * @param preset    Das zugrundeliegende {@link ParticlePreset}.
     * @param position  Die Basis-Position als {@link Vec2}.
     * @param count     Die Anzahl der zu erzeugenden Partikel.
     * @param spawnMode Das Muster, wie Partikel im Raum verteilt werden ({@link ParticleSpawnMode}).
     * @param radius    Der Radius (wird z.B. bei kreisförmigen Spawn-Modi verwendet).
     * @param width     Die Breite (wird z.B. bei rechteckigen Spawn-Modi verwendet).
     * @param height    Die Höhe (wird z.B. bei rechteckigen Spawn-Modi verwendet).
     *
     * @throws RuntimeException (Lunar-Error) Wenn Preset, Position oder SpawnMode {@code null} sind.
     */
    public ParticleEmission(ParticlePreset preset, Vec2 position, int count, ParticleSpawnMode spawnMode, float radius, float width, float height) {
        if (preset == null) {
            throw Errors.argument(ErrorDetails.of("ParticleEmission", "Preset cannot be null")
                    .withActual(null)
                    .withExpected("An existing ParticlePreset")
                    .withExplanation("An emission requires a preset describing its particles"), null);
        }
        if (position == null) {
            throw Errors.argument(ErrorDetails.of("ParticleEmission", "Position cannot be null")
                    .withActual(null)
                    .withExpected("A Vec2 containing the emission position")
                    .withExplanation("The emission stores a copy of the supplied position"), null);
        }
        if (spawnMode == null) {
            throw Errors.argument(ErrorDetails.of("ParticleEmission", "Spawn mode cannot be null")
                    .withActual(null)
                    .withExpected("A ParticleSpawnMode value")
                    .withExplanation("The spawn mode specifies how particle positions are distributed"), null);
        }

        this.preset = preset;
        this.position = new Vec2(position);
        this.count = Math.max(0, count);
        this.spawnMode = spawnMode;
        this.radius = Math.max(0f, radius);
        this.width = Math.max(0f, width);
        this.height = Math.max(0f, height);
    }

    /**
     * Gibt das für diese Emission verwendete Vorlagen-Preset zurück.
     *
     * @return Das {@link ParticlePreset}.
     */
    public ParticlePreset getPreset() {
        return preset;
    }

    /**
     * Gibt die Basis-Position zurück, an der die Emission stattfindet.
     *
     * @return Die Position als {@link Vec2}-Kopie.
     */
    public Vec2 getPosition() {
        return position;
    }

    /**
     * Gibt die Anzahl der Partikel zurück, die bei dieser Emission erzeugt werden.
     *
     * @return Die Partikel-Anzahl.
     */
    public int getCount() {
        return count;
    }

    /**
     * Gibt den Spawn-Modus (Verteilungs-Muster) zurück.
     *
     * @return Der {@link ParticleSpawnMode}.
     */
    public ParticleSpawnMode getSpawnMode() {
        return spawnMode;
    }

    /**
     * Gibt den Radius für kreisförmige Verteilungen zurück.
     *
     * @return Der Radius in Pixeln.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Gibt die Breite für flächenbasierte Verteilungen zurück.
     *
     * @return Die Breite in Pixeln.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Gibt die Höhe für flächenbasierte Verteilungen zurück.
     *
     * @return Die Höhe in Pixeln.
     */
    public float getHeight() {
        return height;
    }
}
