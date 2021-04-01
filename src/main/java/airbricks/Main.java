package airbricks;

import airbricks.model.Button;
import airbricks.model.Note;
import bricks.graphic.ColorText;
import bricks.var.Sup;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Wall;
import suite.suite.action.Statement;

import java.util.Arrays;

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
        var button = button().setPosition(400, 300).setNote("");
        button.width().let(note.width().per(w -> w.floatValue() + 50));
        when(button.selected(), note::select, note::unselect);
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
