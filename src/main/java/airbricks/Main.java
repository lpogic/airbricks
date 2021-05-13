package airbricks;

import airbricks.model.AirComposite;
import bricks.wall.Wall;

import static suite.suite.$.arm$;

public class Main extends Wall implements AirComposite {

    @Override
    protected void setup() {
    }

    @Override
    public void update() {

    }

    public static void main(String[] args) {
        Wall.play(arm$(Wall.class, Main.class));
    }
}
