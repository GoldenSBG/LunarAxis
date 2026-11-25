package LunarAxis.physics;

/**
 * Die Enumeration {@code CollisionDirection} definiert die vier möglichen Richtungen
 * oder Seiten, aus denen eine Kollision zwischen zwei Objekten erfolgen kann.
 *
 * @author Patrick André
 * @version 1.2.6
 */
public enum CollisionDirection {
    /**
     * Kollision an der Oberseite des Objekts
     */
    TOP,

    /**
     * Kollision an der Unterseite des Objekts
     */
    BOTTOM,

    /**
     * Kollision an der linken Seite des Objekts
     */
    LEFT,

    /**
     * Kollision an der rechten Seite des Objekts
     */
    RIGHT
}
