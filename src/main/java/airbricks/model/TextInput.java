package airbricks.model;

import bricks.Point;
import bricks.XOrigin;
import bricks.YOrigin;
import bricks.graphic.ColorText;
import bricks.graphic.Rectangular;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;

public class TextInput extends Airbrick<Host> implements Rectangular {

    public final ColorText text;
    public final Button button;

    public TextInput(Host host, Button button, ColorText text) {
        super(host);
        this.button = button;
        this.text = text;
        button.height().let(text.height().per(h -> h.floatValue() + 20));
        button.width().let(text.width().per(w -> w.floatValue() + 40));

        text.position().let(button.position());
    }

    @Override
    public void show() {
        show(button);
        show(text);
//        show(button);
    }

    @Override
    public void hide() {
        hide(button);
        hide(text);
    }

    @Override
    public void move() {
        move(button);
        move(text);
    }

    @Override
    public void stop() {
        stop(button);
        stop(text);
    }

    @Override
    public Var<Point> position() {
        return button.position();
    }

    @Override
    public Var<XOrigin> xOrigin() {
        return button.xOrigin();
    }

    @Override
    public Var<YOrigin> yOrigin() {
        return button.yOrigin();
    }

    @Override
    public Var<Number> width() {
        return button.width;
    }

    @Override
    public Var<Number> height() {
        return button.height;
    }

    public Var<String> string() {
        return text.string();
    }

    public Source<Number> clicked() {
        return button.clicked();
    }
}
