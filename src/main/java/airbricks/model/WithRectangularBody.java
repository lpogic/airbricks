package airbricks.model;

import bricks.graphic.Rectangular;
import bricks.var.special.Num;
import bricks.var.special.NumSource;

public interface WithRectangularBody extends Rectangular {
    Rectangular getBody();

    @Override
    default NumSource width() {
        return getBody().width();
    }
    @Override
    default NumSource height() {
        return getBody().height();
    }
    @Override
    default NumSource left() {
        return getBody().left();
    }
    @Override
    default NumSource right() {
        return getBody().right();
    }
    @Override
    default NumSource top() {
        return getBody().top();
    }
    @Override
    default NumSource bottom() {
        return getBody().bottom();
    }
    @Override
    default NumSource x() {
        return getBody().x();
    }
    @Override
    default NumSource y() {
        return getBody().y();
    }
}
