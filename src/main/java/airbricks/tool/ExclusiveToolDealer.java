package airbricks.tool;

import bricks.trade.Host;

public class ExclusiveToolDealer implements ToolDealer {

    ToolBrick toolBrick;
    ToolClient owner;

    public ExclusiveToolDealer(Host host) {
        toolBrick = new ToolBrick(host);
    }

    @Override
    public ToolBrick request(ToolClient client) {
        if(owner != null) owner.depriveToolBrick();
        owner = client;
        return toolBrick;
    }
}
