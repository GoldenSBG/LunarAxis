package LunarAxis.core.game;

import LunarAxis.core.LunarEngine;
import LunarAxis.core.inputs.InputManager;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.Validation;

import javax.swing.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Das {@code GameWindow} repräsentiert das eigentliche Programmfenster auf dem Bildschirm (den "Bilderrahmen"),
 * in dem das Spiel läuft. Es basiert auf einem Standard-Java-Fenster ({@link JFrame}).
 * <p>
 * Diese Klasse hält die grundlegenden Bausteine zusammen: Sie beinhaltet die Zeichenfläche ({@link GamePanel})
 * und erstellt den {@link InputManager}, der vom Fenster alle Tasten- und Mausdrücke abfängt.
 * Zudem sorgt sie dafür, dass das gesamte Spiel sauber beendet wird, wenn man oben rechts auf das "X" klickt.
 * </p>
 * <p>
 * Schülerinnen und Schüler interagieren in TigerJython in der Regel nicht direkt mit dieser Klasse,
 * da die {@link LunarEngine} das Fenster automatisch bei der Erstellung aufbaut.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Fokus des Fensters abfragen
 * engine = LunarEngine(800, 600, "Mein Spiel")
 *
 * # Man kann über das Fenster prüfen, ob der Spieler gerade herausgetabbt ist
 * if engine.getGameWindow().hasFocus():
 *     print("Der Spieler ist aktiv im Spiel!")
 * else:
 *     engine.setPaused(True)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.7
 */
public class GameWindow {

    private JFrame jFrame;
    private GamePanel gamePanel;
    private InputManager inputManager;

    private volatile boolean resizableViewport;
    private volatile boolean shown;

