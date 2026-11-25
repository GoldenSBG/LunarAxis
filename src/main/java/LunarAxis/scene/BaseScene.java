package LunarAxis.scene;

import LunarAxis.core.LunarEngine;
import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.particle.Particle;
import LunarAxis.graphics.particle.ParticleRenderer;
import LunarAxis.graphics.particle.ParticleSystem;
import LunarAxis.objects.Actor;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;
import LunarAxis.ui.core.UIManager;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.util.*;

/**
 * Die abstrakte Basisklasse {@code BaseScene} implementiert eine Szene mit eingebautem Actor- und Partikel-Management.
 * <p>
 * Verwaltet eine Liste von Actors und ruft automatisch {@code update()} und {@code drawImage()} auf allen Actors auf.
 * </p>
 *
 * <p>
 * Actors und Partikel werden beim Rendern anhand ihres Z-Index sortiert.
 * Kleinere Z-Index-Werte werden zuerst gezeichnet, grössere Werte liegen visuell darüber.
 * Dadurch wird ein korrekter, ebenenbasierter Zeichenablauf (Layering) zwischen Spielobjekten und Effekten gewährleistet.
 * </p>
 *
 * <p>
 * Der Entwickler muss lediglich {@code init()} implementieren und die gewünschten Actors hinzufügen.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public abstract class BaseScene implements Scene {

    protected final List<Actor> actors = new ArrayList<>();

    /**
     * Liste von Actors, die am Ende des Frames hinzugefügt werden.
     * Verhindert eine {@link ConcurrentModificationException} während der Update-Loop.
     */
    private final List<Actor> actorsToAdd = new ArrayList<>();
    private final List<Actor> actorsToRemove = new ArrayList<>();
    private final List<Actor> renderActors = new ArrayList<>();

    /**
     * Comparator zur Sortierung von Actors nach aufsteigendem Z-Index.
     */
    private static final Comparator<Actor> ACTOR_RENDER_ORDER = Comparator.comparingInt(Actor::getZIndex);
    /**
     * Comparator zur Sortierung von Partikeln nach aufsteigendem Z-Index.
     */
    private static final Comparator<Particle> PARTICLE_RENDER_ORDER = Comparator.comparingLong(Particle::getZIndex);

    private boolean initialized = false;

    /*
     * Optionales Partikelsystem
     */
    protected final ParticleSystem particleSystem;
    private final ParticleRenderer particleRenderer;
    protected final UIManager uiManager = new UIManager();

    /**
     * Wird nur während des Aufrufs durch einen SceneManager gesetzt.
     * So hängt das Verhalten nicht vom globalen LunarEngine-Singleton ab.
     */
    private SceneManager updatingSceneManager;

    /**
     * Erstellt eine Szene ohne Partikelsystem.
     * Partikel-Update, Partikel-Rendering und das Leeren des Partikelsystems beim Beenden der Szene werden in diesem Fall übersprungen.
     */
    protected BaseScene() {
        this(null, null);
    }

    /**
     * Erstellt eine Szene mit Partikelsystem und Partikel-Renderer.
     * Bestehende Szenen können diesen Konstruktor weiterhin verwenden.
     *
     * @param particleSystem   Das Partikelsystem der Szene.
     * @param particleRenderer Der Renderer für die Partikel.
     */
    protected BaseScene(ParticleSystem particleSystem, ParticleRenderer particleRenderer) {
        this.particleSystem = particleSystem;
        this.particleRenderer = particleRenderer;
    }

    /**
     * Fügt einen Actor zur Szene hinzu.
     * Ist sicher auch während {@code update()} aufrufbar.
     *
     * @param actor Der hinzuzufügende {@link Actor}.
     */
    public void addActor(Actor actor) {
        if (actor != null) {
            actorsToAdd.add(actor);
        }
    }

    /**
     * Entfernt einen Actor aus der Szene.
     * Ist sicher auch während {@code update()} aufrufbar.
     *
     * @param actor Der zu entfernende {@link Actor}.
     */
    public void removeActor(Actor actor) {
        if (actor != null) {
            actorsToRemove.add(actor);
        }
    }

    /**
     * Gibt eine Kopie aller Actors in dieser Szene zurück.
     *
     * @return Eine unmodifizierbare Kopie der Actor-Liste.
     */
    public List<Actor> getActors() {
        return new ArrayList<>(actors);
    }

    /**
     * Entfernt alle Actors aus der Szene.
     */
    public void clearActors() {
        actorsToRemove.addAll(actors);
    }

    /**
     * Gibt die Anzahl der Actors in der Szene zurück.
     *
     * @return Die Anzahl der Actors.
     */
    public int getActorCount() {
        return actors.size();
    }

    /**
     * Einstiegspunkt, den der SceneManager für normale Szenenupdates verwendet.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param manager   Der aufrufende {@link SceneManager}.
     */
    void updateFromManager(float deltaTime, SceneManager manager) {
        updatingSceneManager = manager;
        try {
            update(deltaTime);
        } finally {
            updatingSceneManager = null;
        }
    }

    /**
     * Einstiegspunkt für den Pausenpfad. Aktualisiert ausschliesslich das UI.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param manager   Der {@link SceneManager}.
     * @throws RuntimeException (Lunar-Error) wenn {@code deltaTime} ungültig ist.
     */
    void updateUiOnly(float deltaTime, SceneManager manager) {
        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("BaseScene", "UI update time must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite time interval")
                    .withExplanation("NaN and infinite time intervals cannot be forwarded to scene UI updates"), null);
        }

        updatingSceneManager = manager;
        try {
            updateSceneUi(deltaTime);
        } finally {
            updatingSceneManager = null;
        }
    }

    /**
     * Ruft {@code update()} auf allen Actors auf und verarbeitet anschliessend die Hinzufüge- und Löschqueues.
     * <p>
     * Beim Überschreiben dieser Methode für eigene Logik muss <strong>IMMER</strong> {@code super.update(deltaTime)} aufgerufen werden.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @throws RuntimeException (Lunar-Error) wenn {@code deltaTime} ungültig ist.
     */
    @Override
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("BaseScene", "Scene update time must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite time interval")
                    .withExplanation("NaN and infinite time intervals cannot be forwarded to particle, UI or actor updates"), null);
        }

        if (particleSystem != null) {
            particleSystem.update(deltaTime);
        }

        updateSceneUi(deltaTime);

        if (!isStillActiveScene()) {
            return;
        }

        /*
         * Snapshot verhindert, dass ein Actor, der während seines Updates die Szene entfernt, die laufende Iteration strukturell verändert
         */
        List<Actor> actorsToUpdate = new ArrayList<>(actors);
        for (Actor actor : actorsToUpdate) {
            if (!isStillActiveScene()) {
                return;
            }
            if (actors.contains(actor)) {
                actor.update(deltaTime);
            }
        }

        if (!isStillActiveScene()) {
            return;
        }

        processActorQueues();
    }

    /**
     * Interne Methode zur Aktualisierung aller UI-Elemente der Szene.
     *
     * @param deltaTime Die vergangene Zeit in Sekunden.
     */
    private void updateSceneUi(float deltaTime) {
        LunarEngine engine = LunarEngine.getInstance();
        InputManager input = engine != null ? engine.getInputManager() : null;

        if (input == null) {
            return;
        }

        List<UIElement> elements = uiManager.getElements();
        elements.sort(Comparator.comparingInt(UIElement::getZIndex).reversed());

        for (UIElement element : elements) {
            if (!isStillActiveScene()) {
                return;
            }

            /*
             * Ein Callback kann die Szene freigeben und dadurch die ursprüngliche UI-Liste leeren
             */
            if (uiManager.getElements().contains(element) && element.isVisible() && element.isEnabled()) {
                element.update(deltaTime, input);
            }
        }
    }

    /**
     * Prüft, ob diese Szene nach wie vor die vom SceneManager verwaltete aktive Szene ist.
     *
     * @return {@code true}, wenn diese Szene aktiv ist.
     */
    private boolean isStillActiveScene() {
        return updatingSceneManager == null || updatingSceneManager.getActiveScene() == this;
    }

    /**
     * Verarbeitet ausstehende Hinzufüge- und Lösch-Warteschlangen für Actors am Ende des Updates.
     */
    private void processActorQueues() {
        if (!actorsToAdd.isEmpty()) {
            actors.addAll(actorsToAdd);
            actorsToAdd.clear();
        }

        if (!actorsToRemove.isEmpty()) {
            actors.removeAll(actorsToRemove);
            actorsToRemove.clear();
        }
    }

    /**
     * Zeichnet alle sichtbaren Actors sowie Partikel verschränkt nach ihrem Z-Index und rendert anschliessend UI und Debug-Bounds.
     * <p>
     * Beim Überschreiben dieser Methode (z.B. für Hintergründe) muss <strong>IMMER</strong> {@code super.render(g)} aufgerufen werden.
     * </p>
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void render(Graphics g) {
        renderActors.clear();
        renderActors.addAll(actors);
        renderActors.sort(Comparator.comparingInt(Actor::getZIndex));

        List<Particle> renderParticles = Collections.emptyList();
        if (particleRenderer != null && particleSystem != null) {
            renderParticles = particleSystem.getParticles();

            // Nur sortieren, wenn mindestens ein Partikel aus der Reihenfolge geraten ist (Performance-Optimierung)
            for (int i = 1; i < renderParticles.size(); i++) {
                if (renderParticles.get(i - 1).getZIndex() > renderParticles.get(i).getZIndex()) {
                    renderParticles.sort(PARTICLE_RENDER_ORDER);
                    break;
                }
            }
        }

        int particleIndex = 0;
        for (Actor actor : renderActors) {
            if (!actor.isVisible()) {
                continue;
            }

            int actorZIndex = actor.getZIndex();
            while (particleIndex < renderParticles.size()
                    && renderParticles.get(particleIndex).getZIndex() < actorZIndex) {
                particleRenderer.renderParticle(g, renderParticles.get(particleIndex++));
            }

            actor.drawImage(g);
        }

        while (particleIndex < renderParticles.size()) {
            particleRenderer.renderParticle(g, renderParticles.get(particleIndex++));
        }


        uiManager.render(g);

        // Debug-Bounds liegen über den Szenen-Actors, Partikeln und UI
        // Actor.drawDebugBounds entscheidet pro Actor, ob etwas gezeichnet wird
        for (Actor actor : renderActors) {
            actor.drawDebugBounds(g);
        }
    }

    /**
     * Stellt sicher, dass {@code init()} nur genau einmal aufgerufen wird.
     * Wird intern vom SceneManager verwendet.
     */
    public void ensureInitialized() {
        if (!initialized) {
            init();
            initialized = true;
        }
    }

    /**
     * Räumt alle Ressourcen der Szene auf (löscht Actors, Partikel und UI).
     */
    @Override
    public void dispose() {
        actors.clear();
        actorsToAdd.clear();
        actorsToRemove.clear();
        renderActors.clear();

        if (particleSystem != null) {
            particleSystem.clear();
        }

        uiManager.clear();
        initialized = false;
    }

    /**
     * Gibt das Partikelsystem dieser Szene zurück.
     *
     * @return Das {@link ParticleSystem} oder {@code null}, wenn die Szene ohne Partikelsystem erstellt wurde.
     */
    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    /**
     * Gibt den UIManager dieser Szene zurück.
     *
     * @return Der {@link UIManager}.
     */
    public UIManager getUiManager() {
        return uiManager;
    }
}