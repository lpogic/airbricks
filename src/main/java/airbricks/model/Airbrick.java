package airbricks.model;

import airbricks.model.prompt.PromptDealer;
import bricks.trade.Host;
import bricks.wall.Brick;

public abstract class Airbrick<H extends Host> extends Brick<H> implements Airbricklayer {

    public Airbrick(H host) {
        super(host);
    }

    protected Selector selector() {
        return order(Selector.class);
    }
    protected PromptDealer promptDealer() {
        return order(PromptDealer.class);
    }
}
