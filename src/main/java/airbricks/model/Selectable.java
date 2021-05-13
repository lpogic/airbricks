package airbricks.model;

import bricks.var.Var;

public interface Selectable {
    Var<Boolean> selected();
    default void select() {
        selected().set(true);
    }
    default void unselect() {
        selected().set(false);
    }
}
