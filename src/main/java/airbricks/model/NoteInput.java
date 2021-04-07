package airbricks.model;

import bricks.Point;
import bricks.XOrigin;
import bricks.YOrigin;
import bricks.graphic.Rectangular;
import bricks.trade.Host;
import bricks.var.Var;

public class NoteInput extends Airbrick<Host> implements Rectangular {

    public final Note note;
    public final Button button;

    public NoteInput(Host host, Button button, Note note) {
        super(host);
        this.note = note;
        this.button = button;
        button.height().let(note.text().height().per(h -> h.floatValue() + 20));
        button.width().let(note.text().width().per(w -> w.floatValue() + 20));
        note.position().let(button.position());
        note.selected().let(button.selected());
    }

    public void show() {
        show(button);
        show(note);
    }

    public void hide() {
        hide(button);
        hide(note);
    }

    public void move() {
        move(button);
        move(note);
    }

    public void stop() {
        stop(button);
        stop(note);
    }

    public void update() {
        super.update();
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
        return note.string();
    }

    public Var<Boolean> selected() {
        return button.selected();
    }
}
