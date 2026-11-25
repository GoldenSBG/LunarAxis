package LunarAxis.util;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.util.Random;

/**
 * Diese Klasse bietet eine komfortable Schnittstelle zur Verwendung der Java-Standard-Bibliotheks-Klasse {@link Random}.
 * Sie stellt Methoden bereit, um Zufallszahlen aller primären Datentypen
 * sowie nützliche Hilfsfunktionen für Spiele und mathematische Berechnungen zu generieren.
 * <p>
 * Neben der normalen Instanziierung bietet die Klasse eine globale Standard-Instanz {@link #global()},
 * um Zufallszahlen innerhalb des gesamten Engine-Frameworks ohne erneute Objektallokation zu nutzen.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * random = LunarRandom()
 * nextInt = random.nextInt()
 *
 * # Verwendung der globalen Instanz:
 * value = LunarRandom.global().nextFloat(0f, 100f)
 * </pre>

 * @author Patrick André
 * @version 1.2.6
 */
public class LunarRandom {

    private static final LunarRandom GLOBAL = new LunarRandom();

    // Interne Random-Instanz aus der Java-Standardbibliothek
    private final Random random;

    /**
     * Erstellt eine neue {@code LunarRandom}-Instanz mit einem zufälligen Startwert (Seed).
     */
    public LunarRandom() {
        this.random = new Random();
    }

