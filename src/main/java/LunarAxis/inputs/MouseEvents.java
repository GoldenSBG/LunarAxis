package LunarAxis.inputs;

/**
 * Die Klasse {@code MouseEvents} wird verwendet, um Informationen über Mausereignisse zu speichern.
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class MouseEvents {

    /**
     * Das {@code MouseAction}-Enum speichert die Aktion, die durch das Mausereignis ausgelöst wurde.
     * Es speichert die Art der Aktion sowie Statusinformationen zur Interaktion.
     */
    public enum MouseAction {
        /**
         *  Maustaste wurde geklickt
         */
        CLICKED,

        /**
         * Maustaste wurde heruntergedrückt
         */
        PRESSED,

        /**
         * Maustaste wurde losgelassen
         */
        RELEASED,

        /**
         *  Maus wurde bewegt
         */
        MOVED,

        /**
         * Maus wurde bei gedrückter Taste bewegt (Drag)
         */
        DRAGGED,

        /**
         * Mausrad wurde gedreht
         */
        SCROLLED,

        /**
         * Mausrad wurde nach oben gedreht
         */
        WHEEL_UP,

        /**
         * Mausrad wurde nach unten gedreht
         */
        WHEEL_DOWN,

        /**
         * Maus hat das Fenster betreten
         */
        ENTERED,

        /**
         * Maus hat das Fenster verlassen
         */
        EXITED
    }

    /**
     * Die Koordinaten (X, Y) und die gedrückte Maustaste (falls vorhanden).
     */
    private final int x, y, button;

    /**
     * Die konkrete Mausaktion ({@link MouseAction}).
     */
    private final MouseAction mouseAction;

    /**
     * Initialisiert das MouseEvent mit den gegebenen Werten.
     *
     * @param x           Die X-Koordinate des Mausereignisses.
     * @param y           Die Y-Koordinate des Mausereignisses.
     * @param button      Die gedrückte Maustaste.
     * @param mouseAction Die ausgelöste {@link MouseAction}.
     */
    public MouseEvents(int x, int y, int button, MouseAction mouseAction) {
        this.x = x;
        this.y = y;
        this.button = button;
        this.mouseAction = mouseAction;
    }


    /**
     * Gibt die X-Koordinate des Mausereignisses zurück.
     *
     * @return Die X-Position in Pixeln.
     */
    public int getX() { return x; }

    /**
     * Gibt die Y-Koordinate des Mausereignisses zurück.
     *
     * @return Die Y-Position in Pixeln.
     */
    public int getY() { return y; }

    /**
     * Gibt die gedrückte Maustaste zurück.
     * Mögliche Werte sind unter anderem:
     * <ul>
     *   <li>{@code MouseEvent.NOBUTTON}</li>
     *   <li>{@code MouseEvent.BUTTON1}</li>
     *   <li>{@code MouseEvent.BUTTON2}</li>
     *   <li>{@code MouseEvent.BUTTON3} (Mäuse mit drei Tasten unterstützen diese)</li>
     * </ul>
     *
     * @return Der Code der gedrückten Maustaste.
     */
    public int getButton() { return button; }

    /**
     * Gibt die Mausaktion zurück.
     *
     * @return Die {@link MouseAction}.
     */
    public MouseAction getType() { return mouseAction; }
}
