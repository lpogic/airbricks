package airbricks.tool;

import bricks.var.Source;

public interface ToolClient {
    Source<Boolean> hasToolBrick();
    void depriveToolBrick();
}
