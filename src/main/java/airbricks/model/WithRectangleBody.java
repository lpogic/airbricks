package airbricks.model;

import bricks.graphic.Rectangle;
import bricks.var.special.Num;

public interface WithRectangleBody extends Rectangle {
    Rectangle getBody();

    @Override
    default Num width() {
        return getBody().width();
    }
    @Override
    default Num height() {
        return getBody().height();
    }
    @Override
    default Num left() {
        return getBody().left();
    }
    @Override
    default Num right() {
        return getBody().right();
    }
    @Override
    default Num top() {
        return getBody().top();
    }
    @Override
    default Num bottom() {
        return getBody().bottom();
    }
    @Override
    default Num x() {
        return getBody().x();
    }
    @Override
    default Num y() {
        return getBody().y();
    }
}
