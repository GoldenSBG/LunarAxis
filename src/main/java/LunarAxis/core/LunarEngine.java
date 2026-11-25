package LunarAxis.core;

import LunarAxis.audio.AudioManager;
import LunarAxis.core.game.GamePanel;
import LunarAxis.core.game.GameWindow;
import LunarAxis.core.inputs.InputManager;
import LunarAxis.core.manager.IGameLogic;
import LunarAxis.diagnostics.*;
import LunarAxis.graphics.Camera2D;
import LunarAxis.graphics.SpriteSheet;
import LunarAxis.scene.Scene;
import LunarAxis.scene.SceneManager;
import LunarAxis.ui.core.UIManager;
import LunarAxis.util.ResourceManager;
import LunarAxis.util.Scheduler;
import LunarAxis.util.Time;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Die Hauptklasse der LunarAxis-Engine.
 * <p>
 * Der {@code LunarEngine} steuert den gesamten Lebenszyklus eines Spiels.
 * Er öffnet das Fenster, regelt die Bildwiederholrate (FPS) über einen deterministischen
 * <i>Game-Loop</i> (Fixed Timestep) und verknüpft alle Kernsysteme (Grafik, Audio,
 * Input-Verarbeitung, Szenen-/Level-Management und UI) miteinander.
 * </p>
 * <p>
 * In TigerJython oder Standard-Java bildet diese Klasse den zentralen Einstiegspunkt der Anwendung.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython - Ein skalierbares Spielfenster öffnen
 * from LunarAxis.core import LunarEngine
 *
 * # Erstellt ein neues Spiel mit 800x600 Pixeln Basisauflösung und Titel
 * engine = LunarEngine(800, 600, "Mein erstes Spiel")
 * engine.enableResizableViewport() # Erlaubt Maximieren/Skalieren mit Seitenverhältnis
 * engine.setTargetFps(60)
 * engine.start()
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.8
 */
public class LunarEngine {

    /*
     * Statischer Initialisierer: Gibt beim ersten Laden der LunarEngine-Klasse
     * automatisch die Bibliotheks-Metadaten auf der Konsole aus.
     */
    static {
        LunarAxis.printStartupInfo();
    }


    /*
     * Das Hauptfenster des Spiels
     */
    private GameWindow gameWindow;

    /*
     * Das Spielpanel (JPanel)
     */
    private GamePanel gamePanel;

    private InputManager inputManager;
    private SceneManager sceneManager;
    private ResourceManager resourceManager;
    private UIManager uiManager;

    /*
     * Singleton-Instanz des LunarEngine-Objekts
     */
    private static LunarEngine instance;

    private IGameLogic iGameLogic;
    private volatile boolean isRunning = true;
    /*
     * Fixed Timestep / Accumulator System
     */
    private float targetDeltaTime = 1.0f / 60.0f;
    private float accumulator = 0.0f;

    /*
     * FPS System
     */
    private int targetFps = 60;
    private float currentFps = 0f;
    private volatile float lastLoopFrameTimeSeconds = 0f;

    private int frameCounter = 0;
    private long fpsTimer;

    /*
     * Framerate-Limiter (Sleep-Intervall)
     */
    private boolean useSleepLimiter = true;
    private long lastFrameTime = 0;

    /*
     * Volatile macht Änderungen an setPaused() auch sichtbar, wenn sie nicht vom Gameloop-Thread ausgelöst wurden
     */
    private volatile boolean paused = false;

    /**
     * Ein leerer Standard-Konstruktor.
     * <p>
     * <i>Achtung:</i> Dieser Konstruktor initialisiert noch kein Spielfenster oder Grafiksystem.
     * Er ist für spezialisierte Initialisierungsabläufe gedacht. Nutze standardmässig
     * {@link #LunarEngine(int, int, String)}.
     * </p>
     */
    public LunarEngine() {}