    /**
     * Erstellt eine neue {@code LunarRandom}-Instanz mit einem festen Startwert (Seed).
     * <p>
     * Ein fester Seed stellt sicher, dass die generierte Zufallssequenz deterministisch
     * und exakt reproduzierbar ist (z. B. für Procedural Generation oder automatisierte Tests).
     * </p>
     *
     * @param seed Der Startwert für den Zufallsgenerator.
     */
    public LunarRandom(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Gibt die globale Standard-Instanz der {@code LunarRandom}-Klasse zurück.
     *
     * @return Die globale {@code LunarRandom}-Instanz.
     */
    public static LunarRandom global() {
        return GLOBAL;
    }

    /**
     * Setzt einen neuen Startwert (Seed) für den internen Zufallsgenerator.
     *
     * @param seed Der neue Startwert.
     */
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    /**
     * Generiert eine zufällige 32-Bit-Ganzzahl (int) über den gesamten Wertebereich.
     *
     * @return Eine zufällige {@code int}-Zahl.
     */
    public int nextInt() {
        return random.nextInt();
    }

    /**
     * Generiert eine zufällige Ganzzahl im Bereich von 0 (inklusiv) bis {@code bound} (exklusiv).
     *
     * @param bound Die obere Grenze (exklusiv; muss &gt; 0 sein).
     * @return Eine zufällige {@code int}-Zahl im Intervall {@code [0, bound)}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code bound} nicht positiv ist.
     */
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Integer bound must be positive")
                    .withActual(bound)
                    .withExpected("An exclusive upper bound greater than 0")
                    .withExplanation("The interval from 0 to the exclusive upper bound must contain at least one integer"), null);
        }

        return random.nextInt(bound);
    }

    /**
     * Generiert eine zufällige Ganzzahl im angegebenen geschlossenen/halboffenen Bereich.
     *
     * @param min Die untere Grenze (inklusiv).
     * @param max Die obere Grenze (exklusiv).
     * @return Eine zufällige {@code int}-Zahl im Intervall {@code [min, max)}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code min >= max} oder die Bereichsbreite unzulässig ist.
     */
    public int nextInt(int min, int max) {
        long range = (long) max - min;
        if (range <= 0 || range > Integer.MAX_VALUE) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Integer range is invalid or too wide")
                    .withActual("min=" + min + ", max=" + max + ", range=" + range)
                    .withExpected("min < max and a range width no greater than Integer.MAX_VALUE")
                    .withExplanation("The current bounded integer generator requires a positive int range width"), null);
        }
        return random.nextInt(max - min) + min;
    }

    /**
     * Generiert eine doppeltgenaue Gleitkommazahl im Bereich von 0.0 (inklusiv) bis 1.0 (exklusiv).
     *
     * @return Eine zufällige {@code double}-Zahl im Intervall {@code [0.0, 1.0)}.
     */
    public double nextDouble() {
        return random.nextDouble();
    }

    /**
     * Generiert eine doppeltgenaue Gleitkommazahl im Bereich von 0.0 (inklusiv) bis {@code bound} (exklusiv).
     *
     * @param bound Die obere Grenze (exklusiv; muss endlich und &gt; 0 sein).
     * @return Eine zufällige {@code double}-Zahl im Intervall {@code [0.0, bound)}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code bound} ungültig oder nicht positiv ist.
     */
    public double nextDouble(double bound) {
        if (!Double.isFinite(bound) || bound <= 0.0d) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Double bound must be finite and positive")
                    .withActual(bound)
                    .withExpected("A finite exclusive upper bound greater than 0")
                    .withExplanation("The requested interval starts at 0 and requires a finite upper bound above 0"), null);
        }

        return random.nextDouble() * bound;
    }

    /**
     * Generiert eine doppeltgenaue Gleitkommazahl im angegebenen Bereich.
     *
     * @param min Die untere Grenze (inklusiv).
     * @param max Die obere Grenze (exklusiv).
     * @return Eine zufällige {@code double}-Zahl im Intervall {@code [min, max)}.
     * @throws RuntimeException (Lunar-Error) Wenn die Grenzen unendlich oder {@code min >= max} sind.
     */
    public double nextDouble(double min, double max) {
        if (!Double.isFinite(min) || !Double.isFinite(max) || min >= max || !Double.isFinite(max - min)) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Double range is invalid")
                    .withActual("min=" + min + ", max=" + max)
                    .withExpected("Finite bounds with min < max and a finite difference")
                    .withExplanation("The current range calculation multiplies a random fraction by max minus min"), null);
        }

        return random.nextDouble() * (max - min) + min;
    }

    /**
     * Generiert einen zufälligen Wahrheitswert mit einer Wahrscheinlichkeit von jeweils 50 %.
     *
     * @return {@code true} oder {@code false}.
     */
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    /**
     * Generiert einen zufälligen Wahrheitswert basierend auf einer gewichteten Wahrscheinlichkeit.
     *
     * @param probability Die Wahrscheinlichkeit für {@code true} im Bereich von 0.0 bis 1.0 (inklusiv).
     * @return {@code true} mit der angegebenen Wahrscheinlichkeit, sonst {@code false}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code probability} ausserhalb von [0.0, 1.0] liegt.
     */
    public boolean nextBoolean(float probability) {
        if (!Float.isFinite(probability) || probability < 0.0f || probability > 1.0f) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Probability is outside the valid range")
                    .withActual(probability)
                    .withExpected("A finite probability between 0 and 1, inclusive")
                    .withExplanation("NaN does not represent a probability and must not silently produce false"), null);
        }
        return random.nextFloat() < probability;
    }

    /**
     * Generiert eine einfache Gleitkommazahl im Bereich von 0.0f (inklusiv) bis 1.0f (exklusiv).
     *
     * @return Eine zufällige {@code float}-Zahl im Intervall {@code [0.0f, 1.0f)}.
     */
    public float nextFloat() {
        return random.nextFloat();
    }


    /**
     * Generiert eine einfache Gleitkommazahl im Bereich von 0.0f (inklusiv) bis {@code bound} (exklusiv).
     *
     * @param bound Die obere Grenze (exklusiv; muss endlich und &gt; 0 sein).
     * @return Eine zufällige {@code float}-Zahl im Intervall {@code [0.0f, bound)}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code bound} ungültig oder nicht positiv ist.
     */
    public float nextFloat(float bound) {
        if (!Float.isFinite(bound) || bound <= 0.0f) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Float bound must be finite and positive")
                    .withActual(bound)
                    .withExpected("A finite exclusive upper bound greater than 0")
                    .withExplanation("The requested interval starts at 0 and requires a finite upper bound above 0"), null);
        }

        return random.nextFloat() * bound;
    }

    /**
     * Generiert eine einfache Gleitkommazahl im angegebenen Bereich.
     *
     * @param min Die untere Grenze (inklusiv).
     * @param max Die obere Grenze (exklusiv).
     * @return Eine zufällige {@code float}-Zahl im Intervall {@code [min, max)}.
     * @throws RuntimeException (Lunar-Error) Wenn die Grenzen unendlich oder {@code min >= max} sind.
     */
    public float nextFloat(float min, float max) {
        if (!Float.isFinite(min) || !Float.isFinite(max) || min >= max || !Float.isFinite(max - min)) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Float range is invalid")
                    .withActual("min=" + min + ", max=" + max)
                    .withExpected("Finite bounds with min < max and a finite float difference")
                    .withExplanation("The current range calculation requires a finite positive interval width"), null);
        }

        return random.nextFloat() * (max - min) + min;
    }

    /**
     * Generiert eine zufällige 64-Bit-Ganzzahl (long) über den gesamten Wertebereich.
     *
     * @return Eine zufällige {@code long}-Zahl.
     */
    public long nextLong() {
        return random.nextLong();
    }


    /**
     * Generiert eine zufällige lange Ganzzahl im Bereich von 0 (inklusiv) bis {@code bound} (exklusiv).
     * Nutzt Rejection Sampling zur mathematisch exakten Vermeidung von Modulo-Bias.
     *
     * @param bound Die obere Grenze (exklusiv; muss &gt; 0 sein).
     * @return Eine zufällige {@code long}-Zahl im Intervall {@code [0, bound)}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code bound} nicht positiv ist.
     */
    public long nextLong(long bound) {
        if (bound <= 0) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Long bound must be positive")
                    .withActual(bound)
                    .withExpected("An exclusive upper bound greater than 0")
                    .withExplanation("The bounded long generator requires a positive divisor and a non-empty interval"), null);
        }

        long bits, val;
        do {
            bits = random.nextLong() >>> 1; // 1. Vorzeichenbit auf 0 setzen (nur positive Werte)
            val = bits % bound;            // 2. Modulo-Berechnung für den Zielbereich
        } while (bits - val + (bound - 1) < 0); // 3. Rejection Sampling
        return val;
    }

    /**
     * Generiert eine zufällige lange Ganzzahl im angegebenen Bereich.
     *
     * @param min Die untere Grenze (inklusiv).
     * @param max Die obere Grenze (exklusiv).
     * @return Eine zufällige {@code long}-Zahl im Intervall {@code [min, max)}.
     * @throws RuntimeException (Lunar-Error) Wenn {@code min >= max} oder ein Überlauf auftritt.
     */
    public long nextLong(long min, long max) {
        long range = max - min;
        if (min >= max || range <= 0) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Long range is invalid or too wide")
                    .withActual("min=" + min + ", max=" + max)
                    .withExpected("min < max and a positive range width representable as long")
                    .withExplanation("The current bounded long generator cannot accept an interval width that overflows long"), null);
        }

        return nextLong(max - min) + min;
    }


    /**
     * Füllt ein gegebenes Byte-Array mit zufälligen Bytes.
     *
     * @param bytes Das Ziel-Byte-Array.
     * @throws RuntimeException (Lunar-Error) Wenn {@code bytes} {@code null} ist.
     */
    public void nextBytes(byte[] bytes) {
        if (bytes == null) {
            throw Errors.nullValue(ErrorDetails.of("LunarRandom", "Byte array must not be null")
                    .withActual(null)
                    .withExpected("An existing byte array, which may be empty")
                    .withExplanation("Random bytes must be written into an existing destination array"), null);
        }

        random.nextBytes(bytes);
    }


    /**
     * Generiert eine normalverteilte (Gauß-verteilte) Zufallszahl.
     * <p>
     * Die Werte folgen einer Standard-Normalverteilung mit Mittelwert 0.0 und Standardabweichung 1.0.
     * </p>
     *
     * @return Eine normalverteilte {@code double}-Zahl.
     */
    public double nextGaussian() {
        return random.nextGaussian();
    }

    /**
     * Generiert eine normalverteilte (Gauß-verteilte) Zufallszahl mit benutzerdefiniertem Mittelwert
     * und Standardabweichung.
     *
     * @param mean   Der Mittelwert der Gauß-Glockenkurve.
     * @param stdDev Die Standardabweichung (Breite der Streuung; muss &ge; 0.0 sein).
     * @return Eine entsprechend skalierte und verschobene normalverteilte {@code double}-Zahl.
     * @throws RuntimeException (Lunar-Error) Wenn Parameter unendlich oder {@code stdDev < 0.0} ist.
     */
    public double nextGaussian(double mean, double stdDev) {
        if (!Double.isFinite(mean) || !Double.isFinite(stdDev) || stdDev < 0.0d) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Gaussian distribution parameters are invalid")
                    .withActual("mean=" + mean + ", stdDev=" + stdDev)
                    .withExpected("A finite mean and a finite standard deviation greater than or equal to 0")
                    .withExplanation("NaN and infinite parameters cannot describe the supported Gaussian distribution"), null);
        }

        return random.nextGaussian() * stdDev + mean;
    }

    /**
     * Wählt typsicher ein zufälliges Element aus einem gegebenen Array aus.
     *
     * @param array Das Quell-Array.
     * @param <T>   Der generische Datentyp der Array-Elemente.
     * @return Ein zufällig ausgewähltes Element aus dem Array.
     * @throws RuntimeException (Lunar-Error) Wenn das Array {@code null} oder leer ist.
     */
    public <T> T choose(T[] array) {
        if (array == null) {
            throw Errors.nullValue(ErrorDetails.of("LunarRandom", "Selection array must not be null")
                    .withActual(null)
                    .withExpected("An array containing at least one element")
                    .withExplanation("Random selection requires an existing source array"), null);
        }
        if (array.length == 0) {
            throw Errors.argument(ErrorDetails.of("LunarRandom", "Selection array must not be empty")
                    .withActual("length=0")
                    .withExpected("An array containing at least one element")
                    .withExplanation("No random element can be selected from an empty array"), null);
        }
        return array[random.nextInt(array.length)];
    }

    /**
     * Generiert einen zufälligen Rotationswinkel in Grad.
     *
     * @return Ein zufälliger Winkel im Intervall {@code [0.0f, 360.0f)}.
     */
    public float nextAngleDegrees() {
        return random.nextFloat() * 360f;
    }

    /**
     * Generiert einen zufälligen Rotationswinkel in Radiant (Bogenmass).
     *
     * @return Ein zufälliger Winkel im Intervall {@code [0.0f, 2π)}.
     */
    public float nextAngleRadians() {
        return random.nextFloat() * (float) (Math.PI * 2);
    }

    /**
     * Generiert ein zufälliges mathematisches Vorzeichen (-1 oder 1).
     *
     * @return Entweder {@code 1} oder {@code -1} mit jeweils 50 % Wahrscheinlichkeit.
     */
    public int nextSign() {
        return random.nextBoolean() ? 1 : -1;
    }

    /**
     * Mischt ein Array in-place nach dem Fisher-Yates-Algorithmus (O(n) Laufzeit).
     *
     * @param array Das zu mischende Array.
     * @param <T>   Der generische Datentyp der Array-Elemente.
     * @throws RuntimeException (Lunar-Error) Wenn das Array {@code null} ist.
     */
    public <T> void shuffle(T[] array) {
        if (array == null) {
            throw Errors.nullValue(ErrorDetails.of("LunarRandom", "Shuffle array must not be null")
                    .withActual(null)
                    .withExpected("An existing array, which may be empty")
                    .withExplanation("Shuffling rearranges elements inside an existing array"), null);
        }
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Gibt die gekapselte {@link Random}-Instanz der Java-Standardbibliothek zurück.
     * <p>
     * Nur für fortgeschrittene Anwendungsfälle verwenden, die direkten Zugriff auf das AWT/Java-Util-System erfordern.
     * </p>
     *
     * @return Die zugrunde liegende {@link Random}-Instanz.
     */
    public Random getRandom() {
        return random;
    }


}
