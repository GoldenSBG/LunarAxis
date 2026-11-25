package LunarAxis.graphics.particle;

/**
 * Das {@code ParticleSpawnMode}-Enum bestimmt das räumliche Verteilungsmuster
 * (den Modus), nach dem Partikel bei einer Emission im Raum erzeugt werden.
 * <p>
 * So lässt sich festlegen, ob alle Partikel exakt an einem einzigen Punkt,
 * in einem flächigen Bereich, innerhalb eines Kreises oder nur auf dessen Rand entstehen.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 * @see ParticleEmission
 */
public enum ParticleSpawnMode {

    /**
     * Erzeugt Partikel zufällig verteilt innerhalb eines rechteckigen Bereichs
     */
    AREA,

    /**
     *  Erzeugt Partikel gefüllt innerhalb eines kreisartigen Radius
     */
    CIRCLE,

    /**
     *  Erzeugt Partikel ausschliesslich auf der äusseren Kreislinie (z.B. für ringförmige Schockwellen)
     */
    CIRCLE_EDGE,

    /**
     * Erzeugt alle Partikel exakt an einem gemeinsamen, zentralen Punkt
     */
    POINT
}
