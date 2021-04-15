package airbricks.model;

import bricks.graphic.Rectangle;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.special.Num;

public class NoteInput extends Airbrick<Host> implements Rectangle {

    public Note note;
    public Button button;

    public NoteInput(Host host, Button button, Note note) {
        super(host);
        this.note = note;
        this.button = button;
        button.adjust(note.margin(20));
        note.aim(button);
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

    public Var<Boolean> selected() {
        return button.selected();
    }

    public Var<String> string() {
        return note.string();
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
}
