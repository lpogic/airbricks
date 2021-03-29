package airbricks.model;

import bricks.Point;
import bricks.var.Var;

public interface Positioned<T> {
    T setPosition(Point position);
    Point getPosition();
    Var<Point> position();
    default T setPosition(Number x, Number y) {
        return setPosition(new Point(x, y));
    }
}
