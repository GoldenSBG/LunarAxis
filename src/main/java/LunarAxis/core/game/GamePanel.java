package LunarAxis.core.game;

import LunarAxis.core.LunarEngine;
import LunarAxis.core.manager.IGameLogic;
import LunarAxis.graphics.Camera2D;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.Validation;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Das {@code GamePanel} ist die eigentliche Zeichenfläche (die "Leinwand"), auf der das gesamte Spiel
 * dargestellt wird. Es erbt von der Standard-Java-Klasse {@link JPanel}.
 * <p>
 * Hier laufen alle grafischen Fäden zusammen: In jedem Frame (Bild) holt sich das Panel die aktuellen
 * Informationen aus den Szenen, der Kamera und der Spiellogik, und zeichnet diese in der korrekten
 * Reihenfolge auf den Bildschirm.
 * </p>
 * <p>
 * Schülerinnen und Schüler müssen dieses Panel in der Regel nicht selbst erstellen, da die
 * {@link LunarEngine} das automatisch für sie übernimmt. Man kann aber darauf zugreifen,
 * um z.B. eine Kamera zuzuweisen.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Kamera an das GamePanel hängen
 * from LunarAxis.graphics import Camera2D
 *
 * # Kamera erstellen und dem Panel der Engine zuweisen
 * cam = Camera2D(800, 600)
 * engine.getGamePanel().setCamera(cam)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.7
 */
public class GamePanel extends JPanel {

    /*
     * Breite und Höhe des Spielpanels
     */
    private int width, height;

    /*
     * Referenz auf das LunarEngine-Objekt
     */
    private LunarEngine lunarEngine;

    private volatile Camera2D camera;

    private BufferedImage viewportBuffer;
    private ViewportGeometry viewportGeometry;


    private volatile float renderedFps;
    private volatile float lastRenderFrameTimeSeconds;
    private volatile float lastRenderDurationSeconds;

    private long lastRenderStartNanos;
    private long renderFpsWindowStartNanos;
    private int renderFramesInWindow;


    /**
     * Erstellt eine neue, leere Zeichenfläche für das Spiel.
     *
     * @param width  Die Breite der Zeichenfläche in Pixeln.
     * @param height Die Höhe der Zeichenfläche in Pixeln.
     *
     * @throws IllegalArgumentException Wenn Breite oder Höhe nicht positiv (also 0 oder negativ) sind.
     */
    public GamePanel(int width, int height) {
        Validation.checkArgument(width > 0 && height > 0,
                ErrorDetails.of("GamePanel", "Invalid game panel dimensions")
                        .withActual("width=" + width + ", height=" + height)
                        .withExpected("Positive width and height")
                        .withExplanation("The game panel requires a positive drawing area"));

        this.width = width;
        this.height = height;
        this.lunarEngine = LunarEngine.getInstance();
        setPreferredSize(new Dimension(width, height));
        setFocusable(true); // Wichtig für KeyListener später
    }

