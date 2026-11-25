package LunarAxis.core.inputs;

import LunarAxis.core.LunarEngine;
import LunarAxis.core.game.GameWindow;
import LunarAxis.core.game.ViewportGeometry;
import LunarAxis.inputs.Key;
import LunarAxis.inputs.MouseEvents;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;
import java.awt.event.*;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Der {@code InputManager} fängt alle Tastatur- und Mauseingaben des Spielers ab und stellt sie
 * der Engine strukturiert zur Verfügung.
 * <p>
 * Er unterscheidet dabei präzise, ob eine Taste dauerhaft gehalten wird, ob sie in diesem Moment
 * neu gedrückt wurde oder ob der Spieler sie gerade losgelassen hat. Dies ist entscheidend,
 * um z.B. bei einem Tastendruck nur einmal zu schiessen, statt ununterbrochen.
 * </p>
 * <p>
 * Schülerinnen und Schüler greifen in der Regel über die Engine auf diese Klasse zu.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Spieler bewegen und springen lassen
 * input = engine.getInputManager()
 *
 * # Dauerhaftes Gedrückthalten (für flüssige Bewegung)
 * if input.isKeyDown(Key.D):
 *     player.moveRight()
 *
 * # Einmaliges Auslösen (für Sprung)
 * if input.isKeyPressed(Key.SPACE):
 *     player.jump()
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.7
 */