    /**
     * Erstellt das Hauptfenster des Spiels, konfiguriert es (nicht vergrösserbar, mittig auf dem Bildschirm)
     * und verknüpft es mit dem Eingabe-System.
     *
     * @param width  Die exakte innere Breite des Fensters in Pixeln (ohne den Fensterrahmen).
     * @param height Die exakte innere Höhe des Fensters in Pixeln (ohne den Fensterrahmen).
     * @param title  Der Titel, der ganz oben in der Leiste des Fensters angezeigt wird.
     *
     * @throws IllegalArgumentException Wenn eine negative Grösse oder 0 übergeben wird.
     */
    public GameWindow(int width, int height, String title) {
        Validation.checkArgument(width > 0 && height > 0,
                ErrorDetails.of("GameWindow", "Invalid game window dimensions")
                        .withActual("width=" + width + ", height=" + height)
                        .withExpected("Positive width and height")
                        .withExplanation("The window requires a game panel with positive dimensions"));


        jFrame = new JFrame(title);
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setResizable(false);

        // NEU: Wenn das "X" geklickt wird, Engine sauber herunterfahren
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (LunarEngine.getInstance() != null) {
                    LunarEngine.getInstance().stop();
                }
            }
        });


        gamePanel = new GamePanel(width, height);
        jFrame.add(gamePanel);

        /*
         * Initialisiert die Eingabe-Manager [NEU - 18.05.26 Mittag]
         */
        inputManager = new InputManager();
        gamePanel.addMouseListener(inputManager);
        gamePanel.addMouseMotionListener(inputManager);
        gamePanel.addKeyListener(inputManager);
        gamePanel.addMouseWheelListener(inputManager); //neu [18.05]

        jFrame.pack(); // Passt das JFrame exakt an das JPanel an
        jFrame.setLocationRelativeTo(null); // Zentriert auf dem Bildschirm
    }

    /**
     * Aktiviert die skalierbare Viewport-Darstellung vor dem ersten Anzeigen des Fensters.
     * <p>
     * Macht das {@link JFrame} für den Benutzer vergrösserbar und fügt alle notwendigen Listener
     * für Fokus-, Minimierungs- und Grössenänderungs-Events hinzu. Dadurch werden die Mauskoordinaten
     * im {@link InputManager} bei jeder Fensteranpassung automatisch auf die virtuelle Zielauflösung umgerechnet.
     * </p>
     *
     * @throws IllegalStateException (Lunar-Error) Wenn das Fenster bereits auf dem Bildschirm angezeigt wurde.
     */
    public void enableResizableViewport() {
        runOnEdt(() -> {
            if (shown) {
                throw Errors.state(ErrorDetails.of("GameWindow", "The viewport must be enabled before showing the window")
                        .withExpected("A game window that has not been shown")
                        .withExplanation("Window configuration and input mapping must be installed before the first game frame"), null);
            }
            if (resizableViewport) {
                return;
            }

            gamePanel.enableViewport();
            inputManager.enableViewport(gamePanel::getViewportGeometry);
            jFrame.setResizable(true);

            gamePanel.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent event) {
                    inputManager.clearViewportInput();
                }
            });

            WindowAdapter viewportWindowListener = new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent event) {
                    inputManager.clearViewportInput();
                }

                @Override
                public void windowGainedFocus(WindowEvent event) {
                    gamePanel.requestFocusInWindow();
                    refreshViewportPointer();
                }

                @Override
                public void windowIconified(WindowEvent event) {
                    inputManager.clearViewportInput();
                }

                @Override
                public void windowDeiconified(WindowEvent event) {
                    refreshViewportPointer();
                }
            };

            jFrame.addWindowListener(viewportWindowListener);
            jFrame.addWindowFocusListener(viewportWindowListener);

            gamePanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    refreshViewportPointer();
                }

                @Override
                public void componentHidden(ComponentEvent event) {
                    inputManager.clearViewportInput();
                }
            });

            jFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent event) {
                    refreshViewportPointer();
                }
            });

            resizableViewport = true;
        });
    }

    /**
     * Aktualisiert die Zeigerposition der Maus innerhalb des skalierbaren Viewports.
     * <p>
     * Übergibt dem {@link InputManager} die aktuelle Mausposition nur dann, wenn das Zeichen-Panel
     * sichtbar ist und das Fenster den Eingabefokus besitzt, andernfalls {@code null}.
     * </p>
     */
    private void refreshViewportPointer() {
        inputManager.refreshViewportPointer(gamePanel.isShowing() && jFrame.isFocused()
                ? gamePanel.getMousePosition() : null);
    }

    /**
     * Führt eine GUI-/Fenster-Operation sicher auf dem Event Dispatch Thread (EDT) von Swing aus.
     * <p>
     * Falls der Aufruf bereits auf dem EDT erfolgt, wird die Aktion sofort ausgeführt.
     * Andernfalls wird über {@link SwingUtilities#invokeAndWait(Runnable)} gewartet.
     * Auftretende Interrupts oder Target-Exceptions werden über die {@link Errors}-Fabrik
     * in strukturierte LunarAxis-Laufzeitfehler umgewandelt.
     * </p>
     *
     * @param operation Die auf dem EDT auszuführende {@link Runnable}-Aktion.
     * @throws RuntimeException (Lunar-Error) Wenn die EDT-Ausführung unterbrochen wird oder fehlschlägt.
     */
    private static void runOnEdt(Runnable operation) {
        if (SwingUtilities.isEventDispatchThread()) {
            operation.run();
            return;
        }

        //todo: schauen, ob nicht vereinfachbar
        try {
            SwingUtilities.invokeAndWait(operation);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw Errors.runtime(ErrorDetails.of("GameWindow", "Interrupted while waiting for the event dispatch thread")
                    .withExpected("A completed window operation")
                    .withExplanation("The calling thread was interrupted while waiting for Swing"), exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw Errors.runtime(ErrorDetails.of("GameWindow", "Window operation failed on the event dispatch thread")
                    .withExpected("A successful Swing window operation")
                    .withExplanation("Swing reported an unexpected failure while executing the operation"), cause);
        }
    }

    /**
     * Macht das Fenster auf dem Bildschirm sichtbar oder blendet es aus.
     * <p>
     * Bei aktivierter Viewport-Skalierung stellt die Methode sicher, dass der Fensterstart nicht
     * versehentlich direkt auf dem Event Dispatch Thread (EDT) aufgerufen wird, da der blockierende
     * Game-Loop sonst das Swing-Rendering und die Eingabeverarbeitung stoppen würde.
     * </p>
     *
     * @param visible {@code true}, um das Fenster anzuzeigen und zu fokussieren; {@code false}, um es auszublenden.
     * @throws IllegalStateException (Lunar-Error) Wenn ein skalierbares Spiel fälschlicherweise auf dem EDT gestartet wird.
     */
    public void show(boolean visible) {
        if (!resizableViewport) {
            if (visible) {
                shown = true;
            }
            jFrame.setVisible(visible);
            return;
        }

        if (visible && SwingUtilities.isEventDispatchThread()) {
            throw Errors.state(ErrorDetails.of("GameWindow", "The resizable game must be started off the event dispatch thread")
                    .withExpected("A game loop running on a non-EDT thread")
                    .withExplanation("The blocking engine loop would otherwise prevent Swing painting and input delivery"), null);
        }

        runOnEdt(() -> {
            if (visible) {
                shown = true;
            } else {
                inputManager.clearViewportInput();
            }

            jFrame.setVisible(visible);
            if (visible) {
                gamePanel.requestFocusInWindow();
                refreshViewportPointer();
            }
        });
    }

    /**
     * Prüft, ob das Fenster aktuell vom Betriebssystem als "aktiv" markiert ist (Fokus hat).
     * Sobald der Nutzer z.B. auf einen anderen Monitor klickt oder "Alt+Tab" drückt,
     * verliert das Fenster den Fokus.
     *
     * @return {@code true}, wenn das Fenster den Fokus hat, andernfalls {@code false}.
     */
    public boolean hasFocus() {
        return gamePanel.hasFocus();
    }

    /**
     * Schliesst das Fenster endgültig, setzt aktive Eingabezustände im Viewport zurück
     * und gibt alle vom System belegten Grafikressourcen frei.
     */
    public void dispose() {
        if (jFrame == null) {
            return;
        }

        if (!resizableViewport) {
            jFrame.dispose();
            return;
        }

        runOnEdt(() -> {
            inputManager.clearViewportInput();
            jFrame.dispose();
        });
    }

    /**
     * Gibt die eingebettete Zeichenfläche (Leinwand) dieses Fensters zurück.
     *
     * @return Das {@link GamePanel}.
     */
    public GamePanel getGamePanel() {
        return gamePanel;
    }

    /**
     * Gibt den InputManager zurück, der an dieses Fenster und seine Zeichenfläche gekoppelt ist.
     *
     * @return Der aktive {@link InputManager}.
     */
    public InputManager getInputManager() {
        return inputManager;
    }

}
