package airbricks.button;

import airbricks.PowerBrick;
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
import bricks.trade.Host;
import bricks.trait.PushVar;
import bricks.trait.Trait;
import bricks.trait.Push;
import bricks.trait.Traits;
import bricks.trait.number.NumberTrait;

public class TextButtonBrick extends PowerBrick<Host> implements WithSlab {

    public boolean pressed;
    public Trait<Long> clicks;

    public final RectangleSlab background;
    public final Trait<Color> backgroundColorDefault;
    public final Trait<Color> backgroundColorSeeCursor;
    public final Trait<Color> backgroundColorPressed;

    public final RectangleSlab outline;
    public final Trait<Color> outlineColorDefault;
    public final Trait<Color> outlineColorSeeKeyboard;

    public final NumberTrait outlineThick;

    public final TextSlab text;

    public TextButtonBrick(Host host) {
        super(host);

        pressed = false;
        clicks = Traits.set(0L);

        backgroundColorDefault = Traits.set(Color.hex("#292B2B"));
        backgroundColorSeeCursor = Traits.set(Color.hex("#212323"));
        backgroundColorPressed = Traits.set(Color.hex("#191B1B"));

        outlineColorDefault = Traits.set(Color.hex("#1e1a2c"));
        outlineColorSeeKeyboard = Traits.set(Color.mix(1, .8, .6));

        outlineThick = Traits.num(4);

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

        text = new TextSlab(this) {{
            color().set(Color.hex("#1d100e0"));
        }};
        text.aim(this);

        adjust(Sized.relative(text, 40, 20));

        $bricks.set(outline, background, text);
    }

    @Override
    public void update() {

        var in = input();
        var wall = wall();

        for(var e : in.getEvents()) {
            if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == MouseButton.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            pressed = true;
                            wall.trapMouse(this);
                        }
                    } else {
                        if(pressed) {
                            if(!(seeKeyboard() && in.state.isPressed(Key.Code.SPACE))) {
                                pressed = false;
                                if(seeCursor()) {
                                    click();
                                }
                            }
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

        super.update();
    }

    public void click() {
        clicks.set(System.currentTimeMillis());
    }

    public Push<Long> clicks() {
        return clicks;
    }

    public Trait<String> text() {
        return text.text();
    }

    @Override
    public Slab getShape() {
        return outline;
    }
}
