package LunarAxis.math;

import LunarAxis.physics.CollisionDirection;
import LunarAxis.physics.CollisionResult;

/**
 * Die Klasse {@code Rect} repräsentiert ein 2D-Rechteck als Axis-Aligned Bounding Box (AABB).
 * <p>
 * Sie ist fundamental für die 2D-Kollisionserkennung in der Engine.
 * Da TigerJython-Nutzer eine einfache API benötigen, sind die Methoden intuitiv gehalten.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Rect {

    private float x, y, width, height;

    /**
     * Erstellt ein neues Rechteck mit Koordinaten und Dimensionen.
     *
     * @param x      X-Koordinate (meist obere linke Ecke oder Zentrum, je nach Rendering-Logik)
     * @param y      Y-Koordinate
     * @param width  Breite des Rechtecks
     * @param height Höhe des Rechtecks
     */
    public Rect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Prüft, ob sich dieses Rechteck mit einem anderen überschneidet (AABB-Kollision).
     *
     * @param other Das andere {@link Rect}-Objekt.
     * @return {@code true}, wenn eine Überschneidung vorliegt, ansonsten {@code false}.
     */
    public boolean intersects(Rect other) {
        if (other == null) {
            return false;
        }
        
        return this.x < other.x + other.width &&
               this.x + this.width > other.x &&
               this.y < other.y + other.height &&
               this.y + this.height > other.y;
    }

    /**
     * Prüft, ob ein bestimmter Punkt (z.B. ein Mausklick) innerhalb des Rechtecks liegt.
     *
     * @param px X-Koordinate des Punktes
     * @param py Y-Koordinate des Punktes
     * @return {@code true}, wenn der Punkt im Rechteck liegt.
     */
    public boolean contains(float px, float py) {
        return px >= this.x && px <= this.x + this.width &&
               py >= this.y && py <= this.y + this.height;
    }

    /**
     * Prüft, ob ein 2D-Vektor innerhalb des Rechtecks liegt.
     *
     * @param point Der {@link Vec2}-Vektor, der den Punkt repräsentiert.
     * @return {@code true}, wenn der Punkt im Rechteck liegt.
     */
    public boolean contains(Vec2 point) {
        if (point == null) {
            return false;
        }

        return contains(point.getX(), point.getY());
    }

    /**
     * Berechnet die minimale Verschiebungstiefe (Intersection Depth / Penetration Vector),
     * die erforderlich ist, um die Überschneidung mit einem anderen Rechteck aufzulösen.
     *
     * @param other Das andere {@link Rect}-Objekt.
     * @return Ein {@link Vec2}-Vektor mit der Verschiebung (push vector) oder {@code null}, wenn keine Kollision vorliegt.
     */
    public Vec2 getIntersectionDepth(Rect other) {
        if (other == null || !this.intersects(other)) {
            return null;
        }

        //Mittelpunkt der Rechtecke
        float cx1 = getCenterX();
        float cy1 = getCenterY();
        float cx2 = other.getCenterX();
        float cy2 = other.getCenterY();

        //Halbe Breite/Höhe
        float halfW1 = this.width / 2.0f;
        float halfH1 = this.height / 2.0f;
        float halfW2 = other.width / 2.0f;
        float halfH2 = other.height / 2.0f;

        //Überlappung auf beiden Achsen berechnen
        float overlapX = (halfW1 + halfW2) - Math.abs(cx1 - cx2);
        float overlapY = (halfH1 + halfH2) - Math.abs(cy1 - cy2);

        //Richtung der Verschiebung (von diesem Rechteck zu other)
        float dirX = cx2 - cx1;
        float dirY = cy2 - cy1;

        //kleinere Überlappung nehmen ("richtige" Kollisionsrichtung)
        if (overlapX < overlapY) {
            //x-achse Verschiebung
            float pushX = overlapX * (dirX > 0 ? -1 : 1);
            return new Vec2(pushX, 0);
        } else {
            //y-achse Verschiebung
            float pushY = overlapY * (dirY > 0 ? -1 : 1);
            return new Vec2(0, pushY);
        }
    }

    /**
     * Ermittelt die Kollisionsdetails inklusive Verschiebungstiefe, Kollisionsrichtung
     * und exaktem Kontaktpunkt (ähnlich wie {@code getIntersectionDepth}).
     *
     * @param other Das andere {@link Rect}-Objekt.
     * @return Ein {@link CollisionResult}-Objekt oder {@code null}, wenn keine Kollision vorliegt.
     */
    public CollisionResult resolveCollision(Rect other) {
        Vec2 depth = getIntersectionDepth(other);
        if (depth == null) {
            return null;
        }

        //Richtung bestimmen
        CollisionDirection direction;
        if (Maths.abs(depth.getX()) > Maths.abs(depth.getY())) {
            direction = depth.getX() < 0 ? CollisionDirection.LEFT : CollisionDirection.RIGHT;
        } else {
            direction = depth.getY() < 0 ? CollisionDirection.TOP : CollisionDirection.BOTTOM;
        }

        Vec2 contactPoint = calculateContactPoint(other, depth);
        return new CollisionResult(depth, direction, contactPoint);
    }

    /**
     * Interne Hilfsmethode zur präzisen Berechnung des exakten Berührungspunkts (Contact Point) bei einer Kollision.
     *
     * @param other Das andere Rechteck.
     * @param depth Die ermittelte Schnittiefe.
     * @return Der Kontaktpunkt als {@link Vec2}.
     */
    private Vec2 calculateContactPoint(Rect other, Vec2 depth) {
        float correctedX = x + depth.getX();
        float correctedY = y + depth.getY();

        if (depth.getX() != 0.0f) {
            float contactX = depth.getX() < 0.0f ? correctedX + width : correctedX;

            float overlapTop = Math.max(correctedY, other.y);
            float overlapBottom = Math.min(correctedY + height, other.y + other.height);
            float contactY = (overlapTop + overlapBottom) / 2.0f;

            return new Vec2(contactX, contactY);
        }

        float contactY = depth.getY() < 0.0f
                ? correctedY + height
                : correctedY;

        float overlapLeft = Math.max(correctedX, other.x);
        float overlapRight = Math.min(correctedX + width, other.x + other.width);
        float contactX = (overlapLeft + overlapRight) / 2.0f;

        return new Vec2(contactX, contactY);
    }


    /**
     * Gibt die X-Koordinate des Rechtecks zurück.
     * @return X-Position
     */
    public float getX() {
        return x;
    }

    /**
     * Setzt die X-Koordinate des Rechtecks.
     * @param x Neue X-Position
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Gibt die Y-Koordinate des Rechtecks zurück.
     * @return Y-Position
     */
    public float getY() {
        return y;
    }

    /**
     * Setzt die Y-Koordinate des Rechtecks.
     * @param y Neue Y-Position
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Gibt die Breite des Rechtecks zurück.
     * @return Breite in Pixeln
     */
    public float getWidth() {
        return width;
    }

    /**
     * Setzt die Breite des Rechtecks.
     * @param width Neue Breite
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * Gibt die Höhe des Rechtecks zurück.
     * @return Höhe in Pixeln
     */
    public float getHeight() {
        return height;
    }

    /**
     * Setzt die Höhe des Rechtecks.
     * @param height Neue Höhe
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Berechnet die X-Koordinate des Mittelpunkts des Rechtecks.
     * @return Zentrum-X
     */
    public float getCenterX() {
        return x + width / 2.0f;
    }

    /**
     * Berechnet die Y-Koordinate des Mittelpunkts des Rechtecks.
     * @return Zentrum-Y
     */
    public float getCenterY() {
        return y + height / 2.0f;
    }
}
