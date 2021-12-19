package airbricks.note;

import airbricks.PowerBrick;
import airbricks.text.ArticleBrick;
import airbricks.text.TextBrick;
import bricks.Color;
import bricks.Sized;
import bricks.input.Story;
import bricks.input.keyboard.Key;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.input.mouse.MouseButton;
import bricks.slab.GradientSlab;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Push;
import bricks.var.Var;
import bricks.var.num.NumPull;
import suite.suite.Subject;

import static suite.suite.$uite.$;

public class EssayBrick extends PowerBrick<Host> implements WithSlab {

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

    protected ArticleBrick article;
    private final Story story;

    public EssayBrick(Host host) {
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
            color().let(() -> pressed || seeKeyboard() ?
                    backgroundColorPressed.get() : seeCursor() ?
                    backgroundColorSeeCursor.get() :
                    backgroundColorDefault.get());
            aim(outline);
            adjust(Sized.relative(outline, outlineThick.perFloat(t -> -t)));
        }};

        article = new ArticleBrick(this){
            @Override
            public CursorOver cursorOver() {
                var co = super.cursorOver();
                if(co == CursorOver.DIRECT) return CursorOver.DIRECT;
                return EssayBrick.this.cursorOver();
            }
        };
        article.left().let(this.left().plus(10));
        article.top().let(this.top().plus(10));
        article.adjust(Sized.relative(outline, -10));

        story = new Story(10);
        $bricks.set(outline, background, article);
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

    @Override
    public void requestKeyboard() {
        super.requestKeyboard();
        article.hasKeyboard = HasKeyboard.SHARED;
        article.showCursor();
    }

    @Override
    public void depriveKeyboard() {
        super.depriveKeyboard();
        article.hasKeyboard = HasKeyboard.NO;
        article.hideCursor();
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

    public Push<String> text() {
        return article.text();
    }

    public boolean isEditable() {
        return article.isEditable();
    }

    public void editable(boolean should) {
        article.editable(should);
    }

    public ArticleBrick getArticle() {
        return article;
    }

    @Override
    public Slab getShape() {
        return outline;
    }
}
