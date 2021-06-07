package airbricks.model.selection;

import bricks.var.Source;

public interface SelectionClient {
    Source<Boolean> selected();
    void depriveSelection();
    void requestSelection();
}
