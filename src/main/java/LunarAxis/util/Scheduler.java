package LunarAxis.util;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse {@code Scheduler} ermöglicht das zeitgesteuerte Ausführen von Aktionen (Callbacks).
 * <p>
 * Sie unterstützt das zeitverzögerte Ausführen einmaliger Tasks sowie das periodische Wiederholen
 * von Aufgaben in definierten Zeitintervallen. Die Zeitzählung wird dabei in jedem Frame
 * über die {@link #update(float)}-Methode vom Haupt-Game-Loop aktualisiert.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython / Java:
 * Scheduler.doAfter(2.5f, lambda: print("2.5 Sekunden sind vergangen!"))
 * Scheduler.doEvery(1.0f, lambda: print("Sekündlicher Tick"))
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Scheduler {

    /**
     * Die Liste aller aktuell aktiven zeitgesteuerten Tasks.
     */
    private static final List<Task> tasks = new ArrayList<>();

    /**
     * Puffer-Liste für neu hinzugefügte Tasks, um {@link java.util.ConcurrentModificationException}s
     * während des laufenden Update-Loops zu verhindern.
     */
    private static final List<Task> tasksToAdd = new ArrayList<>();

    /**
     * Die innere Klasse {@code Task} kapselt die Daten einer einzelnen zeitgesteuerten Aufgabe.
     * <p>
     * Sie verwaltet die verbleibende Restzeit, das Intervall, den Wiederholungs-Status
     * sowie die auszuführende {@link Runnable}-Aktion.
     * </p>
     */
    private static class Task {
        float timeRemaining;
        float interval;
        boolean repeat;
        Runnable action;

        /**
         * Initialisiert einen neuen zeitgesteuerten Task.
         *
         * @param time   Die Verweildauer bzw. das Intervall in Sekunden.
         * @param repeat {@code true}, wenn die Aktion periodisch wiederholt werden soll.
         * @param action Die auszuführende {@link Runnable}-Aktion.
         */
        Task(float time, boolean repeat, Runnable action) {
            this.timeRemaining = time;
            this.interval = time;
            this.repeat = repeat;
            this.action = action;
        }
    }

    /**
     * Aktualisiert den Fortschritt aller registrierten Tasks.
     * <p>
     * Diese Methode muss in jedem Frame vom zentralen Game-Loop aufgerufen werden.
     * Zunächst werden neu hinzugefügte Tasks aus dem Puffer übernommen, anschliessend wird
     * die verbleibende Zeit für alle aktiven Tasks um {@code deltaTime} verringert.
     * Abgelaufene Aktionen werden ausgeführt und ggf. zurückgesetzt oder entfernt.
     * </p>

     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @throws RuntimeException (Lunar-Error) Wenn {@code deltaTime} unendlich oder {@code Float.NaN} ist,
     *                          oder wenn ein geplanter Callback eine Exception auslöst.
     */
    public static void update(float deltaTime) {
        if (!Float.isFinite(deltaTime)) {
            throw Errors.argument(ErrorDetails.of("Scheduler", "Scheduler update time must be finite")
                    .withActual(deltaTime)
                    .withExpected("A finite time interval")
                    .withExplanation("NaN and infinite time intervals cannot be used to advance scheduled tasks"), null);
        }

        synchronized (tasksToAdd) {
            tasks.addAll(tasksToAdd);
            tasksToAdd.clear();
        }

        List<Task> snapshot = new ArrayList<>(tasks);
        for (Task task : snapshot) {
            if (!tasks.contains(task)) {
                continue;
            }

            task.timeRemaining -= deltaTime;

            if (task.timeRemaining <= 0) {
                if (task.repeat) {
                    task.timeRemaining = task.interval;
                } else {
                    tasks.remove(task);
                }

                if (task.action != null) {
                    try {
                        task.action.run();
                    } catch (RuntimeException failure) {
                        if (Errors.detailsOf(failure) == null) {
                            Errors.describe(failure, ErrorDetails.of("Scheduler", "Scheduled action failed")
                                    .withActual("repeat=" + task.repeat + ", interval=" + task.interval)
                                    .withExpected("The scheduled callback to complete successfully")
                                    .withExplanation("The current execution has been consumed and the original callback exception is propagated"));
                        }
                        throw failure;
                    }
                }
            }
        }
    }

    /**
     * Plant eine einmalige Aktion nach Ablauf einer definierten Verzögerung.
     *
     * @param delayInSeconds Die Verzögerungszeit in Sekunden.
     * @param action         Die auszuführende {@link Runnable}-Aktion.
     * @throws RuntimeException (Lunar-Error) Wenn {@code delayInSeconds} nicht endlich ist.
     */
    public static void doAfter(float delayInSeconds, Runnable action) {
        if (!Float.isFinite(delayInSeconds)) {
            throw Errors.argument(ErrorDetails.of("Scheduler", "Task delay must be finite")
                    .withActual(delayInSeconds)
                    .withExpected("A finite delay in seconds")
                    .withExplanation("A non-finite delay cannot be advanced reliably by scheduler updates"), null);
        }

        synchronized (tasksToAdd) {
            tasksToAdd.add(new Task(delayInSeconds, false, action));
        }
    }

    /**
     * Plant eine sich kontinuierlich wiederholende Aktion in einem festen Zeitintervall.
     *
     * @param intervalInSeconds Das Wiederholungs-Intervall in Sekunden.
     * @param action            Die auszuführende {@link Runnable}-Aktion.
     * @throws RuntimeException (Lunar-Error) Wenn {@code intervalInSeconds} nicht endlich ist.
     */
    public static void doEvery(float intervalInSeconds, Runnable action) {
        if (!Float.isFinite(intervalInSeconds)) {
            throw Errors.argument(ErrorDetails.of("Scheduler", "Task interval must be finite")
                    .withActual(intervalInSeconds)
                    .withExpected("A finite interval in seconds")
                    .withExplanation("A non-finite interval cannot be advanced reliably by scheduler updates"), null);
        }

        synchronized (tasksToAdd) {
            tasksToAdd.add(new Task(intervalInSeconds, true, action));
        }
    }

    /**
     * Löscht alle aktuell eingeplanten und wartenden Tasks vollständig aus dem Scheduler.
     */
    public static void clear() {
        tasks.clear();
        tasksToAdd.clear();
    }
}
