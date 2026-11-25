package LunarAxis.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

import LunarAxis.diagnostics.*;
import LunarAxis.math.Maths;

/**
 * Die Klasse {@code Music} repräsentiert ein einzelnes Hintergrundmusik-Stück im Spiel.
 * <p>
 * Im Gegensatz zu kurzen Soundeffekten (wie Sprüngen oder Explosionen) ist Musik dafür gedacht,
 * über längere Zeiträume zu laufen. Diese Klasse verwaltet das Laden der Audio-Datei,
 * das Abspielen (inklusive Pausieren und Fortsetzen) sowie die Lautstärkeregelung.
 * </p>
 * <p>
 * Normalerweise erstellen Schülerinnen und Schüler diese Klasse nicht selbst mit {@code new Music()},
 * sondern nutzen die komfortablen Methoden im {@link AudioManager}.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Musik steuern
 * audio = AudioManager.getInstance()
 *
 * # Lädt die Musik und gibt ein Music-Objekt zurück
 * bossMusic = audio.loadMusic("boss", "assets/music/boss_theme.wav")
 *
 * bossMusic.setVolume(0.5) # Auf halbe Lautstärke setzen
 * bossMusic.play()         # Musik abspielen
 *
 * # Später im Spiel:
 * bossMusic.pause()        # Pausiert (merkt sich die Stelle)
 * bossMusic.play()         # Spielt exakt da weiter, wo pausiert wurde
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.7
 *
 */
public class Music {
    private Clip clip;
    private boolean looping = false;
    private long pausePosition = 0; // Position in Mikrosekunden speichern
    private boolean isStoppedManually = false;
    private float volume = 1.0f;

