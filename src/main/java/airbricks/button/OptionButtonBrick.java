package airbricks.button;

import airbricks.Airbrick;
import airbricks.text.TextBrick;
import bricks.Color;
import bricks.Sized;
import bricks.input.mouse.MouseButton;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import bricks.input.keyboard.Key;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.trade.Contract;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Push;
import bricks.var.Var;

public class OptionButtonBrick extends Airbrick<Host> implements WithSlab {

    public static final Contract<Boolean> MARK_REQUEST = Contract.emit();

    public boolean pressed;
    public boolean mark;
    public Push<Long> clicks;

    public final RectangleSlab background;
    protected final Pull<Color> backgroundColorDefault;
    protected final Pull<Color> backgroundColorIndicated;
    protected final Pull<Color> backgroundColorPressed;

    public final TextBrick note;

    public OptionButtonBrick(Host host) {
        super(host);

        pressed = false;
        mark = false;
        clicks = Var.push(0L);

        backgroundColorDefault = Var.pull(Color.hex("#292B2B"));
        backgroundColorIndicated = Var.pull(Color.hex("#212323"));
        backgroundColorPressed = Var.pull(Color.hex("#191B1B"));

        background = new RectangleSlab(this) {{
            color().let(() -> pressed ?
                    backgroundColorPressed.get() : mark ?
                    backgroundColorIndicated.get() :
                    backgroundColorDefault.get());
        }};

        note = new TextBrick(this) {{
            text.color().set(Color.hex("#1d100e0"));
            y().let(OptionButtonBrick.this.y());
            left().let(OptionButtonBrick.this.left().plus(10));
        }};

        adjust(Sized.relative(note, 40, 20));

        $bricks.set(background, note);
    }

    @Override
    public void update() {


        var in = input();
        var wall = wall();

        for(var e : in.getEvents()) {
            if(e instanceof Mouse.PositionEvent) {
                if(seeCursor()) {
                    if(!mark) {
                        mark(order(MARK_REQUEST));
                    }
                }
            } else if(e instanceof Keyboard.KeyEvent keyEvent) {
                if(keyEvent.key == Key.Code.ENTER) {
                    if(keyEvent.isPress()) {
                        if(mark) click();
                    }
                }
            } else if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == MouseButton.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            pressed = true;
                        }
                    } else {
                        if(pressed) {
                            pressed = false;
                            if(seeCursor()) {
                                click();
                            }
                        }
                    }
                }
            }
        }

        super.update();
    }

    public Pull<String> text() {
        return note.text();
    }

    public boolean isMarked() {
        return mark;
    }

    public void mark(boolean should) {
        mark = should;
    }

    public void click() {
        clicks.set(System.currentTimeMillis());
    }

    public Push<Long> clicks() {
        return clicks;
    }

    @Override
    public Slab getShape() {
        return background;
    }
}