    /**
     * Initialisiert die Engine und erstellt das zugehörige Spielfenster.
     *
     * @param width  Die logische Breite des Spielfensters in Pixeln (muss &gt; 0 sein).
     * @param height Die logische Höhe des Spielfensters in Pixeln (muss &gt; 0 sein).
     * @param title  Der in der Titelleiste des Fensters angezeigte Text.
     * @throws RuntimeException (Lunar-Error) Wenn {@code width} oder {@code height} &le; 0 sind.
     */
    public LunarEngine(int width, int height, String title) {
        Validation.checkArgument(width > 0 && height > 0,
                ErrorDetails.of("LunarEngine", "Invalid game dimensions")
                        .withActual("width=" + width + ", height=" + height)
                        .withExpected("Positive width and height")
                        .withExplanation("The game requires a visible drawing area with positive dimensions"));

        instance = this;

        gameWindow = new GameWindow(width, height, title);
        gamePanel = gameWindow.getGamePanel();
        inputManager = gameWindow.getInputManager();

        sceneManager = new SceneManager();
        resourceManager = ResourceManager.getInstance();
        uiManager = new UIManager();
    }

    /**
     * Aktiviert die proportionale Skalierung des Spielfelds (Viewport) bei Fenstergrössenänderungen
     * oder beim Maximieren des Fensters.
     * <p>
     * Das ursprüngliche Seitenverhältnis (Aspect Ratio) der logischen Auflösung bleibt beim Vergrössern
     * oder Verkleinern des Fensters stets erhalten, indem schwarze Ränder (Letterboxing/Pillarboxing)
     * automatisch berechnet werden. Diese Methode muss <b>vor</b> dem Aufruf von {@link #start()} aufgerufen werden.
     * </p>
     *
     * @throws RuntimeException (Lunar-Error) Wenn die Engine mit dem leeren Konstruktor ohne Fenster instanziiert wurde.
     */
    public void enableResizableViewport() {
        if (gameWindow == null) {
            throw Errors.state(ErrorDetails.of("LunarEngine", "Cannot enable the viewport without a game window")
                    .withExpected("An engine constructed with width, height and title")
                    .withExplanation("The resizable viewport uses the existing game window and panel"), null);
        }

        gameWindow.enableResizableViewport();
    }

    /**
     * Startet den zentralen Game-Loop und macht das Spielfenster sichtbar.
     * <p>
     * Diese Methode blockiert den aufrufenden Thread bis zum Beenden des Spiels. Der Game-Loop arbeitet
     * nach einem Akkumulator-Prinzip (Fixed Timestep), um eine konstante Physik- und Spiellogik-Geschwindigkeit
     * unabhängig von den aktuellen Frames pro Sekunde zu garantieren.
     * </p>
     *
     * @throws RuntimeException (Lunar-Error) Wenn das Fenstersystem nicht initialisiert wurde oder während des Loops ein unbehandelter Fehler auftritt.
     */
    public void start() {
        if (gameWindow == null) {
            throw Errors.nullValue(ErrorDetails.of("LunarEngine", "Game window is missing")
                            .withActual(null)
                            .withExpected("An engine initialized with width, height and title")
                            .withExplanation("The parameterless constructor does not initialize the game window"), null);
        }
        Logger.info("LunarEngine", "Starting engine with target FPS: " + targetFps);

        try {
            gameWindow.show(true);

            long startTime = System.nanoTime();
            long lastTime = startTime;
            lastFrameTime = startTime;
            fpsTimer = System.currentTimeMillis();

            while (isRunning) {
                long now = System.nanoTime();

                long loopFrameNanos = now - lastFrameTime;
                lastFrameTime = now;
                lastLoopFrameTimeSeconds = loopFrameNanos / 1_000_000_000.0f;

                float elapsedTime = (float) ((now - lastTime) / 1_000_000_000.0);
                lastTime = now;

                if (elapsedTime > 0.25f) {
                    elapsedTime = 0.25f;
                }

                if (paused) {
                    /*
                     * Pausenzeit wird nicht für spätere Simulationsupdates angesammelt
                     * Der update-aufruf verarbeitet weiterhin spiellogik-eingaben und das UI-System
                     */
                    accumulator = 0.0f;
                    update(targetDeltaTime);
                } else {
                    accumulator += elapsedTime;

                    while (accumulator >= targetDeltaTime) {
                        update(targetDeltaTime);
                        accumulator -= targetDeltaTime;
                        /*
                         * Falls setPaused() während dieses updates aufgerufen wurde, werden restliche simulationsschritte verworfen
                         */
                        if (paused) {
                            accumulator = 0.0f;
                            break;
                        }
                    }
                }

                //FPS-Berechnung
                if (System.currentTimeMillis() - fpsTimer >= 1000) {
                    currentFps = frameCounter;
                    frameCounter = 0;
                    fpsTimer += 1000;
                    Logger.debug("LunarEngine", "Current FPS: " + currentFps);
                }

                if (useSleepLimiter) {
                    long workTime = System.nanoTime() - lastFrameTime;
                    long targetFrameTime = 1_000_000_000 / targetFps;

                    if (workTime < targetFrameTime) {
                        long sleepTime = (targetFrameTime - workTime) / 1_000_000L;
                        if (sleepTime > 0) {
                            Time.sleep(sleepTime);
                        }
                    }
                } else {
                    // Fallback: kurzes Sleep, um CPU-Auslastung zu reduzieren
                    Time.sleep(1);
                }

                // repaint auslösen
                gameWindow.getGamePanel().repaint();
                frameCounter++;
            }
        } catch (RuntimeException exception) {
            if (Errors.detailsOf(exception) == null) {
                Errors.describe(exception,
                        ErrorDetails.of("LunarEngine", "Engine execution failed")
                                .withExpected("A game loop that completes its operations without an unhandled exception")
                                .withExplanation("Execution was aborted by a runtime exception; the original exception is passed back to the caller"));
            }

            try {
                stop();
            } catch (RuntimeException cleanupFailure) {
                if (cleanupFailure != exception) {
                    exception.addSuppressed(cleanupFailure);
                }
            }
            throw exception;
        }
    }

