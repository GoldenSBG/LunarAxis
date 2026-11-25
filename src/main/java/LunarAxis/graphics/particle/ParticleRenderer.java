package LunarAxis.graphics.particle;

import LunarAxis.graphics.Graphics;
import LunarAxis.math.Maths;

import java.awt.*;

/**
 * Die Klasse {@code ParticleRenderer} ist für das visuelle Darstellen (Rendering) aller
 * aktiven Partikel im {@link ParticleSystem} verantwortlich.
 * <p>
 * Sie geht die Liste aller Partikel durch, filtert unsichtbare heraus und zeichnet jedes
 * einzelne Partikel basierend auf seiner Form ({@link ParticleShape}), Grösse, Farbe und Rotation
 * mithilfe des {@link Graphics}-Systems auf den Bildschirm.
 * </p>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class ParticleRenderer {

    /**
     * Zeichnet alle aktiven und sichtbaren Partikel aus dem übergebenen Partikel-System.
     *
     * @param graphics       Das {@link Graphics}-Zeichenwerkzeug der Engine.
     * @param particleSystem Das {@link ParticleSystem}, dessen Partikel gerendert werden sollen.
     */
    public void render(Graphics graphics, ParticleSystem particleSystem) {
        if (graphics == null || particleSystem == null) {
            return;
        }

        for (Particle particle : particleSystem.getParticles()) {
            if (!particle.isVisible()) {
                continue;
            }
            drawParticle(graphics, particle);
        }
    }

    /**
     * Zeichnet ein einzelnes sichtbares Partikel an seiner aktuellen Weltposition.
     *
     * @param graphics Der aktuelle Zeichenkontext.
     * @param particle Das zu zeichnende Partikel.
     */
    public void renderParticle(Graphics graphics, Particle particle) {
        if (graphics == null || particle == null || !particle.isVisible()) {
            return;
        }

        drawParticle(graphics, particle);
    }

    /**
     * Zeichnet ein einzelnes Partikel abhängig von seiner geometrischen Form (Shape).
     * Unterstützt Formen wie Kreise, Quadrate, Linien, Dreiecke, Sterne, Texturen und einzelne Pixel.
     *
     * @param graphics Das {@link Graphics}-Objekt.
     * @param particle Das zu zeichnende {@link Particle}.
     */
    private void drawParticle(Graphics graphics, Particle particle) {
        int x = Maths.round(particle.position.x);
        int y = Maths.round(particle.position.y);
        int size = Maths.max(1, Maths.round(particle.size));

        graphics.setColor(particle.color);
        double angle = Maths.toRadians(particle.rotation);

        switch (particle.shape) {
            case CIRCLE:
                graphics.fillCircle(x, y, size);
                break;

            case SQUARE:
                int halfSize = size / 2;
                graphics.fillRect(x - halfSize, y - halfSize, size, size);
                break;

            case LINE:
                int endX = x + Maths.round((float) Maths.cos(angle) * size);
                int endY = y + Maths.round((float) Maths.sin(angle) * size);

                graphics.drawLine(x, y, endX, endY);
                break;

            case TRIANGLE:
                double angle1 = angle;
                double angle2 = angle + Maths.toRadians(140);
                double angle3 = angle - Maths.toRadians(140);

                int x1 = x + Maths.round((float) Maths.cos(angle1) * size);
                int y1 = y + Maths.round((float) Maths.sin(angle1) * size);

                int x2 = x + Maths.round((float) Maths.cos(angle2) * size);
                int y2 = y + Maths.round((float) Maths.sin(angle2) * size);

                int x3 = x + Maths.round((float) Maths.cos(angle3) * size);
                int y3 = y + Maths.round((float) Maths.sin(angle3) * size);

                graphics.drawLine(x1, y1, x2, y2);
                graphics.drawLine(x2, y2, x3, y3);
                graphics.drawLine(x3, y3, x1, y1);
                break;

            case STAR:
                double rotation = Maths.toRadians(particle.rotation);
                int points = 5;

                int[] xPoints = new int[points * 2];
                int[] yPoints = new int[points * 2];

                for (int i = 0; i < points * 2; i++) {
                    double starAngle = rotation + Maths.toRadians(i * 36.0 - 90.0);
                    float radius = (i % 2 == 0) ? size : size * 0.4f;

                    xPoints[i] = x + Maths.round((float) Maths.cos(starAngle) * radius);
                    yPoints[i] = y + Maths.round((float) Maths.sin(starAngle) * radius);
                }

                for (int i = 0; i < xPoints.length; i++) {
                    int next = (i + 1) % xPoints.length;
                    graphics.drawLine(xPoints[i], yPoints[i], xPoints[next], yPoints[next]);
                }
            break;

            case TEXTURE:
                Image texture = particle.texture;
                if (texture == null) {
                    graphics.fillCircle(x, y, size);
                } else {
                    drawTexture(graphics, texture, x, y, size, angle, particle.color.getAlpha() / 255f);
                }
                break;

            case PIXEL:
                graphics.fillRect(x, y, 1, 1);
                break;

            default:
                graphics.fillCircle(x, y, size);
        }
    }

    /**
     * Zeichnet ein Bild (Textur) mit lokaler Rotation und Transparenz auf eine Kopie
     * des aktuellen Zeichenkontexts. Die Transformation übernimmt dabei die bestehende
     * Kamera und zentriert das Bild exakt auf der Partikelposition.
     *
     * @param graphics Der aktuelle Zeichenkontext der Engine.
     * @param texture  Das zu zeichnende {@link Image}.
     * @param x        Die Welt-X-Koordinate des Bildmittelpunkts.
     * @param y        Die Welt-Y-Koordinate des Bildmittelpunkts.
     * @param size     Die Kantenlänge des quadratischen Zielbereichs in Welt-Einheiten.
     * @param angle    Der Rotationswinkel in Bogenmass (Radiant).
     * @param opacity  Die Transparenz des Partikels als Wert von 0.0 bis 1.0.
     */
    private void drawTexture(Graphics graphics, Image texture, int x, int y, int size, double angle, float opacity) {
        Graphics2D local = (Graphics2D) graphics.raw().create();
        try {
            if (opacity < 1f) {
                Composite composite = local.getComposite();
                if (composite instanceof AlphaComposite) {
                    AlphaComposite alpha = (AlphaComposite) composite;
                    local.setComposite(alpha.derive(alpha.getAlpha() * opacity));
                } else {
                    local.setComposite(AlphaComposite.SrcOver.derive(opacity));
                }
            }

            local.translate(x, y);
            local.rotate(angle);
            local.translate(-size / 2.0, -size / 2.0);
            local.drawImage(texture, 0, 0, size, size, null);
        } finally {
            local.dispose();
        }
    }
}