    /**
     * Die zentrale Zeichen-Methode des Panels. Wird vom Java-GUI-System automatisch in jedem Frame aufgerufen,
     * um das aktuelle Bild neu auf den Bildschirm zu malen.
     * <p>
     * Überprüft, ob ein virtueller Viewport-Puffer vorhanden ist. Falls ja, wird das Spiel zunächst in den
     * Offscreen-Puffer gerendert und anschliessend skaliert auf die Panel-Fläche gezeichnet.
     * Andernfalls erfolgt das direkte Rendering auf der Panel-Oberfläche mit folgender Reihenfolge:
     * 1. Hintergrund löschen/zeichnen
     * 2. Kamera-Verschiebung anwenden
     * 3. Eigene Spiellogik (Benutzer-Code) zeichnen
     * 4. Aktive Szene (Level) zeichnen
     * 5. Kamera zurücksetzen (damit UI nicht mit verschoben wird)
     * 6. Szenen-Übergangs-Effekte (wie Fade-Out) darüberlegen
     * </p>
     *
     * @param gRaw Das rohe {@link Graphics}-Objekt des Java-Systems, auf dem gezeichnet wird.
     * @throws RuntimeException (Lunar-Error) Wenn während eines Teilbereichs des Renderings ein Fehler auftritt.
     */
    @Override
    protected void paintComponent(Graphics gRaw) {
        long renderStartNanos = System.nanoTime();
        recordRenderStart(System.nanoTime());
        String operation = "render preparation";

        try {
            Graphics2D g2d = (Graphics2D) gRaw;

            if (lunarEngine == null) {
                throw Errors.nullValue(
                        ErrorDetails.of("GamePanel", "Engine reference is missing during rendering")
                                .withActual(null)
                                .withExpected("A GamePanel associated with an existing LunarEngine")
                                .withExplanation("The panel captures the engine reference when it is constructed"),
                        null);
            }

            if (viewportBuffer != null) {
                operation = "virtual viewport rendering";
                paintViewport(g2d);
                return;
            }

            IGameLogic logic = lunarEngine.getIGameLogic();
            LunarAxis.graphics.Graphics engineGraphics = new LunarAxis.graphics.Graphics(g2d);

            operation = "panel background rendering";
            super.paintComponent(gRaw);

            if (camera != null) {
                operation = "camera transformation";
                engineGraphics.applyCamera(camera);
            }

            if (logic != null) {
                operation = "IGameLogic.render";
                logic.render(engineGraphics);
            }

            if (lunarEngine.getSceneManager() != null) {
                operation = "scene rendering";
                lunarEngine.getSceneManager().render(engineGraphics);
            }

            if (camera != null) {
                operation = "camera reset";
                engineGraphics.resetCamera();
            }

            if (lunarEngine.getSceneManager() != null) {
                operation = "transition overlay rendering";
                lunarEngine.getSceneManager().renderTransitionOverlay(engineGraphics, getWidth(), getHeight());
            }
        } catch (RuntimeException exception) {
            if (Errors.detailsOf(exception) == null) {
                Errors.describe(exception,
                        ErrorDetails.of("GamePanel", "Rendering failed")
                                .withActual(operation)
                                .withExpected("A render operation that completes without an unhandled exception")
                                .withExplanation("The current render pass was aborted; the cause may be in application rendering or a library operation"));
            }
            throw exception;
        } finally {
            lastRenderDurationSeconds = (System.nanoTime() - renderStartNanos) / 1_000_000_000.0f;
        }

    }

