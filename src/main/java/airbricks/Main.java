package airbricks;

import airbricks.model.AirComposite;
import brackettree.reader.BracketTree;
import bricks.Point;
import bricks.wall.Wall;

import static suite.suite.$uite.$;

public class Main extends Wall implements AirComposite {

    @Override
    protected void setup() {
        var note = input(note(BracketTree.read("dane.tree").asString(""), "Wpisz tu co"));
        note.width().let(this.width());
        note.position().let(this.center());

        var save = input("Zapisz");
        save.position().let(() -> new Point(getWidth() / 2f, getHeight() - save.getHeight() / 2));

        when(save.clicked()).then(() -> BracketTree.write(note.string().get(), "dane.tree"));

        use(note);
        use(save);
    }

    @Override
    public void update() {

    }

    public static void main(String[] args) {
        Wall.play($(Wall.class, Main.class));
    }
}
