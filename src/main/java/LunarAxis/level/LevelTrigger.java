package LunarAxis.level;


import LunarAxis.math.Rect;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Die Klasse {@code LevelTrigger} beschreibt die Triggerdaten eines Levels.
 * <p>
 * Ein Trigger besitzt eine frei gewählte Kennung (ID) und eine oder mehrere
 * Rechteckflächen ({@link Rect}). Mehrere Rechtecke erlauben auch unregelmässige Bereiche,
 * ohne unmarkierte Zwischenräume einzuschliessen.
 * </p>
 * <p>
 * <b>Hinweis:</b> Die Klasse selbst erkennt keine Actor-Kollisionen und löst keine Events aus;
 * sie dient als reine Datenstruktur zur Speicherung der Trigger-Bereiche.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class LevelTrigger {

    private final String id;
    private final List<Rect> bounds;

    /**
     * Erstellt einen neuen LevelTrigger mit einer ID und einer Liste von Begrenzungs-Rechtecken.
     *
     * @param id     Die Kennung (ID) des Triggers.
     * @param bounds Eine Liste von {@link Rect}-Objekten, die den Bereich des Triggers definieren.
     * @throws RuntimeException (Lunar-Error) wenn die ID leer/blank ist, die Liste {@code null} oder leer ist, oder eines der Rechtecke {@code null} ist.
     */
    public LevelTrigger(String id, List<Rect> bounds) {
        Validation.notBlank(id, "Trigger ID cannot be blank");
        Validation.notNull(bounds, "Bounds cannot be null");

        if (bounds.isEmpty()) {
            throw Errors.argument(ErrorDetails.of("LevelTrigger", "A trigger must have at least one bounds rectangle")
                    .withActual("id=" + id + ", rectangleCount=0")
                    .withExpected("A list containing at least one non-null rectangle")
                    .withExplanation("A trigger needs at least one area describing its spatial extent"), null);
        }

        this.id = id.trim();
        this.bounds = new ArrayList<>();

        for (Rect rect : bounds) {
            Validation.notNull(rect, "Trigger bound cannot be null");
            this.bounds.add(copy(rect));
        }
    }

    /**
     * Gibt die Kennung (ID) des Triggers zurück.
     *
     * @return Die Trigger-ID als String.
     */
    public String getId() {
        return id;
    }


    /**
     * Gibt eine unveränderbare Liste von Kopien der Trigger-Rechtecke zurück.
     * <p>
     * Da {@link Rect} veränderbar ist, verhindern diese Kopien, dass der gespeicherte
     * Levelzustand von aussen über die zurückgegebenen Objekte manipuliert werden kann.
     * </p>
     *
     * @return Eine unmodifizierbare {@link List} von {@link Rect}-Kopien.
     */
    public List<Rect> getBounds() {
        List<Rect> result = new ArrayList<>();

        for (Rect rect : bounds) {
            result.add(copy(rect));
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Erstellt eine exakte Kopie eines {@link Rect}-Objekts (Defensive Kopie).
     *
     * @param rect Das zu kopierende Rechteck.
     * @return Ein neues {@link Rect} mit denselben Koordinaten und Dimensionen.
     */
    private Rect copy(Rect rect) {
        return new Rect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
}
