package LunarAxis.audio;

import LunarAxis.math.Maths;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.Validation;

/**
 * Die Klasse {@code AudioSource} repräsentiert eine positionierte Schallquelle (wie einen virtuellen Lautsprecher)
 * in der 2D-Spielwelt.
 * <p>
 * Im Gegensatz zu normalen Sounds, die immer gleich laut sind, berechnet eine {@code AudioSource}
 * ihre Lautstärke und ihr Stereo-Panning (ob der Sound aus dem linken oder rechten Kopfhörer kommt) automatisch.
 * Dies geschieht basierend auf der Entfernung und Richtung zum Zuhörer (Listener - meistens die Kamera oder der Spieler).
 * Je weiter der Zuhörer entfernt ist, desto leiser wird der Ton, bis zur definierten Maximaldistanz.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Ein knisterndes Lagerfeuer erstellen
 * audio = AudioManager.getInstance()
 *
 * # AudioSource auf Koordinate x=500, y=200 setzen, ab 300 Pixeln Entfernung ist es unhörbar
 * campfireSound = AudioSource(500, 200, "fire_crackle", 300, audio)
 *
 * # Wenn der Spieler in der Nähe ist, wird der Sound passend abgespielt
 * campfireSound.play()
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class AudioSource {
    private float x, y;
    private final String soundName;
    private final float maxDistance;
    private final AudioManager audioManager;

    /**
     * Erstellt eine neue Schallquelle in der Spielwelt.
     *
     * @param x            Die X-Koordinate der Schallquelle in der Welt.
     * @param y            Die Y-Koordinate der Schallquelle in der Welt.
     * @param soundName    Der Name des Sounds, der im {@link AudioManager} zuvor geladen wurde (z.B. "explosion").
     * @param maxDistance  Die maximale Distanz in Pixeln, ab der der Sound komplett stumm ist (Reichweite).
     * @param audioManager Eine Referenz auf den globalen AudioManager, um den Sound abspielen zu können.
     *
     * @throws IllegalArgumentException Wenn der AudioManager nicht existiert (null ist) oder unzulässige Werte übergeben wurden.
     */
    public AudioSource(float x, float y, String soundName, float maxDistance, AudioManager audioManager) {
        if (audioManager == null) {
            throw Errors.nullValue(ErrorDetails.of("AudioSource", "AudioManager is missing")
                            .withActual(null)
                            .withExpected("An AudioManager instance")
                            .withExplanation(
                                    "The source requires the manager for playback and for reading the listener position"), null);
        }

        Validation.checkArgument(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(maxDistance),
                ErrorDetails.of("AudioSource", "Invalid spatial audio values")
                        .withActual("x=" + x + ", y=" + y + ", maxDistance=" + maxDistance)
                        .withExpected("Finite coordinates and a finite maximum distance")
                        .withExplanation("NaN and Infinity do not allow for reliable distance and volume calculations"));

        this.x = x;
        this.y = y;
        this.soundName = soundName;
        this.maxDistance = Maths.max(1.0f, maxDistance);
        this.audioManager = audioManager;
    }

    /**
     * Verschiebt die Schallquelle an eine neue Position in der Welt.
     * Das ist besonders nützlich für Sounds, die an bewegliche Objekte (z.B. ein fahrendes Auto
     * oder einen fliegenden Feuerball) gebunden sind.
     *
     * @param x Die neue X-Koordinate.
     * @param y Die neue Y-Koordinate.
     */
    public void setPosition(float x, float y) {
        Validation.checkArgument(Float.isFinite(x) && Float.isFinite(y),
                ErrorDetails.of("AudioSource", "Invalid source position")
                        .withActual("x=" + x + ", y=" + y)
                        .withExpected("Two finite world coordinates")
                        .withExplanation("The source position determines distance and stereo panning"));

        this.x = x;
        this.y = y;
    }

    /**
     * Spielt den Sound ab. Dabei wird die Position des Zuhörers (Listener) automatisch
     * vom {@link AudioManager} abgefragt, um die korrekte Lautstärke und den Stereo-Effekt zu berechnen.
     */
    public void play() {
        playAt(audioManager.getListenerX(), audioManager.getListenerY());
    }

    /**
     * Spielt den Sound basierend auf einer bestimmten Zuhörer-Position ab.
     * Die Methode berechnet den Abstand (für die Lautstärke) und den horizontalen Unterschied
     * (für das Stereo-Panning zwischen linkem und rechtem Lautsprecher).
     * Ist der Zuhörer weiter entfernt als die {@code maxDistance}, wird gar kein Sound abgespielt.
     *
     * @param listenerX Die X-Koordinate des Zuhörers (z.B. der Kamera oder des Spielers).
     * @param listenerY Die Y-Koordinate des Zuhörers (z.B. der Kamera oder des Spielers).
     */
    public void playAt(float listenerX, float listenerY) {
        Validation.checkArgument(Float.isFinite(listenerX) && Float.isFinite(listenerY),
                ErrorDetails.of("AudioSource", "Invalid listener position")
                        .withActual("listenerX=" + listenerX + ", listenerY=" + listenerY)
                        .withExpected("Two finite world coordinates")
                        .withExplanation(
                                "The sound can only be spatially positioned based on a valid listener position"));

        float dx = x - listenerX;
        float dy = y - listenerY;
        float distance = (float) Maths.sqrt(dx * dx + dy * dy);

        if (distance >= maxDistance) {
            return; // Zu weit entfernt, nicht abspielen
        }

        // 1. Volumen berechnen (Linearer Drop-off: 1.0 bei Distanz 0, 0.0 bei maxDistance)
        float volume = Maths.max(0.0f, Maths.min(1.0f, 1.0f - (distance / maxDistance)));
        // 2. Pan berechnen (Stereo-Balance: -1.0 = Links, 0.0 = Mitte, 1.0 = Rechts)
        // X-Differenz normalisieren auf die maximale Distanz
        float pan = Maths.max(-1.0f, Maths.min(1.0f, dx / maxDistance));

        audioManager.playSoundWithPan(soundName, volume, pan);
    }

    /**
     * Gibt die aktuelle X-Koordinate dieser Schallquelle zurück.
     *
     * @return Die X-Koordinate in der Spielwelt.
     */
    public float getX() {
        return x;
    }

    /**
     * Gibt die aktuelle Y-Koordinate dieser Schallquelle zurück.
     *
     * @return Die Y-Koordinate in der Spielwelt.
     */
    public float getY() {
        return y;
    }
}
