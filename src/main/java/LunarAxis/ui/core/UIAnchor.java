package LunarAxis.ui.core;

/**
 * Die Enumeration {@code UIAnchor} definiert die Ankerpunkte (Ausrichtungspunkte)
 * zur automatischen Positionierung von UI-Elementen auf dem Bildschirm oder innerhalb von Containern.
 * <p>
 * Sie erlaubt es, Elemente dynamisch an Bildschirmrändern, im Zentrum oder in den Ecken
 * auszurichten, ohne manuelle Koordinaten berechnen zu müssen.
 * </p>
 *
 * @version 1.2.6
 */
public enum UIAnchor {

    /**
     * Ankerpunkt oben links.
     */
    TOP_LEFT,

    /**
     * Ankerpunkt oben zentriert.
     */
    TOP_CENTER,

    /**
     * Ankerpunkt oben rechts.
     */
    TOP_RIGHT,

    /**
     * Ankerpunkt vertikal zentriert am linken Rand.
     */
    CENTER_LEFT,

    /**
     *  Ankerpunkt exakt in der Mitte.
     */
    CENTER,

    /**
     * Ankerpunkt vertikal zentriert am rechten Rand.
     */
    CENTER_RIGHT,

    /**
     * Ankerpunkt unten links.
     */
    BOTTOM_LEFT,

    /**
     * Ankerpunkt unten zentriert.
     */
    BOTTOM_CENTER,

    /**
     * Ankerpunkt unten rechts.
     */
    BOTTOM_RIGHT,

    /**
     * Manuelle Positionierung über feste Koordinaten (Standard).
     */
    ABSOLUTE;
}
