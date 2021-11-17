package airbricks.selection;

import bricks.trade.Contract;

public interface KeyboardClient {
    record KeyboardTransfer(KeyboardClient current, boolean front) implements Contract<Boolean>{}
    enum HasKeyboard{ EXCLUSIVE, SHARED, NO }
    void depriveKeyboard();
    void requestKeyboard();
    boolean acceptKeyboard(boolean front);
    HasKeyboard hasKeyboard();
    default boolean seeKeyboard() {
        var has = hasKeyboard();
        return has == HasKeyboard.EXCLUSIVE || has == HasKeyboard.SHARED;
    }
}
