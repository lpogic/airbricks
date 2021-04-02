package airbricks;

import airbricks.model.Button;
import airbricks.model.Note;
import bricks.wall.Wall;

import static suite.suite.$uite.$;

public class Main extends Wall {

    public Button button() {
        return new Button(this);
    }
    public Note note() {
        return new Note(this);
    }

    @Override
    protected void setup() {
        var note = note();
        var button = button().setPosition(400, 300);
        button.setWidth(200);
        button.height().let(note.text().height().per(h -> h.floatValue() + 20));
        note.selected().let(button.selected());
//        when(button.clicked()).then(note::select);
//        when(button.clicked()).then(() -> System.out.print("click!"));
//        when(button.selected().willGive(false)).then(note::unselect);
        use(button);
        use(note);

    }

    @Override
    public void update() {

    }

    public static void main(String[] args) {
        Wall.play($(Wall.class, Main.class));
    }
}