public class InputManager implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    private final Set<Integer> pressedKeys = new HashSet<>();

    /*
     * Die Tasten, die im VORHERIGEN Frame gedrückt waren
     * -> Flankenerkennung (just pressed / just released)
     */
    private final Set<Integer> previousPressedKeys = new HashSet<>();

    private final StringBuffer typedCharacters = new StringBuffer();

    private MouseEvents lastMouseEvent;
    private int mouseX, mouseY, mouseButton;
    private boolean mousePressed;

    /*
    * Maus-Flankenerkennung (just pressed / just released)
     */
    private boolean previousMousePressed = false;
    private int previousMouseButton = -1;

    private int scrollDelta = 0;

    private ViewportMouse viewportMouse;

    private static final class ViewportMouse {
        private final Supplier<ViewportGeometry> geometry;
        private final BitSet held = new BitSet();
        private final BitSet pressed = new BitSet();
        private final BitSet released = new BitSet();
        private final BitSet releasedInside = new BitSet();
        private final BitSet clickCandidates = new BitSet();
        private final BitSet clicked = new BitSet();

        private final BitSet frameHeld = new BitSet();
        private final BitSet framePressed = new BitSet();
        private final BitSet frameReleased = new BitSet();
        private final BitSet frameReleasedInside = new BitSet();
        private final BitSet frameClicked = new BitSet();

        private Point position;
        private Point framePosition;
        private MouseEvents event;
        private MouseEvents frameEvent;
        private int button;
        private int frameButton;

        private ViewportMouse(Supplier<ViewportGeometry> geometry) {
            this.geometry = geometry;
        }

        private void moveTo(Point panelPosition) {
            if (panelPosition == null) {
                position = null;
                return;
            }

            ViewportGeometry current = geometry.get();
            position = current == null ? null : current.mapToLogical(panelPosition.x, panelPosition.y);
        }

        private void record(MouseEvents.MouseAction action, int eventButton) {
            if (position != null) {
                event = new MouseEvents(position.x, position.y, eventButton, action);
            }
        }

        private void beginUpdate() {
            framePosition = position;
            frameButton = button;
            frameEvent = event;
            event = null;

            copyBits(held, frameHeld);
            copyBits(pressed, framePressed);
            copyBits(released, frameReleased);
            copyBits(releasedInside, frameReleasedInside);
            copyBits(clicked, frameClicked);

            pressed.clear();
            released.clear();
            releasedInside.clear();
            clicked.clear();
        }

        private void clear() {
            held.clear();
            pressed.clear();
            released.clear();
            releasedInside.clear();
            clickCandidates.clear();
            clicked.clear();
            frameHeld.clear();
            framePressed.clear();
            frameReleased.clear();
            frameReleasedInside.clear();
            frameClicked.clear();

            position = null;
            framePosition = null;
            event = null;
            frameEvent = null;
            button = 0;
            frameButton = 0;
        }

        private static void copyBits(BitSet source, BitSet destination) {
            destination.clear();
            destination.or(source);
        }
    }

    /**
     * Installiert die Geometrie-Quelle des Zeichen-Panels vor dem ersten Engine-Update.
     * <p>
     * Wird von {@link LunarEngine} bzw. {@link GameWindow} aufgerufen, um das logische Viewport-Mapping
     * für die Umrechnung von Bildschirm- in Spielkoordinaten einzurichten.
     * </p>
     *
     * @param geometry Ein {@link Supplier}, der die aktuelle {@link ViewportGeometry} liefert.
     * @throws RuntimeException (Lunar-Error) Wenn {@code geometry} {@code null} ist.
     */
    public synchronized void enableViewport(Supplier<ViewportGeometry> geometry) {
        if (geometry == null) {
            throw Errors.nullValue(ErrorDetails.of("InputManager", "Viewport geometry source is missing")
                    .withActual(null)
                    .withExpected("A supplier for the game panel geometry")
                    .withExplanation("Mouse events require the same geometry used to present the game image"), null);
        }

        if (viewportMouse == null) {
            viewportMouse = new ViewportMouse(geometry);
            clearViewportInput();
        }
    }

    /**
     * Aktualisiert die Zeigerposition im Viewport nach einer Geometrie- oder Fensteränderung auf dem EDT.
     *
     * @param panelPosition Die physische Mausposition auf dem Zeichen-Panel.
     */
    public synchronized void refreshViewportPointer(Point panelPosition) {
        if (viewportMouse != null) {
            viewportMouse.moveTo(panelPosition);
        }
    }

    /**
     * Bereitet die internen Mauszustände und Flanken für den nächsten Simulationsschritt der Engine vor.
     * <p>
     * Wird zu Beginn des Engine-Updates aufgerufen, um Puffer-Eingaben für das aktuelle Frame einzufrieren.
     * </p>
     */
    public synchronized void beginViewportUpdate() {
        if (viewportMouse != null) {
            viewportMouse.beginUpdate();
        }
    }

    /**
     * Setzt gehaltene Eingaben zurück und verwirft ausstehende Ereignisse nach einem Fokusverlust.
     */
    public synchronized void clearViewportInput() {
        if (viewportMouse != null) {
            viewportMouse.clear();
            pressedKeys.clear();
            previousPressedKeys.clear();
            typedCharacters.setLength(0);
            lastMouseEvent = null;
            scrollDelta = 0;
        }
    }

    /**
     * Prüft, ob sich der Mauszeiger aktuell innerhalb der logischen Spielfläche (Viewport) befindet.
     * <p>
     * Im klassischen Modus ohne Skalierung liefert die Methode stets {@code true}.
     * </p>
     *
     * @return {@code true}, wenn die Maus über dem Spielfeld liegt, sonst {@code false}.
     */
    public synchronized boolean isMouseInsideViewport() {
        return viewportMouse == null || viewportMouse.framePosition != null;
    }

    /**
     * Prüft, ob eine Maus-Geste beendet und die Taste losgelassen wurde (inklusive Loslassen ausserhalb der Spielfläche).
     *
     * @param button Die Maustaste (1 = Links, 2 = Mitte, 3 = Rechts).
     * @return {@code true}, wenn das Loslassen im aktuellen Frame registriert wurde.
     */
    public synchronized boolean isMouseGestureReleased(int button) {
        if (viewportMouse == null) {
            return isMouseReleased(button);
        }

        return button > 0 && viewportMouse.frameReleased.get(button);
    }

    /**
     * Prüft, ob eine bestimmte Taste auf der Tastatur im aktuellen Frame gedrückt gehalten wird.
     *
     * @param key Die zu überprüfende Taste aus der {@link Key}-Enumeration.
     * @return {@code true}, solange die Taste gedrückt bleibt.
     */
    public synchronized boolean isKeyDown(Key key) {
        return pressedKeys.contains(requireKeyCode(key));
    }

    /**
     * Prüft, ob eine Taste im aktuellen Frame frisch gedrückt wurde (Flankenerkennung).
     * Gibt NUR im allerersten Moment des Drückens {@code true} zurück. Danach ist das Resultat
     * {@code false}, selbst wenn der Spieler den Finger auf der Taste lässt.
     *
     * <pre>
     * # Beispiel Tigerjython:
     * if input.isKeyPressed(Key.SPACE):
     *     # triggert genau EINMAL pro drücken
     * </pre>
     *
     * @param key Die zu überprüfende Taste.
     * @return {@code true}, genau in dem Frame, in dem die Taste gedrückt wird.
     */
    public synchronized boolean isKeyPressed(Key key) {
        int code = requireKeyCode(key);
        return pressedKeys.contains(code) && !previousPressedKeys.contains(code);
    }

    /**
     * Prüft, ob eine Taste im aktuellen Frame losgelassen wurde.
     * Gibt NUR im allerersten Moment nach dem Loslassen {@code true} zurück.
     *
     * <pre>
     * # Beispiel Tigerjython:
     * if input.isKeyReleased(Key.SPACE):
     *     # führt Aktion beim Loslassen aus
     * </pre>
     *
     * @param key Die zu überprüfende Taste.
     * @return {@code true}, exakt dann, wenn der Spieler den Finger von der Taste nimmt.
     */
    public synchronized boolean isKeyReleased(Key key) {
        int code = requireKeyCode(key);
        return !pressedKeys.contains(code) && previousPressedKeys.contains(code);
    }

    /**
     * Interne Hilfsmethode, die den Java-Tastencode sicher ausliest.
     *
     * @param key Das {@link Key}-Objekt.
     * @return Der interne Integer-Code der Taste.
     * @throws IllegalArgumentException Wenn keine gültige Taste übergeben wurde (null).
     */
    private int requireKeyCode(Key key) {
        if (key == null) {
            throw Errors.nullValue(ErrorDetails.of("InputManager", "Key is missing")
                            .withActual(null)
                            .withExpected("A LunarAxis Key value")
                            .withExplanation("A keyboard query requires a key from which a key code can be obtained"),
                    null);
        }
        return key.getKeyCode();
    }

    /**
     * Aktualisiert den internen Zustand für die Flankenerkennung am Ende jedes Frames.
     * <p>
     * Speichert den aktuellen Tastatur- und Mauszustand als Referenz für den nächsten Frame.
     * Wird von der {@link LunarEngine} automatisch im Game-Loop aufgerufen.
     */
    public synchronized void update() {
        // Tastatur-Zustand speichern
        previousPressedKeys.clear();
        previousPressedKeys.addAll(pressedKeys);

        // Maus-Zustand speichern
        previousMousePressed = mousePressed;
        previousMouseButton = mouseButton;
    }

    /**
     * Registriert eingetippte Zeichen für die Textverarbeitung.
     */
    @Override
    public synchronized void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c != KeyEvent.CHAR_UNDEFINED) {
            typedCharacters.append(c);
        }
    }

    /**
     * Registriert das Herunterdrücken einer Taste.
     */
    @Override
    public synchronized void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
    }

    /**
     * Registriert das Loslassen einer Taste.
     */
    @Override
    public synchronized void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    /**
     * Gibt alle im aktuellen Frame eingetippten Zeichen aus und leert den Puffer.
     *
     * @return Die im aktuellen Frame eingetippten Zeichen als String.
     */
    public synchronized String consumeTypedCharacters() {
        String result = typedCharacters.toString();
        typedCharacters.setLength(0);
        return result;
    }

    /**
     * Prüft, ob eine bestimmte Maustaste im aktuellen Frame  gedrückt wurde (Flankenerkennung).
     *
     * @param button Die Maustaste (1 = Links, 2 = Mitte/Mausrad, 3 = Rechts).
     * @return {@code true} nur im ersten Frame des Drückens.
     */
    public synchronized boolean isMousePressed(int button) {
        if (viewportMouse == null) {
            return mousePressed && !previousMousePressed && mouseButton == button;
        }

        return button > 0 && viewportMouse.framePosition != null && viewportMouse.framePressed.get(button);
    }

    /**
     * Prüft, ob eine Maustaste im aktuellen Frame auf dem Spielfeld losgelassen wurde.
     *
     * @param button Die Maustaste (1 = Links, 2 = Mitte, 3 = Rechts).
     * @return {@code true} nur im allerersten Moment nach dem Loslassen.
     */
    public synchronized boolean isMouseReleased(int button) {
        if (viewportMouse == null) {
            return !mousePressed && previousMousePressed && mouseButton == button;
        }

        return button > 0 && viewportMouse.framePosition != null && viewportMouse.frameReleasedInside.get(button);
    }

    /**
     * Prüft, ob eine Maustaste aktuell gedrückt gehalten wird.
     *
     * @param button Die Maustaste (1 = Links, 2 = Mitte, 3 = Rechts).
     * @return {@code true}, solange die Taste gedrückt bleibt.
     */
    public synchronized boolean isMouseDown(int button) {
        if (viewportMouse == null) {
            return mousePressed && mouseButton == button;
        }

        return button > 0 && viewportMouse.frameHeld.get(button);
    }

    /**
     * Verarbeitet ein abgeschlossenes Klick-Ereignis des Betriebssystems.
     */
    @Override
    public synchronized void mouseClicked(MouseEvent e) {
        if (viewportMouse == null) {
            lastMouseEvent = new MouseEvents(e.getX(), e.getY(), e.getButton(), MouseEvents.MouseAction.CLICKED);
            return;
        }

        viewportMouse.moveTo(e.getPoint());
        int button = e.getButton();
        if (button <= 0 || !viewportMouse.clickCandidates.get(button)) {
            return;
        }

        viewportMouse.clickCandidates.clear(button);
        if (viewportMouse.position != null) {
            viewportMouse.clicked.set(button);
            viewportMouse.record(MouseEvents.MouseAction.CLICKED, button);
        }
    }

    /**
     * Prüft, ob eine bestimmte Maustaste geklickt (gedrückt und wieder losgelassen) wurde.
     *
     * <p>
     * Nach der Abfrage wird das Ereignis "gelöscht", um Mehrfachauslösungen zu vermeiden.
     * </p>
     *
     * @param mouseButton Die Maustaste (1 = Links, 2 = Mitte, 3 = Rechts).
     * @return {@code true}, wenn die definierte Taste geklickt wurde.
     */

    public synchronized boolean mouseClicked(int mouseButton) {
        if (viewportMouse != null) {
            if (mouseButton <= 0 || !viewportMouse.frameClicked.get(mouseButton)) {
                return false;
            }

            viewportMouse.frameClicked.clear(mouseButton);
            MouseEvents event = viewportMouse.frameEvent;
            if (event != null && event.getType() == MouseEvents.MouseAction.CLICKED
                    && event.getButton() == mouseButton) {
                viewportMouse.frameEvent = null;
            }
            return true;
        }

        if (lastMouseEvent != null && lastMouseEvent.getType() == MouseEvents.MouseAction.CLICKED) {
            if (lastMouseEvent.getButton() == mouseButton) {
                lastMouseEvent = null;
                return true;
            }
        }
        return false;
    }

    /**
     * Verarbeitet das Drücken einer Maustaste.
     */
    @Override
    public synchronized void mousePressed(MouseEvent e) {
        if (viewportMouse == null) {
            mouseButton = e.getButton();
            mousePressed = true;

            lastMouseEvent = new MouseEvents(e.getX(), e.getY(), mouseButton, MouseEvents.MouseAction.PRESSED);
            return;
        }

        viewportMouse.moveTo(e.getPoint());
        int button = e.getButton();
        if (button <= 0) {
            return;
        }

        viewportMouse.clickCandidates.clear(button);
        if (viewportMouse.position != null && !viewportMouse.held.get(button)) {
            viewportMouse.button = button;
            viewportMouse.held.set(button);
            viewportMouse.pressed.set(button);
            viewportMouse.record(MouseEvents.MouseAction.PRESSED, button);
        }
    }

    /**
     * Verarbeitet das Loslassen einer Maustaste.
     */
    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        if (viewportMouse == null) {
            mousePressed = false;

            lastMouseEvent = new MouseEvents(e.getX(), e.getY(), e.getButton(), MouseEvents.MouseAction.RELEASED);
            return;
        }

        viewportMouse.moveTo(e.getPoint());
        int button = e.getButton();
        if (button <= 0 || !viewportMouse.held.get(button)) {
            return;
        }

        viewportMouse.button = button;
        viewportMouse.held.clear(button);
        viewportMouse.released.set(button);

        if (viewportMouse.position != null) {
            viewportMouse.releasedInside.set(button);
            viewportMouse.clickCandidates.set(button);
            viewportMouse.record(MouseEvents.MouseAction.RELEASED, button);
        }
    }


    /*
     * Wird aufgerufen, wenn der Mauszeiger das Fenster betritt
     */
    @Override
    public synchronized void mouseEntered(MouseEvent e) {
        if (viewportMouse != null) {
            viewportMouse.moveTo(e.getPoint());
        }
    }

    /*
     * Wird aufgerufen, wenn der Mauszeiger das Fenster verlässt
     */
    @Override
    public synchronized void mouseExited(MouseEvent e) {
        if (viewportMouse != null) {
            viewportMouse.moveTo(null);
        }
    }


    /*
     * Wird aufgerufen, wenn die Maus bei gedrückter Taste bewegt wird (Ziehen / Drag)
     */
    @Override
    public synchronized void mouseDragged(MouseEvent e) {
        if (viewportMouse == null) {
            mouseX = e.getX();
            mouseY = e.getY();
            lastMouseEvent = new MouseEvents(mouseX, mouseY, mouseButton, MouseEvents.MouseAction.DRAGGED);
            return;
        }

        viewportMouse.moveTo(e.getPoint());
        if (!viewportMouse.held.isEmpty()) {
            int dragButton = viewportMouse.held.get(viewportMouse.button)
                    ? viewportMouse.button : viewportMouse.held.nextSetBit(1);
            viewportMouse.record(MouseEvents.MouseAction.DRAGGED, dragButton);
        }
    }

    /*
     * Wird aufgerufen, wenn die Maus ohne gedrückte Taste bewegt wird
     */
    @Override
    public synchronized void mouseMoved(MouseEvent e) {
        if (viewportMouse == null) {
            mouseX = e.getX();
            mouseY = e.getY();
            lastMouseEvent = new MouseEvents(mouseX, mouseY, mouseButton, MouseEvents.MouseAction.MOVED);
            return;
        }

        viewportMouse.moveTo(e.getPoint());
        viewportMouse.record(MouseEvents.MouseAction.MOVED, viewportMouse.button);
    }

    /**
     * Erfasst das Drehen am Mausrad.
     *
     * @param e Das Mausrad-Ereignis vom Java-System.
     */
    @Override
    public synchronized void mouseWheelMoved(MouseWheelEvent e) {
        if (viewportMouse != null) {
            viewportMouse.moveTo(e.getPoint());
            if (viewportMouse.position == null) {
                return;
            }
        }

        scrollDelta += -e.getWheelRotation();
    }

    /**
     * Gibt zurück, wie weit das Mausrad gedreht wurde, und setzt diesen Wert für den nächsten
     * Aufruf sofort auf 0 zurück. Ein positiver Wert bedeutet "nach oben", ein negativer "nach unten".
     *
     * @return Der Bewegungswert des Mausrads.
     */
    public synchronized int getScrollDelta() {
        int delta = scrollDelta;
        scrollDelta = 0; // Zurücksetzen nach dem Abfragen
        return delta;
    }

    /**
     * Gibt die X-Koordinate der Maus im Koordinatensystem des Spiels zurück.
     *
     * @return Die X-Koordinate in Pixeln oder {@code -1}, wenn sich die Maus ausserhalb befindet.
     */
    public synchronized int getMouseX() {
        if (viewportMouse == null) {
            return mouseX;
        }

        Point position = viewportMouse.framePosition;
        return position == null ? -1 : position.x;
    }

    /**
     * Gibt die Y-Koordinate der Maus im Koordinatensystem des Spiels zurück.
     *
     * @return Die Y-Koordinate in Pixeln oder {@code -1}, wenn sich die Maus ausserhalb befindet.
     */
    public synchronized int getMouseY() {
        if (viewportMouse == null) {
            return mouseY;
        }

        Point position = viewportMouse.framePosition;
        return position == null ? -1 : position.y;
    }

    /**
     * Prüft, ob generell irgendeine Maustaste gedrückt wird.
     *
     * @return {@code true}, wenn mindestens eine Maustaste gedrückt ist, andernfalls {@code false}.
     */
    public synchronized boolean isMousePressed() {
        return viewportMouse == null ? mousePressed : !viewportMouse.frameHeld.isEmpty();
    }

    /**
     * Gibt die ID der Maustaste der letzten Interaktion zurück (1, 2 oder 3).
     *
     * @return Die Tasten-ID (1 = Links, 2 = Mitte, 3 = Rechts).
     */
    public synchronized int getMouseButton() {
        return viewportMouse == null ? mouseButton : viewportMouse.frameButton;
    }

    /**
     * Gibt das letzte {@link MouseEvents}-Objekt zurück (z.B. Klicken oder Ziehen) und löscht dieses intern danach.
     *
     * @return Das letzte Mausereignis oder {@code null}.
     */
    public synchronized MouseEvents getLastMouseEvent() {
        if (viewportMouse != null) {
            MouseEvents event = viewportMouse.frameEvent;
            viewportMouse.frameEvent = null;
            if (event != null && event.getType() == MouseEvents.MouseAction.CLICKED) {
                viewportMouse.frameClicked.clear(event.getButton());
            }
            return event;
        }

        MouseEvents e = lastMouseEvent;
        lastMouseEvent = null;
        return e;
    }

}
