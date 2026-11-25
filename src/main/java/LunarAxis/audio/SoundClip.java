package LunarAxis.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import LunarAxis.diagnostics.*;
import LunarAxis.math.Maths;

/**
 * Die Klasse {@code SoundClip} repräsentiert einen einzelnen, kurzen Soundeffekt (z.B. einen Sprung,
 * einen Schuss oder das Einsammeln einer Münze).
 * <p>
 * Im Gegensatz zur {@link Music}-Klasse wird ein {@code SoundClip} komplett in den Arbeitsspeicher
 * (RAM) geladen. Dadurch kann er ohne spürbare Verzögerung sofort abgespielt werden, auch mehrfach
 * direkt hintereinander.
 * </p>
 * <p>
 * Schülerinnen und Schüler müssen diese Klasse meistens nicht direkt verwenden, da der
 * {@link AudioManager} (in Kombination mit dem {@link SoundPool}) das Laden und Abspielen
 * von Sounds automatisch verwaltet.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Indirekte Nutzung durch den AudioManager
 * audio = AudioManager.getInstance()
 *
 * # Der AudioManager erstellt intern mehrere SoundClips für den Sound "coin"
 * audio.loadSound("coin", "assets/sounds/coin.wav", 3)
 *
 * # Spielt einen dieser internen SoundClips ab
 * audio.playSound("coin")
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.7
 *
 */
public class SoundClip {
    private Clip clip;
    private float volume = 1.0f;

    /**
     * Lädt einen kurzen Soundeffekt aus einer Datei oder Classpath-Ressource vollständig in den Arbeitsspeicher.
     * <p>
     * Überprüft, ob der Pfad im lokalen Dateisystem oder innerhalb der gepackten Anwendungsressourcen zu finden ist.
     * Tritt beim Einlesen oder Öffnen des Audio-Streams ein Fehler auf, wird eine entsprechende Fehlermeldung
     * protokolliert und Ressourcen sicher freigegeben.
     * </p>
     *
     * @param path Der Dateipfad zur Audio-Datei (z. B. "sounds/jump.wav").
     * @throws RuntimeException (Lunar-Error) Wenn die Audio-Ressource unter dem angegebenen Pfad nicht existiert.
     */
    public SoundClip(String path) {
        try {
            File file = new File(path);
            boolean fromFile = file.exists();
            InputStream resource = fromFile ? null : getClass().getClassLoader().getResourceAsStream(path);

            if (!fromFile && resource == null) {
                throw Errors.io(ErrorDetails.of("SoundClip", "Sound resource not found")
                        .withActual(path)
                        .withExpected("An existing file or classpath resource")
                        .withExplanation("Neither a file nor a classpath resource was found at this path"), null);
            }

            try (BufferedInputStream buffered = resource == null ? null : new BufferedInputStream(resource);
                 AudioInputStream stream = fromFile
                         ? AudioSystem.getAudioInputStream(file)
                         : AudioSystem.getAudioInputStream(buffered)) {
                clip = AudioSystem.getClip();
                clip.open(stream);
            }

            updateVolume();
        } catch (Exception exception) {
            if (clip != null) {
                try {
                    clip.close();
                } catch (Exception closeException) {
                    exception.addSuppressed(closeException);
                }
                clip = null;
            }

            Logger.report(LogLevel.ERROR, ErrorDetails.of("SoundClip", "Sound could not be loaded")
                    .withActual(path)
                    .withExpected("A readable audio resource supported by Java Sound and an available audio output")
                    .withExplanation("The loading process was aborted and this SoundClip instance cannot play audio"), exception);
        }
    }

    /**
     * Spielt den Soundeffekt ab.
     * Falls der Sound gerade schon abgespielt wird, wird er unsichtbar "zurückgespult"
     * und startet sofort wieder bei null. Das ist ideal für Aktionen, die schnell
     * hintereinander passieren (z.B. Maschinengewehrfeuer).
     */
    public void play() {
        if (clip != null) {
            // Zurückspulen, damit man denselben Sound mehrmals schnell abspielen kann
            clip.setMicrosecondPosition(0);
            clip.start();
        }
    }

    /**
     * Bricht das Abspielen dieses Soundeffekts sofort und hart ab.
     */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    /**
     * Schliesst den Soundeffekt und gibt den belegten Arbeitsspeicher wieder frei.
     * Sollte aufgerufen werden, wenn der Sound im gesamten Spiel nie wieder benötigt wird,
     * um zu verhindern, dass das Spiel mit der Zeit immer mehr Arbeitsspeicher verbraucht.
     */
    public void close() {
        if (clip != null) {
            clip.close();
        }
    }

    /**
     * Setzt die individuelle Lautstärke für diesen speziellen Sound-Schnipsel.
     *
     * @param volume Die gewünschte Lautstärke als Kommazahl zwischen 0.0f (stumm) und 1.0f (volle Lautstärke).
     * @throws IllegalArgumentException Wenn ein ungültiger (nicht-endlicher) Wert für die Lautstärke übergeben wird.
     */
    public void setVolume(float volume) {
        Validation.checkArgument(
                Float.isFinite(volume),
                ErrorDetails.of("SoundClip", "Ungültige Soundlautstärke")
                        .withActual(volume)
                        .withExpected("A finite number; values are limited to a range of 0 to 1")
                        .withExplanation("NaN and Infinity cannot be used as volume"));

        this.volume = Maths.max(0f, Maths.min(1f, volume));
        updateVolume();
    }

    /**
     * Eine interne Hilfsmethode, welche die eingestellte Lautstärke (von 0.0 bis 1.0)
     * in den für das System nötigen logarithmischen Dezibel-Wert (dB) umrechnet und auf
     * das Audio-Element anwendet.
     */
    private void updateVolume() {
        if (clip == null) {
            return;
        }

        try {
            if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return;
            }

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (
                    Maths.log(volume == 0.0 ? 0.0001 : volume) / Maths.log(10.0) * 20.0);
            gainControl.setValue(dB);
        } catch (Exception exception) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("SoundClip", "Sound volume could not be applied")
                            .withActual(volume)
                            .withExpected("An available MASTER_GAIN control that accepts the calculated level")
                            .withExplanation("Missing volume control is skipped. Any other error encountered while applying the level is explicitly reported here"), exception);
        }
    }

    /**
     * Prüft, ob dieser Sound-Schnipsel in diesem exakten Moment hörbar abgespielt wird.
     *
     * @return {@code true}, wenn der Sound gerade läuft, ansonsten {@code false}.
     */
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
}
