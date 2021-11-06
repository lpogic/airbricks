package airbricks;

import bricks.slab.RectangleSlab;
import bricks.wall.Wall;

import static suite.suite.$uite.$;

public class Main extends Wall {

    @Override
    protected void setup() {
        $bricks.set(new RectangleSlab(this){{
            aim(Main.this);
        }});
    }

    @Override
    public void update() {

    }

    public static void main(String[] args) {
        Wall.play($(Wall.class, $(new Main())));
    }
}
