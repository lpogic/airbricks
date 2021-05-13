package airbricks.model;

import bricks.trade.Composite;

public interface AirComposite extends Composite {

    default InputBase input() {
        return new InputBase(this);
    }

    default Button button() {
        return new Button(this);
    }

    default Button button(String string) {
        var button = new Button(this);
        button.string().set(string);
        return button;
    }

    default NoteInput note() {
        return new NoteInput(this);
    }

    default FeaturedNoteInput featuredNote(String feature) {
        var note = new FeaturedNoteInput(this);
        note.getFeature().string().set(feature);
        return note;
    }

    default Note note(String string) {
        var note = new Note(this);
        note.string().set(string);
        return note;
    }
}
