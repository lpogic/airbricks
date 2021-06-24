package airbricks.model.tool;

import bricks.var.Source;

public interface ToolBrickClient {
    Source<Boolean> hasToolBrick();
    void depriveToolBrick();
}
