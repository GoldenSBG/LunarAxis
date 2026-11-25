package LunarAxis.physics;

import LunarAxis.math.Vec2;

/**
 * Die Klasse {@code CollisionResult} kapselt die Detailergebnisse einer erfolgreich
 * aufgelösten AABB-Kollision zwischen zwei Rechtecken.
 * <p>
 * Sie speichert die Eindringtiefe (Minimum Translation Vector / MTV), die primäre
 * {@link CollisionDirection} sowie den exakten Berührungspunkt (Contact Point).
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class CollisionResult {

    private final Vec2 depth;
    private final CollisionDirection direction;
    private final Vec2 contactPoint;

    /**
     * Erstellt ein neues CollisionResult ohne expliziten Kontaktpunkt.
     * Der bestehende Konstruktor bleibt für bestehende Aufrufer erhalten.
     *
     * @param depth     Der Eindringtiefen-Vektor (MTV).
     * @param direction Die Hauptkollisionsrichtung als {@link CollisionDirection}.
     */
    public CollisionResult(Vec2 depth, CollisionDirection direction) {
        this(depth, direction, null);
    }

    /**
     * Erstellt ein neues CollisionResult mit Eindringtiefe, Richtung und exaktem Kontaktpunkt.
     *
     * @param depth        Der Eindringtiefen-Vektor (MTV).
     * @param direction    Die {@link CollisionDirection}.
     * @param contactPoint Der geometrische Kontaktpunkt als {@link Vec2} (kann {@code null} sein).
     */
    public CollisionResult(Vec2 depth, CollisionDirection direction, Vec2 contactPoint) {
        this.depth = depth;
        this.direction = direction;
        this.contactPoint = contactPoint;
    }

    /**
     * Gibt den Eindringtiefen-Vektor (MTV) zurück.
     *
     * @return {@link Vec2} der Verschiebung.
     */
    public Vec2 getDepth() {
        return depth;
    }

    /**
     * Gibt die Kollisionsrichtung zurück.
     *
     * @return Die {@link CollisionDirection}.
     */
    public CollisionDirection getDirection() {
        return direction;
    }

    /**
     * Gibt die MTV-Richtung als normalisierten Vektor zurück.
     * Das Vorzeichen zeigt in die Richtung, in welche das erste Rechteck aus dem zweiten herausbewegt wird.
     *
     * @return Ein normalisierter {@link Vec2}-Einheitsvektor (oder Nullvektor, falls depth {@code null} ist).
     */
    public Vec2 getNormal() {
        if (depth == null) {
            return new Vec2(0, 0);
        }
        return depth.normalize();
    }

    /**
     * Gibt eine defensive Kopie des repräsentativen Punkts auf der Kontaktkante nach Anwendung des MTV zurück.
     *
     * @return Der Kontaktpunkt als {@link Vec2} oder {@code null}.
     */
    public Vec2 getContactPoint() {
        return contactPoint != null ? contactPoint.copy() : null;
    }

    /**
     * Prüft, ob die Kollision von oben erfolgt ist.
     *
     * @return {@code true}, wenn Richtung TOP ist, sonst {@code false}.
     */
    public boolean isTop() {
        return direction == CollisionDirection.TOP;
    }

    /**
     * Prüft, ob die Kollision von unten erfolgt ist.
     *
     * @return {@code true}, wenn Richtung BOTTOM ist, sonst {@code false}.
     */
    public boolean isBottom() {
        return direction == CollisionDirection.BOTTOM;
    }

    /**
     * Prüft, ob die Kollision von links erfolgt ist.
     *
     * @return {@code true}, wenn Richtung LEFT ist, sonst {@code false}.
     */
    public boolean isLeft() {
        return direction == CollisionDirection.LEFT;
    }

    /**
     * Prüft, ob die Kollision von rechts erfolgt ist.
     *
     * @return {@code true}, wenn Richtung RIGHT ist, sonst {@code false}.
     */
    public boolean isRight() {
        return direction == CollisionDirection.RIGHT;
    }
}
