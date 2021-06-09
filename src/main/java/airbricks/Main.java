package airbricks;

import bricks.wall.Wall;

import static suite.suite.$.arm$;

public class Main extends Wall {

    @Override
    protected void setup() {
    }

    @Override
    public void frontUpdate() {

    }

    public static void main(String[] args) {
        Wall.play(arm$(Wall.class, Main.class));
    }
}
