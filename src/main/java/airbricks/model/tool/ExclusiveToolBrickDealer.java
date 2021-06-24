package airbricks.model.tool;

import bricks.trade.Host;

public class ExclusiveToolBrickDealer implements ToolBrickDealer {

    ToolBrick toolBrick;
    ToolBrickClient owner;

    public ExclusiveToolBrickDealer(Host host) {
        toolBrick = new ToolBrick(host);
    }

    @Override
    public ToolBrick request(ToolBrickClient client) {
        if(owner != null) owner.depriveToolBrick();
        owner = client;
        return toolBrick;
    }
}
