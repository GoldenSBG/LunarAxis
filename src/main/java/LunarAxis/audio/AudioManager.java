package LunarAxis.audio;

import java.util.HashMap;
import java.util.Map;

import LunarAxis.math.Maths;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;
import LunarAxis.diagnostics.Validation;

import javax.sound.sampled.FloatControl;

/**
 *
 * Der {@code AudioManager} ist das zentrale Steuerungselement für alle Audio-Ausgaben in der LunarAxis-Engine.
 * Er verwaltet sowohl kurze Soundeffekte (über Sound-Pools für gleichzeitiges Abspielen) als auch Hintergrundmusik (mit Unterstützung für fliessende Übergänge/Fading).
 *
 *  <p>
 * Da diese Klasse als sogenanntes Singleton entworfen ist, gibt es immer nur genau eine Instanz davon,
 * auf die von überall aus zugegriffen werden kann.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Sound laden und abspielen
 * audio = AudioManager.getInstance()
 * audio.loadSound("jump", "assets/sounds/jump.wav")
 * audio.playSound("jump")
 *
 * # Hintergrundmusik starten
 * audio.loadMusic("bgm", "assets/music/level1.wav")
 * audio.playMusic("bgm", True)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 *
 */
public class AudioManager {
    private static AudioManager instance;

    private final Map<String, SoundPool> soundPools = new HashMap<>();
    private final Map<String, Music> musicCache = new HashMap<>();
    
    private Music currentMusic;
    private String currentMusicName;
    private float masterVolume = 1.0f;

    // Listener-Position für 3D-Audio (standardmässig bildschirmmitte)
    private float listenerX = 400f;
    private float listenerY = 300f;

    private final AudioThread audioThread;

    /**
     * Privater Konstruktor, der vom Singleton-Muster verwendet wird.
     * Initialisiert und startet den Hintergrund-Thread für Audio-Aufgaben (wie z.B. Fading).
     */
    private AudioManager() {
        audioThread = new AudioThread();
        audioThread.start();
    }

    /**
     * Gibt die einzige Instanz des {@code AudioManager} zurück.
     * Falls diese noch nicht existiert, wird sie beim ersten Aufruf erstellt.
     *
     * @return Die globale {@code AudioManager}-Instanz.
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Lädt einen kurzen Soundeffekt (z.B. einen Sprung- oder Explosions-Sound) in den Speicher.
     * Standardmässig können bis zu 5 Instanzen dieses Sounds gleichzeitig abgespielt werden.
     *
     * @param name Der eindeutige Name, unter dem der Sound später aufgerufen wird (z.B. "jump").
     * @param path Der Dateipfad zur Audio-Datei.
     */
    public void loadSound(String name, String path) {
        loadSound(name, path, 5); //standard-poolgrösse: 5
    }

    /**
     * Lädt einen kurzen Soundeffekt in den Speicher und legt fest, wie oft dieser exakt gleiche
     * Sound gleichzeitig übereinander abgespielt werden darf.
     *
     * @param name     Der eindeutige Name für diesen Sound.
     * @param path     Der Dateipfad zur Audio-Datei.
     * @param poolSize Die maximale Anzahl, wie oft dieser Sound gleichzeitig hörbar sein kann (z.B. bei vielen gleichzeitigen Münzen-Einsammlungen).
     */
    public void loadSound(String name, String path, int poolSize) {
        if (soundPools.containsKey(name)) {
            return;
        }
        SoundPool pool = new SoundPool(name, path, poolSize);
        soundPools.put(name, pool);
    }

    /**
     * Spielt einen zuvor mit {@link #loadSound(String, String)} geladenen Soundeffekt ab.
     * Der Sound wird in der Mitte des Stereo-Panoramas (beide Lautsprecher gleich laut)
     * und in voller Lautstärke abgespielt.
     *
     * @param name Der Name des Sounds, der abgespielt werden soll.
     */
    public void playSound(String name) {
        playSoundWithPan(name, 1.0f, 0.0f);
    }

