package airbricks.model;

import bricks.trade.Host;
import bricks.wall.Brick;

public abstract class Airbrick<H extends Host> extends Brick<H> implements AirComposite {

    public Airbrick(H host) {
        super(host);
    }
}
