package LunarAxis.events;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Der {@code EventBus} ist die zentrale "Poststelle" der LunarAxis-Engine.
 * <p>
 * Komponenten können Events an den Bus schicken ({@code publish}), und der Bus liefert
 * diese Events automatisch an alle aus, die sich dafür angemeldet haben ({@code subscribe}).
 * </p>
 * <p>
 * <b>Warum ist das nützlich?</b> Ein Gegner (Enemy) muss nicht direkt wissen, dass das User Interface (UI)
 * existiert, um den Punktestand zu aktualisieren. Der Gegner ruft einfach
 * {@code publish(EnemyDiedEvent)} auf, und das UI lauscht auf dieses Event.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Ein Event verschicken
 * bus = EventBus.getInstance()
 * bus.publish(CoinCollectedEvent(10))
 * </pre>
 *
 * <i>Interne Design-Entscheidungen:</i>
 * <ul>
 * <li>Bill-Pugh-Singleton (thread-safe, lazy, kein synchronized-Overhead)</li>
 * <li>KEINE Event-Vererbung (exakter Type-Match)</li>
 * <li>Snapshot-Iteration für Thread-Safety während publish()</li>
 * <li>Duplicate Prevention bei subscribe()</li>
 * </ul>
 *
 * @author Patrick André
 * @version 1.2.6
 *
 * @see Event
 * @see EventListener
 */
public class EventBus {

    // 1. Bill-Pugh-Singleton: Robuster als Double-Checked-Locking, kein volatile nötig
    private static class Holder {
        private static final EventBus INSTANCE = new EventBus();
    }

    /**
     * Gibt die einzige, globale Instanz des Event-Verteilers zurück.
     *
     * @return Der {@link EventBus}.
     */
    public static EventBus getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<Class<? extends Event>, List<EventListener<?>>> listeners;

    private EventBus() {
        listeners = new HashMap<>();
    }

    /**
     * Meldet einen Zuhörer (Listener) an, der sofort benachrichtigt werden soll,
     * wenn ein bestimmtes Event verschickt wird.
     *
     * @param eventType Die Klasse des Events, auf das gehorcht werden soll (z.B. {@code CoinCollectedEvent.class}).
     * @param listener  Die Funktion (der Callback), die ausgeführt werden soll.
     * @param <T>       Der Typ des Events.
     */
    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        if (eventType == null) {
            Logger.report(LogLevel.WARN,
                    ErrorDetails.of("EventBus", "Listener registration requires an event type")
                            .withActual("eventType=null")
                            .withExpected("A non-null event class")
                            .withExplanation("The registration was discarded because the event type determines which events are delivered to the listener"),
                    null);
            return;
        }
        if (listener == null) {
            Logger.report(LogLevel.WARN,
                    ErrorDetails.of("EventBus", "Listener registration requires a listener")
                            .withActual("listener=null, eventType=" + eventType.getName())
                            .withExpected("A non-null EventListener instance")
                            .withExplanation("The registration was discarded because no callback is available to receive events"),
                    null);
            return;
        }

