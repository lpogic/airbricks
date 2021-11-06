package airbricks.button;

import airbricks.Airbrick;
import airbricks.note.NoteBrick;
import bricks.Color;
import bricks.Sized;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Contract;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Var;

public class OptionButtonBrick extends Airbrick<Host> implements WithSlab {

    public static final Contract<Boolean> INDICATE_REQUEST = new Contract<>();

    public boolean pressed;
    public boolean indicated;
    public int click;

    public final RectangleSlab background;
    protected final Pull<Color> backgroundColorDefault;
    protected final Pull<Color> backgroundColorIndicated;
    protected final Pull<Color> backgroundColorPressed;

    public final NoteBrick note;

    public OptionButtonBrick(Host host) {
        super(host);

        pressed = false;
        indicated = false;
        click = 0;

        backgroundColorDefault = Var.pull(Color.hex("#292B2B"));
        backgroundColorIndicated = Var.pull(Color.hex("#212323"));
        backgroundColorPressed = Var.pull(Color.hex("#191B1B"));

        background = new RectangleSlab(this) {{
            color().let(() -> pressed ?
                    backgroundColorPressed.get() : indicated ?
                    backgroundColorIndicated.get() :
                    backgroundColorDefault.get());
        }};

        note = new NoteBrick(this) {{
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
                    if(!indicated) {
                        indicate(order(INDICATE_REQUEST));
                    }
                }
            } else if(e instanceof Keyboard.KeyEvent keyEvent) {
                if(keyEvent.key == Key.Code.ENTER) {
                    if(keyEvent.isPress()) {
                        if(indicated) click();
                    }
                }
            } else if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == Mouse.Button.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            pressed = true;
                        }
                    } else {
                        if(pressed && seeCursor()) {
                            pressed = false;
                            click();
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

    public boolean isIndicated() {
        return indicated;
    }

    public void indicate(boolean should) {
        indicated = should;
    }

    public void click() {
        ++click;
    }

    public int getClicks() {
        return click;
    }

    @Override
    public Slab getShape() {
        return background;
    }
}
