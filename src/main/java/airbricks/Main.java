package airbricks;

import bricks.wall.Wall;

import static suite.suite.$uite.$;

public class Main extends Wall {

    @Override
    protected void setup() {
    }

    @Override
    public void frontUpdate() {

    }

    public static void main(String[] args) {
        Wall.play($(Wall.class, Main.class));
    }
}