    /**
     * Stoppt den Game-Loop geordnet, gibt alle geladenen Ressourcen (Grafiken, Audio-Buffer) frei
     * und schliesst das Hauptfenster.
     * <p>
     * Die Methode stellt sicher, dass auch bei Exceptions in Teilbereichen das Herunterfahren aller
     * Komponenten versucht wird und Fehler als unterdrückte Ausnahmen (Suppressed Exceptions) weitergegeben werden.
     * </p>
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        Logger.info("LunarEngine", "Stopping engine");
        isRunning = false;
        RuntimeException failure = null;

        if (sceneManager != null) {
            try {
                sceneManager.disposeAllScenes();
            } catch (RuntimeException exception) {
                failure = exception;
            }
        }

        try {
            AudioManager.getInstance().dispose();
        } catch (RuntimeException exception) {
            if (failure == null) {
                failure = exception;
            } else if (failure != exception) {
                failure.addSuppressed(exception);
            }
        }

        if (gameWindow != null) {
            try {
                gameWindow.dispose();
            } catch (RuntimeException exception) {
                if (failure == null) {
                    failure = exception;
                } else if (failure != exception) {
                    failure.addSuppressed(exception);
                }
            }
        }

        if (failure != null) {
            if (Errors.detailsOf(failure) == null) {
                Errors.describe(failure,
                        ErrorDetails.of("LunarEngine", "Engine cleanup failed")
                                .withExpected("Scene, audio and window cleanup without exceptions")
                                .withExplanation("Cleanup was attempted for all three components; additional cleanup failures are attached as suppressed exceptions"));
            }
            throw failure;
        }

        Logger.info("LunarEngine", "Engine stopped");
    }

    /**
     * Führt einen einzelnen Simulationsschritt aller Engine-Subsysteme aus.
     * <p>
     * Die Methode synchronisiert Eingabedaten an Viewport-Transformationen, aktualisiert den Scheduler,
     * führt benutzerdefinierte Spiellogik ({@link IGameLogic}) aus, berechnet Szenenänderungen sowie
     * UI-Interaktionen und schliesst die Eingabeverarbeitung für den aktuellen Frame ab.
     * Bei aktiver Pause werden Spielwelt-Simulationen ausgesetzt, während UI und Menü-Eingaben aktiv bleiben.
     * </p>
     *
     * @param deltaTime Die verstrichene Zeit für diesen Simulationsschritt in Sekunden.
     */
    private void update(float deltaTime) {
        if (inputManager != null) {
            inputManager.beginViewportUpdate();
        }

        /*
         * Scheduler-Aufgaben gehören zur automatischen Simulation und werden während der Pause nicht ausgeführt
         */
        if (!paused) {
            Scheduler.update(deltaTime);
        }

        /*
         * Spiellogik bleibt aktiv, damit der User eigene Eingaben und Resume-Aktionen verarbeiten kann (kein Gameloop-Freeze)
         */
        if (iGameLogic != null) {
            iGameLogic.update(deltaTime);
        }

        if (sceneManager != null) {
            if (paused) {
                /*
                 * Verarbeitet im SceneManager nur UI-System und Szenenwechsel, aber kein nomrlaes Szene-/Actors-Update
                 */
                sceneManager.updatePaused(deltaTime);
            } else {
                sceneManager.update(deltaTime);
            }
        }
        /*
         * Engine-UI und die Eingabeflanken werden auch während der Pause aktualisiert
         * InputManager.update() wird genau einmal am Ende dieses Engine-Updates aufgerufen
         */

        if (uiManager != null) {
            uiManager.update(deltaTime, inputManager);
        }

        if (inputManager != null) {
            inputManager.update();
        }
    }

