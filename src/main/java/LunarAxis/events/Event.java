package LunarAxis.events;

/**
 * Ein leeres Interface (sogenanntes "Marker-Interface"), das ein Objekt als Event
 * für das LunarAxis-Event-System markiert.
 *
 * <p>
 * <b>Was ist ein Event?</b> Ein Event ist wie eine Briefumschlag, in den du Informationen
 * packen kannst (z.B. "Spieler hat Münze eingesammelt" oder "Level abgeschlossen").
 * Du erstellst eine eigene Klasse, die dieses Interface implementiert, und kannst sie
 * dann über den {@link EventBus} im ganzen Spiel verteilen, ohne dass die einzelnen
 * Programmteile direkt miteinander sprechen müssen.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Ein eigenes Event erstellen
 * class CoinCollectedEvent(Event):
 *     def __init__(self, coinValue):
 *         self.value = coinValue
 *
 *     def getValue(self):
 *         return self.value
 * </pre>
 * 
 * @author Patrick André
 * @version 1.2.6
 *
 * @see EventBus
 * @see EventListener
 */
public interface Event {
}