package airbricks;

import bricks.wall.Wall;
import suite.suite.$uite;

import static suite.suite.$uite.$;
import static suite.suite.$uite.$;

public class Main extends Wall {

    @Override
    protected void setup() {
    }

    @Override
    public void frontUpdate() {

    }

    public static void main(String[] args) {
        Wall.play($uite.$(Wall.class, $(Main.class)));
    }
}
