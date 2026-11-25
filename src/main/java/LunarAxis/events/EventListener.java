package LunarAxis.events;

/**
 * Ein Interface für eine Empfänger-Funktion im LunarAxis-Event-System.
 *
 * <p>Wenn du möchtest, dass dein Code auf ein bestimmtes Event reagiert,
 * übergibst du eine Funktion (in Java oft als Lambda, in TigerJython als def-Methode),
 * die dieses Interface erfüllt, an den {@link EventBus}.</p>
 *
 * <h3>Beispiel (Java):</h3>
 * <pre>
 * EventBus.getInstance().subscribe(PlayerDiedEvent.class, event -> {
 *     System.out.println("Player " + event.getPlayerName() + " died!");
 * });
 * </pre>
 *
 * <h3>Beispiel (TigerJython):</h3>
 * <pre>
 * def on_player_died(event):
 *     print("Player " + event.getPlayerName() + " died!")
 *
 * # Verbindet die Funktion mit dem Event
 * EventBus.getInstance().subscribe(PlayerDiedEvent, on_player_died)
 * </pre>
 *
 * <h4>Wichtige Hinweise für die Spielentwicklung:</h4>
 * <ul>
 *   <li>Listener sollten sehr schnell ausführen. Vermeide es, hier lange Berechnungen zu machen, da dies das Spiel einfrieren lassen könnte.</li>
 *   <li>Falls in deinem Listener ein Programmierfehler passiert (Exception), wird das restliche Spiel nicht abstürzen. Der EventBus fängt den Fehler auf und schreibt ihn ins Log.</li>
 *   <li>Listener müssen explizit via {@code unsubscribe()} entfernt werden, wenn du sie nicht mehr brauchst, sonst verbrauchen sie unnötig Speicher.</li>
 * </ul>
 *
 * @param <T> Der Event-Typ, den dieser Listener empfangen kann (z.B. ein {@code PlayerDiedEvent}).
 *
 * @author Patrick André
 * @version 1.2.6
 *
 * @see EventBus#subscribe(Class, EventListener)
 * @see EventBus#unsubscribe(Class, EventListener)
 */
@FunctionalInterface
public interface EventListener<T extends Event> {

    /**
     * Wird automatisch aufgerufen, sobald das erwartete Event vom Typ {@code T} verschickt wird.
     *
     * @param event Das fertige Datenpaket des Events (wird garantiert nicht {@code null} sein).
     *
     * @see EventBus#publish(Event)
     */
    void onEvent(T event);
}
