package airbricks.tool;

import bricks.trait.Source;

public interface ToolClient {
    Source<Boolean> hasToolBrick();
    void depriveToolBrick();
}
