package airbricks.model;

import bricks.Color;
import bricks.Point;
import bricks.XOrigin;
import bricks.YOrigin;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.input.Mouse;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Brick;
import suite.suite.action.Statement;

public class Button extends Brick implements Positioned<Button> {

    ColorRectangle rect;
    ColorText text;

    ColorRectangle selectRect;
    ColorRectangle textRect;
    Var<Color> textRectColor;

    Var<Color> rectColor;
    Var<Color> rectPressColor;
    Var<Color> textRectPressColor;
    Var<Color> selectColor;
    Var<Boolean> selected;
    Var<Number> width;
    Var<Number> height;

    Monitor pressingMonitor;
    Monitor releasingMonitor;

    public Button(Host host) {
        super(host);

        rectColor = Vars.set(Color.mix(.15, .15, .25));
        rectPressColor = Vars.set(Color.mix(.2, .2, .25));
        textRectColor = Vars.set(Color.mix(.15, .15, .35));
        textRectPressColor = Vars.set(Color.mix(.2, .2, .4));
        selectColor = Vars.set(Color.mix(1, .8, .6));
        selected = Vars.set(false);

        text = text().setText("Click me!").setSize(20).setColor(Color.mix(1,1,1))
                .setOrigin(XOrigin.CENTER, YOrigin.CENTER);
        width = Vars.let(text.width().per(Number::floatValue).per(w -> w + 50));
        height = Vars.let(text.size().per(s -> s.floatValue() + 40));
        rect = rect().setOrigin(XOrigin.CENTER, YOrigin.CENTER).setSize(200,50);
        rect.color().set(rectColor.get());
        selectRect = rect().setOrigin(XOrigin.CENTER, YOrigin.CENTER);
        selectRect.color().set(selectColor.get());
        selectRect.width().let(rect.width(), x -> x.floatValue() + 4);
        selectRect.height().let(rect.height(), x -> x.floatValue() + 4);
        selectRect.position().let(rect.position());

        textRect = rect().setOrigin(XOrigin.CENTER, YOrigin.CENTER);
        textRect.position().let(rect.position());
        textRect.height().let(rect.height().per(h -> h.floatValue() - 20));
        textRect.width().let(rect.width().per(w -> w.floatValue() - 20));
        textRect.color().set(textRectColor.get());
        text.position().let(rect.position());

        rect.setPosition(400, 300);

        pressingMonitor = when(mouse().leftButton().willGive(Mouse.Button::pressing)).then(()-> {
            if(rect.contains(mouse().position())) {
                select();
            } else {
                unselect();
            }
        }, false);

        releasingMonitor = when(mouse().leftButton().willGive(Mouse.Button::releasing)).then(()-> {
            if(rect.contains(mouse().position()) && selected.get()) {
                click();
            }
        }, false);
    }

    Var<Boolean> shown = Vars.set(false);
    @Override
    public void show() {
        shown.set(true);
        pressingMonitor.use();
        releasingMonitor.use();
        show(rect);
        show(textRect);
        show(text);
    }

    @Override
    public void hide() {
        pressingMonitor.cancel();
        releasingMonitor.cancel();
        hide(rect);
        hide(text);
        shown.set(false);
    }

    @Override
    public void update() {
        super.update();
        var mouse = mouse();

        boolean mouseIn = rect.contains(mouse.position());
        if(!mouseIn) {
            rect.setWidth(width.get()).setHeight(height.get()).setColor(rectColor.get());
            textRect.setColor(textRectColor.get());
        } else {
            boolean leftButtonPressed = mouse.leftButton().isPressed();
            if(!leftButtonPressed) {
                rect.setWidth(width.get().floatValue() + 8).setHeight(height.get().floatValue() + 8)
                        .setColor(rectColor.get());
                textRect.setColor(textRectColor.get());
            } else {
                boolean selected = this.selected.get();
                if(selected) {
                    rect.setWidth(width.get().floatValue() + 4).setHeight(height.get().floatValue() + 4)
                            .setColor(rectPressColor.get());
                    textRect.setColor(textRectPressColor.get());
                } else {
                    rect.setWidth(width.get()).setHeight(height.get()).setColor(rectColor.get());
                    textRect.setColor(textRectColor.get());
                }
            }
        }
    }

    public void select() {
        selected.set(true);
        show(selectRect, rect);
    }

    public void unselect() {
        selected.set(false);
        hide(selectRect);
    }

    public Source<Boolean> selected() {
        return selected;
    }

    Statement click = () -> {};
    public void click() {
        click.play();
    }

    public void click(Statement whenClick) {
        click = whenClick;
    }

    public boolean isShown() {
        return shown.get();
    }

    public Var<Number> width() {
        return width;
    }

    public Var<Number> height() {
        return height;
    }

    public Var<String> label() {
        return text.text();
    }

    @Override
    public Button setPosition(Point position) {
        rect.setPosition(position);
        return this;
    }

    @Override
    public Point getPosition() {
        return rect.getPosition();
    }

    public Var<Point> position() {
        return rect.position();
    }

    public Button setWidth(Number width) {
        this.width.let(() -> width);
        return this;
    }

    public Button setNote(String note) {
        this.text.setText(note);
        return this;
    }
}