    /**
     * Bindet eine externe Spiellogik-Klasse an die Engine an und ruft deren Initialisierung auf.
     *
     * @param iGameLogic Das {@link IGameLogic}-Objekt, das die spezifische Logik der Anwendung enthält.
     */
    public void setGameLogic(IGameLogic iGameLogic) {
        this.iGameLogic = iGameLogic;
        if (iGameLogic != null) {
            iGameLogic.init(this);
        }
    }

    /**
     * Registriert eine neue {@link Scene} im {@link SceneManager} unter einem eindeutigen Namen.
     *
     * @param name  Der Identifikationsname der Szene (z. B. "Level1" oder "MainMenu").
     * @param scene Die hinzuzufügende {@link Scene}-Instanz.
     */
    public void addScene(String name, Scene scene) {
        sceneManager.addScene(name, scene);
    }

    /**
     * Setzt eine zuvor registrierte Szene sofort als aktiv.
     *
     * @param name Der Name der zu aktivierenden Szene.
     */
    public void setActiveScene(String name) {
        sceneManager.setActiveScene(name);
    }

    /**
     * Veranlasst einen verzögerten Wechsel zur angegebenen Szene am Ende des laufenden Frame-Updates.
     *
     * @param name Der Name der anzufordernden Szene.
     */
    public void requestActiveScene(String name) {
        if (sceneManager == null) {
            Logger.report(LogLevel.ERROR,
                    ErrorDetails.of("LunarEngine", "Deferred scene change cannot be requested")
                            .withActual("sceneManager=null, requestedScene=" + name)
                            .withExpected("An initialized SceneManager")
                            .withExplanation("The request was discarded because this engine has no scene manager"),
                    null);
            return;
        }
        sceneManager.requestActiveScene(name);
    }

    /**
     * Initiert einen weichen, überblendenden Szenenwechsel (Fade-Out zu Schwarz, gefolgt von Fade-In in die Zielszene).
     *
     * <pre>
     * # Beispiel TigerJython - Sanfter Übergang ins Hauptmenü über 1.5 Sekunden
     * engine.transitionTo("MainMenu", 1.5)
     * </pre>
     *
     * @param name     Der Name der Zielszene.
     * @param duration Die Gesamtdauer der Überblendung in Sekunden.
     */
    public void transitionTo(String name, double duration) {
        if (sceneManager == null) {
            Logger.report(LogLevel.ERROR,
                    ErrorDetails.of("LunarEngine", "Scene transition cannot be requested")
                            .withActual("sceneManager=null, requestedScene=" + name + ", duration=" + duration)
                            .withExpected("An initialized SceneManager")
                            .withExplanation("The transition request was discarded because this engine has no scene manager"),
                    null);
            return;
        }
        sceneManager.transitionTo(name, duration);
    }

    /**
     * Aktiviert oder deaktiviert den Framerate-Limiter.
     * Wenn deaktiviert, läuft die Engine so schnell wie möglich (z.B. hunderte Frames pro Sekunde).
     *
     * <pre>
     * # Beispiel Tigerjython
     * engine.setFpsLimiterEnabled(False)  # Unbegrenzte FPS (für Benchmarks)
     * </pre>
     *
     * @param enabled {@code true}, um das FPS-Target einzuhalten, {@code false} für unbegrenzte Frame-Raten.
     */
    public void setFpsLimiterEnabled(boolean enabled) {
        this.useSleepLimiter = enabled;
    }

    /**
     * Prüft, ob der Framerate-Limiter aktiv ist.
     *
     * @return {@code true}, wenn die Bildrate begrenzt wird, sonst {@code false}.
     */
    public boolean isFpsLimiterEnabled() {
        return this.useSleepLimiter;
    }

