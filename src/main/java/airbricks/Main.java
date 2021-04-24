package airbricks;

import airbricks.model.AirComposite;
import brackettree.reader.BracketTree;
import bricks.wall.Wall;

import static suite.suite.$uite.arm$;

public class Main extends Wall implements AirComposite {

    @Override
    protected void setup() {
        var note = input(note(BracketTree.read("dane.tree").asString(""), "Wpisz tu co"));
        note.width().let(width());
        note.aim(this);

        var save = input("Zapisz");
        save.x().let(() -> width().getFloat() / 2);
        save.y().let(() -> height().getFloat() - save.height().getFloat() / 2);

        when(save.clicked()).then(() -> BracketTree.write(note.string().get(), "dane.tree"));

        use(note);
        use(save);
    }

    @Override
    public void update() {

    }

    public static void main(String[] args) {
        Wall.play(arm$(Wall.class, Main.class));
    }
}
