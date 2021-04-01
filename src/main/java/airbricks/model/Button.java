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


        shown = Vars.set(false);
        when(shown, this::_show, this::_hide);
        selected = Vars.set(false);
        when(selected, this::_select, this::_unselect);
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

    Var<Boolean> shown;
    protected void _show() {
        pressingMonitor.use();
        releasingMonitor.use();
        show(rect);
        show(contentRect);
    }

    protected void _hide() {
        pressingMonitor.cancel();
        releasingMonitor.cancel();
        hide(rect);
        hide(contentRect);
    }
    @Override
    public void show() {
        shown.set(true);
    }

    @Override
    public void hide() {
        shown.set(false);
    }

    public boolean isShown() {
        return shown.get();
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

    Var<Boolean> selected;
    protected void _select() {
        show(selectRect, rect);
    }

    protected void _unselect() {
        hide(selectRect);
    }
    public void select() {
        selected.set(true);
    }

    public void unselect() {
        selected.set(false);
    }

    public Source<Boolean> selected() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    Var<Number> clicked;

    public void click() {
        clicked.set(System.currentTimeMillis());
    }

    public Var<Number> clicked() {
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
    public Source<XOrigin> xOrigin() {
        return () -> XOrigin.CENTER;
    }

    @Override
    public Source<YOrigin> yOrigin() {
        return () -> YOrigin.CENTER;
    }
}
