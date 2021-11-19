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
import bricks.var.Pull;
import bricks.var.Var;
import bricks.var.num.NumPull;

public class TextButtonBrick extends PowerBrick<Host> implements WithSlab {

//    public static final Contract<Supplier<Color>> BACKGROUND_COLOR = new Contract<>();
//    public static final Contract<Supplier<Color>> SEE_CURSOR_BACKGROUND_COLOR = new Contract<>();
//    public static final Contract<Supplier<Color>> PRESSED_BACKGROUND_COLOR = new Contract<>();
//    public static final Contract<Supplier<Color>> OUTLINE_COLOR = new Contract<>();
//    public static final Contract<Supplier<Color>> SEE_KEYBOARD_OUTLINE_COLOR = new Contract<>();


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

    public final TextSlab text;

    public TextButtonBrick(Host host) {
        super(host);

        pressed = false;
        click = 0;

//        backgroundColorDefault = Vars.let(order(BACKGROUND_COLOR));
//        backgroundColorSeeCursor = Vars.let(order(SEE_CURSOR_BACKGROUND_COLOR));
//        backgroundColorPressed = Vars.let(order(PRESSED_BACKGROUND_COLOR));


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

        super.update();
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
