package airbricks.selection;

public interface KeyboardClient {
    enum HasKeyboard{ EXCLUSIVE, SHARED, NO }
    void depriveKeyboard();
    void requestKeyboard();
    HasKeyboard hasKeyboard();
    default boolean seeKeyboard() {
        var has = hasKeyboard();
        return has == HasKeyboard.EXCLUSIVE || has == HasKeyboard.SHARED;
    }
}
