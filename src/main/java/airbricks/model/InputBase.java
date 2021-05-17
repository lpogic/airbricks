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


public class InputBase extends Airbrick<Host> implements Rectangle, WithRectangleBody {

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

        selectRect = rect(Color.mix(1, .8, .6));
        selectRect.aim(rect);
        selectRect.adjust(Sized.relative(rect, 4));


        contentRect = rect();
        contentRect.aim(rect);
        contentRect.adjust(Sized.relative(rect, -14));
        contentRect.color().let(contentRectColor);

        $bricks.set(rect, contentRect);
    }

    State<Boolean> pressed;

    public void press(boolean state) {
        if(pressed.get() != state) {
            if(state) {
                rect.color().let(rectPressColor);
                contentRect.color().let(contentRectPressColor);
            } else {
                rect.color().let(rectColor);
                contentRect.color().let(contentRectColor);
            }
            pressed.setState(state);
        }
    }

    public void press() {
        press(true);
    }

    public void release() {
        press(false);
    }

    public Var<Boolean> pressed() {
        return pressed;
    }

    State<Boolean> highlighted;

    public void highlight(boolean state) {
        if(highlighted.get() != state) {
            if(state) {
                offset.set(4);
            } else {
                offset.set(0);
            }
            highlighted.setState(state);
        }
    }

    public void highlight() {
        highlight(true);
    }

    public void equalize() {
        highlight(false);
    }

    public Var<Boolean> highlighted() {
        return highlighted;
    }

    State<Boolean> selected;

    public void select(boolean state) {
        if(state != selected.get()) {
            if(state) {
                $bricks.aimedSet(rect, selectRect);
            } else {
                $bricks.unset(selectRect);
            }
            selected.setState(state);
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
    public Rectangle getBody() {
        return body;
    }
}
