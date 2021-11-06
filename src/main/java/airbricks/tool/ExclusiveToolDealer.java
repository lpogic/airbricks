package airbricks.tool;

import airbricks.Wall;
import bricks.trade.Agent;
import bricks.trade.Host;

public class ExclusiveToolDealer extends Agent<Host> implements ToolDealer {

    ToolBrick toolBrick;
    ToolClient owner;

    public ExclusiveToolDealer(Host host) {
        super(host);
        toolBrick = new ToolBrick(this);
    }

    @Override
    public ToolBrick request(ToolClient client) {
        if(owner != null) owner.depriveToolBrick();
        owner = client;
        return toolBrick;
    }

    @Override
    public void deprive(ToolBrick toolBrick) {
        if(owner != null) {
            owner = null;
            order(Wall.class).drop(toolBrick);
        }
    }
}
