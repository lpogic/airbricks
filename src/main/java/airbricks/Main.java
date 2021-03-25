package airbricks;

import app.model.Point;
import app.model.YOrigin;
import app.model.wall.Wall;
import brackettree.reader.BracketTree;

import static suite.suite.$uite.$;

public class Main extends Wall {

    @Override
    protected void setup() {
        String str = BracketTree.read("z.tree").as(String.class);
        var txt = text().setText(str).setSize(50).setYOrigin(YOrigin.CENTER);
        txt.position().let(width().per(n -> n.intValue() / 2), height().per(n -> n.intValue() / 2), Point::new);
        show(txt);
    }

    public static void main(String[] args) {
        Wall.play($(Wall.class, Main.class));


    }
}