    /**
     * Spielt einen geladenen Soundeffekt mit spezifischer Lautstärke und räumlicher Verteilung (Stereo-Pan) ab.
     *
     * @param name   Der Name des abzuspielenden Sounds.
     * @param volume Die Lautstärke (1.0f ist Standard, 0.5f ist halbe Lautstärke).
     * @param pan    Die Stereo-Position: -1.0f (nur links), 0.0f (Mitte), 1.0f (nur rechts).
     */
    public void playSoundWithPan(String name, float volume, float pan) {
        SoundPool pool = soundPools.get(name);
        if (pool != null) {
             //float finalVolume = volume * masterVolume;
            pool.play(volume, pan);
        }
    }

    /**
     * Spielt einen Soundeffekt basierend auf Koordinaten in der Spielwelt ab (3D-Audio).
     * Die Lautstärke und das Stereo-Panning werden automatisch berechnet, je nachdem wie weit
     * die Soundquelle von der aktuellen Hörer-Position (Listener) entfernt ist.
     *
     * <pre>
     * # Beispiel Tigerjython - Eine Explosion auf Koordinate x=800, y=100
     * audio.playSound3D("explosion", 800, 100, 600)
     * </pre>
     *
     * @param name        Der Name des abzuspielenden Sounds.
     * @param sourceX     Die X-Koordinate, an der der Sound in der Spielwelt entsteht.
     * @param sourceY     Die Y-Koordinate, an der der Sound in der Spielwelt entsteht.
     * @param maxDistance Die maximale Distanz in Pixeln, bis zu der der Sound noch hörbar ist.
     *
     * @see #setListenerPosition(float, float)
     */
    public void playSound3D(String name, float sourceX, float sourceY, float maxDistance) {
        new AudioSource(sourceX, sourceY, name, maxDistance, this).play();
    }

    /**
     * Lädt ein Hintergrundmusik-Stück. Im Gegensatz zu Soundeffekten ist Musik für längere
     * Audio-Dateien gedacht (z.B. Level-Soundtracks) und benötigt andere Kontrollmechanismen.
     *
     * @param name Der eindeutige Name für dieses Musikstück (z.B. "level_theme").
     * @param path Der Dateipfad zur Audio-Datei.
     * @return Das geladene {@link Music}-Objekt.
     */
    public Music loadMusic(String name, String path) {
        if (musicCache.containsKey(name)) {
            return musicCache.get(name);
        }
        Music music = new Music(path);
        musicCache.put(name, music);
        return music;
    }

