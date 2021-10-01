package airbricks;

import bricks.graphic.RectangleBrick;
import bricks.wall.Wall;

import static suite.suite.$uite.$;

public class Main extends Wall {

    @Override
    protected void setup() {
//        var r = Shapes.rectangle($(
//                "aim", $(this)
//        ));
//        $bricks.set(r);
        $bricks.set(new RectangleBrick(this){{
            aim(Main.this);
        }});
    }

    @Override
    public void frontUpdate() {

    }

    public static void main(String[] args) {
        Wall.play($(Wall.class, $(new Main())));
    }
}
