package LunarAxis.ui.elements;

import LunarAxis.graphics.Colors;

/**
 * Die Klasse {@code UIDialog} repräsentiert ein Dialog-Fenster (Pop-up) mit einer Nachricht
 * sowie Bestätigungs- und Abbrechen-Schaltflächen.
 * <p>
 * Sie erweitert {@link UIWindow} und eignet sich ideal für Warnungen, Bestätigungsfragen
 * (z. B. "Spiel beenden?") oder informative Nachrichten. Beim Auslösen eines Buttons
 * schliesst sich der Dialog automatisch und führt den entsprechenden Callback aus.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * dialog = UIDialog("Beenden", "Möchtest du das Spiel wirklich beenden?", 200, 150, 350)
 * dialog.setOnConfirm(lambda: engine.stop())
 * </pre>
 *
 * @version 1.2.6
 */
public class UIDialog extends UIWindow {
    private UILabel messageLabel;
    private UIButton confirmButton;
    private UIButton cancelButton;
    private Runnable onConfirm;
    private Runnable onCancel;

    /**
     * Erstellt einen neuen Dialog mit Standard-Schaltflächenbeschriftungen ("OK" und "Abbrechen").
     *
     * @param title   Der Titel des Dialogfensters.
     * @param message Die anzuzeigende Nachricht.
     * @param x       Die X-Koordinate auf dem Bildschirm.
     * @param y       Die Y-Koordinate auf dem Bildschirm.
     * @param width   Die Breite des Dialogfensters in Pixeln.
     */
    public UIDialog(String title, String message, float x, float y, int width) {
        this(title, message, "OK", "Abbrechen", x, y, width);
    }

    /**
     * Erstellt einen neuen Dialog mit benutzerdefinierten Schaltflächenbeschriftungen.
     *
     * @param title       Der Titel des Dialogfensters.
     * @param message     Die anzuzeigende Nachricht.
     * @param confirmText Der Text für den Bestätigungs-Button.
     * @param cancelText  Der Text für den Abbrechen-Button.
     * @param x           Die X-Koordinate auf dem Bildschirm.
     * @param y           Die Y-Koordinate auf dem Bildschirm.
     * @param width       Die Breite des Dialogfensters in Pixeln.
     */
    public UIDialog(String title, String message, String confirmText, String cancelText, float x, float y, int width) {
        super(title, x, y, width, 150);
        this.setDrawBackground(true);
        this.setBackgroundColor(new Colors(40, 40, 50, 230)); // Leicht transparent

        // Nachricht
        messageLabel = new UILabel(message, x + 20, y + 50, 16, Colors.WHITE);
        this.addChild(messageLabel);


        // Buttons
        int btnWidth = 80;
        int btnHeight = 30;
        float btnY = y + height - 50;
        float centerX = x + width / 2f;

        confirmButton = new UIButton(confirmText, centerX - btnWidth - 10, btnY, btnWidth, btnHeight, Colors.GREEN, Colors.WHITE);
        cancelButton = new UIButton(cancelText, centerX + 10, btnY, btnWidth, btnHeight, Colors.RED, Colors.WHITE);

        addChild(confirmButton);
        addChild(cancelButton);
    }

    /**
     * Setzt die Aktion (Callback), die beim Bestätigen des Dialogs ausgeführt werden soll.
     * Blendet den Dialog nach dem Klick automatisch aus.
     *
     * @param onConfirm Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
        confirmButton.setOnClick(() -> {
            this.setVisible(false);
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
    }

    /**
     * Setzt die Aktion (Callback), die beim Abbrechen des Dialogs ausgeführt werden soll.
     * Blendet den Dialog nach dem Klick automatisch aus.
     *
     * @param onCancel Die auszuführende {@link Runnable}-Aktion.
     */
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        cancelButton.setOnClick(() -> {
            this.setVisible(false);
            if (onCancel != null) {
                onCancel.run();
            }
        });
    }

    /**
     * Ändert den Nachrichtentext des Dialogs.
     *
     * @param message Der neue Nachrichtentext.
     */
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * Gibt den Bestätigungs-Button des Dialogs zurück.
     *
     * @return Der Bestätigungs-{@link UIButton}.
     */
    public UIButton getConfirmButton() {
        return confirmButton;
    }

    /**
     * Gibt den Abbrechen-Button des Dialogs zurück.
     *
     * @return Der Abbrechen-{@link UIButton}.
     */
    public UIButton getCancelButton() {
        return cancelButton;
    }
}