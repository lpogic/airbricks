package airbricks.note;

import airbricks.PowerBrick;
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
import bricks.input.Story;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Push;
import bricks.var.Var;
import bricks.var.num.NumPull;
import suite.suite.Subject;

import static suite.suite.$uite.$;

public class NoteBrick extends PowerBrick<Host> implements WithSlab {

    public boolean pressed;
    public Push<Long> clicks;

    public final RectangleSlab background;
    public final Pull<Color> backgroundColorDefault;
    public final Pull<Color> backgroundColorSeeCursor;
    public final Pull<Color> backgroundColorPressed;

    public final RectangleSlab outline;
    public final Pull<Color> outlineColorDefault;
    public final Pull<Color> outlineColorSeeKeyboard;

    public final NumPull outlineThick;

    protected TextBrick note;
    private final Story story;

    public NoteBrick(Host host) {
        super(host);

        pressed = false;
        clicks = Var.push(0L);

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

        note = new TextBrick(this);
        note.left().let(this.left().plus(10));
        note.y().let(this.y());

        adjust(Sized.relative(note, 20));
        when(this::seeKeyboard, () -> {
            note.hasKeyboard = HasKeyboard.SHARED;
            note.showCursor();
            note.updateCursorPosition(true);
        }, () -> {
            note.hasKeyboard = HasKeyboard.NO;
            note.hideCursor();
        });

        story = new Story(10);
        $bricks.set(outline, background, note);
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

    public Subject order(Subject $) {
        if(Story.class.equals($.raw())) {
            return $(story);
        }
        return super.order($);
    }

    public void click() {
        clicks.set(System.currentTimeMillis());
    }

    public Push<Long> clicks() {
        return clicks;
    }

    public Pull<String> text() {
        return note.text();
    }

    public boolean isEditable() {
        return note.isEditable();
    }

    public void editable(boolean should) {
        note.editable(should);
    }

    public TextBrick getNote() {
        return note;
    }

    @Override
    public Slab getShape() {
        return outline;
    }
}
