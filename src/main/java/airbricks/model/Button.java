package airbricks.model;

import bricks.*;
import bricks.graphic.ColorRectangle;
import bricks.graphic.Rectangle;
import bricks.input.Mouse;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.State;
import bricks.var.special.Num;
import bricks.wall.Brick;


public class Button extends Brick<Host> implements Rectangle {

    final Rectangle body;
    final Num offset;

    final ColorRectangle rect;
    final Var<Color> rectColor;
    final Var<Color> rectPressColor;

    final ColorRectangle contentRect;
    final Var<Color> contentRectColor;
    final Var<Color> contentRectPressColor;

    final ColorRectangle selectRect;
    final Var<Color> selectRectColor;

    final Monitor pressingMonitor;
    final Monitor releasingMonitor;

    public Button(Host host) {
        super(host);

        shown = new State<>(false);
        when(shown.signal(), () -> {
            if(shown.getInput()) show();
            else hide();
        });
        selected = new State<>(false);
        when(selected.signal(), () -> {
            if(selected.getInput()) select();
            else unselect();
        });
        clicked = Vars.get();

        rectColor = Vars.set(Color.mix(.15, .15, .25));
        rectPressColor = Vars.set(Color.mix(.2, .2, .25));
        contentRectColor = Vars.set(Color.mix(.15, .15, .35));
        contentRectPressColor = Vars.set(Color.mix(.2, .2, .4));
        selectRectColor = Vars.set(Color.mix(1, .8, .6));

        body = new Centroid();
        offset = Vars.num(0);
        rect = rect();
        rect.aim(body);
        rect.adjust(body.margin(offset));
        rect.color().let(rectColor);

        selectRect = rect();
        selectRect.aim(rect);
        selectRect.adjust(rect.margin(4));
        selectRect.color().let(selectRectColor);

        contentRect = rect();
        contentRect.aim(rect);
        contentRect.adjust(rect.margin(-20));
        contentRect.color().let(contentRectColor);

        pressingMonitor = when(mouse().leftButton().willBe(Mouse.Button::pressed)).then(()-> {
            selected.set(rect.contains(mouse().position()));
        }, false);

        releasingMonitor = when(mouse().leftButton().willBe(Mouse.Button::released)).then(()-> {
            if(rect.contains(mouse().position()) && selected.get()) {
                click();
            }
        }, false);
    }

    State<Boolean> shown;

    @Override
    public void show() {
        if(!shown.get()) {
            pressingMonitor.use();
            releasingMonitor.use();
            show(rect);
            show(contentRect);
            shown.setState(true);
        }
    }

    @Override
    public void hide() {
        if(shown.get()) {
            pressingMonitor.cancel();
            releasingMonitor.cancel();
            hide(rect);
            hide(contentRect);
            hide(selectRect);
            shown.setState(false);
        }
    }

    public Var<Boolean> shown() {
        return shown;
    }

    @Override
    public void move() {

    }

    @Override
    public void update() {
        super.update();

        if(shown.get()) {
            var mouse = mouse();

            boolean mouseIn = body.contains(mouse.position());
            if (!mouseIn) {
                offset.set(0);
                rect.color().let(rectColor);
                contentRect.color().let(contentRectColor);
            } else {
                boolean leftButtonPressed = mouse.leftButton().isPressed();
                if (!leftButtonPressed) {
                    offset.set(4);
                    rect.color().let(rectColor);
                    contentRect.color().let(contentRectColor);
                } else {
                    boolean selected = this.selected.get();
                    if (selected) {
                        offset.set(4);
                        rect.color().let(rectPressColor);
                        contentRect.color().let(contentRectPressColor);
                    } else {
                        offset.set(0);
                        rect.color().let(rectColor);
                        contentRect.color().let(contentRectColor);
                    }
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    State<Boolean> selected;

    public void select() {
        if(!selected.get()) {
            show(selectRect, rect);
            selected.setState(true);
        }
    }

    public void unselect() {
        if(selected.get()) {
            hide(selectRect);
            selected.setState(false);
        }
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
