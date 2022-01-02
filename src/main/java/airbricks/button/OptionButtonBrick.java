package airbricks.button;

import airbricks.Airbrick;
import bricks.Color;
import bricks.Sized;
import bricks.input.mouse.MouseButton;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.slab.TextSlab;
import bricks.slab.WithSlab;
import bricks.input.keyboard.Key;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.trade.Contract;
import bricks.trade.Host;
import bricks.trait.*;

public class OptionButtonBrick extends Airbrick<Host> implements WithSlab {

    public static final Contract<Boolean> MARK_REQUEST = Contract.emit();

    public boolean pressed;
    public boolean mark;
    public StoredPushVar<Long> clicks;

    public final RectangleSlab background;
    protected final Trait<Color> backgroundColorDefault;
    protected final Trait<Color> backgroundColorIndicated;
    protected final Trait<Color> backgroundColorPressed;

    public final TextSlab textSlab;

    public OptionButtonBrick(Host host) {
        super(host);

        pressed = false;
        mark = false;
        clicks = PushVar.store(0L);

        backgroundColorDefault = Traits.set(Color.hex("#292B2B"));
        backgroundColorIndicated = Traits.set(Color.hex("#212323"));
        backgroundColorPressed = Traits.set(Color.hex("#191B1B"));

        background = new RectangleSlab(this) {{
            color().let(() -> pressed ?
                    backgroundColorPressed.get() : mark ?
                    backgroundColorIndicated.get() :
                    backgroundColorDefault.get());
        }};

        textSlab = new TextSlab(this) {{
            color().set(Color.hex("#1d100e0"));
            y().let(OptionButtonBrick.this.y());
            left().let(OptionButtonBrick.this.left().plus(10));
        }};

        adjust(Sized.relative(textSlab, 40, 20));

        $bricks.set(background, textSlab);
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

    public Trait<String> text() {
        return textSlab.text();
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
