package LunarAxis.inputs;

import java.awt.event.KeyEvent;

/**
 * Die Enumeration {@code Key} stellt eine übersichtliche und typsichere Sammlung
 * aller unterstützten Tastatur-Tasten für die LunarAxis-Engine bereit.
 * <p>
 * Jede Taste ist direkt mit ihrem entsprechenden Java AWT {@link KeyEvent}-Code
 * (Virtual Key Code) verknüpft, was die Abfrage von Tastatureingaben im Spiel vereinfacht.
 * </p>

 * @author Patrick André
 * @version 1.2.6
 */
public enum Key {

    /**
     * Hier sind alle Tasten definiert, die verwendet werden können.
     * Die Tasten werden mit ihrer VK-Konstanten (Virtual Key Code) definiert.
     *
     * Diese Konstanten können in der Klasse KeyEvent definiert werden und
     * geben einen bestimmten Schlüssel an.
     * */

    // buchstaben
    A(KeyEvent.VK_A), B(KeyEvent.VK_B), C(KeyEvent.VK_C), D(KeyEvent.VK_D),
    E(KeyEvent.VK_E), F(KeyEvent.VK_F), G(KeyEvent.VK_G), H(KeyEvent.VK_H),
    I(KeyEvent.VK_I), J(KeyEvent.VK_J), K(KeyEvent.VK_K), L(KeyEvent.VK_L),
    M(KeyEvent.VK_M), N(KeyEvent.VK_N), O(KeyEvent.VK_O), P(KeyEvent.VK_P),
    Q(KeyEvent.VK_Q), R(KeyEvent.VK_R), S(KeyEvent.VK_S), T(KeyEvent.VK_T),
    U(KeyEvent.VK_U), V(KeyEvent.VK_V), W(KeyEvent.VK_W), X(KeyEvent.VK_X),
    Y(KeyEvent.VK_Y), Z(KeyEvent.VK_Z),

    // navigation
    LEFT(KeyEvent.VK_LEFT),
    RIGHT(KeyEvent.VK_RIGHT),
    UP(KeyEvent.VK_UP),
    DOWN(KeyEvent.VK_DOWN),
    PAGE_UP(KeyEvent.VK_PAGE_UP),
    PAGE_DOWN(KeyEvent.VK_PAGE_DOWN),
    HOME(KeyEvent.VK_HOME),
    END(KeyEvent.VK_END),
    INSERT(KeyEvent.VK_INSERT),
    DELETE(KeyEvent.VK_DELETE),
    BACK_SPACE(KeyEvent.VK_BACK_SPACE),
    TAB(KeyEvent.VK_TAB),
    ENTER(KeyEvent.VK_ENTER),
    ESCAPE(KeyEvent.VK_ESCAPE),
    SPACE(KeyEvent.VK_SPACE),
    CAPS_LOCK(KeyEvent.VK_CAPS_LOCK),
    NUM_LOCK(KeyEvent.VK_NUM_LOCK),
    SCROLL_LOCK(KeyEvent.VK_SCROLL_LOCK),
    PRINT_SCREEN(KeyEvent.VK_PRINTSCREEN),
    PAUSE(KeyEvent.VK_PAUSE),

    // modifiers
    SHIFT(KeyEvent.VK_SHIFT),
    CTRL(KeyEvent.VK_CONTROL),
    ALT(KeyEvent.VK_ALT),
    ALT_GRAPH(KeyEvent.VK_ALT_GRAPH),
    META(KeyEvent.VK_META),

    // digits
    DIGIT0(KeyEvent.VK_0), DIGIT1(KeyEvent.VK_1), DIGIT2(KeyEvent.VK_2), DIGIT3(KeyEvent.VK_3),
    DIGIT4(KeyEvent.VK_4), DIGIT5(KeyEvent.VK_5), DIGIT6(KeyEvent.VK_6), DIGIT7(KeyEvent.VK_7),
    DIGIT8(KeyEvent.VK_8), DIGIT9(KeyEvent.VK_9),

