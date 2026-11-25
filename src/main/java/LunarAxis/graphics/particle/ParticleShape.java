package LunarAxis.graphics.particle;


/**
 * Das {@code ParticleShape}-Enum definiert die verschiedenen geometrischen Formen,
 * die ein Partikel beim Rendern annehmen kann.
 * <p>
 * Je nach zugewiesener Form zeichnet der {@link ParticleRenderer} das Partikel
 * als Kreis, Quadrat, Linie, Dreieck, einzelnes Pixel, Textur oder Stern.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 * @see ParticleRenderer
 */
public enum ParticleShape {

    /**
     * Klassischer runder Kreis
     */
    CIRCLE,

    /**
     * Quadratische Form
     */
    SQUARE,

    /**
     * Linienförmiges Partikel (ideal für Regen oder Geschosse)
     */
    LINE,

    /**
     * Dreieckige Form
     */
    TRIANGLE,

    /**
     * Winziger Punkt aus genau einem Pixel (für sehr kleine Partikel wie Staub oder feine Funken)
     */
    PIXEL,

    /**
     * Bild-basierte Textur (für zukünftige Erweiterungen)
     */
    TEXTURE,

    /**
     * Sternenform
     */
    STAR
}
