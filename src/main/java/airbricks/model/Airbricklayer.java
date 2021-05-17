package airbricks.model;

import airbricks.model.button.TextButton;
import bricks.trade.Bricklayer;

public interface Airbricklayer extends Bricklayer {

    default TextButton button() {
        return new TextButton(this);
    }

    default TextButton button(String text) {
        var button = button();
        button.string().set(text);
        return button;
    }

    default Note note() {
        return new Note(this);
    }

    default Note note(String text) {
        var note = note();
        note.string().set(text);
        return note;
    }

    default NoteInput input() {
        return new NoteInput(this);
    }
}
