package LunarAxis.math;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.util.Objects;

/**
 * Die Klasse {@code Vec2} repräsentiert einen mathematischen 2D-Vektor mit X- und Y-Komponenten.
 * <p>
 * Sie wird in der gesamten Engine für Positionen, Geschwindigkeiten, Kräfte und Richtungsberechnungen
 * in der zweidimensionalen Spielwelt verwendet.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Vec2 {

    /**
     * Die X- und Y-Komponenten des Vektors.
     */
    public float x, y;

    /**
     * Erstellt einen neuen 2D-Vektor initialisiert bei (0, 0).
     */
    public Vec2() {
        this(0f, 0f);
    }

    /**
     * Erstellt einen neuen 2D-Vektor mit den angegebenen Koordinaten.
     *
     * @param x X-Komponente
     * @param y Y-Komponente
     */
    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy-Konstruktor: Erstellt einen neuen Vektor als exakte Kopie eines anderen Vektors.
     *
     * @param other Der zu kopierende {@link Vec2}-Vektor
     */
    public Vec2(Vec2 other) {
        this(other.x, other.y);
    }

    /**
     * Addiert einen anderen Vektor zu diesem Vektor und gibt das Ergebnis als neuen Vektor zurück.
     *
     * @param other Der zu addierende {@link Vec2}
     * @return Ein neuer {@link Vec2} mit der Summe
     */
    public Vec2 add(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    /**
     * Addiert feste X- und Y-Werte zu diesem Vektor und gibt das Ergebnis als neuen Vektor zurück.
     *
     * @param x Der zu addierende X-Wert
     * @param y Der zu addierende Y-Wert
     * @return Ein neuer {@link Vec2} mit der Summe
     */
    public Vec2 add(float x, float y) {
        return new Vec2(this.x + x, this.y + y);
    }

    /**
     * Subtrahiert einen anderen Vektor von diesem Vektor und gibt das Ergebnis als neuen Vektor zurück.
     *
     * @param other Der zu subtrahierende {@link Vec2}
     * @return Ein neuer {@link Vec2} mit der Differenz
     */
    public Vec2 subtract(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }

    /**
     * Subtrahiert feste X- und Y-Werte von diesem Vektor und gibt das Ergebnis als neuen Vektor zurück.
     *
     * @param x Der zu subtrahierende X-Wert
     * @param y Der zu subtrahierende Y-Wert
     * @return Ein neuer {@link Vec2} mit der Differenz
     */
    public Vec2 subtract(float x, float y) {
        return new Vec2(this.x - x, this.y - y);
    }

    /**
     * Multipliziert den Vektor mit einem Skalar (Skalierung).
     *
     * @param scalar Der Skalierungsfaktor
     * @return Ein neuer {@link Vec2} mit den skalierten Komponenten
     */
    public Vec2 multiply(float scalar) {
        return new Vec2(x * scalar, y * scalar);
    }

    /**
     * Dividiert den Vektor durch einen Skalar.
     *
     * @param scalar Der Divisor
     * @return Ein neuer {@link Vec2} mit den dividierten Komponenten
     * @throws RuntimeException (Lunar-Error) wenn der Skalar gleich 0 ist
     */
    public Vec2 divide(float scalar) {
        if (scalar == 0f) {
            throw Errors.arithmetic(ErrorDetails.of("Vec2", "Cannot divide a vector by zero")
                    .withActual("scalar=" + scalar + ", x=" + x + ", y=" + y)
                    .withExpected("A scalar other than positive or negative zero")
                    .withExplanation("Vector division divides both components by the scalar; this operation does not permit a zero divisor"), null);
        }

        return new Vec2(x / scalar, y / scalar);
    }

    /**
     * Berechnet die Länge (den Betrag) des Vektors.
     *
     * @return Die Vektorlänge als Float
     */
    public float length() {
        return (float) Maths.sqrt(x * x + y * y);
    }

    /**
     * Berechnet das quadrierte Quadrat der Vektorlänge (Vermeidung langsamer Wurzelberechnungen).
     *
     * @return Die quadrierte Länge ($x^2 + y^2$)
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Berechnet den euklidischen Abstand zu einem anderen Vektor.
     *
     * @param other Der Ziel-{@link Vec2}
     * @return Die Distanz zwischen den beiden Vektoren
     */
    public float distance(Vec2 other) {
        float dx = x - other.x;
        float dy = y - other.y;

        return (float) Maths.sqrt(dx * dx + dy * dy);
    }

    /**
     * Normalisiert den Vektor (gibt einen Einheitsvektor mit Länge 1.0 in dieselbe Richtung zurück).
     *
     * @return Ein neuer, normalisierter {@link Vec2} oder ein Nullvektor, falls die Länge 0 ist
     */
    public Vec2 normalize() {
        float len = length();

        if (len == 0) {
            return new Vec2();
        }

        return new Vec2(x / len, y / len);
    }

    /**
     * Berechnet das Skalarprodukt (Dot Product) mit einem anderen Vektor.
     *
     * @param other Der andere {@link Vec2}
     * @return Das Skalarprodukt als Float
     */
    public float dot(Vec2 other) {
        return x * other.x + y * other.y;
    }

    /**
     * Berechnet das quadratische Distanzquadrat zu einem anderen Vektor (optimiert ohne Wurzel).
     *
     * @param other Der Ziel-{@link Vec2}
     * @return Das Distanzquadrat
     */
    public float distanceSquared(Vec2 other) {
        float dx = x - other.x;
        float dy = y - other.y;

        return dx * dx + dy * dy;
    }

    /**
     * Berechnet das 2D-Kreuzprodukt (Z-Wert des Kreuzprodukts zweier 2D-Vektoren).
     *
     * @param other Der andere {@link Vec2}
     * @return Den Z-Wert des Kreuzprodukts
     */
    public float cross(Vec2 other) {
        return x * other.y - y * other.x;
    }

    /**
     * Erstellt eine defensive Kopie dieses Vektors.
     *
     * @return Ein neuer {@link Vec2} mit denselben Koordinaten
     */
    public Vec2 copy() {
        return new Vec2(this);
    }

    /**
     * Setzt die Komponenten dieses Vektors auf neue X- und Y-Werte.
     *
     * @param x Neuer X-Wert
     * @param y Neuer Y-Wert
     * @return Diesen Vektor (für Method Chaining)
     */
    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Übernimmt die Koordinaten eines anderen Vektors.
     *
     * @param other Der Quell-{@link Vec2}
     * @return Diesen Vektor (für Method Chaining)
     */
    public Vec2 set(Vec2 other) {
        return set(other.x, other.y);
    }

    /**
     * Prüft, ob dieser Vektor wertmäßig gleich einem anderen Objekt ist.
     *
     * @param obj Das Vergleichsobjekt
     * @return {@code true}, wenn beide Vektoren exakt übereinstimmen
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Vec2)) {
            return false;
        }

        Vec2 other = (Vec2) obj;

        return Float.compare(other.x, x) == 0 && Float.compare(other.y, y) == 0;
    }

    /**
     * Generiert einen Hash-Code für diesen Vektor basierend auf seinen Komponenten.
     *
     * @return Der Hash-Code
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    /**
     * Gibt die X-Komponente des Vektors zurück.
     * @return X-Wert
     */
    public float getX() {
        return x;
    }

    /**
     * Gibt die Y-Komponente des Vektors zurück.
     * @return Y-Wert
     */
    public float getY() {
        return y;
    }

    /**
     * Setzt die X-Komponente des Vektors.
     * @param x Neuer X-Wert
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Setzt die Y-Komponente des Vektors.
     * @param y Neuer Y-Wert
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Gibt eine formatierte textuelle Darstellung des Vektors zurück (z.B. für Debugging).
     *
     * @return Ein formatierter String im Format {@code Vec2(x, y)}
     */
    @Override
    public String toString() {
        return String.format("Vec2(%.2f, %.2f)", x, y);
    }

}
