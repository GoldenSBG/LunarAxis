package LunarAxis.graphics;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Der {@code Animator} ist eine Art Manager oder Zustandsautomat (State Machine) für Animationen.
 * <p>
 * Anstatt im Spielcode mühsam selbst zu zählen, wie lange ein Charakter schon läuft,
 * übergibt man diesem Animator verschiedene {@link Animation}-Objekte (z.B. unter den Namen
 * "idle", "walk", "jump"). Anschliessend kann man dem Animator einfach Befehle wie
 * {@code playAnimation("walk")} geben. Er kümmert sich automatisch um die Zeitmessung
 * und liefert immer das exakt richtige Bild zurück.
 * </p>
 *
 * <pre>
 * # Beispiel Tigerjython - Einen Spieler animieren
 * animator = Animator()
 *
 * # Animationen registrieren (vorausgesetzt walkAnim und idleAnim wurden vorher erstellt)
 * animator.addAnimation("idle", idleAnim)
 * animator.addAnimation("walk", walkAnim)
 *
 * # In der update(deltaTime)-Methode:
 * if input.isKeyDown(Key.D):
 *     player.moveRight()
 *     animator.playAnimation("walk") # Wechselt nahtlos zur Lauf-Animation
 * else:
 *     animator.playAnimation("idle") # Wechselt zurück zur Steh-Animation
 *
 * animator.update(deltaTime)
 *
 * # In der render(graphics)-Methode:
 * graphics.drawImage(animator.getCurrentFrame(), player.x, player.y)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Animator {

    private final Map<String, Animation> animations = new HashMap<>();
    private String currentAnimationState;
    private float stateTime;

    /**
     * Fügt dem Animator eine neue Animation unter einem bestimmten Namen hinzu.
     * Wenn dies die allererste Animation ist, die hinzugefügt wird, setzt der Animator
     * sie automatisch als Start-Zustand.
     *
     * @param name      Der Name (Zustand), unter dem die Animation gespeichert wird (z.B. "run").
     * @param animation Das dazugehörige {@link Animation}-Objekt.
     */
    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);

        if (currentAnimationState == null) {
            currentAnimationState = name;
        }
    }

    /**
     * Wechselt zur angegebenen Animation.
     * <p>
     * <b>Wichtig:</b> Wenn die angeforderte Animation bereits läuft, macht diese Methode gar nichts.
     * Das verhindert, dass eine Animation bei jedem Game-Loop-Durchlauf (60-mal pro Sekunde)
     * fälschlicherweise immer wieder von vorne gestartet wird.
     * </p>
     *
     * @param name Der Name der Animation, die abgespielt werden soll.
     */
    public void playAnimation(String name) {
        if (currentAnimationState != null && currentAnimationState.equals(name)) {
            return;
        }
        if (animations.containsKey(name)) {
            currentAnimationState = name;
            stateTime = 0f;
        }
    }

    /**
     * Stoppt die aktuelle Animation vollständig.
     * Beim nächsten Zeichnen wird kein Bild ({@code null}) zurückgegeben, solange nicht
     * wieder {@link #playAnimation(String)} aufgerufen wird.
     */
    public void stopAnimation() {
        currentAnimationState = null;
        stateTime = 0f;
    }

    /**
     * Aktualisiert die interne Zeitzählung der momentan aktiven Animation.
     * Muss zwingend in jedem Frame (in der {@code update}-Methode) aufgerufen werden,
     * damit die Animation voranschreitet.
     *
     * @param deltaTime Die vergangene Zeit seit dem letzten Frame in Sekunden.
     */
    public void update(float deltaTime) {
        stateTime += deltaTime;
    }

    /**
     * Holt sich das exakte Einzelbild (Frame) der aktuell laufenden Animation,
     * basierend auf der Zeit, die seit ihrem Start vergangen ist.
     *
     * @return Das aktuell sichtbare {@link Image} oder {@code null}, wenn keine Animation läuft.
     */
    public Image getCurrentFrame() {
        if (currentAnimationState == null)
            return null;
        Animation anim = animations.get(currentAnimationState);
        if (anim != null) {
            return anim.getFrame(stateTime);
        }
        return null;
    }

    /**
     * Gibt den Namen der momentan laufenden Animation zurück.
     * Nützlich, wenn man im Code überprüfen will, was die Figur gerade tut.
     *
     * @return Der Name des aktuellen Zustands (z.B. "jump") oder {@code null}.
     */
    public String getCurrentState() {
        return currentAnimationState;
    }
}
