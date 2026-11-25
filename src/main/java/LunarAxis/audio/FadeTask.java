package LunarAxis.audio;

import LunarAxis.math.Maths;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.Validation;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Der {@code FadeTask} repräsentiert einen einzelnen Ein- oder Ausblendvorgang (Fade) für ein Audio-Element.
 * Er berechnet die kontinuierliche Veränderung der Lautstärke von einem Startwert zu einem Zielwert
 * über eine genau definierte Zeitspanne.
 * <p>
 * Diese Klasse arbeitet als "unsichtbarer Helfer" im Hintergrund. Wenn Schülerinnen oder Schüler
 * in TigerJython einen Fade-Befehl aufrufen, erstellt der {@link AudioManager} automatisch einen
 * solchen Task und übergibt ihn an den {@link AudioThread}.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - So wird ein FadeTask indirekt ausgelöst:
 * audio = AudioManager.getInstance()
 *
 * # Erstellt intern einen FadeTask, der die Lautstärke in 2.5 Sekunden von 0 auf 100% (0 dB) regelt
 * audio.fadeInMusic("theme", 2.5, True)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 *
 */
public class FadeTask {

    private final Clip clip;
    private final float startDb;
    private final float targetDb;
    private final float durationSeconds;
    private final long startTime;

    private final FloatControl gainControl;
    private final Runnable onComplete;

    /**
     * Erstellt einen neuen Auftrag, um die Lautstärke eines Audio-Clips über Zeit zu verändern.
     *
     * @param clip            Das Audio-Element ({@link Clip}), dessen Lautstärke angepasst werden soll.
     * @param startDb         Die Start-Lautstärke in Dezibel (z.B. -80.0f für stumm).
     * @param targetDb        Die Ziel-Lautstärke in Dezibel (z.B. 0.0f für volle Lautstärke).
     * @param durationSeconds Die Dauer des Übergangs in Sekunden.
     * @param onComplete      Ein optionaler Code-Block (Callback), der ausgeführt wird, sobald der Fade komplett abgeschlossen ist (kann {@code null} sein).
     *
     * @throws IllegalArgumentException Wenn der Clip fehlt oder ungültige (nicht-endliche) Zahlenwerte für den Fade übergeben wurden.
     */
    public FadeTask(Clip clip, float startDb, float targetDb, float durationSeconds, Runnable onComplete) {
        if (clip == null) {
            throw Errors.nullValue(ErrorDetails.of("FadeTask", "Audio clip missing")
                            .withActual(null)
                            .withExpected("A clip instance")
                            .withExplanation("A fade requires the clip whose volume or playback is being controlled"), null);
        }

        Validation.checkArgument(Float.isFinite(startDb) && Float.isFinite(targetDb) && Float.isFinite(durationSeconds),
                ErrorDetails.of("FadeTask", "Non-finite fade parameters")
                        .withActual("startDb=" + startDb
                                        + ", targetDb=" + targetDb
                                        + ", durationSeconds=" + durationSeconds)
                        .withExpected("Finite levels and a finite duration; the duration is limited to a minimum of 0.001 seconds")
                        .withExplanation("NaN and Infinity prevent reliable interpolation and final verification."));

        this.clip = clip;
        this.startDb = startDb;
        this.targetDb = targetDb;
        this.durationSeconds = Maths.max(0.001f, durationSeconds);
        this.startTime = System.nanoTime();
        this.onComplete = onComplete;

        FloatControl floatControl = null;
        try {
            floatControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (IllegalArgumentException ignored) {
            // MASTER_GAIN ist optional
            // Ohne diese Control wird kein Pegel gesetzt
        }
        this.gainControl = floatControl;
    }

    /**
     * Aktualisiert die aktuelle Lautstärke des Clips basierend auf der vergangenen Zeit.
     * Diese Methode wird vom {@link AudioThread} viele Male pro Sekunde (ca. 60 FPS) aufgerufen,
     * um einen flüssigen Lautstärkeübergang (lineare Interpolation) zu erzeugen.
     * <p>
     * Ist der Zielwert erreicht und entspricht dieser absoluter Stille (≤ -80.0 dB),
     * wird das Abspielen des Clips automatisch gestoppt.
     * </p>
     */
    public void update() {
        float elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000f;
        float progress = Maths.min(1.0f, elapsedSeconds / durationSeconds);

        // Lineare Interpolation (Lerp) der Dezibel
        float currentDb = startDb + (targetDb - startDb) * progress;

        synchronized (clip) {
            if (gainControl != null) {
                gainControl.setValue(currentDb);
            }
        }

        if (progress >= 1.0f && targetDb <= -80.0f) {
           /* // Wenn Fade-Out fertig ist, Clip stoppen
            synchronized (clip) {
                clip.stop();
            }
            */
            clip.stop();
        }
    }

    /**
     * Prüft, ob die definierte Dauer für den Ein- oder Ausblendvorgang bereits abgelaufen ist.
     *
     * @return {@code true}, wenn die Zeitspanne (durationSeconds) verstrichen ist, andernfalls {@code false}.
     */
    public boolean isFinished() {
        float elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0f;
        return elapsedSeconds >= durationSeconds;
    }

    /**
     * Löst das optionale Abschluss-Ereignis (Callback) aus.
     * Wird normalerweise direkt nach Abschluss des Fades aufgerufen, um z.B. Variablen aufzuräumen
     * oder der Engine mitzuteilen, dass der Sound nun beendet ist.
     */
    public void triggerCompletion() {
        if (onComplete != null) {
            onComplete.run();
        }
    }

    /**
     * Gibt den Audio-Clip zurück, der von diesem Task manipuliert wird.
     *
     * @return Das dazugehörige {@link Clip}-Objekt.
     */
    public Clip getClip() {
        return clip;
    }
}