    /**
     * Setzt die angestrebte Bildwiederholrate (FPS) der Engine.
     * Passt gleichzeitig das interne {@code targetDeltaTime} für den Fixed-Timestep-Loop an.
     *
     * @param fps Die Ziel-Bildrate (muss &gt; 0 sein).
     * @throws RuntimeException (Lunar-Error) Wenn {@code fps} &le; 0 ist.
     */
    public void setTargetFps(int fps) {
        if (fps <= 0) {
            throw Errors.lunar(
                    ErrorDetails.of("LunarEngine", "Invalid target frame rate")
                            .withActual(fps)
                            .withExpected("An integer greater than 0")
                            .withExplanation("The frame rate is used as a divisor for the update interval and frame limiter"),
                    null);
        }

        this.targetFps = fps;
        this.targetDeltaTime = 1.0f / fps;
        Logger.info("Engine", "FPS limit set to " + fps + " (DeltaTime: " + targetDeltaTime + "s)");
    }

    /**
     * Prüft, ob das Spielfenster gerade ausgewählt (im Fokus) ist.
     *
     * <pre>
     * # Beispiel Tigerjython - Das Spiel pausieren, wenn der Spieler heraustabbt
     * if not engine.hasFocus():
     *     engine.setPaused(True)
     * </pre>
     *
     * @return {@code true}, wenn das Fenster fokussiert ist, sonst {@code false}.
     */
    public boolean hasFocus() {
        return gameWindow != null && gameWindow.hasFocus();
    }

    /**
     * Gibt die aktuell gemessene Bildwiederholrate als handlichen Text zurück.
     * Nützlich, um die FPS direkt als Text auf dem Bildschirm anzuzeigen.
     *
     *
     * @return Ein String der Form {@code "60.0 FPS"}.
     */
    public String getFpsString() {
        return String.format("%.1f FPS", currentFps);
    }

    /**
     * Aktiviert oder beendet die globale Engine-Pause.
     * <p>
     * Während der Pause werden Bewegungen, Animationen und Scheduler-Aufgaben (Timers) angehalten.
     * Eingaben (Maus/Tastatur), Buttons und Menüs funktionieren jedoch weiterhin,
     * damit man das Spiel über ein Pausen-Menü auch wieder fortsetzen kann.
     * </p>
     *
     * @param paused {@code true}, um das Spiel zu pausieren, {@code false} um es fortzusetzen.
     */
    public void setPaused(boolean paused) {
        if (this.paused == paused) {
            return;
        }
        this.paused = paused;
        Logger.info("LunarEngine", "LunarEngine " + (paused ? "paused" : "resumed"));
    }

    /**
     * Prüft, ob das Spiel momentan pausiert ist.
     *
     * @return {@code true} falls pausiert, sonst {@code false}.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Gibt die aktuell im Spiel gemessene Bildrate zurück.
     *
     * @return Die aktuellen FPS.
     */
    public float getCurrentFps() {
        return currentFps;
    }

    /**
     * Gibt die eingestellte Ziel-Bildrate zurück.
     *
     * @return Die gewünschten FPS.
     */
    public int getTargetFps() {
        return targetFps;
    }


    /**
     * Gemessene Zeit zwischen zwei Game-Loop-Starts in Sekunden.
     *
     * @return Die Loop-Zeit in Sekunden.
     */
    public float getLastLoopFrameTimeSeconds() {
        return lastLoopFrameTimeSeconds;
    }

    /**
     * Delta-Zeit, die bei jedem festen Update an die Update-Methoden (wie {@code update(deltaTime)})
     * übergeben wird. Dies entspricht in der Regel {@code 1 / TargetFPS}.
     *
     * @return Die fixe Delta-Zeit in Sekunden.
     */
    public float getTargetDeltaTimeSeconds() {
        return targetDeltaTime;
    }

    /**
     * Tatsächlich gezeichnete Bilder pro Sekunde, direkt am Zeichnungs-Panel gemessen.
     *
     * @return Die Grafik-FPS.
     */
    public float getRenderedFps() {
        return gamePanel != null ? gamePanel.getRenderedFps() : 0.0f;
    }

    /**
     * Zeit zwischen zwei Zeichen-Aufrufen in Sekunden.
     *
     * @return Zeitabstand des Renderns in Sekunden.
     */
    public float getLastRenderFrameTimeSeconds() {
        return gamePanel != null ? gamePanel.getLastRenderFrameTimeSeconds() : 0.0f;
    }

