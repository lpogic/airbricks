package airbricks.note;

import airbricks.PowerBrick;
import airbricks.text.ArticleBrick;
import bricks.Color;
import bricks.Sized;
import bricks.input.Story;
import bricks.input.keyboard.Key;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.input.mouse.MouseButton;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import bricks.trade.Host;
import bricks.trait.*;
import bricks.trait.number.NumberTrait;
import suite.suite.Subject;

import static suite.suite.$uite.$;

public class EssayBrick extends PowerBrick<Host> implements WithSlab {

    public boolean pressed;
    public Trait<Long> clicks;

    public final RectangleSlab background;
    public final Trait<Color> backgroundColorDefault;
    public final Trait<Color> backgroundColorSeeCursor;
    public final Trait<Color> backgroundColorPressed;

    public final RectangleSlab outline;
    public final Trait<Color> outlineColorDefault;
    public final Trait<Color> outlineColorSeeKeyboard;

    public final RectangleSlab cursorLine;

    public final NumberTrait outlineThick;

    protected ArticleBrick article;
    private final Story story;

    public EssayBrick(Host host) {
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
            color().let(() -> pressed || seeKeyboard() ?
                    backgroundColorPressed.get() : seeCursor() ?
                    backgroundColorSeeCursor.get() :
                    backgroundColorDefault.get());
            aim(outline);
            adjust(Sized.relative(outline, outlineThick.perFloat(t -> -t)));
        }};

        cursorLine = new RectangleSlab(this);
        cursorLine.color().set(Color.hex("#212121"));
        cursorLine.x().let(x());
        cursorLine.width().let(background.width());

        article = new ArticleBrick(this){
            @Override
            public CursorOver cursorOver() {
                var co = super.cursorOver();
                if(co == CursorOver.DIRECT) return CursorOver.DIRECT;
                return EssayBrick.this.cursorOver();
            }

            @Override
            public void showCursor() {
                super.showCursor();
                EssayBrick.this.bricks().aimedSet(article, cursorLine);
            }

            @Override
            public void hideCursor() {
                super.hideCursor();
                EssayBrick.this.drop(cursorLine);
            }
        };
        article.left().let(this.left().plus(10));
        article.top().let(this.top().plus(10));
        article.adjust(Sized.relative(outline, -10));

        cursorLine.height().let(article.cursor.height());
        cursorLine.y().let(article.cursor.y());

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

    public Var<String> text() {
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
