package airbricks.model;

import bricks.*;
import bricks.graphic.ColorRectangle;
import bricks.graphic.Rectangle;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.State;
import bricks.var.special.Num;
import bricks.wall.MouseObserver;


public class InputBase extends Airbrick<Host> implements Rectangle {

    final Rectangle body;
    final Num offset;

    final ColorRectangle rect;
    final Var<Color> rectColor;
    final Var<Color> rectPressColor;

    final ColorRectangle contentRect;
    final Var<Color> contentRectColor;
    final Var<Color> contentRectPressColor;

    final ColorRectangle selectRect;

    public InputBase(Host host) {
        super(host);

        selected = state(false, this::select);
        pressed = state(false, this::press);
        highlighted = state(false, this::highlight);
        clicked = Vars.get();

        rectColor = Vars.set(Color.hex("#255a09"));
        rectPressColor = Vars.set(Color.hex("#255a09"));
        contentRectColor = Vars.set(Color.hex("#0d3205"));
        contentRectPressColor = Vars.set(Color.hex("#0d3f05"));

        body = new Centroid();
        offset = Vars.num(0);
        rect = rect();
        rect.aim(body);
        rect.adjust(Sized.relative(body, offset));
        rect.color().let(rectColor);

        selectRect = rect();
        selectRect.aim(rect);
        selectRect.adjust(Sized.relative(rect, 4));
        selectRect.color().set(Color.mix(1, .8, .6));

        contentRect = rect();
        contentRect.aim(rect);
        contentRect.adjust(Sized.relative(rect, -14));
        contentRect.color().let(contentRectColor);

        $bricks.set(rect, contentRect);
    }

    State<Boolean> pressed;

    public void press(boolean state) {
        if(state) press();
        else release();
    }

    public void press() {
        if(!pressed.get()) {
            rect.color().let(rectPressColor);
            contentRect.color().let(contentRectPressColor);
            pressed.setState(true);
        }
    }

    public void release() {
        if(pressed.get()) {
            rect.color().let(rectColor);
            contentRect.color().let(contentRectColor);
            pressed.setState(false);
        }
    }

    public Var<Boolean> pressed() {
        return pressed;
    }

    State<Boolean> highlighted;

    public void highlight(boolean state) {
        if(state) highlight();
        else equalize();
    }

    public void highlight() {
        if(!highlighted.get()) {
            offset.set(4);
            highlighted.setState(true);
        }
    }

    public void equalize() {
        if(highlighted.get()) {
            offset.set(0);
            highlighted.setState(false);
        }
    }

    public Var<Boolean> highlighted() {
        return highlighted;
    }

    State<Boolean> selected;

    public void select(boolean state) {
        if(state != selected.get()) {
            if(state) {
                $bricks.aimedSet(rect, selectRect);
                selected.setState(true);
            } else {
                $bricks.unset(selectRect);
                selected.setState(false);
            }
        }
    }

    public void select() {
        select(true);
    }

    public void unselect() {
        select(false);
    }

    public Var<Boolean> selected() {
        return selected;
    }

    Var<Number> clicked;
    public void click() {
        clicked.set(System.currentTimeMillis());
    }

    public Source<Number> clicked() {
        return clicked;
    }

    @Override
    public Num width() {
        return body.width();
    }
    @Override
    public Num height() {
        return body.height();
    }
    @Override
    public Num left() {
        return body.left();
    }
    @Override
    public Num right() {
        return body.right();
    }
    @Override
    public Num top() {
        return body.top();
    }
    @Override
    public Num bottom() {
        return body.bottom();
    }
    @Override
    public Num x() {
        return body.x();
    }
    @Override
    public Num y() {
        return body.y();
    }

}
