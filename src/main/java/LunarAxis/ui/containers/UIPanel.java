package LunarAxis.ui.containers;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIAnchor;
import LunarAxis.ui.core.UIElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Die Klasse {@code UIPanel} repräsentiert ein Container-UI-Element, das andere UI-Elemente als Kinder verwaltet.
 * <p>
 * Sie eignet sich ideal für Menüs, Dialogfenster, Inventare oder gruppierte HUD-Elemente.
 * Die Kind-Elemente werden relativ zur Position des Panels verwaltet und bei Updates sowie beim Zeichnen
 * automatisch entsprechend transformiert.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UIPanel extends UIElement {
    private final List<UIElement> children = new ArrayList<>();
    private Colors backgroundColor;
    private Colors borderColor;
    private boolean drawBackground = true;
    private boolean drawBorder = true;

    /**
     * Erstellt ein neues UI-Panel mit Position und Dimensionen.
     *
     * @param x      Die absolute X-Position auf dem Bildschirm.
     * @param y      Die absolute Y-Position auf dem Bildschirm.
     * @param width  Die Breite des Panels in Pixeln.
     * @param height Die Höhe des Panels in Pixeln.
     */
    public UIPanel(float x, float y, int width, int height) {
        super(x, y, width, height);
        this.backgroundColor = new Colors(50, 50, 60, 200); // Halbtransparentes Dunkelgrau
        this.borderColor = Colors.WHITE;
    }

    /**
     * Fügt ein Kind-Element zu diesem Panel hinzu.
     * <p>
     * Wandelt die bisherige absolute Position des Kindes automatisch in eine relative Position
     * in Bezug auf dieses Panel um und setzt dieses Panel als Eltern-Element.
     * </p>
     *
     * @param child Das hinzuzufügende {@link UIElement}.
     */
    public void addChild(UIElement child) {
        if (child != null && !children.contains(child)) {
            // absolute Position in relative Position umrechnen
            float relativeX = child.getX() - this.x;
            float relativeY = child.getY() - this.y;
            child.setPosition(relativeX, relativeY);

            children.add(child);
            child.setParent(this);
        }
    }

    /**
     * Entfernt ein Kind-Element aus diesem Panel und hebt die Eltern-Beziehung auf.
     *
     * @param child Das zu entfernende {@link UIElement}.
     */
    public void removeChild(UIElement child) {
        children.remove(child);
        child.setParent(null);
    }

    /**
     * Entfernt alle Kind-Elemente aus dem Panel und setzt deren Eltern-Referenzen zurück.
     */
    public void clearChildren() {
        for (UIElement child : children) {
            child.setParent(null);
        }
        children.clear();
    }

    /**
     * Aktualisiert den Zustand des Panels und leitet den Update-Aufruf an alle Kind-Elemente weiter.
     * <p>
     * Rechnet die relativen Koordinaten der Kinder für die Input-Verarbeitung temporär in absolute
     * Bildschirm-Koordinaten um und stellt diese anschliessend wieder her.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Eingabe-Abfragen.
     */
    @Override
    public void update(float deltaTime, InputManager input) {
        if (!visible) {
            return;
        }

        visitPositionedChildren(child -> child.update(deltaTime, input));
    }

    /**
     * Zeichnet das Panel (Hintergrund und Rahmen) sowie alle enthaltenen Kind-Elemente.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (!visible) {
            return;
        }

        if (drawBackground) {
            g.setColor(backgroundColor);
            g.fillRect((int) x, (int) y, width, height);
        }
        if (drawBorder) {
            g.setColor(borderColor);
            g.drawRect((int) x, (int) y, width, height);
        }

        drawChildren(g);
    }

    /**
     * Zeichnet ausschliesslich die Kind-Elemente des Panels ohne dessen Hintergrund oder Rahmen.
     * <p>
     * Diese Methode wird insbesondere von abgeleiteten Klassen wie {@link LunarAxis.ui.elements.UIWindow}
     * genutzt, um den spezifischen Fensterinhalt zu rendern.
     * </p>
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    protected final void drawChildren(Graphics g) {
        visitPositionedChildren(child -> child.draw(g));
    }

    /**
     * Iteriert über einen sicheren Schnappschuss (Snapshot) der Kind-Elemente und transformiert
     * deren relative Positionen temporär in absolute Bildschirm-Koordinaten.
     * <p>
     * Das Erstellen des Schnappschusses verhindert {@link java.util.ConcurrentModificationException}s,
     * falls Kind-Elemente während des Iterierens (z. B. durch Callback-Aktionen) hinzugefügt oder entfernt werden.
     * </p>

     * @param action Die auszuführende Aktion für jedes Kind-Element (z. B. update oder draw).
     */
    private void visitPositionedChildren(Consumer<UIElement> action) {
        if (children.isEmpty()) {
            return;
        }

        List<UIElement> snapshot = new ArrayList<>(children);
        for (UIElement child : snapshot) {
            if (!children.contains(child)) {
                continue;
            }

            float previousX = child.getX();
            float previousY = child.getY();
            UIAnchor previousAnchor = child.getAnchor();

            try {
                child.setX(previousX + x);
                child.setY(previousY + y);
                action.accept(child);
            } finally {
                child.setX(previousX);
                child.setY(previousY);
                child.setAnchor(previousAnchor);
            }
        }
    }

    /**
     * Gibt die Hintergrundfarbe des Panels zurück.
     *
     * @return Die {@link Colors}-Farbe des Hintergrunds.
     */
    public Colors getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Setzt die Hintergrundfarbe des Panels.
     *
     * @param backgroundColor Die neue {@link Colors}-Farbe.
     */
    public void setBackgroundColor(Colors backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Gibt die Rahmenfarbe des Panels zurück.
     *
     * @return Die {@link Colors}-Farbe des Rahmens.
     */
    public Colors getBorderColor() {
        return borderColor;
    }

    /**
     * Setzt die Rahmenfarbe des Panels.
     *
     * @param borderColor Die neue {@link Colors}-Farbe.
     */
    public void setBorderColor(Colors borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Prüft, ob der Hintergrund gezeichnet wird.
     *
     * @return {@code true}, wenn der Hintergrund aktiv ist, sonst {@code false}.
     */
    public boolean isDrawBackground() {
        return drawBackground;
    }

    /**
     * Legt fest, ob der Hintergrund gezeichnet werden soll.
     *
     * @param drawBackground {@code true} zum Zeichnen des Hintergrunds.
     */
    public void setDrawBackground(boolean drawBackground) {
        this.drawBackground = drawBackground;
    }

    /**
     * Prüft, ob der Rahmen gezeichnet wird.
     *
     * @return {@code true}, wenn der Rahmen aktiv ist, sonst {@code false}.
     */
    public boolean isDrawBorder() {
        return drawBorder;
    }

    /**
     * Legt fest, ob der Rahmen gezeichnet werden soll.
     *
     * @param drawBorder {@code true} zum Zeichnen des Rahmens.
     */
    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
    }

    /**
     * Gibt eine Kopie der Liste aller Kind-Elemente zurück.
     *
     * @return Eine neue {@link List} mit den {@link UIElement}-Kindern.
     */
    public List<UIElement> getChildren() {
        return new ArrayList<>(children);
    }
}
