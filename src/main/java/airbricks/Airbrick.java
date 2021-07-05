package airbricks;

import airbricks.assistance.AssistanceDealer;
import airbricks.selection.SelectionDealer;
import bricks.trade.Host;
import bricks.wall.Brick;

public abstract class Airbrick<H extends Host> extends Brick<H> {

    public Airbrick(H host) {
        super(host);
    }

    protected SelectionDealer selectionDealer() {
        return order(SelectionDealer.class);
    }
    protected AssistanceDealer assistanceDealer() {
        return order(AssistanceDealer.class);
    }
}
