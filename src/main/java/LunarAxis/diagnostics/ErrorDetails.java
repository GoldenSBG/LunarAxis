package LunarAxis.diagnostics;

/**
 * Die Klasse {@code ErrorDetails} repräsentiert unveränderliche, benutzerverständliche
 * Angaben zu einem aufgetretenen Fehler innerhalb der LunarAxis-Engine.
 * <p>
 * Sie ist bewusst frei von direkten {@link Throwable}-Referenzen gehalten und speichert
 * strukturierte Kontextinformationen wie betroffene Komponente, tatsächlichen Wert, erwarteten
 * Wert sowie eine erklärende Beschreibung. Sensible Daten wie Passwörter dürfen
 * hierbei nicht übergeben werden.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public final class ErrorDetails {

    private final String component;
    private final String title;
    private final boolean actualPresent;
    private final String actual;
    private final String expected;
    private final String explanation;
    private final StackTraceElement callSite;

    /**
     * Erstellt eine neue {@code ErrorDetails}-Instanz mit allen Fehlerattributen und dem Aufruf-Ort.
     *
     * @code component     Die betroffene Engine-Komponente.
     * @code title         Der aussagekräftige Fehlertitel.
     * @code actualPresent Gibt an, ob ein tatsächlicher Wert erfasst wurde.
     * @code actual        Der tatsächliche Wert als Text.
     * @code expected      Der erwartete Wert oder Zustand.
     * @code explanation   Eine detaillierte Erklärung des Fehlers.
     * @code callSite      Der {@link StackTraceElement}-Ort des Aufrufs.
     */
    private ErrorDetails(String component, String title, boolean actualPresent, String actual, String expected, String explanation, StackTraceElement callSite) {
        this.component = component;
        this.title = title;
        this.actualPresent = actualPresent;
        this.actual = actual;
        this.expected = expected;
        this.explanation = explanation;
        this.callSite = callSite;
    }

    /**
     * Erstellt eine neue {@code ErrorDetails}-Basisinstanz für eine Komponente und einen Fehlertitel.
     * Automatische Ermittlung des Aufruf-Orts über den Stacktrace.
     *
     * @code component Die betroffene Engine-Komponente.
     * @code title     Der Fehlertitel.
     * @return Eine neue {@code ErrorDetails}-Instanz.
     */
    public static ErrorDetails of(String component, String title) {
        return new ErrorDetails(component, title, false, null, null, null, findLocation(new Throwable()));
    }

    /**
     * Speichert eine Textdarstellung des tatsächlich übergebenen Werts.
     * Ein explizit übergebenes {@code null} wird dabei als String `"null"` repräsentiert.
     *
     * @code value Der tatsächliche Wert.
     * @return Eine neue {@code ErrorDetails}-Instanz mit gesetztem Ist-Wert.
     */
    public ErrorDetails withActual(Object value) {
        return new ErrorDetails(component, title, true, String.valueOf(value), expected, explanation, callSite);
    }

    /**
     * Ergänzt die Fehlerdetails um die Angabe des erwarteten Wertes oder Verhaltens.
     *
     * @code value Der Erwartungswert als Text.
     * @return Eine neue {@code ErrorDetails}-Instanz mit gesetztem Soll-Wert.
     */
    public ErrorDetails withExpected(String value) {
        return new ErrorDetails(component, title, actualPresent, actual, value, explanation, callSite);
    }

    /**
     * Ergänzt die Fehlerdetails um eine erklärende Beschreibung der Ursache.
     *
     * @code value Die Erklärung als Text.
     * @return Eine neue {@code ErrorDetails}-Instanz mit gesetzter Erklärung.
     */
    public ErrorDetails withExplanation(String value) {
        return new ErrorDetails(component, title, actualPresent, actual, expected, value, callSite);
    }

    /**
     * Gibt den Namen der betroffenen Engine-Komponente zurück.
     *
     * @return Der Komponentenname.
     */
    public String getComponent() {
        return component;
    }

    /**
     * Gibt den Titel oder die Kurzbeschreibung des Fehlers zurück.
     *
     * @return Der Fehlertitel.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Prüft, ob ein tatsächlicher Wert (Actual) erfasst wurde.
     *
     * @return {@code true}, wenn ein Ist-Wert vorhanden ist, sonst {@code false}.
     */
    public boolean hasActual() {
        return actualPresent;
    }

    /**
     * Gibt den tatsächlich übergebenen Wert als Text zurück.
     *
     * @return Der Ist-Wert-String oder {@code null}.
     */
    public String getActual() {
        return actual;
    }

    /**
     * Gibt den erwarteten Zustand oder Wert zurück.
     *
     * @return Der Soll-Wert-String oder {@code null}.
     */
    public String getExpected() {
        return expected;
    }

    /**
     * Gibt die ausführliche Erklärung zum Fehler zurück.
     *
     * @return Der Erklärungstext oder {@code null}.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Gibt den {@link StackTraceElement}-Aufrufpunkt im Quellcode zurück.
     *
     * @return Das {@link StackTraceElement} des FehlerspUrsprungs oder {@code null}.
     */
    public StackTraceElement getCallSite() {
        return callSite;
    }

    /**
     * Durchsucht den Stacktrace nach dem ersten Frame ausserhalb des internen Logger- und Reflektions-Infrastrukturpakets.
     * Der übergebene Stacktrace wird dabei unverändert beibehalten.
     *
     * @code throwable Die {@link Throwable}-Instanz zur Stacktrace-Analyse.
     * @return Das gefundene {@link StackTraceElement} oder {@code null}.
     */
    static StackTraceElement findLocation(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        for (StackTraceElement frame : throwable.getStackTrace()) {
            String className = frame.getClassName();

            if (!className.startsWith("LunarAxis.diagnostics.")
                    && !className.startsWith("java.lang.reflect.")
                    && !className.startsWith("jdk.internal.reflect.")
                    && !className.startsWith("sun.reflect.")) {
                return frame;
            }
        }

        return null;
    }
}