package LunarAxis.graphics.particle;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Die Klasse {@code ParticleSystem} verwaltet die Erstellung, Aktualisierung und Lebenszeit
 * aller aktiven Partikel in der Spielwelt.
 * <p>
 * Sie arbeitet eng mit dem {@link ParticlePool} zusammen, um Partikel effizient wiederzuverwenden
 * und Performance-Einbrüche durch ständige Objekterstellung im Speicher zu vermeiden.
 * In jedem Frame aktualisiert das System alle aktiven Partikel und räumt verstorbene Partikel automatisch auf.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Ein Partikel-System initialisieren und im Game-Loop aktualisieren
 * pSystem = ParticleSystem(300) # Erstellt ein System für bis zu 300 vorbereitete Partikel
 *
 * # In der update(deltaTime)-Methode:
 * pSystem.update(deltaTime)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticleSystem {

    private final ParticlePool pool;
    private final List<Particle> activeParticles;

    /**
     * Erstellt ein neues Partikelsystem mit einer vorgegebenen Anfangskapazität für den Pool.
     *
     * @param initialCapacity Anzahl der im Voraus vorbereiteten Partikel im Pool.
     */
    public ParticleSystem(int initialCapacity) {
        pool = new ParticlePool(initialCapacity);
        activeParticles = new ArrayList<>();
    }

    /**
     * Erstellt ein neues Partikelsystem mit einer Standardkapazität von 500 Partikeln.
     */
    public ParticleSystem() {
        this(500);
    }

    /**
     * Erstellt oder holt ein neues Partikel aus dem Pool, aktiviert es
     * und registriert es in der Liste der aktiven Partikel.
     *
     * @return Das neu erstellte bzw. reaktivierte {@link Particle}-Objekt.
     */
    public Particle createParticle() {
        Particle particle = pool.obtain();
        particle.alive = true;
        activeParticles.add(particle);

        return particle;
    }

    /**
     * Aktualisiert alle aktiven Partikel für das aktuelle Frame.
     * Verstorbene Partikel (deren Lebenszeit abgelaufen ist) werden automatisch aus der aktiven Liste
     * entfernt und an den Pool zurückgegeben.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @throws RuntimeException (Lunar-Error) Wenn {@code deltaTime} ungültig (NaN oder unendlich) ist.
     */
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("ParticleSystem", "Update duration must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite duration in seconds")
                    .withExplanation("NaN and infinite durations would corrupt particle movement and lifetime calculations"), null);
        }

        if (deltaTime <= 0) {
            return;
        }
        Iterator<Particle> iterator = activeParticles.iterator();

        while (iterator.hasNext()) {
            Particle particle = iterator.next();
            particle.update(deltaTime);

            if (!particle.alive) {
                iterator.remove();
                pool.free(particle);
            }
        }
    }

    /**
     * Räumt das System komplett auf, indem alle aktiven Partikel freigegeben
     * und an den Pool zurückgeschickt werden.
     */
    public void clear() {
        for (Particle particle : activeParticles) {
            pool.free(particle);
        }

        activeParticles.clear();
    }

    /**
     * Gibt die aktuelle Anzahl aller aktiven Partikel zurück.
     *
     * @return Die Anzahl aktiver Partikel.
     */
    public int getParticleCount() {
        return activeParticles.size();
    }
    
    /**
     * Gibt eine thread-sichere Momentaufnahme (Kopie) aller aktuell aktiven Partikel zurück.
     * <p>
     * Die zurückgegebene Liste kann vom Renderer sicher verwendet werden,
     * ohne dass gleichzeitige Änderungen im Partikelsystem zu einer {@code ConcurrentModificationException} führen.
     * </p>
     *
     * @return Eine neue {@link List} mit den aktuell aktiven Partikeln.
     */
    public synchronized List<Particle> getParticles() {
        return new ArrayList<>(activeParticles);
    }
    
    /**
     * Gibt die Gesamtanzahl aller vom Pool verwalteten Partikel (aktive und freie) synchronisiert zurück.
     *
     * @return Die Grösse des zugrundeliegenden {@link ParticlePool}s.
     */
    public synchronized int getPoolSize() {
        return pool.size();
    }
}
