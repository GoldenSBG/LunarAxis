package LunarAxis.ui.elements;

import LunarAxis.core.inputs.InputManager;
import LunarAxis.graphics.Graphics;
import LunarAxis.ui.core.UIElement;

import java.awt.Image;

/**
 * Die Klasse {@code UIImage} repräsentiert ein UI-Element zur Anzeige eines Bildes auf der Benutzeroberfläche.
 * <p>
 * Sie eignet sich ideal für Icons, Menü-Hintergründe, HUD-Grafiken oder Profilbilder.
 * Das zugewiesene {@link Image} wird automatisch auf die definierte Breite und Höhe des UI-Elements skaliert.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * img = engine.getResourceManager().getImage("icon.png")
 * uiImage = UIImage(img, 20, 20, 64, 64)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UIImage extends UIElement {
    private Image image;

    /**
     * Erstellt ein neues Bild-UI-Element mit Bildreferenz, Position und Dimensionen.
     *
     * @param image  Das anzuzeigende Bild (z.B. geladen über den ResourceManager).
     * @param x      Die X-Koordinate in Screen-Koordinaten.
     * @param y      Die Y-Koordinate in Screen-Koordinaten.
     * @param width  Die Breite, auf die das Bild skaliert wird.
     * @param height Die Höhe, auf die das Bild skaliert wird.
     */
    public UIImage(Image image, float x, float y, int width, int height) {
        super(x, y, width, height);
        this.image = image;
    }

    /**
     * Aktualisiert die Logik des Bild-UI-Elements.
     * <p>
     * Ein statisches Bild benötigt derzeit keine laufenden Frame-Updates.
     * </p>
     *
     * @param deltaTime    Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @param inputManager Der {@link InputManager} für Eingabeabfragen.
     */
    @Override
    public void update(float deltaTime, InputManager inputManager) {
    }

    /**
     * Zeichnet das skaliere Bild auf den Bildschirm, sofern das Element sichtbar und ein Bild vorhanden ist.
     *
     * @param g Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    @Override
    public void draw(Graphics g) {
        if (visible && image != null) {
            // drawImage mit width/height skaliert das Bild automatisch
            g.drawImage(image, (int) x, (int) y, width, height);
        }
    }

    /**
     * Gibt das aktuell zugewiesene Bild zurück.
     *
     * @return Das {@link Image} oder {@code null}.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Setzt ein neues Bild für dieses UI-Element.
     *
     * @param image Das neue anzuzeigende {@link Image}.
     */
    public void setImage(Image image) {
        this.image = image;
    }
}