    /**
     * Startet ein zuvor geladenes Hintergrundmusik-Stück.
     * Falls bereits ein anderes Musikstück läuft, wird dieses gestoppt.
     *
     * @param name Der Name der Musik, die abgespielt werden soll.
     * @param loop {@code true}, wenn die Musik in einer Endlosschleife laufen soll, sonst {@code false}.
     */
    public void playMusic(String name, boolean loop) {
        if (currentMusicName != null && currentMusicName.equals(name)) {
            if (currentMusic != null) {
                currentMusic.setLooping(loop);
            }
            return;
        }

        Music music = musicCache.get(name);
        if (music == null) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("AudioManager", "Music name is not registered")
                            .withActual(name)
                            .withExpected("A name previously registered using loadMusic")
                            .withExplanation("The playback request is discarded. The current music remains unchanged"), null);
            return;
        }

        if (currentMusic != null) {
            audioThread.cancelFade(currentMusic.getClip());
            currentMusic.stop();
        }

        audioThread.cancelFade(music.getClip());
        music.setLooping(loop);
        music.setVolume(masterVolume);
        music.play();
        currentMusic = music;
        currentMusicName = name;
    }

    /**
     * Pausiert die momentan laufende Hintergrundmusik.
     * Sie kann später mit {@link #resumeMusic()} an derselben Stelle fortgesetzt werden.
     */
    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    /**
     * Setzt eine zuvor pausierte Hintergrundmusik exakt an der Stelle fort,
     * an der sie angehalten wurde.
     */
    public void resumeMusic() {
        if (currentMusic != null) {
            currentMusic.play();
        }
    }

    /**
     * Stoppt die momentan laufende Hintergrundmusik vollständig.
     * Ein erneutes Abspielen startet das Musikstück wieder von Anfang an.
     */
    public void stopMusic() {
        if (currentMusic != null) {
            audioThread.cancelFade(currentMusic.getClip());
            currentMusic.stop();
            currentMusic = null;
            currentMusicName = null;
        }
    }

    /**
     * Blendet ein Musikstück weich ein (Fade-in). Das Stück beginnt stumm und steigert
     * die Lautstärke über die angegebene Zeit bis zur vollen {@code masterVolume}.
     *
     * <pre>
     * # Beispiel Tigerjython - Musik über 3 Sekunden einblenden
     * audio.fadeInMusic("boss_theme", 3.0, True)
     * </pre>
     *
     * @param name            Der Name des Musikstücks, das eingeblendet werden soll.
     * @param durationSeconds Die Dauer des Einblendens in Sekunden.
     * @param loop            {@code true}, wenn die Musik als Endlosschleife laufen soll.
     */
    public void fadeInMusic(String name, float durationSeconds, boolean loop) {
        Validation.checkArgument(Float.isFinite(durationSeconds), ErrorDetails.of("AudioManager", "Invalid fade-in duration")
                        .withActual(durationSeconds)
                        .withExpected("A finite duration in seconds")
                        .withExplanation("NaN and Infinity do not allow for a reliably completable fade"));

        Music music = musicCache.get(name);

        if (music == null) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("AudioManager", "Music name for fade-in is not registered")
                            .withActual(name)
                            .withExpected("A name previously registered using loadMusic")
                            .withExplanation("The fade-in command is discarded. The existing music remains unchanged"), null);
            return;
        }

        synchronized (music) {
            try {
                FloatControl gainControl = (FloatControl) music.getClip().getControl(FloatControl.Type.MASTER_GAIN);
                float startDb = -80.0f;
                float targetDb = (float) (Maths.log(masterVolume == 0.0 ? 0.0001 : masterVolume) / Maths.log(10.0) * 20.0);

                if (currentMusic != null && currentMusic != music) {
                    audioThread.cancelFade(currentMusic.getClip());
                    currentMusic.stop();
                }

                audioThread.cancelFade(music.getClip());
                music.setLooping(loop);
                music.setVolume(masterVolume);

                gainControl.setValue(startDb);
                music.playFromStart();
                audioThread.addFadeTask(new FadeTask(music.getClip(), startDb, targetDb, durationSeconds, null));

                currentMusic = music;
                currentMusicName = name;
            } catch (Exception exception) {
                Logger.report(LogLevel.ERROR, ErrorDetails.of("AudioManager", "Fade-in could not be set up")
                                .withActual("name=" + name
                                                + ", durationSeconds=" + durationSeconds
                                                + ", masterVolume=" + masterVolume)
                                .withExpected("A loaded clip with an available MASTER_GAIN control and a supported level range")
                                .withExplanation("The fade operation was aborted at the point where the error occurred. Audio actions that have already been performed are not automatically undone"), exception);
            }
        }
    }

    /**
     * Blendet die momentan laufende Hintergrundmusik weich aus (Fade-out).
     * Nachdem die Lautstärke auf null gesunken ist, wird die Musik automatisch gestoppt.
     *
     * @param durationSeconds Die Dauer des Ausblendens in Sekunden.
     */
    public void fadeOutMusic(float durationSeconds) {
        Music fadingMusic = currentMusic;
        if (fadingMusic == null) {
            return;
        }

        Validation.checkArgument(Float.isFinite(durationSeconds), ErrorDetails.of("AudioManager", "Invalid fade-out duration")
                        .withActual(durationSeconds)
                        .withExpected("A finite duration in seconds")
                        .withExplanation("NaN and Infinity do not allow for a reliably completable fade"));

        synchronized (fadingMusic) {
            try {
                FloatControl gainControl = (FloatControl) currentMusic.getClip().getControl(FloatControl.Type.MASTER_GAIN);
                float currentDb = gainControl.getValue();
                float targetDb = -80.0f; // Stumm

                //callback sorgt, dass status resettet wird wenn fade fertig
                Runnable onComplete = () -> {
                    if (currentMusic == fadingMusic) {
                        currentMusic = null;
                        currentMusicName = null;
                    }
                };

                FadeTask task = new FadeTask(fadingMusic.getClip(), currentDb, targetDb, durationSeconds, onComplete);

                audioThread.cancelFade(fadingMusic.getClip());
                audioThread.addFadeTask(task);
            } catch (Exception exception) {
                Logger.report(LogLevel.ERROR, ErrorDetails.of("AudioManager", "Fade-out could not be set up")
                                .withActual(durationSeconds)
                                .withExpected("A finite duration and a loaded clip with MASTER_GAIN control")
                                .withExplanation("The new fade-out command was not set up successfully. In this case, the error does not automatically cause the music to stop"), exception);
            }
        }
    }

    /**
     * Setzt die globale Gesamtlautstärke (Master Volume) für alle Sounds und Musikstücke.
     *
     * @param volume Ein Wert zwischen 0.0f (komplett stumm) und 1.0f (volle Lautstärke).
     */
    public void setMasterVolume(float volume) {
        Validation.checkArgument(Float.isFinite(volume), ErrorDetails.of("AudioManager", "Invalid total volume")
                        .withActual(volume)
                        .withExpected("A finite number; values are limited to a range of 0 to 1")
                        .withExplanation("The value is passed on to all music and sound objects and must be valid before saving"));

        this.masterVolume = Maths.max(0f, Maths.min(1f, volume));

        for (SoundPool pool : soundPools.values()) {
            pool.setVolume(this.masterVolume);
        }

        for (Music music : musicCache.values()) {
            music.setVolume(this.masterVolume);
        }
    }

    /**
     * Stoppt sofort alle aktuell spielenden Soundeffekte und die Hintergrundmusik.
     */
    public void stopAll() {
        stopMusic();

        for (SoundPool pool : soundPools.values()) {
            pool.stopAll();
        }

    }

    /**
     * Gibt alle Ressourcen (Speicher) frei und beendet den Audio-Hintergrund-Thread.
     * Sollte aufgerufen werden, wenn das Spiel geschlossen wird, um Speicherlecks (Memory Leaks) zu vermeiden.
     */
    public void dispose() {
        audioThread.stopThread();
        stopAll();
        for (SoundPool pool : soundPools.values()) {
            pool.close();
        }
        for (Music m : musicCache.values()) {
            m.close();
        }
        soundPools.clear();
        musicCache.clear();
        currentMusic = null;
        currentMusicName = null;
        instance = null;
    }

    /**
     * Setzt die Position des Zuhörers (Listener) in der Spielwelt.
     * Dies wird für die 3D-Audio-Berechnung ({@link #playSound3D(String, float, float, float)}) verwendet.
     * Normalerweise ist dies die Position der Kamera oder des Spielcharakters.
     *
     * @param x Die X-Koordinate des Zuhörers.
     * @param y Die Y-Koordinate des Zuhörers.
     */
    public void setListenerPosition(float x, float y) {
        Validation.checkArgument(Float.isFinite(x) && Float.isFinite(y), ErrorDetails.of("AudioManager", "Invalid listener position")
                        .withActual("x=" + x + ", y=" + y)
                        .withExpected("Two finite world coordinates")
                        .withExplanation("The listener position is used for distance and stereo panning"));


        this.listenerX = x;
        this.listenerY = y;
    }

    /**
     * Gibt die aktuelle X-Koordinate des Zuhörers für 3D-Audio zurück.
     *
     * @return Die X-Koordinate des Zuhörers.
     */
    public float getListenerX() {
        return listenerX;
    }

    /**
     * Gibt die aktuelle Y-Koordinate des Zuhörers für 3D-Audio zurück.
     *
     * @return Die Y-Koordinate des Zuhörers.
     */
    public float getListenerY() {
        return listenerY;
    }

    /**
     * Gibt das aktuell laufende (oder pausierte) Hintergrundmusik-Objekt zurück.
     *
     * @return Das aktuelle {@link Music}-Objekt oder {@code null}, wenn keine Musik geladen/gespielt wird.
     */
    public Music getCurrentMusic() {
        return currentMusic;
    }

    /**
     * Gibt die aktuell eingestellte globale Lautstärke zurück.
     *
     * @return Die Gesamtlautstärke (zwischen 0.0f und 1.0f).
     */
    public float getMasterVolume() {
        return masterVolume;
    }
}
