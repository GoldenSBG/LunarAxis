package LunarAxis.audio;

import LunarAxis.math.Maths;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;
import LunarAxis.diagnostics.Validation;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Der {@code SoundPool} verwaltet mehrere Kopien (Instanzen) ein und desselben Soundeffekts im Arbeitsspeicher,
 * um das gleichzeitige, überlappende Abspielen zu ermöglichen.
 * <p>
 * <b>Warum ist das wichtig?</b> Wenn ein Spieler in kurzer Zeit viele Münzen einsammelt oder eine
 * Schnellfeuerwaffe benutzt, würde ein einfacher Sound sich bei jedem neuen Abspielen selbst abwürgen.
 * Der Pool lädt den Sound z.B. 5-mal vorab in den Speicher. Wird der Sound abgespielt, schnappt sich
 * der Pool eine "freie" Kopie. Ist der Sound fertig, wird die Kopie wieder als "frei" markiert.
 * </p>
 * <p>
 * Diese Klasse arbeitet im Hintergrund für den {@link AudioManager}.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Indirekte Erstellung eines SoundPools
 * audio = AudioManager.getInstance()
 *
 * # Erstellt intern einen SoundPool für "laser" mit Platz für 10 gleichzeitige Schüsse
 * audio.loadSound("laser", "assets/sounds/pew.wav", 10)
 *
 * # Wenn dies 12 mal im gleichen Moment aufgerufen wird, sind 10 hörbar und 2 werden ignoriert
 * audio.playSound("laser")
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.7
 */
public class SoundPool {
    private final Queue<Clip> availableClips = new LinkedList<>();
    private final Set<Clip> activeClips = new HashSet<>();

    private final Object poolLock = new Object();

    private final int poolSize;
    private final String name;

    private float masterVolume = 1.0f;
    private boolean closed = false;

