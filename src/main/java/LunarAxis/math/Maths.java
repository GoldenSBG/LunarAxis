package LunarAxis.math;

/**
 * Die Klasse {@code Maths} stellt eine Sammlung mathematischer Hilfsfunktionen
 * und Konstanten (wie E und PI) bereit.
 * <p>
 * Sie dient als Wrapper bzw. Erweiterung um die Standard-Java-Mathematikfunktionen ({@link Math}),
 * optimiert für die Verwendung in der LunarAxis-Engine. Da alle Methoden statisch sind,
 * muss diese Klasse nicht instanziiert werden.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Maths {

    /**
     * Die mathematische Konstante e (Eulersche Zahl).
     */
    public static final double E = Math.E;

    /**
     *  Die mathematische Konstante Pi .
     */
    public static final double PI = Math.PI;

    /**
     * Öffentlicher Standardkonstruktor (da reine Hilfsklasse, ist keine Instanziierung erforderlich).
     */
    public Maths() {}

    /**
     * Berechnet den Sinus eines Winkels (im Bogenmass).
     *
     * @param a Der Winkel in Radianten.
     * @return Der Sinus-Wert.
     */
    public static double sin(double a) {
        return Math.sin(a);
    }

    /**
     * Berechnet den Cosinus eines Winkels (im Bogenmass).
     *
     * @param a Der Winkel in Radianten.
     * @return Der Cosinus-Wert.
     */
    public static double cos(double a) {
        return Math.cos(a);
    }

    /**
     * Berechnet den Tangens eines Winkels (im Bogenmass).
     *
     * @param a Der Winkel in Radianten.
     * @return Der Tangens-Wert.
     */
    public static double tan(double a) {
        return Math.tan(a);
    }

    /**
     * Berechnet den Arcussinus (Umkehrfunktion des Sinus).
     *
     * @param a Der Wert.
     * @return Der Winkel in Radianten.
     */
    public static double asin(double a) {
        return Math.asin(a);
    }

    /**
     * Berechnet den Arcuscosinus (Umkehrfunktion des Cosinus).
     *
     * @param a Der Wert.
     * @return Der Winkel in Radianten.
     */
    public static double acos(double a) {
        return Math.acos(a);
    }

    /**
     * Berechnet den Arcustangens (Umkehrfunktion des Tangens).
     *
     * @param a Der Wert.
     * @return Der Winkel in Radianten.
     */
    public static double atan(double a) {
        return Math.atan(a);
    }

    /**
     * Wandelt einen Grad-Winkel in Bogenmass (Radiant) um.
     *
     * @param angdeg Der Winkel in Grad.
     * @return Der Winkel in Radianten.
     */
    public static double toRadians(double angdeg) {
        return Math.toRadians(angdeg);
    }

    /**
     * Wandelt ein Bogenmass (Radiant) in einen Grad-Winkel um.
     *
     * @param angrad Der Winkel in Radianten.
     * @return Der Winkel in Grad.
     */
    public static double toDegrees(double angrad) {
        return Math.toDegrees(angrad);
    }

    /**
     * Gibt die e-Hoch-Potenz (e^a) zurück.
     *
     * @parm a Der Exponent.
     * @return Das Ergebnis.
     */
    public static double exp(double a) {
        return Math.exp(a);
    }

    /**
     * Berechnet den natürlichen Logarithmus (Basis e) einer Zahl.
     *
     * @param a Die Zahl.
     * @return Der natürliche Logarithmus.
     */
    public static double log(double a) {
        return Math.log(a);
    }

    /**
     * Berechnet den Zehnerlogarithmus (Basis 10) einer Zahl.
     *
     * @param a Die Zahl.
     * @return Der Logarithmus zur Basis 10.
     */
    public static double log10(double a) {
        return Math.log10(a);
    }

    /**
     * Berechnet die Quadratwurzel einer Zahl.
     *
     * @param a Die Zahl.
     * @return Die Quadratwurzel ($\sqrt{a}$).
     */
    public static double sqrt(double a) {
        return Math.sqrt(a);
    }

    /**
     * Berechnet die Kubikwurzel einer Zahl.
     *
     * @param a Die Zahl.
     * @return Die Kubikwurzel.
     */
    public static double cbrt(double a) {
        return Math.cbrt(a);
    }

    /**
     * Rundet eine Zahl auf die nächste ganze Zahl auf (Ceiling).
     *
     * @param a Der Ausgangswert.
     * @return Der aufgerundete Wert.
     */
    public static double ceil(double a) {
        return Math.ceil(a);
    }

    /**
     * Rundet eine Zahl auf die nächste ganze Zahl ab (Floor).
     *
     * @param a Der Ausgangswert.
     * @return Der abgerundete Wert.
     */
    public static double floor(double a) {
        return Math.floor(a);
    }

    /**
     * Konvertiert rechteckige Koordinaten (x, y) in Polar-Koordinaten (theta).
     *
     * @param y Die Y-Koordinate.
     * @param x Die X-Koordinate.
     * @return Der Winkel in Radianten.
     */
    public static double atan2(double y, double x) {
        return Math.atan2(y, x);
    }

    /**
     * Potenziert eine Zahl ($a^b$).
     *
     * @param a Die Basis.
     * @param b Der Exponent.
     * @return Das Ergebnis.
     */
    public static double pow(double a, double b) {
        return Math.pow(a, b);
    }

    /**
     * Rundet eine Fließkommazahl (float) auf den nächsten Integer-Wert.
     *
     * @param a Der Float-Wert.
     * @return Der gerundete Integer.
     */
    public static int round(float a) {
        return Math.round(a);
    }

    /**
     * Rundet eine Fließkommazahl (double) auf den nächsten Long-Wert.
     *
     * @param a Der Double-Wert.
     * @return Der gerundete Long-Wert.
     */
    public static long round(double a) {
        return Math.round(a);
    }

    /**
     * Generiert eine Zufallszahl als Double zwischen 0.0 (inklusive) und 1.0 (exklusive).
     *
     * @return Eine Zufallszahl.
     */
    public static double random() {
        return Math.random();
    }

    /**
     * Generiert eine Zufallszahl als Float zwischen 0.0f (inklusive) und 1.0f (exklusive).
     *
     * @return Eine zufällige Gleitkommazahl.
     */
    public static float randomFloat() {
        return (float) Math.random();
    }

    /**
     * Gibt den Betrag (absoluten Wert) eines Integers zurück.
     *
     * @param a Der Integer.
     * @return Der positive Betrag.
     */
    public static int abs(int a) {
        return Math.abs(a);
    }

    /**
     * Gibt den Betrag (absoluten Wert) eines Long-Wertes zurück.
     *
     * @param a Der Long-Wert.
     * @return Der positive Betrag.
     */
    public static long abs(long a) {
        return Math.abs(a);
    }

    /**
     * Gibt den Betrag (absoluten Wert) eines Long-Wertes zurück.
     *
     * @param a Der Long-Wert.
     * @return Der positive Betrag.
     */
    public static float abs(float a) {
        return Math.abs(a);
    }

    /**
     * Gibt den Betrag (absoluten Wert) eines Doubles zurück.
     *
     * @param a Der Double-Wert.
     * @return Der positive Betrag.
     */
    public static double abs(double a) {
        return Math.abs(a);
    }

    /**
     * Ermittelt das Maximum zweier Integer-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die größere der beiden Zahlen.
     */
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    /**
     * Ermittelt das Maximum zweier Long-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die größere der beiden Zahlen.
     */
    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    /**
     * Ermittelt das Maximum zweier Float-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die größere der beiden Zahlen.
     */
    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    /**
     * Ermittelt das Maximum zweier Double-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die größere der beiden Zahlen.
     */
    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    /**
     * Ermittelt das Minimum zweier Integer-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die kleinere der beiden Zahlen.
     */
    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    /**
     * Ermittelt das Minimum zweier Long-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die kleinere der beiden Zahlen.
     */
    public static long min(long a, long b) {
        return Math.min(a, b);
    }

    /**
     * Ermittelt das Minimum zweier Float-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die kleinere der beiden Zahlen.
     */
    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    /**
     * Ermittelt das Minimum zweier Double-Zahlen.
     *
     * @param a Erste Zahl.
     * @param b Zweite Zahl.
     * @return Die kleinere der beiden Zahlen.
     */
    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    /**
     * Gibt das Vorzeichen eines Double-Wertes zurück (-1.0, 0.0 oder 1.0).
     *
     * @param d Der Wert.
     * @return Das Vorzeichen.
     */
    public static double signum(double d) {
        return Math.signum(d);
    }

    /**
     * Gibt das Vorzeichen eines Float-Wertes zurück (-1.0f, 0.0f oder 1.0f).
     *
     * @param f Der Wert.
     * @return Das Vorzeichen.
     */
    public static float signum(float f) {
        return Math.signum(f);
    }

    /**
     * Begrenzt (clampet) einen Float-Wert strikt auf den Bereich zwischen {@code 0.0f} und {@code 1.0f}.
     *
     * @param value Der zu prüfende Wert.
     * @return Der begrenzte Wert im Intervall [0.0, 1.0].
     */
    public static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

}