    /**
     * Dauer des letzten reinen Zeichen-Aufrufs in Sekunden.
     * Nützlich um zu prüfen, ob zu viele Grafiken auf dem Bildschirm das Spiel verlangsamen.
     *
     * @return Die Zeichnungs-Dauer.
     */
    public float getLastRenderDurationSeconds() {
        return gamePanel != null ? gamePanel.getLastRenderDurationSeconds() : 0.0f;
    }

    /**
     * Gibt die zentrale (Singleton) Instanz der Engine zurück, falls man von überall
     * im Code aus darauf zugreifen muss.
     *
     * @return Das globale {@link LunarEngine}-Objekt.
     */
    public static LunarEngine getInstance() {
        return instance;
    }

    /**
     * Gibt den InputManager zurück, der für alle Tasten- und Mauseingaben verantwortlich ist.
     *
     * @return Der aktuelle {@link InputManager}.
     */
    public InputManager getInputManager() {
        return inputManager;
    }

    /**
     * Gibt den SceneManager zurück, der das Wechseln von Leveln und Menüs verwaltet.
     *
     * @return Der aktuelle {@link SceneManager}.
     */
    public SceneManager getSceneManager() {
        return sceneManager;
    }

    /**
     * Gibt die aktuell eingebundene Spiellogik-Klasse zurück.
     *
     * @return Das hinterlegte {@link IGameLogic}-Objekt.
     */
    public IGameLogic getIGameLogic() {
        return iGameLogic;
    }

    /**
     * Gibt den ResourceManager zurück, der beim Laden und Speichern von Grafiken hilft.
     *
     * @return Der zentrale {@link ResourceManager}.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Gibt den globalen AudioManager zurück.
     *
     * @return Die einzige Instanz des {@link AudioManager}s.
     */
    public AudioManager getAudioManager() {
        return AudioManager.getInstance();
    }

    /**
     * Gibt den UIManager zurück, der Buttons, Panels und Textfelder auf dem Bildschirm verwaltet.
     *
     * @return Der {@link UIManager}.
     */
    public UIManager getUiManager() {
        return uiManager;
    }


    /**
     * Weist dem Zeichenpanel eine virtuelle Kamera zu.
     * Damit lässt sich der sichtbare Bildausschnitt (z.B. in einem grossen Level) verschieben.
     *
     * @param camera Das anzuhängende {@link Camera2D}-Objekt.
     */
    public void setCamera(Camera2D camera) {
        if (gamePanel != null) {
            gamePanel.setCamera(camera);
        }
    }

    /**
     * Gibt die momentan aktive Kamera des Spiels zurück.
     *
     * @return Das aktive {@link Camera2D}-Objekt oder {@code null}, falls keine gesetzt ist.
     */
    public Camera2D getCamera() {
        return gamePanel != null ? gamePanel.getCamera() : null;
    }

    /**
     * Hilfsmethode, die ein einzelnes Bild bequem über den ResourceManager lädt.
     *
     * @param path Der Dateipfad zum Bild.
     * @return Das geladene {@link BufferedImage}.
     */
    public BufferedImage loadImage(String path) {
        return resourceManager.loadImage(path);
    }

    /**
     * Hilfsmethode, die einen grossen Grafik-Bogen (SpriteSheet) über den ResourceManager lädt
     * und direkt in kleine Einzelbilder zerteilt.
     *
     * @param path   Der Dateipfad zum grossen Bild.
     * @param height Die Höhe eines einzelnen Feldes (Sprites) in Pixeln.
     * @param width  Die Breite eines einzelnen Feldes (Sprites) in Pixeln.
     * @return Das fertig zerlegte {@link SpriteSheet}.
     */
    public SpriteSheet loadSpriteSheet(String path, int height, int width) {
        return resourceManager.loadSpriteSheet(path, width, height);
    }

    /**
     * Hilfsmethode, die eine eigene Schriftart (.ttf) über den ResourceManager lädt.
     *
     * @param name Der Name, unter dem die Schrift intern gespeichert wird.
     * @param path Der Dateipfad zur Schriftart-Datei.
     * @return Das fertig geladene {@link Font}-Objekt.
     */
    public Font loadFont(String name, String path) {
        return resourceManager.loadFont(name, path);
    }
}
