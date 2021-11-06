package airbricks.switches;

import airbricks.PowerBrick;
import bricks.Color;
import bricks.Sized;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.slab.TextSlab;
import bricks.slab.WithSlab;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Var;
import bricks.var.special.NumPull;

public class SwitchBrick extends PowerBrick<Host> implements WithSlab {

    public boolean pressed;
    public int click;

    public final RectangleSlab background;
    public final Pull<Color> backgroundColorDefault;
    public final Pull<Color> backgroundColorSeeCursor;
    public final Pull<Color> backgroundColorPressed;

    public final RectangleSlab outline;
    public final Pull<Color> outlineColorDefault;
    public final Pull<Color> outlineColorSeeKeyboard;

    public final NumPull outlineThick;

    public final RectangleSlab stateBox;
    public final TextSlab text;
    public final TextSlab stateRune;

    public SwitchBrick(Host host) {
        super(host);

        pressed = false;
        click = 0;

        backgroundColorDefault = Var.pull(Color.hex("#292B2B"));
        backgroundColorSeeCursor = Var.pull(Color.hex("#212323"));
        backgroundColorPressed = Var.pull(Color.hex("#191B1B"));

        outlineColorDefault = Var.pull(Color.hex("#1e1a2c"));
        outlineColorSeeKeyboard = Var.pull(Color.mix(1, .8, .6));

        outlineThick = Var.num(4);

        outline = new RectangleSlab(this) {{
            color().let(() -> seeKeyboard() ?
                    outlineColorSeeKeyboard.get() :
                    outlineColorDefault.get());
        }};

        background = new RectangleSlab(this) {{
            color().let(() -> pressed ?
                    backgroundColorPressed.get() : seeCursor() ?
                    backgroundColorSeeCursor.get() :
                    backgroundColorDefault.get());
            aim(outline);
            adjust(Sized.relative(outline, outlineThick.perFloat(t -> -t)));
        }};

        stateBox = new RectangleSlab(this) {{
            color().set(Color.hex("#393B3B"));
        }};
        stateBox.y().let(y());
        stateBox.height().let(height().perFloat(h -> h - 14));
        stateBox.width().let(stateBox.height());
        stateBox.left().let(left().perFloat(l -> l + 7));

        text = new TextSlab(this);
        text.y().let(y());
        text.left().let(stateBox.right().plus(5));
        height().let(text.height().plus(20));
        width().let(() -> {
            var textWidth = text.width().getFloat();
            return textWidth > 0 ? height().getFloat() + textWidth + 10 : height().getFloat();
        });

        stateRune = new TextSlab(this) {{
            color().set(Color.hex("#1d100e0"));
            aim(stateBox);
        }};

        $bricks.set(outline, background, text, stateBox, stateRune);
    }

    @Override
    public void update() {

        var in = input();
        var wall = wall();

        for(var e : in.getEvents()) {
            if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == Mouse.Button.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            pressed = true;
                            wall.trapMouse(this);
                        } else {
                            click = 0;
                        }
                    } else {
                        if(pressed && seeCursor() && (!seeKeyboard() || !in.state.isPressed(Key.Code.SPACE))) {
                            pressed = false;
                            click();
                        }
                        if(wall.mouseTrappedBy(this)) {
                            wall.freeMouse();
                        }
                    }
                }
            } else if(e instanceof Keyboard.KeyEvent ke) {
                if(seeKeyboard()) {
                    if (ke.key == Key.Code.SPACE) {
                        if (ke.isPress()) {
                            pressed = true;
                        } else if(ke.isRelease()) {
                            if(pressed && !wall.mouseTrappedBy(this)){
                                pressed = false;
                                click();
                            }
                        }
                    }
                }
            }
        }
    }

    public void click() {
        ++click;
    }

    public int getClicks() {
        return click;
    }

    public Pull<String> text() {
        return text.text();
    }

    @Override
    public Slab getShape() {
        return outline;
    }
}
