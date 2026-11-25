package LunarAxis.graphics;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;

/**
 * Die Klasse {@code Animation} repräsentiert eine zeitbasierte Abfolge von Einzelbildern (Frames).
 * <p>
 * Das Prinzip funktioniert genau wie bei einem Daumenkino: Man übergibt der Animation eine Reihe
 * von Bildern und legt fest, wie lange es dauern soll, bis alle Bilder einmal durchgeblättert wurden.
 * Wenn das Spiel läuft, übergibt man der Animation die aktuelle Zeit, und sie gibt automatisch
 * das exakt richtige Einzelbild für diesen Moment zurück.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Eine Lauf-Animation erstellen und abspielen
 * # Angenommen, 'walk_frames' ist ein Array/eine Liste von geladenen Bildern
 *
 * # Die gesamte Animation dauert 1.0 Sekunde und wiederholt sich endlos (True)
 * walkAnim = Animation(walk_frames, 1.0, True)
 *
 * # In der update(deltaTime)-Methode deines Spiels:
 * self.animationTime += deltaTime
 *
 * # In der render(graphics)-Methode:
 * currentImage = walkAnim.getFrame(self.animationTime)
 * graphics.drawImage(currentImage, self.x, self.y)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Animation {

    private final Image[] frames;
    private final float frameDuration;
    private final boolean loop;
    private float elapsedTime = 0f;

    /**
     * Erstellt eine neue Animation aus einer Liste von Einzelbildern.
     *
     * @param frames        Ein Array von Bildern ({@link Image}), die hintereinander abgespielt werden sollen.
     * @param frameDuration Die Gesamtzeit in Sekunden, die vergehen soll, bis <b>alle</b> Bilder einmal durchgespielt wurden.
     * @param loop          {@code true}, wenn die Animation nach dem letzten Bild wieder von vorne beginnen soll
     *                      (z.B. Laufen). {@code false}, wenn sie beim letzten Bild stehen bleiben soll (z.B. Sterben).
     *
     * @throws RuntimeException (Lunar-Error) Wenn kein Array übergeben wurde, die Bilder leer sind oder die Zeit ungültig ist.
     */
    public Animation(Image[] frames, float frameDuration, boolean loop) {
        if (frames == null) {
            throw Errors.lunar(ErrorDetails.of("Animation", "Animation frames must not be null")
                    .withActual(null)
                    .withExpected("An Image array; an empty array is allowed")
                    .withExplanation("The animation requires an array to determine which frames are available"), null);
        }
        if (!Float.isFinite(frameDuration)) {
            throw Errors.lunar(ErrorDetails.of("Animation", "Animation frame duration must be finite")
                    .withActual(frameDuration)
                    .withExpected("A finite duration greater than 0")
                    .withExplanation("NaN and infinite durations cannot define valid animation timing"), null);
        }
        if (frameDuration <= 0f) {
            throw Errors.lunar(ErrorDetails.of("Animation", "Animation frame duration must be greater than zero")
                    .withActual(frameDuration)
                    .withExpected("A duration greater than 0")
                    .withExplanation("Frame selection divides the supplied duration into intervals and requires a positive duration"), null);
        }

        for (int i = 0; i < frames.length; i++) {
            if (frames[i] == null) {
                throw Errors.lunar(ErrorDetails.of("Animation", "Animation frame at index " + i + " must not be null")
                        .withActual("frames[" + i + "]=null")
                        .withExpected("An existing Image at every occupied array index")
                        .withExplanation("A frame entry must contain an image that can be returned for rendering"), null);
            }
        }

        this.frames = frames;
        this.frameDuration = frameDuration;
        this.loop = loop;
    }

    /**
     * Berechnet und liefert das Bild (Frame), das zu einem bestimmten Zeitpunkt sichtbar sein soll.
     * <p>
     * <b>Mathematischer Hintergrund:</b> Damit das Spiel auch nach Stunden noch flüssig läuft,
     * rechnet diese Methode intern mit der doppelten Genauigkeit ({@code double}) und sehr grossen
     * Zahlenbereichen ({@code long}), um sogenannte Rundungsfehler oder Überläufe (Abstürze) zu verhindern.
     * </p>
     *
     * @param time Die Zeit in Sekunden, die seit dem Start <i>dieser spezifischen Animation</i> vergangen ist.
     * @return Das aktuelle {@link Image}, das auf den Bildschirm gezeichnet werden soll, oder {@code null} bei einer leeren Animation.
     */
    public Image getFrame(float time) {
        if (frames.length == 0) {
            return null;
        }

        if (!Float.isFinite(time) || time < 0.0f) {
            time = 0.0f;
        }

        double frameTime = (double) frameDuration / frames.length;
        long frameIndex = (long) (time / frameTime);

        if (frameIndex >= frames.length) {
            if (loop) {
                /*
                 * %= ist der Modulo-Operator. Er teilt die linke Zahl durch die rechte und gibt NUR den Rest zurück.
                 * Beispiel: 5 Bilder (frames.length = 5). Wenn der frameIndex 7 erreicht, rechnet Modulo: 7 geteilt durch 5 ist 1, Rest 2.
                 * Der neue frameIndex wird also 2. So fängt die Animation automatisch sauber von vorne an.
                 */
                frameIndex %= frames.length;
            } else {
                frameIndex = frames.length - 1;
            }
        }

        return frames[(int) frameIndex];
    }

    /**
     * Gibt die Gesamtzeit zurück, die diese Animation für einen vollständigen Durchlauf benötigt.
     *
     * @return Die Animationsdauer in Sekunden.
     */
    public float getFrameDuration() {
        return frameDuration;
    }

}
