package airbricks.selection;

public class ExclusiveKeyboardDealer implements KeyboardDealer {

    KeyboardClient owner;

    @Override
    public KeyboardClient.HasKeyboard requestKeyboard(KeyboardClient keyboardClient) {
        if(owner != null) owner.depriveKeyboard();
        owner = keyboardClient;
        return KeyboardClient.HasKeyboard.EXCLUSIVE;
    }
}