        synchronized (listeners) {
            @SuppressWarnings("unchecked")
            List<EventListener<T>> eventListeners = (List<EventListener<T>>) (List<?>) listeners.computeIfAbsent(eventType, k -> new ArrayList<>());

            // Duplicate Prevention
            if (!eventListeners.contains(listener)) {
                eventListeners.add(listener);
                Logger.debug("EventBus", "Subscribed listener for " + eventType.getSimpleName());
            }
        }
    }

    /**
     * Meldet einen zuvor registrierten Zuhörer wieder ab.
     *
     * @param eventType Die Klasse des Events.
     * @param listener  Der Listener, der entfernt werden soll.
     * @param <T>       Der Typ des Events.
     * @return {@code true}, wenn der Listener gefunden und entfernt wurde, sonst {@code false}.
     */
    public <T extends Event> boolean unsubscribe(Class<T> eventType, EventListener<T> listener) {
        if (eventType == null || listener == null) {
            return false;
        }

        synchronized (listeners) {
            @SuppressWarnings("unchecked")
            List<EventListener<T>> eventListeners = (List<EventListener<T>>) (List<?>) listeners.get(eventType);
            if (eventListeners != null) {
                boolean removed = eventListeners.remove(listener);
                if (removed) {
                    Logger.debug("EventBus", "Unsubscribed listener for " + eventType.getSimpleName());
                    if (eventListeners.isEmpty()) {
                        listeners.remove(eventType);
                    }
                }
                return removed;
            }
        }
        return false;
    }

    /**
     * Verschickt (veröffentlicht) ein Event an das System.
     * Der EventBus sucht automatisch alle Listener, die sich für genau dieses Event
     * angemeldet haben, und ruft sie unmittelbar nacheinander auf.
     *
     * @param event Das befüllte Daten-Paket (Event).
     * @param <T>   Der Typ des Events.
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        if (event == null) {
            Logger.report(LogLevel.WARN,
                    ErrorDetails.of("EventBus", "Cannot publish a missing event")
                            .withActual(null)
                            .withExpected("A non-null Event instance")
                            .withExplanation("The publish request was discarded because there is no event to deliver"),
                    null);
            return;
        }

        // Snapshot erstellen für Thread-Safety während der Iteration
        List<EventListener<?>> eventListeners;
        synchronized (listeners) {
            List<EventListener<?>> list = listeners.get(event.getClass());
            if (list == null || list.isEmpty()) {
                return; // Kein Log-Spam für Events ohne Listener
            }
            eventListeners = new ArrayList<>(list);
        }

        // Alle Listener aufrufen
        for (EventListener<?> listener : eventListeners) {
            try {
                ((EventListener<T>) listener).onEvent(event);
            } catch (Exception exception) {
                String eventType = event.getClass().getName();
                String listenerType = listener.getClass().getName();
                ErrorDetails existing = Errors.detailsOf(exception);

                ErrorDetails details = ErrorDetails.of("EventBus", "Event listener failed: " + listenerType + " for " + eventType);

                if (existing == null || !existing.hasActual()) {
                    details = details.withActual("eventType=" + eventType + ", listenerType=" + listenerType);
                }

                if (existing == null || existing.getExpected() == null || existing.getExpected().isBlank()) {
                    details = details.withExpected("A listener callback that completes without an unhandled exception");
                }

                String explanation = "This listener call was aborted; processing continues with the remaining listeners in the current snapshot";
                if (existing != null && existing.getExplanation() != null && !existing.getExplanation().isBlank()) {
                    explanation += "\nListener error: " + existing.getExplanation();
                }
                details = details.withExplanation(explanation);

                Logger.report(LogLevel.ERROR, details, exception);
            }
        }
    }

    /**
     * Entfernt restlos alle registrierten Listener.
     * <p>
     * <b>WICHTIG:</b> Darf NUR für einen globalen Engine-Shutdown oder Test-Reset verwendet werden!
     * NICHT in einer Methode wie {@code Scene.dispose()} aufrufen, da dies versehentlich auch
     * die Listener aller anderen, noch aktiven Programmteile zerstören würde.
     * </p>
     */
    public void clear() {
        synchronized (listeners) {
            int count = listeners.values().stream().mapToInt(List::size).sum();
            listeners.clear();
            Logger.info("EventBus", "Cleared all " + count + " listener(s)");
        }
    }

    /**
     * Gibt zurück, wie viele Zuhörer momentan für ein spezifisches Event angemeldet sind.
     *
     * @param eventType Die Klasse des Events.
     * @return Die Anzahl der Listener für dieses Event.
     */
    public int getListenerCount(Class<? extends Event> eventType) {
        if (eventType == null) return 0;
        synchronized (listeners) {
            List<?> list = listeners.get(eventType);
            return list != null ? list.size() : 0;
        }
    }

    /**
     * Gibt die Gesamtzahl aller Listener für alle Events im gesamten System zurück.
     *
     * @return Die Gesamtanzahl registrierter Listener.
     */
    public int getTotalListenerCount() {
        synchronized (listeners) {
            return listeners.values().stream().mapToInt(List::size).sum();
        }
    }
}
