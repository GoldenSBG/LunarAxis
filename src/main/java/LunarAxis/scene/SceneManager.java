package LunarAxis.scene;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;
import LunarAxis.graphics.Graphics;
import LunarAxis.math.Maths;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Die Klasse {@code SceneManager} verwaltet alle registrierten Szenen und steuert deren Lebenszyklus
 * sowie flüssige Szenenwechsel (Transitions).
 * <p>
 * Der SceneManager dient als zentrales Bindeglied zwischen der {@code LunarEngine} (Game-Loop)
 * und den einzelnen Szenen. Er leitet {@code update()} und {@code render()} an die aktuell aktive Szene weiter.
 * </p>
 *
 * <pre>
 * # Nutzung in TigerJython:
 * sceneManager.addScene("level1", myLevel)
 * sceneManager.setActiveScene("level1")
 * sceneManager.transitionTo("gameover", 1.5)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class SceneManager {

    /**
     * Map aller registrierten Szenen (Name -> Scene).
     */
    private final Map<String, Scene> scenes = new HashMap<>();

    /*
     * Identitätsbasierte Mengen: Zwei unterschiedliche Szenen dürfen unabhängig voneinander gleich implementiert sein.
     *
     * TECHNISCHER HINWEIS (IdentityHashMap & Set):
     * Eine IdentityHashMap vergleicht Schlüssel mittels Referenzgleichheit (==) anstelle der equals()-Methode.
     * Collections.newSetFromMap wandelt diese Map in ein Set um. Der generische Typ verwendet die Wrapper-Klasse Boolean
     * (Objekt-Referenz), da Java-Generics keine primitiven Datentypen (wie boolean) akzeptieren.
     */
    private final Set<Scene> initializedScenes = Collections.newSetFromMap(new IdentityHashMap<Scene, Boolean>());
    private final Set<Scene> disposedScenes = Collections.newSetFromMap(new IdentityHashMap<Scene, Boolean>());

    /**
     * Die aktuell aktive Szene.
     */
    private Scene activeScene;

    /**
     * Der Name der aktuell aktiven Szene.
     */
    private String activeSceneName;

    /**
     * Der Name der aktuell aktiven Szene.
     */
    private String pendingSceneName;

    /*
     * TECHNISCHER HINWEIS (volatile):
     * Das Schlüsselwort 'volatile' garantiert die Thread-Sichtbarkeit. Der Update-Thread schreibt den Transition-Zustand,
     * während der Render-Thread diesen liest. 'volatile' erzwingt, dass Lese- und Schreibzugriffe direkt im Hauptspeicher (RAM)
     * stattfinden und nicht in den CPU-Caches der einzelnen Threads zwischengespeichert werden.
     */
    private volatile boolean transitionActive;
    private volatile boolean awaitingBlackRender;
    private volatile float transitionAlpha;

    private String transitionTargetName;
    private float transitionDuration;
    private float transitionElapsed;
    private boolean fadingIn;

    /*
     * TECHNISCHER HINWEIS (AtomicBoolean & getAndSet):
     * AtomicBoolean stellt eine threadsichere boolesche Variable bereit.
     * Bestätigt, dass das GamePanel das vollständig schwarze Overlay im Renderdurchlauf gezeichnet hat.
     */
    private final AtomicBoolean blackFrameRendered = new AtomicBoolean(false);

    /**
     * Erstellt einen neuen SceneManager.
     */
    public SceneManager() {
    }

    /**
     * Registriert eine Szene unter einem eindeutigen Namen.
     *
     * @param name  Eindeutiger Name der Szene (z.B. "menu", "level1", "gameover").
     * @param scene Die zu registrierende {@link Scene}-Instanz.
     * @throws RuntimeException (Lunar-Error) Wenn der Name leer ist, die Szene {@code null} ist, bereits freigegeben wurde oder doppelt registriert werden soll.
     */
    public void addScene(String name, Scene scene) {
        if (name == null || name.trim().isEmpty()) {
            throw Errors.lunar(ErrorDetails.of("SceneManager", "Scene name must not be blank")
                    .withActual(name)
                    .withExpected("A non-null scene name containing non-whitespace characters")
                    .withExplanation("A scene requires a name before it can be registered and selected"), null);
        }
        if (scene == null) {
            throw Errors.lunar(ErrorDetails.of("SceneManager", "Scene must not be null")
                    .withActual(null)
                    .withExpected("A scene instance")
                    .withExplanation("A scene name cannot be registered without a scene instance"), null);
        }
        if (disposedScenes.contains(scene)) {
            throw Errors.lunar(ErrorDetails.of("SceneManager", "A disposed scene cannot be registered again")
                    .withActual(name)
                    .withExpected("A scene instance that has not been disposed by this manager")
                    .withExplanation("Create a new scene instance instead of registering an instance whose disposal has already been attempted"), null);
        }

        Scene existing = scenes.get(name);
        if (existing == scene) {
            return;
        }
        if (existing != null) {
            throw Errors.lunar(ErrorDetails.of("SceneManager", "Scene name is already registered")
                    .withActual(name)
                    .withExpected("An unused name or the same scene instance already registered under this name")
                    .withExplanation("Registering a different scene under an existing name would replace the current registration"), null);
        }

        for (Map.Entry<String, Scene> entry : scenes.entrySet()) {
            if (entry.getValue() == scene) {
                throw Errors.lunar(ErrorDetails.of("SceneManager", "Scene instance is already registered")
                        .withActual("requestedName=" + name + ", registeredName=" + entry.getKey())
                        .withExpected("One registered name per scene instance")
                        .withExplanation("The same scene instance cannot be registered under two different names in this manager"), null);
            }
        }
        scenes.put(name, scene);
    }

    /**
     * Entfernt eine registrierte Szene endgültig.
     * Eine aktive Szene wird zuerst pausiert. Die Szene wird danach freigegeben (disposed)
     * und kann nicht erneut registriert werden.
     *
     * @param name Name der zu entfernenden Szene.
     * @throws RuntimeException (Lunar-Error) Wenn der Name leer ist oder Fehler beim Pausieren/Freigeben auftreten.
     */
    public void removeScene(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw Errors.lunar(ErrorDetails.of("SceneManager", "Scene name must not be blank")
                    .withActual(name)
                    .withExpected("A non-null scene name containing non-whitespace characters")
                    .withExplanation("Removing a scene requires its registered name"), null);
        }

        Scene scene = scenes.remove(name);
        if (scene == null) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("SceneManager", "Scene removal rejected")
                    .withActual(name)
                    .withExpected("The name of a registered scene")
                    .withExplanation("No scene is registered under this name, so nothing was removed"), null);
            return;
        }

        if (name.equals(pendingSceneName)) {
            pendingSceneName = null;
        }

        if (transitionActive && (name.equals(transitionTargetName) || scene == activeScene)) {
            cancelTransition();
        }

        RuntimeException failure = null;

        if (scene == activeScene) {
            try {
                scene.onPause();
            } catch (RuntimeException exception) {
                failure = exception;
                if (Errors.detailsOf(exception) == null) {
                    Errors.describe(exception, ErrorDetails.of("SceneManager", "Scene pause callback failed during removal")
                            .withActual(name)
                            .withExpected("The scene pause callback to complete before disposal")
                            .withExplanation("The scene has been unregistered and will be deactivated; disposal will still be attempted"));
                }
            } finally {
                if (activeScene == scene) {
                    activeScene = null;
                    activeSceneName = null;
                }
            }
        }

        try {
            disposeScene(scene);
        } catch (RuntimeException exception) {
            if (Errors.detailsOf(exception) == null) {
                Errors.describe(exception, ErrorDetails.of("SceneManager", "Scene disposal failed")
                        .withActual(name)
                        .withExpected("The scene disposal callback to release its resources")
                        .withExplanation("The scene has been unregistered, but its disposal callback did not complete successfully"));
            }

            if (failure == null) {
                failure = exception;
            } else if (failure != exception) {
                failure.addSuppressed(exception);
            }
        }

        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Aktiviert eine Szene sofort ohne Übergangseffekt.
     *
     * @param name Name der zu aktivierenden Szene.
     */
    public void setActiveScene(String name) {
        if (transitionActive) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("SceneManager", "Immediate scene change rejected")
                    .withActual(name)
                    .withExpected("No active scene transition")
                    .withExplanation("The current transition must finish before an immediate scene change can be performed"), null);
            return;
        }
        if (!hasValidTarget(name)) {
            return;
        }

        pendingSceneName = null;
        activateScene(name);
    }

    /**
     * Plant einen Szenenwechsel für den nächsten sicheren Update-Punkt.
     * Mehrere identische Anforderungen sind wirkungsgleich.
     * Eine weitere Anforderung auf ein anderes Ziel wird abgelehnt, solange bereits ein Ziel vorgemerkt ist.
     *
     * @param activeSceneName Der Name der anzufordernden Szene.
     */
    public void requestActiveScene(String activeSceneName) {
        if (transitionActive) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("SceneManager", "Deferred scene change rejected")
                    .withActual(activeSceneName)
                    .withExpected("No active scene transition")
                    .withExplanation("A deferred scene change cannot be scheduled while a transition is active"), null);
            return;
        }
        if (!hasValidTarget(activeSceneName)) {
            return;
        }

        if (pendingSceneName != null) {
            if (pendingSceneName.equals(activeSceneName)) {
                return;
            }
            Logger.report(LogLevel.ERROR, ErrorDetails.of("SceneManager", "A different scene change is already pending")
                    .withActual("requested=" + activeSceneName + ", pending=" + pendingSceneName)
                    .withExpected("No pending scene change or another request for the same target")
                    .withExplanation("The existing deferred scene change is retained and the new request is rejected"), null);
            return;
        }

        if (scenes.get(activeSceneName) == activeScene) {
            return;
        }
        pendingSceneName = activeSceneName;
    }


    /**
     * Startet einen zeitbasierten Fade-out/Fade-in Übergang zu einer Zielszene.
     *
     * @param name     Zielszene.
     * @param duration Gesamtdauer des Übergangs in Sekunden; muss endlich &amp; positiv sein.
     */
    public void transitionTo(String name, double duration) {
        if (transitionActive) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("SceneManager", "Scene transition rejected")
                    .withActual(name)
                    .withExpected("No active scene transition")
                    .withExplanation("The current transition must finish before another transition can start"), null);
            return;
        }
        if (!hasValidTarget(name)) {
            return;
        }

        float fadeDuration = (float) duration;
        if (!Double.isFinite(duration) || duration <= 0.0d || duration > Float.MAX_VALUE || fadeDuration / 2.0f <= 0.0f) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("SceneManager", "Fade duration is invalid")
                    .withActual(duration)
                    .withExpected("A finite positive duration within the float range whose half remains greater than 0 after conversion")
                    .withExplanation("The transition stores time as float values and requires a positive duration for both fade phases"), null);
            return;
        }
        if (pendingSceneName != null) {
            Logger.report(LogLevel.WARN, ErrorDetails.of("SceneManager", "Scene transition rejected while an immediate cut is pending")
                    .withActual("requested=" + name + ", pending=" + pendingSceneName)
                    .withExpected("No pending deferred scene change")
                    .withExplanation("The existing deferred scene change is retained instead of starting a competing transition"), null);
            return;
        }
        if (scenes.get(name) == activeScene) {
            return;
        }

        transitionTargetName = name;
        transitionDuration = fadeDuration;
        transitionElapsed = 0.0f;
        transitionAlpha = 0.0f;
        fadingIn = false;
        awaitingBlackRender = false;
        blackFrameRendered.set(false);
        transitionActive = true;
    }

    /**
     * Bisherige öffentliche Manager-Signatur für Java-Aufrufer (Konvertiert float zu double).
     *
     * @param name     Name der Zielszene.
     * @param duration Dauer des Übergangs in Sekunden.
     */
    public void transitionTo(String name, float duration) {
        transitionTo(name, (double) duration);
    }

    /**
     * Führt vorgemerkte Wechsel aus und aktualisiert die aktive Szene.
     * Während des Fades wird die Szenenaktualisierung ausgesetzt.
     * Wird aus IGameLogic.update() aufgerufen.
     *
     * @param deltaTime Zeit seit dem letzten Frame in Sekunden.
     */
    public void update(float deltaTime) {
        processSceneChangesAndTransition(deltaTime);

        if (transitionActive || activeScene == null) {
            return;
        }

        Scene sceneToUpdate = activeScene;
        if (sceneToUpdate instanceof BaseScene) {
            ((BaseScene) sceneToUpdate).updateFromManager(deltaTime, this);
        } else {
            sceneToUpdate.update(deltaTime);
        }
    }

    /**
     * Verarbeitet Szenenwechsel und Transitionen, aktualisiert aber keine normale Szenensimulation.
     * Für {@link BaseScene} wird ausschliesslich das UI verarbeitet.
     *
     * @param deltaTime Vergangene Zeit seit dem letzten Frame.
     */
    public void updatePaused(float deltaTime) {
        processSceneChangesAndTransition(deltaTime);

        if (transitionActive || !(activeScene instanceof BaseScene)) {
            return;
        }

        Scene sceneAtUiStart = activeScene;
        ((BaseScene) sceneAtUiStart).updateUiOnly(deltaTime, this);
    }

    /**
     * Verarbeitet ausstehende Szenenwechsel und führt den Übergangs-Timer fort.
     */
    private void processSceneChangesAndTransition(float deltaTime) {
        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("SceneManager", "Scene update time must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite time interval")
                    .withExplanation("NaN and infinite time intervals cannot be used to update scenes or advance transitions"), null);
        }

        if (!transitionActive && pendingSceneName != null) {
            String requestedName = pendingSceneName;
            pendingSceneName = null;
            activateScene(requestedName);
        }

        if (transitionActive) {
            updateTransition(deltaTime);
        }
    }

    /**
     * Berechnet den Alpha-Wert des Fades und verwaltet den Übergangsmoment zwischen Fade-Out und Fade-In.
     */
    private void updateTransition(float deltaTime) {
        if (awaitingBlackRender) {
            if (blackFrameRendered.getAndSet(false)) {
                String targetName = transitionTargetName;

                /*
                 * Das Ziel wird erst nach dem schwarzen Renderdurchlauf aktiviert
                 * Diese Update-Runde aktualisiert die neue Szene noch nicht
                 */
                if (!activateScene(targetName)) {
                    cancelTransition();
                    return;
                }

                fadingIn = true;
                transitionElapsed = transitionDuration / 2.0f;
                transitionAlpha = 1.0f;
                awaitingBlackRender = false;
                Logger.debug("SceneManager", "Black frame confirmed; fade-in started with alpha=" + transitionAlpha);
            }
            // Szene bleibt während des Fades eingefroren, auch am Übergabepunkt
            return;
        }

        transitionElapsed +=  Maths.max(0.0f, deltaTime);
        float halfDuration = transitionDuration / 2.0f;

        if (!fadingIn) {
            if (transitionElapsed >= halfDuration) {
                /*
                 * Deckkraft exakt auf 1 setzen und auf den tatsächlichen Renderdurchlauf warten.
                 * Ein grosser deltaTime-Sprung überspringt den schwarzen Übergabepunkt dadurch nicht
                 */
                transitionElapsed = halfDuration;
                transitionAlpha = 1.0f;
                awaitingBlackRender = true;
                blackFrameRendered.set(false);
            } else {
                transitionAlpha = Maths.clamp(transitionElapsed / halfDuration);
            }
            return;
        }

        if (transitionElapsed >= transitionDuration) {
            finishTransition();
            return;
        }

        float rawAlpha = 1.0f - ((transitionElapsed - halfDuration) / halfDuration);
        transitionAlpha = Maths.max(0.0f, Maths.min(1.0f, rawAlpha));
    }

    /**
     * Beendet die Transition und setzt alle Übergangsvariablen zurück.
     */
    private void finishTransition() {
        transitionAlpha = 0.0f;
        transitionActive = false;
        awaitingBlackRender = false;
        transitionTargetName = null;
        transitionDuration = 0.0f;
        transitionElapsed = 0.0f;
        fadingIn = false;
        blackFrameRendered.set(false);
    }

    /**
     * Rendert das Abdunkelungs-Overlay nach dem Szenenrendern in Panel-/Bildschirmkoordinaten.
     * GamePanel setzt die Renderbestätigung erst nach dem Overlay-Zeichnen.
     *
     * @param graphics    Das {@link Graphics}-Objekt.
     * @param panelWidth  Breite des Zeichenbereichs.
     * @param panelHeight Höhe des Zeichenbereichs.
     */
    public void renderTransitionOverlay(Graphics graphics, int panelWidth, int panelHeight) {
        if (!transitionActive || graphics == null || panelWidth <= 0 || panelHeight <= 0 || transitionAlpha <= 0.0f) {
            return;
        }
        graphics.fillScreenRectWithAlpha(panelWidth, panelHeight, transitionAlpha);

        if (awaitingBlackRender && transitionAlpha >= 1.0f) {
            blackFrameRendered.set(true);
        }
    }

    /**
     * Steuert den Render-Aufruf an die aktive Szene.
     * Wird aus IGameLogic.render() aufgerufen.
     *
     * @param g Das {@link Graphics}-Objekt.
     */
    public void render(Graphics g) {
        if (activeScene != null) {
            activeScene.render(g);
        }
    }

    /**
     * Prüft, ob eine Zielszene unter diesem Namen registriert und zum Wechsel bereit ist.
     *
     * @param activeSceneName Der zu prüfende Szenenname.
     * @return {@code true}, wenn die Szene gültig und verfügbar ist.
     */
    public boolean hasValidTarget(String activeSceneName) {
        if (activeSceneName == null || !scenes.containsKey(activeSceneName)) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("SceneManager", "Target scene is not registered")
                    .withActual(activeSceneName)
                    .withExpected("The name of a registered scene")
                    .withExplanation("Register the target scene before requesting a scene change"), null);
            return false;
        }

        Scene target = scenes.get(activeSceneName);
        if (target == null || disposedScenes.contains(target)) {
            Logger.report(LogLevel.ERROR, ErrorDetails.of("SceneManager", "Target scene is unavailable")
                    .withActual(activeSceneName)
                    .withExpected("A registered scene whose disposal has not been attempted")
                    .withExplanation("The target cannot be activated because its instance is missing or has already entered disposal"), null);
            return false;
        }

        return true;
    }

    /**
     * Interne Methode zur eigentlichen Aktivierung der Zielszene inklusive Lifecycle-Callbacks.
     *
     * @param sceneName Name der zu aktivierenden Szene.
     * @return {@code true}, wenn die Aktivierung erfolgreich war.
     */
    private boolean activateScene(String sceneName) {
        if (!hasValidTarget(sceneName)) {
            return false;
        }

        Scene nextScene = scenes.get(sceneName);
        if (nextScene == activeScene) {
            activeSceneName = sceneName;
            return true;
        }

        Scene previousScene = activeScene;
        if (previousScene != null) {
            previousScene.onPause();
        }

        activeScene = nextScene;
        activeSceneName = sceneName;

        try {
            if (!initializedScenes.contains(nextScene)) {
                if (nextScene instanceof BaseScene) {
                    ((BaseScene) nextScene).ensureInitialized();
                } else {
                    nextScene.init();
                }
                initializedScenes.add(nextScene);
            }
            nextScene.onResume();
        } catch (RuntimeException | Error failure) {
            if (activeScene == nextScene) {
                activeScene = null;
                activeSceneName = null;
                cancelTransition();
            }

            if (Errors.detailsOf(failure) == null) {
                Errors.describe(failure, ErrorDetails.of("SceneManager", "Scene activation failed")
                        .withActual(sceneName)
                        .withExpected("Scene initialization and resume callbacks to complete successfully")
                        .withExplanation("The failing scene is not kept active; the original callback failure is propagated"));
            }
            throw failure;
        }

        return true;
    }

    /**
     * Gibt eine bestimmte Szene frei, sofern sie nicht bereits als disposed markiert wurde.
     *
     * @param scene Die freizugebende Szene.
     */
    private void disposeScene(Scene scene) {
        if (scene != null && disposedScenes.add(scene)) {
            scene.dispose();
        }
    }

    /**
     * Gibt alle registrierten Szenen endgültig frei (z. B. beim Engine-Stop).
     */
    public void disposeAllScenes() {
        pendingSceneName = null;
        cancelTransition();

        Set<Scene> scenesToDispose = Collections.newSetFromMap(new IdentityHashMap<Scene, Boolean>());
        scenesToDispose.addAll(scenes.values());

        activeScene = null;
        activeSceneName = null;

        RuntimeException failure = null;

        for (Scene scene : scenesToDispose) {
            try {
                disposeScene(scene);
            } catch (RuntimeException exception) {
                if (Errors.detailsOf(exception) == null) {
                    Errors.describe(exception, ErrorDetails.of("SceneManager", "Scene disposal failed")
                            .withActual(scene.getClass().getName())
                            .withExpected("Each scene disposal callback to complete successfully")
                            .withExplanation("Disposal of the remaining scenes will still be attempted before the original failure is propagated"));
                }

                if (failure == null) {
                    failure = exception;
                } else if (failure != exception) {
                    failure.addSuppressed(exception);
                }
            }
        }
        scenes.clear();

        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Bricht eine laufende Szenen-Transition ab und setzt alle Übergangsvariablen zurück.
     */
    private void cancelTransition() {
        transitionActive = false;
        awaitingBlackRender = false;
        transitionAlpha = 0.0f;
        transitionTargetName = null;
        transitionDuration = 0.0f;
        transitionElapsed = 0.0f;
        fadingIn = false;
        blackFrameRendered.set(false);
    }

    /**
     * Gibt die aktuell aktive Szene zurück.
     *
     * @return Die aktive {@link Scene} oder {@code null}.
     */
    public Scene getActiveScene() {
        return activeScene;
    }

    /**
     * Gibt den Namen der aktuell aktiven Szene zurück.
     *
     * @return Name der aktiven Szene oder {@code null}.
     */
    public String getActiveSceneName() {
        return activeSceneName;
    }

    /**
     * Prüft, ob eine Szene mit diesem Namen registriert ist.
     *
     * @param name Der zu prüfende Name.
     * @return {@code true}, wenn die Szene existiert.
     */
    public boolean hasScene(String name) {
        return scenes.containsKey(name);
    }

    /**
     * Gibt die Anzahl der registrierten Szenen zurück.
     *
     * @return Anzahl der Szenen.
     */
    public int getSceneCount() {
        return scenes.size();
    }
}
