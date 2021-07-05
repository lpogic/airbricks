package airbricks.assistance;

import bricks.var.Source;

public interface AssistanceClient {
    Source<Boolean> assisted();
    void depriveAssistance();
}
