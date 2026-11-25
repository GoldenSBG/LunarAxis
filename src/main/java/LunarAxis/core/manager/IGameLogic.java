package LunarAxis.core.manager;

import LunarAxis.core.LunarEngine;
import LunarAxis.inputs.MouseEvents;
import LunarAxis.scene.SceneManager;

import LunarAxis.graphics.Graphics;

/**
 * Das {@code IGameLogic}-Interface (das "I" steht für Interface) ist das zentrale Verbindungsstück
 * zwischen der LunarAxis-Engine und deinem eigenen Spiel.
 * <p>
 * Die Engine kümmert sich im Hintergrund um das Fenster, die Bildrate und das Timing. Damit sie aber weiss,
 * was <i>in deinem spezifischen Spiel</i> passieren soll, übergibst du ihr eine Klasse, die diesen
 * "Vertrag" (das Interface) erfüllt. Die Engine ruft dann in jedem Frame automatisch deine {@code update}-
 * und {@code render}-Methoden auf.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Eine einfache Spiel-Klasse
 * class MeinSpiel(IGameLogic):
 *
 *     def init(self, engine):
 *         self.x = 100
 *
 *     def update(self, deltaTime):
 *         # Spielfigur bewegen
 *         self.x += 50 * deltaTime
 *
 *     def render(self, graphics):
 *         # Spielfigur zeichnen
 *         graphics.setColor(Colors.RED)
 *         graphics.fillRect(self.x, 200, 50, 50)
 *
 * # Spiel an die Engine übergeben
 * engine.setGameLogic(MeinSpiel())
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public interface IGameLogic {

    /**
     * Wird einmalig ganz zu Beginn aufgerufen, kurz nachdem die Spiellogik an die Engine
     * übergeben wurde. Hier sollten alle Start-Werte, Variablen und Level-Daten geladen werden.
     *
     * @param lunarEngine Die Hauptinstanz der Engine, damit das Spiel Zugriff auf Audio, Inputs etc. hat.
     */
    void init(LunarEngine lunarEngine);

    /**
     * Wird automatisch in jedem Frame (Bildaufbau) aufgerufen.
     * Hier gehört die gesamte Logik deines Spiels hin: Bewegungen, Kollisionsabfragen,
     * Lebenspunkte-Berechnungen und das Auswerten von Tastendrücken.
     * <p>
     * <i>Hinweis: Da dies eine `default`-Methode ist, musst du sie in TigerJython nicht zwingend
     * aufschreiben, wenn dein Spiel gar keine durchgehende Logik braucht.</i>
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden (z.B. 0.016s).
     *                  Wichtig, um Bewegungen ruckelfrei zu berechnen.
     */
    default void update(float deltaTime) {
    }

    /**
     * Wird automatisch direkt nach dem {@code update} aufgerufen.
     * Hier kannst du eigene Formen, Texte oder Bilder frei auf den Bildschirm zeichnen.
     * Das Zeichnen von vorbereiteten Szenen (Scenes) übernimmt die Engine bereits selbst.
     *
     * @param graphics Das Zeichen-Werkzeug der Engine (ähnlich wie ein Pinsel).
     */
    default void render(Graphics graphics) {

    }

    // Default-Methoden ersparen dem User, alle Mouse-Events zwingend implementieren zu müssen

    /**
     * Wird automatisch ausgelöst, wenn der Spieler eine Maustaste drückt und sofort wieder loslässt (Klick).
     *
     * @param e Die Details zum Ereignis (z.B. X/Y-Position und welche Taste geklickt wurde).
     */
    default void mouseClicked(MouseEvents e) {}

    /**
     * Wird in dem exakten Moment ausgelöst, wenn der Spieler eine Maustaste herunterdrückt.
     *
     * @param e Die Details zum Maus-Ereignis.
     */
    default void mousePressed(MouseEvents e) {}

    /**
     * Wird in dem exakten Moment ausgelöst, wenn der Spieler eine gehaltene Maustaste wieder loslässt.
     *
     * @param e Die Details zum Maus-Ereignis.
     */
    default void mouseReleased(MouseEvents e) {}

    /**
     * Wird kontinuierlich ausgelöst, wenn der Spieler die Maus auf dem Bildschirm bewegt,
     * ohne dabei eine Taste zu drücken.
     *
     * @param e Die Details zur neuen Maus-Position.
     */
    default void mouseMoved(MouseEvents e) {}

    /**
     * Wird kontinuierlich ausgelöst, wenn der Spieler die Maus bewegt <i>und gleichzeitig</i>
     * eine Maustaste gedrückt hält (Ziehen / Drag and Drop).
     *
     * @param e Die Details zur Maus-Bewegung.
     */
    default void mouseDragged(MouseEvents e) {}
}
