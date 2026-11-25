package LunarAxis.audio;

import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.LogLevel;
import LunarAxis.diagnostics.Logger;
import javax.sound.sampled.Clip;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Der {@code AudioThread} ist ein dedizierter Hintergrundprozess (Thread) für alle kontinuierlichen Audio-Operationen.
 * <p>
 * Er ist hauptsächlich dafür verantwortlich, dass sogenannte Fade-Effekte (weiches Ein- und Ausblenden von Musik)
 * flüssig berechnet werden. Da dies in einem separaten Thread geschieht, friert das Spiel während eines
 * Fades nicht ein und die Lautstärke wird sanft im Hintergrund angepasst.
 * </p>
 * <p>
 * Schülerinnen und Schüler greifen in der Regel nicht direkt auf diese Klasse zu. Sie wird automatisch
 * aktiv, wenn im {@link AudioManager} entsprechende Methoden aufgerufen werden.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Indirekte Nutzung des AudioThreads
 * audio = AudioManager.getInstance()
 *
 * # Der AudioThread berechnet nun im Hintergrund 3 Sekunden lang die Lautstärke
 * audio.fadeInMusic("level_theme", 3.0, True)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class AudioThread extends Thread {
    private volatile boolean running = true;
    private final List<FadeTask> fadeTasks = new CopyOnWriteArrayList<>();

    /**
     * Erstellt den neuen Hintergrund-Thread für Audioberechnungen.
     * Er wird als "Daemon"-Thread konfiguriert, was bedeutet, dass er sich automatisch im Hintergrund
     * beendet, sobald das Hauptprogramm (das Spiel) geschlossen wird.
     */
    public AudioThread() {
        super("LunarAxis-AudioThread");
        setDaemon(true); // Beendet sich automatisch, wenn die Hauptanwendung schliesst
    }

    /**
     * Die Hauptschleife des Threads.
     * Sie läuft kontinuierlich im Hintergrund, solange {@code running} auf {@code true} steht.
     * Die Schleife pausiert jeweils für 16 Millisekunden, was ungefähr 60 Aktualisierungen
     * pro Sekunde (60 FPS) entspricht – ideal für flüssige Audio-Übergänge.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(16); // ~60 Updates pro Sekunde für weiche Fades
                processFades();
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
            }
        }
    }

    /**
     * Fügt einen neuen Fade-Auftrag (Ein- oder Ausblenden) zur Liste der aktiven Berechnungen hinzu.
     * Der Thread wird diesen Auftrag ab sofort in seiner Hauptschleife kontinuierlich aktualisieren.
     *
     * @param task Der neue {@link FadeTask}, der die Details zum Fade (Dauer, Ziel-Lautstärke) enthält.
     */
    public void addFadeTask(FadeTask task) {
        if (task != null) {
            fadeTasks.add(task);
        }
    }

    /**
     * Bricht alle aktiven Fade-Aufträge ab, die zu einem bestimmten Audio-Clip gehören.
     * Dies wird benötigt, wenn ein Musikstück plötzlich gestoppt wird, während es eigentlich
     * noch am Ein- oder Ausblenden war.
     *
     * @param clip Das Audio-Element ({@link Clip}), dessen Fades gestoppt werden sollen.
     */
    public void cancelFade(Clip clip) {
        if (clip == null) {
            return;
        }
        fadeTasks.removeIf(task -> task.getClip() == clip);
    }

    /**
     * Arbeitet alle aktuell aktiven Fade-Aufträge ({@link FadeTask}) ab.
     * Diese Methode wird ca. 60 Mal pro Sekunde aufgerufen. Sie aktualisiert die Lautstärke
     * jedes Auftrags und entfernt ihn aus der Liste, sobald der Zielwert (Target dB) erreicht wurde.
     */
    private void processFades() {
        for (FadeTask task : fadeTasks) {
            try {
                task.update();

                if (task.isFinished()) {
                    task.triggerCompletion();// callback ausführen
                    fadeTasks.remove(task);
                }
            } catch (RuntimeException exception) {
                running = false;
                fadeTasks.clear();

                Logger.report(LogLevel.ERROR, ErrorDetails.of("AudioThread", "Audio fade processing was terminated due to an error")
                                .withExpected(
                                        "A fade update and a completion callback with no unhandled RuntimeException")
                                .withExplanation("A fade or its callback has failed. The audio thread is terminating processing; no further fade tasks will be executed. Clips that are already playing are not stopped by this"), exception);
                return;
            }
        }
    }

    /**
     * Stoppt diesen Hintergrund-Thread sicher und bricht alle aktuell laufenden Fade-Berechnungen ab.
     * Wird normalerweise beim Schliessen des {@link AudioManager} oder der Engine aufgerufen, um Ressourcen freizugeben.
     */
    public void stopThread() {
        running = false;
        this.interrupt();
        fadeTasks.clear();
    }
}