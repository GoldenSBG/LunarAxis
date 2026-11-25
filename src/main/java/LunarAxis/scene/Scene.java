package LunarAxis.scene;

import LunarAxis.graphics.Graphics;

/**
 * Das Interface {@code Scene} bildet den zentralen Vertrag (Contract) für alle Szenen in der LunarAxis-Engine.
 * <p>
 * Jede Szene (wie z. B. ein Hauptmenü, ein Spiellevel, ein Pausenmenü oder ein Game-Over-Bildschirm)
 * implementiert dieses Interface, um eine einheitliche Steuerung von Initialisierung, Frame-Updates,
 * Rendering, Ressourcen-Aufräumung und Pausenlogik zu gewährleisten.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public interface Scene {

    /**
     * Wird einmalig aufgerufen, wenn die Szene zum ersten Mal aktiviert wird.
     * <p>
     * Hier werden anfängliche Ressourcen wie Actors erstellt, Bilder geladen,
     * UI-Elemente aufgebaut und Vorberechnungen durchgeführt.
     * </p>
     */
    void init();

    /**
     * Wird bei jedem Frame aus der Hauptschleife (Game-Loop) der Engine aufgerufen.
     * <p>
     * Dient zur Aktualisierung des Spiellogik-Zustands, von Eingaben, Physik,
     * Bewegungen und Animationen.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     */
    void update(float deltaTime);

    /**
     * Wird in jedem Frame aufgerufen, um die visuelle Darstellung der Szene zu zeichnen.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    void render(Graphics g);

    /**
     * Wird aufgerufen, wenn die Szene deaktiviert oder verlassen wird (z. B. bei einem Szenenwechsel).
     * <p>
     * Bietet eine Standardimplementierung, in der verwendete Ressourcen, Speicherstrukturen
     * oder Texturen sicher freigegeben werden können.
     * </p>
     */
    default void dispose() {}

    /**
     * Wird vom {@link SceneManager} aufgerufen, wenn die Szene pausiert wird (z. B. bei einem Overlay-Pausenmenü).
     */
    default void onPause() {}

    /**
     * Wird vom {@link SceneManager} aufgerufen, wenn eine pausierte Szene wieder in den Vordergrund tritt und fortgesetzt wird.
     */
    default void onResume() {}
}