    /**
     * Erstellt einen neuen Pool für einen bestimmten Sound und lädt die gewünschte Anzahl an Kopien in den Speicher.
     *
     * @param name     Der Name des Sounds zur internen Verwaltung (z.B. "coin").
     * @param path     Der Dateipfad zur Audio-Datei.
     * @param poolSize Die Anzahl der Kopien, die gleichzeitig existieren (und damit überlappen) dürfen.
     */
    public SoundPool(String name, String path, int poolSize) {
        this.name = name;
        this.poolSize = Maths.max(1, poolSize);

        for (int i = 0; i < this.poolSize; i++) {
            Clip clip = createClip(path);
            if (clip != null) {
                // Listener gibt den Clip automatisch zurück, wenn er fertig ist
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        synchronized (poolLock) {
                            activeClips.remove(clip);

                            if (!closed && clip.isOpen()) {
                                availableClips.add(clip);
                            }
                        }
                    }
                });
                synchronized (poolLock) {
                    if (!closed) {
                        availableClips.add(clip);
                    } else {
                        clip.close();
                    }
                }
            }
        }
    }

    /**
     * Erzeugt eine einzelne {@link Clip}-Instanz aus einer angegebenen Datei oder einer Classpath-Ressource.
     * <p>
     * Die Methode versucht zuerst, die Audiodatei vom Dateisystem zu laden. Falls die Datei dort nicht existiert,
     * wird versucht, sie über den {@link ClassLoader} der Anwendung einzulesen. Im Fehlerfall wird ein
     * strukturierter Warn- oder Fehlerbericht protokolliert und {@code null} zurückgegeben, damit bestehende Clips
     * weiterhin funktionstüchtig bleiben.
     * </p>
     *
     * @param path Der Pfad zur Audio-Datei.
     * @return Das erfolgreich geöffnete {@link Clip}-Objekt oder {@code null}, falls das Laden fehlschlug.
     */
    private Clip createClip(String path) {
        Clip createdClip = null;

        try {
            File file = new File(path);
            boolean fromFile = file.exists();
            InputStream resource = fromFile ? null : getClass().getClassLoader().getResourceAsStream(path);

            if (!fromFile && resource == null) {
                Logger.report(LogLevel.WARN, ErrorDetails.of("SoundPool", "Sound resource not found")
                        .withActual("name=" + name + ", path=" + path)
                        .withExpected("An existing file or classpath resource")
                        .withExplanation("This clip is skipped while previously loaded clips remain available"), null);
                return null;
            }

            try (BufferedInputStream buffered = resource == null ? null : new BufferedInputStream(resource);
                 AudioInputStream stream = fromFile
                         ? AudioSystem.getAudioInputStream(file)
                         : AudioSystem.getAudioInputStream(buffered)) {
                createdClip = AudioSystem.getClip();
                createdClip.open(stream);
            }

            return createdClip;
        } catch (Exception exception) {
            if (createdClip != null) {
                try {
                    createdClip.close();
                } catch (Exception closeException) {
                    exception.addSuppressed(closeException);
                }
            }

            Logger.report(LogLevel.ERROR, ErrorDetails.of("SoundPool", "Pool clip could not be loaded")
                    .withActual("name=" + name + ", path=" + path)
                    .withExpected("A readable, supported audio resource and an available audio output")
                    .withExplanation("This clip is skipped while other successfully loaded clips remain available"), exception);
            return null;
        }
    }


    /**
     * Spielt eine freie Kopie dieses Sounds mit bestimmter Lautstärke und räumlicher Verteilung ab.
     * <p>
     * <b>Wichtig:</b> Wenn alle Clips dieses Pools aktuell beschäftigt sind (weil der Sound schon
     * {@code poolSize}-mal gleichzeitig spielt), wird der Abspielbefehl aus Performancegründen
     * lautlos ignoriert. Das ist in der Spielentwicklung eine gängige Technik, um das System
     * nicht durch hunderte gleichzeitige Sounds zu überlasten.
     * </p>
     *
     * @param volume Die gewünschte Lautstärke für diesen speziellen Aufruf.
     * @param pan    Die Stereo-Position: -1.0f (links), 0.0f (Mitte), 1.0f (rechts).
     */
    public void play(float volume, float pan) {
        Clip clipToPlay;

        synchronized (poolLock) {
            if (closed || availableClips.isEmpty()) {
                return; // Pool erschöpft, Sound droppen (besser als Blockieren)
            }
            clipToPlay = availableClips.poll();

            if (clipToPlay == null) {
                return;
            }
            activeClips.add(clipToPlay);
        }

        synchronized (clipToPlay) {
            if (!clipToPlay.isOpen()) {
                synchronized (poolLock) {
                    activeClips.remove(clipToPlay);
                }
                return;
            }
            //clipToPlay.stop();
            clipToPlay.setMicrosecondPosition(0);

            // Einfache, robuste Berechnung: Sound-Volume * Master-Volume
            float finalVolume = volume * masterVolume;
            setGain(clipToPlay, finalVolume);
            setPan(clipToPlay, pan);
            clipToPlay.start();

        }
    }

    /**
     * Bricht alle aktuell laufenden Kopien dieses Sounds sofort ab.
     * Die abgebrochenen Clips werden automatisch wieder in die Liste der verfügbaren Clips eingereiht.
     */
    public void stopAll() {
        /*
         * Wir erstellen zuerst eine Momentaufnahme der aktiven Clips
         * Das ist wichtig, weil clip.stop() einen LineListener auslösen kann -> das verändert activeClips
         * Würden wir direkt über activeClips iterieren, könnte dabei eine ConcurrentModificationException entstehen
         */
        Set<Clip> clipsToStop;
        synchronized (poolLock) {
            clipsToStop = new HashSet<>(activeClips);
        }

        /*
         * Jetzt können die Clips ausserhalb des Locks gestoppt werden
         * Der LineListener darf währenddessen problemlos reagieren
         */
        for (Clip clip : clipsToStop) {
            synchronized (clip) {
                synchronized (poolLock) {
                    if (!activeClips.contains(clip)) {
                        continue;
                    }
                }
                if (clip.isRunning()) {
                    clip.stop();
                }
                if (clip.isOpen()) {
                    clip.setMicrosecondPosition(0);
                }
            }
        }
    }

    /**
     * Schliesst den gesamten Pool und löscht alle geladenen Sound-Kopien aus dem Arbeitsspeicher.
     * Sollte nur beim Beenden des Spiels aufgerufen werden.
     */
    public void close() {
        Set<Clip> clipsToClose;

        synchronized (poolLock) {
            if (closed) {
                return;
            }
            closed = true;
            clipsToClose = new HashSet<>(availableClips);
            clipsToClose.addAll(activeClips);
            availableClips.clear();
            activeClips.clear();
        }

        for (Clip clip : clipsToClose) {
            synchronized (clip) {
                if (clip.isOpen()) {
                    clip.close();
                }
            }
        }
    }

    /**
     * Setzt die globale Master-Lautstärke für diesen spezifischen SoundPool.
     *
     * @param volume Die gewünschte Lautstärke zwischen 0.0f (stumm) und 1.0f (volle Lautstärke).
     * @throws IllegalArgumentException Wenn eine ungültige Zahl übergeben wird.
     */
    public void setVolume(float volume) {
        Validation.checkArgument(Float.isFinite(volume), ErrorDetails.of("SoundPool", "Invalid pool volume")
                        .withActual(volume)
                        .withExpected("A finite number; values are limited to a range of 0 to 1")
                        .withExplanation("This value is stored and multiplied by the volume levels of subsequent playback requests"));

        this.masterVolume = Maths.max(0.0f, Maths.min(1.0f, volume));

        // Nur verfügbare Clips updaten, aktive werden beim nächsten play() korrekt berechnet
        synchronized (poolLock) {
            for (Clip clip : availableClips) {
                setGain(clip, this.masterVolume);
            }
        }
    }

    /**
     * Interne Hilfsmethode, welche die Lautstärke logarithmisch (in Dezibel) berechnet und
     * direkt auf eine einzelne Sound-Kopie anwendet.
     *
     * @param clip   Der Clip, dessen Lautstärke geändert werden soll.
     * @param volume Die lineare Lautstärke (0.0f bis 1.0f).
     */
    private void setGain(Clip clip, float volume) {
        try {
            Validation.checkArgument(Float.isFinite(volume), ErrorDetails.of("SoundPool", "Invalid calculated sound volume")
                            .withActual(volume)
                            .withExpected("A finite volume")
                            .withExplanation("The volume is determined by the playback volume and the pool volume"));

            if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return;
            }

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float safeVolume = Maths.max(0.0001f, Maths.min(1.0f, volume));
            float dB = (float) (Maths.log(safeVolume) / Maths.log(10.0) * 20.0);

            gainControl.setValue(dB);
        } catch (IllegalArgumentException exception) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("SoundPool", "Volume adjustment failed")
                            .withActual(volume)
                            .withExpected("A finite volume and a level supported by the device")
                            .withExplanation("The volume change is omitted. The existing playback fallback remains in place"), exception);
        }
    }

    /**
     * Interne Hilfsmethode, die das Stereo-Panning (links/rechts-Verteilung) für einen
     * einzelnen Clip festlegt. Dies ist besonders für die 3D-Audio-Funktion wichtig.
     *
     * @param clip Der Clip, dessen Balance geändert werden soll.
     * @param pan  Der Pan-Wert (-1.0f bis 1.0f).
     */
    private void setPan(Clip clip, float pan) {
        try {
            Validation.checkArgument(Float.isFinite(pan), ErrorDetails.of("SoundPool", "Invalid panning value")
                            .withActual(pan)
                            .withExpected("A finite number; values are limited to the range of -1 to 1")
                            .withExplanation("NaN and Infinity cannot describe a stereo position"));

            if (!clip.isControlSupported(FloatControl.Type.PAN)) {
                return;
            }

            FloatControl panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            panControl.setValue(Maths.max(-1.0f, Maths.min(1.0f, pan)));
        } catch (IllegalArgumentException exception) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("SoundPool", "Stereo panning could not be applied")
                            .withActual(pan)
                            .withExpected("A finite value supported by PAN-Control")
                            .withExplanation("The panning change is omitted. A clip without a PAN control will continue to play without panning"), exception);
        }
    }
}