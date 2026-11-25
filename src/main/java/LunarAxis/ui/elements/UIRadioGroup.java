package LunarAxis.ui.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Klasse {@code UIRadioGroup} verwaltet eine Gruppe von {@link UIRadioButton}s.
 * <p>
 * Sie stellt sicher, dass innerhalb der Gruppe immer nur genau ein Radio-Button
 * gleichzeitig selektiert sein kann. Wird ein neuer Button ausgewählt, werden
 * alle anderen Gruppenmitglieder automatisch abgewählt.
 * </p>
 *
 * <pre>
 * # Beispiel TigerJython:
 * group = UIRadioGroup("Difficulty")
 * group.addButton(easyBtn)
 * group.addButton(hardBtn)
 * group.setSelectedIndex(0)
 * </pre>
 *
 * @author Patrick André
 * @version 1.2.6
 */
public class UIRadioGroup {
    private final List<UIRadioButton> buttons = new ArrayList<>();
    private String groupName;
    private UIRadioButton selectedButton;

    /**
     * Erstellt eine neue Radio-Button-Gruppe mit einem bestimmten Gruppennamen.
     *
     * @param groupName Der Name der Optionen-Gruppe.
     */
    public UIRadioGroup(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Fügt einen {@link UIRadioButton} zur Gruppe hinzu und verknüpft ihn.
     *
     * @param button Der hinzuzufügende {@link UIRadioButton}.
     */
    public void addButton(UIRadioButton button) {
        if (button != null && !buttons.contains(button)) {
            buttons.add(button);
            button.setGroup(this); // Verknüpfe Button mit dieser Gruppe
            
            // Wenn dieser Button selektiert ist, setze ihn als selectedButton
            if (button.isSelected()) {
                selectedButton = button;
            }
        }
    }

    /**
     * Entfernt einen {@link UIRadioButton} aus der Gruppe und hebt die Verknüpfung auf.
     *
     * @param button Der zu entfernende {@link UIRadioButton}.
     */
    public void removeButton(UIRadioButton button) {
        buttons.remove(button);
        button.setGroup(null);
        if (selectedButton == button) {
            selectedButton = null;
        }
    }

    /**
     * Wählt einen bestimmten {@link UIRadioButton} aus und deselektiert alle anderen Gruppenmitglieder.
     *
     * @param button Der auszuwählende {@link UIRadioButton}.
     */
    public void select(UIRadioButton button) {
        if (button == null || !buttons.contains(button)) {
            return;
        }
        
        // Alle anderen deselectieren
        for (UIRadioButton btn : buttons) {
            if (btn != button) {
                btn.setSelected(false);
            }
        }
        
        button.setSelected(true);
        selectedButton = button;
    }

    /**
     * Gibt den aktuell selektierten {@link UIRadioButton} zurück.
     *
     * @return Der ausgewählte {@link UIRadioButton} oder {@code null}, wenn keiner gewählt ist.
     */
    public UIRadioButton getSelectedButton() {
        return selectedButton;
    }

    /**
     * Gibt den Index des aktuell selektierten Buttons innerhalb der Gruppe zurück.
     *
     * @return Der 0-basierte Index oder {@code -1}, falls kein Button gewählt ist.
     */
    public int getSelectedIndex() {
        return buttons.indexOf(selectedButton);
    }

    /**
     * Wählt einen Button innerhalb der Gruppe anhand seines Index aus.
     *
     * @param index Der 0-basierte Index des auszuwählenden Buttons.
     */
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < buttons.size()) {
            select(buttons.get(index));
        }
    }

    /**
     * Gibt eine Kopie der Liste aller in dieser Gruppe enthaltenen Buttons zurück.
     *
     * @return Eine neue {@link List} mit den {@link UIRadioButton}s.
     */
    public List<UIRadioButton> getButtons() {
        return new ArrayList<>(buttons);
    }

    /**
     * Gibt den Namen der Gruppe zurück.
     *
     * @return Der Gruppenname.
     */
    public String getGroupName() {
        return groupName;
    }
}