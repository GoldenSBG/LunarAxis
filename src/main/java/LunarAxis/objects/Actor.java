package LunarAxis.objects;

import LunarAxis.graphics.Animator;
import LunarAxis.graphics.Colors;
import LunarAxis.graphics.Graphics;
import LunarAxis.math.Rect;
import LunarAxis.math.Vec2;
import LunarAxis.physics.RigidBody2D;
import LunarAxis.util.ResourceManager;
import LunarAxis.diagnostics.ErrorDetails;
import LunarAxis.diagnostics.Errors;

import java.awt.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Die Klasse {@code Actor} repräsentiert ein Basis-Objekt (Entität) in der Spielwelt,
 * wie zum Beispiel den Spieler, Gegner oder sammelbare Items.
 * <p>
 * Sie verwaltet Position, Grösse, Geschwindigkeit, Bilder, Animationen, Tags
 * sowie anpassbare Hitboxen und die Anbindung an die Physik ({@link RigidBody2D}).
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class Actor {

    protected float x, y;
    protected float speed = 0f;

    protected int width, height;
    protected int direction = 0;
    protected int zIndex = 0;

    protected boolean visible = true;

    protected Image image;
    protected Vec2 velocity;
    protected RigidBody2D rigidBody;
    protected Animator animator;

    private final Set<String> tags = new HashSet<>();

    private boolean customHitbox;
    private int hitboxWidth;
    private int hitboxHeight;
    private int hitboxOffsetX;
    private int hitboxOffsetY;

    private boolean hitboxVisible;
    private Colors hitboxColor = Colors.GREEN;

    /**
     * Erstellt einen neuen, leeren Actor an den Standard-Koordinaten (0, 0).
     */
    public Actor() {}

    /**
     * Erstellt einen neuen Actor und lädt direkt ein Bild von dem angegebenen Pfad.
     *
     * @param imagePath Der Dateipfad zum Bild der Textur.
     */
    public Actor(String imagePath) {
        loadImage(imagePath);
    }

    /**
     * Lädt das Bild für diesen Actor über den {@link ResourceManager} und
     * passt die Dimensionen (Breite und Höhe) automatisch an das Bild an.
     *
     * @param imagePath Der Dateipfad des Bildes.
     */
    public void loadImage(String imagePath) {
        image = ResourceManager.getInstance().loadImage(imagePath);

        if (image != null) {
            width = image.getWidth(null);
            height = image.getHeight(null);
        }
    }

    /**
     * Aktualisiert den Zustand des Actors (Physik, Bewegung und Animationen) für das aktuelle Frame.
     *
     * @param delta Die vergangene Zeit seit dem letzten Frame in Sekunden.
     * @throws RuntimeException (Lunar-Error) wenn {@code delta} ungültig (NaN oder unendlich) ist.
     */
    public void update(float delta) {
        if (!Float.isFinite(delta)) {
            throw Errors.argument(ErrorDetails.of("Actor", "Actor update time must be finite")
                    .withActual(delta)
                    .withExpected("A finite time interval")
                    .withExplanation("NaN and infinite time intervals cannot be used to update actor movement, physics or animation"), null);
        }

        if (rigidBody != null) {
            rigidBody.update(delta);
            this.x = rigidBody.getPosition().getX();
            this.y = rigidBody.getPosition().getY();
        } else if (velocity != null) {
            x += velocity.getX() * delta;
            y += velocity.getY() * delta;
        }

        if (animator != null) {
            animator.update(delta);
        }
    }

    /**
     * Zeichnet das aktuelle Bild oder den aktuellen Animations-Frame des Actors auf den Bildschirm.
     *
     * @param graphics Das {@link Graphics}-Zeichenwerkzeug der Engine.
     */
    public void drawImage(Graphics graphics) {
        if (!visible) {
            return;
        }

        // Priorität: Animator-Frame -> sonst Fallback auf statisches Image
        Image frameToDraw = image;
        if (animator != null) {
            Image animFrame = animator.getCurrentFrame();
            if (animFrame != null) {
                frameToDraw = animFrame;
            }
        }

        if (frameToDraw != null) {
            graphics.drawImage(frameToDraw, (int) x, (int) y, width, height);
        }
    }

    /**
     * Zeichnet die Debug-Hitbox dieses Actors, falls die Debug-Ansicht dafür aktiviert ist.
     * Die Methode verändert weder Position noch Kollisionszustand.
     *
     * @param graphics Das {@link Graphics}-Objekt.
     */
    public void drawDebugBounds(Graphics graphics) {
        if (!hitboxVisible || graphics == null) {
            return;
        }
        graphics.setColor(hitboxColor);
        graphics.drawRect(getPhysicsBounds());
    }

    /**
     * Gibt die Actor-Bounds als AWT-{@link Rectangle} zurück.
     * Bei einer benutzerdefinierten Hitbox beschreibt das Rechteck diese Hitbox.
     *
     * @return Ein {@link Rectangle} der Kollisionsbox.
     */
    public Rectangle getBounds() {
        Rect bounds = getPhysicsBounds();
        return new Rectangle((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
    }

    /**
     * Gibt die tatsächlich verwendete rechteckige Kollisionsgeometrie als {@link Rect} zurück.
     * Berücksichtigt dabei, ob eine benutzerdefinierte Hitbox definiert wurde.
     *
     * @return Das {@link Rect} der Physik-Kollisionsbox.
     */
    public Rect getPhysicsBounds() {
        int baseX = (int) x;
        int baseY = (int) y;

        if (customHitbox) {
            return new Rect( baseX + hitboxOffsetX, baseY + hitboxOffsetY, hitboxWidth, hitboxHeight);
        }
        return new Rect(baseX, baseY, width, height);
    }


    /**
     * Prüft, ob dieser Actor mit einem anderen Actor kollidiert.
     *
     * @param actor Der zu prüfende andere {@link Actor}.
     * @return {@code true}, wenn sich die Hitboxen überschneiden, sonst {@code false}.
     */
    public boolean collidesWith(Actor actor) {
        if (actor == null) {
            return false;
        }
        return getPhysicsBounds().intersects(actor.getPhysicsBounds());
    }

    /**
     * Setzt eine benutzerdefinierte, rechteckige Hitbox relativ zur oberen linken Actor-Position.
     * Die eigentliche Bildgrösse des Actors wird dadurch nicht verändert.
     *
     * @param width   Hitbox-Breite, muss grösser als 0 sein
     * @param height  Hitbox-Höhe, muss grösser als 0 sein
     * @param offsetX Horizontale Verschiebung relativ zum Actor
     * @param offsetY Vertikale Verschiebung relativ zum Actor
     * @throws RuntimeException (Lunar-Error) wenn Breite oder Höhe kleiner/gleich 0 sind
     */
    public void setHitbox(int width, int height, int offsetX, int offsetY) {
        if (width <= 0) {
            throw Errors.lunar(ErrorDetails.of("Actor", "Hitbox width must be greater than 0")
                    .withActual(width)
                    .withExpected("An integer greater than 0")
                    .withExplanation("A custom actor hitbox requires a positive width"), null);
        }
        if (height <= 0) {
            throw Errors.lunar(ErrorDetails.of("Actor", "Hitbox height must be greater than 0")
                    .withActual(height)
                    .withExpected("An integer greater than 0")
                    .withExplanation("A custom actor hitbox requires a positive height"), null);
        }

        this.customHitbox = true;
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        this.hitboxOffsetX = offsetX;
        this.hitboxOffsetY = offsetY;
    }

    /**
     * Setzt die Hitbox wieder auf die Standard-Actor-Grösse und -Position zurück.
     */
    public void resetHitbox() {
        customHitbox = false;
        hitboxWidth = 0;
        hitboxHeight = 0;
        hitboxOffsetX = 0;
        hitboxOffsetY = 0;
    }

    /**
     * Setzt das Bild (die Textur) des Actors direkt und passt die Dimensionen an.
     *
     * @param image Das neue {@link Image}.
     */
    public void setImage(Image image) {
        this.image = image;
        if (image != null) {
            width = image.getWidth(null);
            height = image.getHeight(null);
        }
    }

    /**
     * Fügt einen Tag (Schlagwort) zu diesem Actor hinzu.
     *
     * @param tag Der hinzuzufügende Tag.
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            tags.add(tag.trim());
        }
    }

    /**
     * Entfernt einen Tag von diesem Actor.
     *
     * @param tag Der zu entfernende Tag.
     */
    public void removeTag(String tag) {
        tags.remove(tag);
    }

    /**
     * Prüft, ob dieser Actor den angegebenen Tag besitzt.
     *
     * @param tag Der zu prüfende Tag.
     * @return {@code true}, wenn der Tag vorhanden ist, sonst {@code false}.
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /**
     * Gibt eine unmodifizierbare Ansicht aller Tags dieses Actors zurück.
     *
     * @return Ein {@link Set} aller Tags.
     */
    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Prüft, ob der Actor sichtbar ist.
     * @return {@code true}, wenn er gezeichnet wird.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Gibt das aktuelle Bild des Actors zurück.
     * @return Das {@link Image}.
     */
    public Image getImage() {
        return image;
    }

    /**
     * Gibt die Bewegungsgeschwindigkeit zurück.
     * @return Die Speed.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Gibt die Blick- oder Bewegungsrichtung des Actors zurück.
     * @return Der Richtungs-Code.
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Setzt die Blick- oder Bewegungsrichtung des Actors.
     * @param direction Die neue Richtung.
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Legt fest, ob der Actor sichtbar sein soll.
     * @param visible {@code true} für sichtbar, {@code false} für unsichtbar.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Setzt die Geschwindigkeit des Actors.
     * @param speed Die Geschwindigkeit.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Gibt die Höhe des Actors in Pixeln zurück.
     * @return Die Höhe.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gibt die Breite des Actors in Pixeln zurück.
     * @return Die Breite.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gibt die aktuelle X-Koordinate in der Welt zurück.
     * @return X-Position.
     */
    public float getX() {
        return x;
    }

    /**
     * Gibt die aktuelle Y-Koordinate in der Welt zurück.
     * @return Y-Position.
     */
    public float getY() {
        return y;
    }

    /**
     * Setzt die Höhe des Actors.
     * @param height Die neue Höhe.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Setzt die Breite des Actors.
     * @param width Die neue Breite.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Setzt die X-Koordinate des Actors in der Welt.
     * Aktualisiert bei vorhandenem Rigidbody auch direkt dessen Position.
     *
     * @param x Die neue X-Position.
     * @throws RuntimeException (Lunar-Error) wenn {@code x} nicht endlich ist (NaN/Infinite).
     */
    public void setX(float x) {
        if (!Float.isFinite(x)) {
            throw Errors.argument(ErrorDetails.of("Actor", "Actor X position must be finite")
                    .withActual(x)
                    .withExpected("A finite X coordinate")
                    .withExplanation("NaN and infinite coordinates cannot represent an actor position for rendering and collision detection"), null);
        }

        this.x = x;

        if (rigidBody != null) {
            rigidBody.getPosition().setX(x);
        }
    }

    /**
     * Setzt die Y-Koordinate des Actors in der Welt.
     * Aktualisiert bei vorhandenem Rigidbody auch direkt dessen Position.
     *
     * @param y Die neue Y-Position.
     * @throws RuntimeException (Lunar-Error) wenn {@code y} nicht endlich ist (NaN/Infinite).
     */
    public void setY(float y) {
        if (!Float.isFinite(y)) {
            throw Errors.argument(ErrorDetails.of("Actor", "Actor Y position must be finite")
                    .withActual(y)
                    .withExpected("A finite Y coordinate")
                    .withExplanation("NaN and infinite coordinates cannot represent an actor position for rendering and collision detection"), null);
        }

        this.y = y;

        if (rigidBody != null) {
            rigidBody.getPosition().setY(y);
        }
    }

    /**
     * Gibt den Z-Index (Zeichenreihenfolge / Ebene) des Actors zurück.
     * @return Der Z-Index.
     */
    public int getZIndex() {
        return zIndex;
    }

    /**
     * Setzt den Z-Index (Zeichenreihenfolge / Ebene) des Actors.
     * @param zIndex Der neue Z-Index.
     */
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * Gibt den Geschwindigkeitsvektor des Actors zurück.
     * @return Der {@link Vec2}-Vektor der Geschwindigkeit.
     */
    public Vec2 getVelocity() {
        return velocity;
    }

    /**
     * Setzt den Geschwindigkeitsvektor des Actors.
     * @param velocity Der neue {@link Vec2}-Geschwindigkeitsvektor.
     */
    public void setVelocity(Vec2 velocity) {
        this.velocity = velocity;
    }

    /**
     * Gibt den zugewiesenen {@link Animator} für Sprite-Animationen zurück.
     * @return Der Animator.
     */
    public Animator getAnimator() {
        return animator;
    }

    /**
     * Weist dem Actor einen {@link Animator} zu.
     * @param animator Der zu verwendende Animator.
     */
    public void setAnimator(Animator animator) {
        this.animator = animator;
    }

    /**
     * Startet oder wechselt die Animation mit dem gegebenen Namen.
     * Dies ist eine Convenience-Methode, die intern {@code animator.playAnimation(name)} aufruft.
     *
     * @param name Der Name der Animation.
     */
    public void playAnimation(String name) {
        if (animator != null) {
            animator.playAnimation(name);
        }
    }

    /**
     * Gibt den physikalischen {@link RigidBody2D} des Actors zurück.
     * @return Der RigidBody oder {@code null}.
     */
    public RigidBody2D getRigidBody() {
        return rigidBody;
    }

    /**
     * Weist dem Actor einen physischen {@link RigidBody2D} zu und synchronisiert
     * dessen Startposition direkt mit der aktuellen Position des Actors.
     *
     * @param rigidBody Der neue {@link RigidBody2D}.
     */
    public void setRigidBody(RigidBody2D rigidBody) {
            this.rigidBody = rigidBody;

        // Wenn ein Body zugewiesen wird, synchronisiere sein Startposition mit der aktuellen Position des Actors
        if (this.rigidBody != null) {
            this.rigidBody.getPosition().set(this.x, this.y);
        }
    }

    /**
     * Prüft, ob eine benutzerdefinierte Hitbox aktiv ist.
     * @return {@code true} bei Custom-Hitbox, sonst {@code false}.
     */
    public boolean hasCustomHitbox() {
        return customHitbox;
    }

    /**
     * Gibt die effektive Breite der Hitbox zurück (entweder Custom-Breite oder Bildbreite).
     * @return Hitbox-Breite.
     */
    public int getHitboxWidth() {
        return customHitbox ? hitboxWidth : width;
    }

    /**
     * Gibt die effektive Höhe der Hitbox zurück (entweder Custom-Höhe oder Bildhöhe).
     * @return Hitbox-Höhe.
     */
    public int getHitboxHeight() {
        return customHitbox ? hitboxHeight : height;
    }

    /**
     * Gibt den horizontalen Offset der Hitbox zurück.
     * @return Hitbox X-Offset.
     */
    public int getHitboxOffsetX() {
        return customHitbox ? hitboxOffsetX : 0;
    }

    /**
     * Gibt den vertikalen Offset der Hitbox zurück.
     * @return Hitbox Y-Offset.
     */
    public int getHitboxOffsetY() {
        return customHitbox ? hitboxOffsetY : 0;
    }

    /**
     * Legt fest, ob die Debug-Hitbox auf dem Bildschirm sichtbar sein soll.
     * @param visible {@code true} zum Einblenden, {@code false} zum Ausblenden.
     */
    public void setHitboxVisible(boolean visible) {
        this.hitboxVisible = visible;
    }

    /**
     * Prüft, ob die Debug-Hitbox sichtbar ist.
     * @return {@code true} wenn sichtbar.
     */
    public boolean isHitboxVisible() {
        return hitboxVisible;
    }

    /**
     * Setzt die Farbe der Debug-Hitbox.
     *
     * @param color Die neue {@link Colors}-Farbe.
     */
    public void setHitboxColor(Colors color) {
        if (color != null) {
            hitboxColor = color;
        }
    }

    /**
     * Gibt die aktuelle Farbe der Debug-Hitbox zurück.
     * @return Die Hitbox-Farbe.
     */
    public Colors getHitboxColor() {
        return hitboxColor;
    }

}