    // nummern
    N1(KeyEvent.VK_1), N2(KeyEvent.VK_2), N3(KeyEvent.VK_3), N4(KeyEvent.VK_4),
    N5(KeyEvent.VK_5), N6(KeyEvent.VK_6), N7(KeyEvent.VK_7), N8(KeyEvent.VK_8),
    N9(KeyEvent.VK_9), N0(KeyEvent.VK_0),

    // numpad
    NUMPAD0(KeyEvent.VK_NUMPAD0), NUMPAD1(KeyEvent.VK_NUMPAD1), NUMPAD2(KeyEvent.VK_NUMPAD2),
    NUMPAD3(KeyEvent.VK_NUMPAD3), NUMPAD4(KeyEvent.VK_NUMPAD4), NUMPAD5(KeyEvent.VK_NUMPAD5),
    NUMPAD6(KeyEvent.VK_NUMPAD6), NUMPAD7(KeyEvent.VK_NUMPAD7), NUMPAD8(KeyEvent.VK_NUMPAD8),
    NUMPAD9(KeyEvent.VK_NUMPAD9),
    MULTIPLY(KeyEvent.VK_MULTIPLY), ADD(KeyEvent.VK_ADD), SEPARATOR(KeyEvent.VK_SEPARATOR),
    SUBTRACT(KeyEvent.VK_SUBTRACT), DECIMAL(KeyEvent.VK_DECIMAL), DIVIDE(KeyEvent.VK_DIVIDE),

    // symbole / punktuation
    MINUS(KeyEvent.VK_MINUS), EQUALS(KeyEvent.VK_EQUALS),
    COMMA(KeyEvent.VK_COMMA), PERIOD(KeyEvent.VK_PERIOD), SLASH(KeyEvent.VK_SLASH),
    SEMICOLON(KeyEvent.VK_SEMICOLON), QUOTE(KeyEvent.VK_QUOTE),
    OPEN_BRACKET(KeyEvent.VK_OPEN_BRACKET), CLOSE_BRACKET(KeyEvent.VK_CLOSE_BRACKET),
    BACK_SLASH(KeyEvent.VK_BACK_SLASH),
    BACK_QUOTE(KeyEvent.VK_BACK_QUOTE),

    // funktionstasten
    F1(KeyEvent.VK_F1),  F2(KeyEvent.VK_F2),  F3(KeyEvent.VK_F3),  F4(KeyEvent.VK_F4),
    F5(KeyEvent.VK_F5),  F6(KeyEvent.VK_F6),  F7(KeyEvent.VK_F7),  F8(KeyEvent.VK_F8),
    F9(KeyEvent.VK_F9),  F10(KeyEvent.VK_F10), F11(KeyEvent.VK_F11), F12(KeyEvent.VK_F12),
    F13(KeyEvent.VK_F13), F14(KeyEvent.VK_F14), F15(KeyEvent.VK_F15), F16(KeyEvent.VK_F16),
    F17(KeyEvent.VK_F17), F18(KeyEvent.VK_F18), F19(KeyEvent.VK_F19), F20(KeyEvent.VK_F20),
    F21(KeyEvent.VK_F21), F22(KeyEvent.VK_F22), F23(KeyEvent.VK_F23), F24(KeyEvent.VK_F24);

    /**
     * Die VK-Konstante (Virtual Key Code) des Tastencodes.
     */
    private final int keyCode;

    /**
     * Erstellt ein neues Key-Enum-Element mit dem angegebenen Virtual Key Code.
     *
     * @param keyCode Der Java {@link KeyEvent}-Code für diese Taste.
     */
    Key(int keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * Gibt die originale VK-Konstante (Virtual Key Code) des Tastencodes zurück.
     *
     * @return Der Integer-Wert des KeyCodes.
     */
    public int getKeyCode() {
        return keyCode;
    }
}
