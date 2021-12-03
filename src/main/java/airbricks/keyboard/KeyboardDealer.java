package airbricks.keyboard;

public interface KeyboardDealer {
    KeyboardClient.HasKeyboard requestKeyboard(KeyboardClient keyboardClient);
}
