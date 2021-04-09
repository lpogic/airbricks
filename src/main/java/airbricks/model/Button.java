package airbricks.model;

import bricks.*;
import bricks.graphic.ColorRectangle;
import bricks.graphic.Rectangular;
import bricks.input.Mouse;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Brick;


public class Button extends Brick<Host> implements Rectangular {

    ColorRectangle rect;
    Var<Color> rectColor;
    Var<Color> rectPressColor;

    ColorRectangle contentRect;
    Var<Color> contentRectColor;
    Var<Color> contentRectPressColor;

    ColorRectangle selectRect;
    Var<Color> selectRectColor;

    Var<Number> width;
    Var<Number> height;

    Monitor pressingMonitor;
    Monitor releasingMonitor;

    public Button(Host host) {
        super(host);

        shown = new AutoState<>(Vars.set(false), Vars.set(false));
        when(shown.formal, this::show, this::hide);
        selected = new AutoState<>(Vars.set(false), Vars.set(false));
        when(selected.formal, this::select, this::unselect);
        clicked = Vars.get();

        rectColor = Vars.set(Color.mix(.15, .15, .25));
        rectPressColor = Vars.set(Color.mix(.2, .2, .25));
        contentRectColor = Vars.set(Color.mix(.15, .15, .35));
        contentRectPressColor = Vars.set(Color.mix(.2, .2, .4));
        selectRectColor = Vars.set(Color.mix(1, .8, .6));

        width = Vars.set(200);
        height = Vars.set(100);
        rect = rect();
        rect.color().set(rectColor.get());

        selectRect = rect();
        selectRect.color().set(selectRectColor.get());
        selectRect.width().let(rect.width().per(w -> w.floatValue() + 4));
        selectRect.height().let(rect.height().per(h -> h.floatValue() + 4));
        selectRect.position().let(rect.position());

        contentRect = rect();
        contentRect.position().let(rect.position());
        contentRect.height().let(rect.height().per(h -> h.floatValue() - 20));
        contentRect.width().let(rect.width().per(w -> w.floatValue() - 20));
        contentRect.color().set(contentRectColor.get());

        pressingMonitor = when(mouse().leftButton().willBe(Mouse.Button::pressed)).then(()-> {
            selected.set(rect.contains(mouse().position()));
        }, false);

        releasingMonitor = when(mouse().leftButton().willBe(Mouse.Button::released)).then(()-> {
            if(rect.contains(mouse().position()) && selected.get()) {
                click();
            }
        }, false);
    }

    AutoState<Boolean> shown;

    @Override
    public void show() {
        if(!shown.inner.get()) {
            pressingMonitor.use();
            releasingMonitor.use();
            show(rect);
            show(contentRect);
            shown.inner.set(true);
        }
    }

    @Override
    public void hide() {
        if(shown.inner.get()) {
            pressingMonitor.cancel();
            releasingMonitor.cancel();
            hide(rect);
            hide(contentRect);
            hide(selectRect);
            shown.inner.set(false);
        }
    }

    public boolean isShown() {
        return shown.get();
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

        if(isShown()) {
            var mouse = mouse();

            boolean mouseIn = rect.contains(mouse.position());
            if (!mouseIn) {
                rect.setWidth(width.get()).setHeight(height.get()).setColor(rectColor.get());
                contentRect.setColor(contentRectColor.get());
            } else {
                boolean leftButtonPressed = mouse.leftButton().isPressed();
                if (!leftButtonPressed) {
                    rect.setWidth(width.get().floatValue() + 8).setHeight(height.get().floatValue() + 8)
                            .setColor(rectColor.get());
                    contentRect.setColor(contentRectColor.get());
                } else {
                    boolean selected = this.selected.get();
                    if (selected) {
                        rect.setWidth(width.get().floatValue() + 4).setHeight(height.get().floatValue() + 4)
                                .setColor(rectPressColor.get());
                        contentRect.setColor(contentRectPressColor.get());
                    } else {
                        rect.setWidth(width.get()).setHeight(height.get()).setColor(rectColor.get());
                        contentRect.setColor(contentRectColor.get());
                    }
                }
            }
        }
    }

    @Override
    public void stop() {

    }

    AutoState<Boolean> selected;

    public void select() {
        if(!selected.inner.get()) {
            show(selectRect, rect);
            selected.inner.set(true);
        }
    }

    public void unselect() {
        if(selected.inner.get()) {
            hide(selectRect);
            selected.inner.set(false);
        }
    }

    public Var<Boolean> selected() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    Var<Number> clicked;
    public void click() {
        clicked.set(System.currentTimeMillis());
    }

    public Source<Number> clicked() {
        return clicked;
    }

    public Var<Number> width() {
        return width;
    }

    public Button setWidth(Number width) {
        this.width.set(width);
        return this;
    }

    public Var<Number> height() {
        return height;
    }

    public Var<Point> position() {
        return rect.position();
    }

    public Button setPosition(Point position) {
        rect.setPosition(position);
        return this;
    }

    public Button setPosition(Number x, Number y) {
        rect.setPosition(x, y);
        return this;
    }

    @Override
    public Var<XOrigin> xOrigin() {
        return rect.xOrigin();
    }

    @Override
    public Var<YOrigin> yOrigin() {
        return rect.yOrigin();
    }
}