    /**
     * Initialisiert den internen Offscreen-Puffer ({@link BufferedImage}) für die fixe logische Zielauflösung auf dem EDT.
     * <p>
     * Diese Methode bereitet das Panel auf das proportionale Skalieren bei Fenstergrössenänderungen vor.
     * </p>
     */
    void enableViewport() {
        if (viewportBuffer == null) {
            viewportBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
    }

    /**
     * Berechnet und liefert die aktuelle {@link ViewportGeometry} basierend auf der tatsächlichen Panel-Grösse auf dem EDT.
     * <p>
     * Erstellt bei Bedarf eine neue Geometrie-Instanz, die das Skalierungsverhältnis und die schwarzen Ränder (Letterboxing) berechnet.
     * </p>
     *
     * @return Die aktuelle {@link ViewportGeometry} oder {@code null}, falls der Viewport nicht aktiviert wurde.
     */
    ViewportGeometry getViewportGeometry() {
        if (viewportBuffer == null) {
            return null;
        }

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (viewportGeometry == null || viewportGeometry.getPanelWidth() != panelWidth
                || viewportGeometry.getPanelHeight() != panelHeight) {
            viewportGeometry = ViewportGeometry.fit(width, height, panelWidth, panelHeight);
        }

        return viewportGeometry;
    }

    /**
     * Rendert das gesamte Spiel in den hochauflösenden Offscreen-Puffer und zeichnet diesen anschliessend 
     * mit zentrierter Einpassung (Letterboxing/Pillarboxing) und bilinearer Interpolation auf das tatsächliche Fensterelement.
     *
     * @param panelGraphics Das {@link Graphics2D}-Zeichenwerkzeug des Ziel-Panels.
     */
    private void paintViewport(Graphics2D panelGraphics) {
        ViewportGeometry geometry = getViewportGeometry();
        if (geometry == null) {
            return;
        }

        Graphics2D logicalGraphics = viewportBuffer.createGraphics();
        try {
            logicalGraphics.setClip(0, 0, width, height);
            logicalGraphics.setRenderingHints(panelGraphics.getRenderingHints());
            logicalGraphics.setFont(panelGraphics.getFont());
            logicalGraphics.setStroke(panelGraphics.getStroke());

            logicalGraphics.setComposite(AlphaComposite.Src);
            logicalGraphics.setColor(getBackground());
            logicalGraphics.fillRect(0, 0, width, height);
            logicalGraphics.setComposite(AlphaComposite.SrcOver);
            logicalGraphics.setPaint(panelGraphics.getPaint());

            LunarAxis.graphics.Graphics engineGraphics = new LunarAxis.graphics.Graphics(logicalGraphics);
            Camera2D frameCamera = camera;
            if (frameCamera != null) {
                engineGraphics.applyCamera(frameCamera);
            }

            IGameLogic logic = lunarEngine.getIGameLogic();
            if (logic != null) {
                logic.render(engineGraphics);
            }

            if (lunarEngine.getSceneManager() != null) {
                lunarEngine.getSceneManager().render(engineGraphics);
            }

            engineGraphics.resetCamera();

            if (lunarEngine.getSceneManager() != null) {
                lunarEngine.getSceneManager().renderTransitionOverlay(engineGraphics, width, height);
            }
        } finally {
            logicalGraphics.dispose();
        }

        Graphics2D output = (Graphics2D) panelGraphics.create();
        try {
            output.setComposite(AlphaComposite.SrcOver);
            output.setColor(Color.BLACK);
            output.fillRect(0, 0, geometry.getPanelWidth(), geometry.getPanelHeight());
            output.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            AffineTransform presentation = AffineTransform.getTranslateInstance(geometry.getOffsetX(), geometry.getOffsetY());
            presentation.scale(geometry.getScale(), geometry.getScale());
            output.drawImage(viewportBuffer, presentation, null);
        } finally {
            output.dispose();
        }
    }

    /**
     * Interne Hilfsmethode, die den exakten Startzeitpunkt jedes neuen Frames misst.
     * Das ist wichtig, um berechnen zu können, wie flüssig das Spiel tatsächlich *gezeichnet* wird
     * (was von der logischen Update-Rate abweichen kann).
     *
     * @param now Die aktuelle Systemzeit in Nanosekunden.
     */
    private void recordRenderStart(long now) {
        if (lastRenderStartNanos != 0L) {
            lastRenderFrameTimeSeconds = (now - lastRenderStartNanos) / 1_000_000_000.0f;
        }

        lastRenderStartNanos = now;

        if (renderFpsWindowStartNanos == 0L) {
            renderFpsWindowStartNanos = now;
            renderFramesInWindow = 0;
        }

        renderFramesInWindow++;

        long elapsed = now - renderFpsWindowStartNanos;
        if (elapsed >= 1_000_000_000L) {renderedFps = renderFramesInWindow * 1_000_000_000.0f / elapsed;
            renderFramesInWindow = 0;
            renderFpsWindowStartNanos = now;
        }
    }

    /**
     * Gibt zurück, wie viele Bilder in der letzten Sekunde physisch auf den Bildschirm gezeichnet wurden (Render-FPS).
     *
     * @return Die Bilder pro Sekunde.
     */
    public float getRenderedFps() {
        return renderedFps;
    }

    /**
     * Gibt die exakte Zeit zurück, die zwischen dem Start des aktuellen und dem Start
     * des vorherigen Zeichnungs-Vorgangs (Frames) vergangen ist.
     *
     * @return Die Zeitabstände zwischen Frames in Sekunden.
     */
    public float getLastRenderFrameTimeSeconds() {
        return lastRenderFrameTimeSeconds;
    }

    /**
     * Gibt zurück, wie lange die reine Rechenarbeit für das Zeichnen des letzten Bildes gedauert hat.
     * Wenn dieser Wert zu hoch ist, weiss man, dass zu viele oder zu komplexe Grafiken gezeichnet werden.
     *
     * @return Die reine Zeichnungsdauer in Sekunden.
     */
    public float getLastRenderDurationSeconds() {
        return lastRenderDurationSeconds;
    }

    /**
     * Verbindet eine Kamera mit dieser Zeichenfläche.
     * Ab diesem Moment wird alles (ausser dem UI) durch das "Objektiv" dieser Kamera gezeichnet.
     *
     * @param camera Das anzuhängende {@link Camera2D}-Objekt.
     */
    public void setCamera(Camera2D camera) {
        this.camera = camera;
    }

    /**
     * Gibt die aktuell zugewiesene Kamera zurück.
     *
     * @return Das aktuelle {@link Camera2D}-Objekt oder {@code null}, falls noch keine gesetzt wurde.
     */
    public Camera2D getCamera() {
        return camera;
    }
}
