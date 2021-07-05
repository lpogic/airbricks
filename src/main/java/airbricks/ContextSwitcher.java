package airbricks;

import bricks.trade.Guest;
import bricks.trade.Host;
import bricks.wall.Wall;
import suite.suite.Subject;

import java.util.Stack;

public class ContextSwitcher extends Guest<Host> {

    public class Context {
        Wall wall = order(Wall.class);
        Subject $wallBricks = wall.getBricks();

        public void forward(Subject $bricks) {
            wall.setBricks($bricks);
        }

        public void restore() {
            wall.setBricks($wallBricks);
        }
    }

    public ContextSwitcher(Host host) {
        super(host);
    }

    public Context current() {
        return new Context();
    }
}
