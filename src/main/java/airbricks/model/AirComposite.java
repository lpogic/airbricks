package airbricks.model;

import bricks.trade.Composite;

public interface AirComposite extends Composite {

    default Button input() {
        return new Button(this);
    }
    default Note note() {
        return new Note(this);
    }
    default Note note(String text) {
        var note = new Note(this);
        note.string().set(text);
        note.editable().set(false);
        return note;
    }
    default Note note(String text, String placeholder) {
        var note = new NoteWithPlaceholder(this, placeholder);
        note.string().set(text);
        return note;
    }
    default NoteInput input(Note note) {
        note.editable().set(true);
        return new NoteInput(this, input(), note);
    }
    default TextInput input(String string) {
        var text = text();
        text.string().set(string);
        return new TextInput(this, input(), text);
    }
}
