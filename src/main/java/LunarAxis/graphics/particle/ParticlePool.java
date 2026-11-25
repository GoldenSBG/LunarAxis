package LunarAxis.graphics.particle;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse {@code ParticlePool} verwaltet einen Vorrat (Pool) von wiederverwendbaren {@link Particle}-Objekten.
 * <p>
 * <b>Warum ist das wichtig für die Performance?</b> In einem Spiel entstehen oft hunderte Partikel pro Sekunde.
 * Ständig neue Objekte im Arbeitsspeicher zu erschaffen und danach wieder zu löschen (Garbage Collection)
 * kostet extrem viel Rechenleistung und kann zu Rucklern im Spiel führen. Ein Pool hält einmal erstellte Partikel
 * bereit, schaltet sie bei Bedarf ein ({@code obtain}) und recycelt sie, wenn sie sterben ({@code free}).
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticlePool {

    private final List<Particle> particles;

    /**
     * Erstellt einen neuen Partikel-Pool mit einer festgelegten Startgrösse.
     *
     * @param initialSize Die Anzahl der Partikel, die direkt zu Beginn im Voraus im Speicher erzeugt werden sollen.
     */
    public ParticlePool(int initialSize) {
        if (initialSize < 0) {
            initialSize = 0;
        }

        particles = new ArrayList<>(initialSize);

        for (int i = 0; i < initialSize; i++) {
            particles.add(new Particle());
        }
    }

    /**
     * Sucht nach einem bereits existierenden, aber aktuell inaktiven Partikel im Pool und gibt es zurück.
     * <p>
     * Falls im Moment kein freies Partikel verfügbar ist (weil alle gerade im Einsatz sind),
     * wird automatisch ein neues Partikel erstellt und dem Pool hinzugefügt.
     * </p>
     *
     * @return Ein einsatzbereites, freies {@link Particle}-Objekt.
     */
    public Particle obtain() {
        for (Particle particle : particles) {
            if (!particle.alive) {
                return particle;
            }
        }

        /*
         * Pool ist voll.
         * automatisch erweitern
         */
        Particle particle = new Particle();
        particles.add(particle);

        return particle;
    }

    /**
     * Gibt ein nicht mehr benötigtes Partikel in den Pool zurück, indem seine Werte
     * zurückgesetzt und es deaktiviert wird.
     *
     * @param particle Das freizugebende {@link Particle}.
     */
    public void free(Particle particle) {
        if (particle == null) {
            return;
        }
        particle.reset();
    }

    /**
     * Setzt alle Partikel im gesamten Pool zurück und schaltet sie ab.
     */
    public void clear() {
        for (Particle particle : particles) {
            particle.reset();
        }
    }

    /**
     * Gibt die Gesamtzahl aller aktuell im Pool befindlichen Partikel zurück
     * (sowohl aktive als auch inaktive).
     *
     * @return Die Grösse des Pools.
     */
    public int size() {
        return particles.size();
    }

    /**
     * Gibt die zugrundeliegende Liste aller Partikel im Pool zurück.
     *
     * @return Die {@link List} der Partikel.
     */
    public List<Particle> getParticles() {
        return particles;
    }
}