    /**
     * Lädt ein Musikstück aus einer Datei oder Classpath-Ressource und bereitet es für die Wiedergabe vor.
     * <p>
     * Installiert automatisch einen {@link LineListener}, um beim Erreichen des Endes bei aktivierter
     * Schleifen-Option (Looping) das Musikstück nahtlos von vorne neu zu starten. Tritt beim Laden
     * oder Bereitstellen der Datei ein Fehler auf, wird ein detaillierter Fehlerbericht protokolliert.
     * </p>
     *
     * @param path Der Dateipfad zur Audio-Datei (z. B. "music/level1.wav").
     * @throws RuntimeException (Lunar-Error) Wenn das Musikstück weder im Dateisystem noch in den Ressourcen gefunden wurde.
     */
    public Music(String path) {
        try {
            File file = new File(path);
            boolean fromFile = file.exists();
            InputStream resource = fromFile ? null : getClass().getClassLoader().getResourceAsStream(path);

            if (!fromFile && resource == null) {
                throw Errors.io(ErrorDetails.of("Music", "Music resource not found")
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

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP && looping && !isStoppedManually) {
                        clip.setMicrosecondPosition(0);
                        clip.start();
                    }
                });
            }
        } catch (Exception exception) {
            if (clip != null) {
                try {
                    clip.close();
                } catch (Exception closeException) {
                    exception.addSuppressed(closeException);
                }
                clip = null;
            }

            Logger.report(LogLevel.ERROR, ErrorDetails.of("Music", "Music could not be loaded")
                    .withActual(path)
                    .withExpected("A readable audio resource supported by Java Sound and an available audio output")
                    .withExplanation("The loading process was aborted and this Music instance cannot play audio"), exception);
        }
    }

    /**
     * Spielt die Musik ab.
     * Wenn die Musik zuvor mit {@link #pause()} angehalten wurde, wird sie exakt
     * an der gemerkten Position fortgesetzt.
     */
    public void play() {
        if (clip != null) {
            isStoppedManually = false;
            clip.setMicrosecondPosition(pausePosition); // sofort fortsetzen
            clip.start();
        }
    }

    /**
     * Pausiert die aktuell laufende Musik.
     * Die genaue Position wird im Hintergrund gespeichert, sodass ein späterer Aufruf
     * von {@link #play()} die Musik nahtlos fortsetzt.
     */
    public void pause() {
        if (clip != null && clip.isRunning()) {
            pausePosition = clip.getMicrosecondPosition(); // Position merken
            isStoppedManually = true;
            clip.stop();
        }
    }

    /**
     * Stoppt die Musik vollständig und setzt die Abspielposition wieder auf den Anfang (0).
     * Ein erneutes Abspielen beginnt das Lied von vorne.
     */
    public void stop() {
        if (clip != null) {
            isStoppedManually = true;
            clip.stop();
            clip.setMicrosecondPosition(0);
            pausePosition = 0;
        }
    }

    /**
     * Legt fest, ob dieses Musikstück in einer Endlosschleife (Loop) laufen soll.
     * Wenn es das Ende erreicht, fängt es automatisch wieder von vorne an.
     *
     * @param looping {@code true}, um die Endlosschleife zu aktivieren, sonst {@code false}.
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    /**
     * Ändert die Lautstärke für dieses spezifische Musikstück.
     * Die Lautstärke wird im Hintergrund logarithmisch (in Dezibel) berechnet,
     * damit es sich für das menschliche Ohr natürlich anhört.
     *
     * @param volume Die gewünschte Lautstärke als Kommazahl zwischen 0.0f (stumm) und 1.0f (volle Lautstärke).
     */
    public void setVolume(float volume) {
        Validation.checkArgument(Float.isFinite(volume),
                ErrorDetails.of("Music", "Invalid music volume")
                        .withActual(volume)
                        .withExpected("A finite number; values are limited to a range of 0 to 1")
                        .withExplanation("NaN and Infinity cannot be used as volume"));

        this.volume = Maths.max(0f, Maths.min(1f, volume));

        if (clip == null) {
            return;
        }

        try {
            if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return;
            }

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float safeVolume = this.volume == 0.0f ? 0.0001f : this.volume;
            float dB = (float) (Maths.log(safeVolume) / Maths.log(10.0) * 20.0);
            gainControl.setValue(dB);
        } catch (Exception exception) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("Music", "Could not apply music volume")
                            .withActual(this.volume)
                            .withExpected("An available MASTER_GAIN control, which accepts the calculated level")
                            .withExplanation(
                                    "An unsupported volume control is being skipped. An error occurred while applying the supported control"), exception);
        }
    }

    /**
     * Stoppt die Musik, setzt die Position sofort auf den Anfang zurück und spielt sie ab.
     * Das ist nützlich, um ein Lied hart neu zu starten, selbst wenn es gerade lief.
     */
    public void playFromStart() {
        if (clip != null) {
            isStoppedManually = false;
            pausePosition = 0;
            clip.setMicrosecondPosition(0);
            clip.start();
        }
    }

    /**
     * Prüft, ob dieses Musikstück in diesem Moment hörbar abgespielt wird.
     *
     * @return {@code true}, wenn die Musik läuft, andernfalls {@code false} (auch wenn sie pausiert ist).
     */
    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    /**
     * Schliesst das Musikstück und gibt den belegten Speicherplatz im System frei.
     * Sollte aufgerufen werden, wenn das Musikstück im ganzen restlichen Spiel nicht mehr benötigt wird.
     */
    public void close() {
        if (clip != null) clip.close();
    }

    /**
     * Gibt das zugrundeliegende Audio-Element (Clip) des Java-Systems zurück.
     *
     * @return Der geladene {@link Clip}.
     */
    public Clip getClip() {
        return clip;
    }

    /**
     * Gibt die aktuell eingestellte Lautstärke dieses Musikstücks zurück.
     *
     * @return Die Lautstärke als Wert zwischen 0.0f und 1.0f.
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Gibt die exakte Zeitposition zurück, an der die Musik das letzte Mal pausiert wurde.
     *
     * @return Die Pausen-Position in Mikrosekunden.
     */
    public long getPausePosition() {
        return pausePosition;
    }
}
