package airbricks.model;

import bricks.graphic.ColorText;
import bricks.graphic.Rectangle;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.special.Num;

public class TextInput extends Airbrick<Host> implements Rectangle {

    public final ColorText text;
    public final Button button;

    public TextInput(Host host, Button button, ColorText text) {
        super(host);
        this.button = button;
        this.text = text;
        button.adjust(text.margin(40, 20));
        text.aim(button);
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
    public Num width() {
        return button.width();
    }
    @Override
    public Num height() {
        return button.height();
    }
    @Override
    public Num left() {
        return button.left();
    }
    @Override
    public Num right() {
        return button.right();
    }
    @Override
    public Num top() {
        return button.top();
    }
    @Override
    public Num bottom() {
        return button.bottom();
    }
    @Override
    public Num x() {
        return button.x();
    }
    @Override
    public Num y() {
        return button.y();
    }

    public Var<String> string() {
        return text.string();
    }

    public Source<Number> clicked() {
        return button.clicked();
    }
}
