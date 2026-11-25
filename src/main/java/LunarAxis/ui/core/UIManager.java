package LunarAxis.ui.core;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Graphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Die Klasse {@code UIManager} verwaltet alle UI-Elemente einer Szene.
 * <p>
 * Sie kümmert sich um die Sortierung der Zeichenebenen (Z-Index), die automatische
 * Berechnung der Bildschirmanformationen (Anchors) und leitet Eingabe-Events (Input)
 * an interaktive UI-Elemente weiter.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UIManager {
    private final List<UIElement> elements = new ArrayList<>();
    private int screenWidth = 800;
    private int screenHeight = 600;

    /**
     * Erstellt einen neuen, leeren UIManager mit Standard-Bildschirmabmessungen (800x600).
     */
    public UIManager() {
    }

    /**
     * Setzt die aktuelle Bildschirmgrösse für die automatische Anker-Berechnung.
     *
     * @param width  Die Breite des Fensters/Bildschirms in Pixeln.
     * @param height Die Höhe des Fensters/Bildschirms in Pixeln.
     */
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    /**
     * Fügt ein UI-Element zur Verwaltung hinzu.
     *
     * @param element Das hinzuzufügende {@link UIElement}.
     */
    public synchronized void addElement(UIElement element) {
        if (element != null && !elements.contains(element)) {
            elements.add(element);
        }
    }

    /**
     * Entfernt ein UI-Element aus der Verwaltung.
     *
     * @param element Das zu entfernende {@link UIElement}.
     */
    public synchronized void removeElement(UIElement element) {
        elements.remove(element);
    }

    /**
     * Entfernt alle verwalteten UI-Elemente.
     */
    public synchronized void clear() {
        elements.clear();
    }

    /**
     * Aktualisiert die Logik aller UI-Elemente und leitet Input-Events weiter.
     * <p>
     * Die Eingabeverarbeitung erfolgt in umgekehrter Zeichenreihenfolge (höchster Z-Index zuerst),
     * damit weiter vorne liegende Elemente Interaktionen abfangen können.
     * </p>
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param input     Der {@link InputManager} für Eingabeabfragen.
     */
    public synchronized void update(float deltaTime, InputManager input) {
        // Sortiere nach Z-Index (absteigend für Input, aufsteigend für Render)
        elements.sort(Comparator.comparingInt(UIElement::getZIndex));
        List<UIElement> snapshot = new ArrayList<>(elements);

        // Input von hinten nach vorne (höchster Z-Index zuerst)
        for (int i = snapshot.size() - 1; i >= 0; i--) {
            UIElement element = snapshot.get(i);
            if (elements.contains(element) && element.isVisible() && element.isEnabled()) {
                element.update(deltaTime, input);
            }
        }
    }

    /**
     * Zeichnet alle UI-Elemente in der korrekten Z-Index-Reihenfolge.
     * Vor dem eigentlichen Zeichenvorgang werden die Ankerpunkte aller Elemente
     * auf Basis der aktuellen Bildschirmabmessungen berechnet.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    public synchronized void render(Graphics g) {
        // Anchors berechnen
        List<UIElement> anchorSnapshot = new ArrayList<>(elements);
        for (UIElement element : anchorSnapshot) {
            if (elements.contains(element)) {
                element.applyAnchor(screenWidth, screenHeight);
            }
        }

        // Sortiere nach Z-Index (aufsteigend: niedriger = hinten)
        elements.sort(Comparator.comparingInt(UIElement::getZIndex));
        List<UIElement> renderSnapshot = new ArrayList<>(elements);

        // Zeichne alle sichtbaren Elemente
        for (UIElement element : renderSnapshot) {
            if (elements.contains(element) && element.isVisible()) {
                element.draw(g);
            }
        }
    }

    /**
     * Gibt die Gesamtzahl der aktuell verwalteten UI-Elemente zurück.
     *
     * @return Die Anzahl der Elemente.
     */
    public synchronized int getElementCount() {
        return elements.size();
    }

    /**
     * Gibt eine defensive Kopie der Liste aller verwalteten UI-Elemente zurück.
     *
     * @return Eine {@link List} aller {@link UIElement}e.
     */
    public synchronized List<UIElement> getElements() {
        return new ArrayList<>(elements);
    }

    /**
     * Gibt die derzeit eingestellte Bildschirmhöhe zurück.
     *
     * @return Die Höhe in Pixeln.
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Setzt die Bildschirmhöhe für Anker-Berechnungen.
     *
     * @param screenHeight Die Höhe in Pixeln.
     */
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    /**
     * Gibt die derzeit eingestellte Bildschirmbreite zurück.
     *
     * @return Die Breite in Pixeln.
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Setzt die Bildschirmbreite für Anker-Berechnungen.
     *
     * @param screenWidth Die Breite in Pixeln.
     */
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }
}
