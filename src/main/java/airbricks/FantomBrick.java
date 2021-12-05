package airbricks;

import airbricks.button.TextButtonBrick;
import airbricks.note.AssistedNoteBrick;
import airbricks.note.NoteBrick;
import airbricks.keyboard.KeyboardClient;
import bricks.Color;
import bricks.Located;
import bricks.slab.BluntLineSlab;
import bricks.slab.CircleSlab;
import bricks.slab.RectangleSlab;
import bricks.slab.TextSlab;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Push;
import bricks.wall.Brick;
import bricks.wall.MouseClient;
import suite.suite.Sub;
import suite.suite.Subject;
import suite.suite.action.Statement;

import java.util.function.Supplier;

import static suite.suite.$uite.$;

public abstract class FantomBrick<H extends Host> extends Brick<H> implements MouseClient, KeyboardClient {

    protected CursorOver cursorOver;
    protected Subject $resources;

    public FantomBrick(H host) {
        super(host);
        cursorOver = CursorOver.NO;
        $resources = $();
    }

    @Override
    public CursorOver acceptCursor(Located crd) {
        CursorOver brickCursorOver = CursorOver.NO;
        for (var mo : $bricks.reverse().list().each(MouseClient.class)) {
            if (brickCursorOver != CursorOver.NO) mo.depriveCursor();
            else brickCursorOver = mo.acceptCursor(crd);
        }
        return cursorOver = brickCursorOver == CursorOver.NO ? CursorOver.NO : CursorOver.INDIRECT;
    }

    @Override
    public void depriveCursor() {
        for(var mc : $bricks.list().each(MouseClient.class)) {
            mc.depriveCursor();
        }
        cursorOver = CursorOver.NO;
    }

    @Override
    public CursorOver cursorOver() {
        return cursorOver;
    }

    @Override
    public Subject order(Subject trade) {
        var t = trade.one();
        if(t instanceof KeyboardClient.KeyboardTransfer kt) {
            if(transferKeyboard(kt)) return $(true);
            else return super.order($(new KeyboardClient.KeyboardTransfer(this, kt.front())));
        } else if(t instanceof Defaults d) {
            return getHost().order($(d)).alter($resources.in(Defaults.class).in(d.object()));
        }
        return super.order(trade);
    }

    public Subject getDefaults(Object o) {
        var defaults = order($(new Defaults(o)));
        for(Class<?> c = o.getClass();!Object.class.equals(c);c = c.getSuperclass()) {
            defaults = order($(new Defaults(c))).alter(defaults);
        }
        return defaults;
    }

    public Sub defaults() {
        return $resources.in(Defaults.class);
    }

    protected boolean transferKeyboard(KeyboardClient.KeyboardTransfer transfer) {
        var clients = (transfer.front() ?
                bricks().front(transfer.current()) :
                bricks().reverse(transfer.current()))
                .each(KeyboardClient.class)
                .cascade();
        clients.next();
        for(var c : clients) {
            if(c.acceptKeyboard(transfer.front())) return true;
        }
        return false;
    }

    @Override
    public boolean acceptKeyboard(boolean front) {
        var clients = (front ?
                bricks().front() :
                bricks().reverse())
                .each(KeyboardClient.class);
        for(var c : clients) {
            if(c.acceptKeyboard(front)) return true;
        }
        return false;
    }

    @Override
    public void depriveKeyboard() {}

    @Override
    public void requestKeyboard() {}

    @Override
    public HasKeyboard hasKeyboard() {
        return HasKeyboard.NO;
    }

    public class Rectangle extends RectangleSlab {

        public Rectangle() {
            this($());
        }

        public Rectangle(Subject data) {
            super(FantomBrick.this);
            setup($().alter(FantomBrick.this.getDefaults(this)).alter(data));
        }

        public void setup(Subject data) {
            for(var d : data) {
                if(d.is(String.class)) {
                    switch (d.asString()) {
                        case "x" -> pullNumber(x(), d.in());
                        case "y" -> pullNumber(y(), d.in());
                        case "aim" -> aim(d.in().one());
                        case "w", "width" -> pullNumber(width(), d.in());
                        case "h", "height" -> pullNumber(height(), d.in());
                        case "color" -> pullColor(color(), d.in());
                    }
                }
            }
        }
    }

    public class Circle extends CircleSlab {

        public Circle() {
            this($());
        }

        public Circle(Subject data) {
            super(FantomBrick.this);
            setup($().alter(FantomBrick.this.getDefaults(this)).alter(data));
        }

        public void setup(Subject data) {
            for(var d : data) {
                if(d.is(String.class)) {
                    switch (d.asString()) {
                        case "x" -> pullNumber(x(), d.in());
                        case "y" -> pullNumber(y(), d.in());
                        case "aim" -> aim(d.in().one());
                        case "r", "radius" -> pullNumber(radius(), d.in());
                        case "color" -> pullColor(color(), d.in());
                    }
                }
            }
        }
    }

    public class Line extends BluntLineSlab {

        public Line() {
            this($());
        }

        public Line(Subject data) {
            super(FantomBrick.this);
            setup($().alter(FantomBrick.this.getDefaults(this)).alter(data));
        }

        public void setup(Subject data) {
            for(var d : data) {
                if(d.is(String.class)) {
                    switch (d.asString()) {
                        case "x0" -> pullNumber(begin().x(), d.in());
                        case "y0" -> pullNumber(begin().y(), d.in());
                        case "aim0" -> begin().aim(d.in().one());
                        case "x1" -> pullNumber(end().x(), d.in());
                        case "y1" -> pullNumber(end().y(), d.in());
                        case "aim1" -> end().aim(d.in().one());
                        case "thick" -> pullNumber(thick(), d.in());
                        case "color" -> pullColor(color(), d.in());
                    }
                }
            }
        }
    }

    public class Text extends TextSlab {

        public Text() {
            this($());
        }

        public Text(Subject data) {
            super(FantomBrick.this);
            setup($().alter(FantomBrick.this.getDefaults(this)).alter(data));
        }

        public void setup(Subject data) {
            for(var d : data) {
                if(d.is(String.class)) {
                    switch (d.asString()) {
                        case "text" -> pullString(text(), d.in());
                        case "x" -> pullNumber(x(), d.in());
                        case "y" -> pullNumber(y(), d.in());
                        case "aim" -> aim(d.in().one());
                        case "height" -> pullNumber(height(), d.in());
                        case "color" -> pullColor(color(), d.in());
                    }
                }
            }
        }
    }

    public class Button extends TextButtonBrick {

        public Button() {
            this($());
        }

        public Button(Subject data) {
            super(FantomBrick.this);
            setup($().alter(FantomBrick.this.getDefaults(this)).alter(data));
        }

        public void setup(Subject data) {
            for(var d : data) {
                if(d.is(String.class)) {
                    switch (d.asString()) {
                        case "text" -> pullString(text(), d.in());
                        case "textColor" -> pullColor(text.color(), d.in());
                        case "x" -> pullNumber(x(), d.in());
                        case "y" -> pullNumber(y(), d.in());
                        case "aim" -> aim(d.in().one());
                        case "w", "width" -> pullNumber(width(), d.in());
                        case "background" -> pullColor(backgroundColorDefault, d.in());
                        case "backgroundSeeCursor" -> pullColor(backgroundColorSeeCursor, d.in());
                        case "backgroundPressed" -> pullColor(backgroundColorPressed, d.in());
                        case "outlineColor" -> pullColor(outlineColorDefault, d.in());
                        case "outlineColorSeeKeyboard" -> pullColor(outlineColorSeeKeyboard, d.in());
                        case "outlineThick" -> pullNumber(outlineThick, d.in());
                        case "onClick" -> {
                            if(d.in().is(Statement.class)) clicks().act(d.in().as(Statement.class));
                        }
                    }
                }
            }
        }

        public void onClick(Statement st) {
            clicks().act(st);
        }
    }

    public class Note extends NoteBrick {

        public Note() {
            this($());
        }

        public Note(Subject data) {
            super(FantomBrick.this);
            setup($().alter(FantomBrick.this.getDefaults(this)).alter(data));
        }

        public void setup(Subject data) {
            for(var d : data) {
                if(d.is(String.class)) {
                    switch (d.asString()) {
                        case "text" -> pushString(text(), d.in());
                        case "textColor" -> pullColor(note.textSlab.color(), d.in());
                        case "x" -> pullNumber(x(), d.in());
                        case "y" -> pullNumber(y(), d.in());
                        case "aim" -> aim(d.in().one());
                        case "w", "width" -> pullNumber(width(), d.in());
                        case "background" -> pullColor(backgroundColorDefault, d.in());
                        case "backgroundSeeCursor" -> pullColor(backgroundColorSeeCursor, d.in());
                        case "backgroundPressed" -> pullColor(backgroundColorPressed, d.in());
                        case "outlineColor" -> pullColor(outlineColorDefault, d.in());
                        case "outlineColorSeeKeyboard" -> pullColor(outlineColorSeeKeyboard, d.in());
                        case "outlineThick" -> pullNumber(outlineThick, d.in());
                    }
                }
            }
        }
    }

    public class AssistedNote extends AssistedNoteBrick {

        public AssistedNote() {
            this($());
        }

        public AssistedNote(Subject data) {
            super(FantomBrick.this);
            setup($().alter(FantomBrick.this.getDefaults(this)).alter(data));
        }

        public void setup(Subject data) {
            for(var d : data) {
                if(d.is(String.class)) {
                    switch (d.asString()) {
                        case "advices" -> advices().unset().alter(d.in().get());
                        case "x" -> pullNumber(x(), d.in());
                        case "y" -> pullNumber(y(), d.in());
                        case "aim" -> aim(d.in().one());
                        case "w", "width" -> pullNumber(width(), d.in());
                        case "background" -> pullColor(backgroundColorDefault, d.in());
                        case "backgroundSeeCursor" -> pullColor(backgroundColorSeeCursor, d.in());
                        case "backgroundPressed" -> pullColor(backgroundColorPressed, d.in());
                        case "outlineColor" -> pullColor(outlineColorDefault, d.in());
                        case "outlineColorSeeKeyboard" -> pullColor(outlineColorSeeKeyboard, d.in());
                        case "outlineThick" -> pullNumber(outlineThick, d.in());
                    }
                }
            }
        }
    }

    private static void pullNumber(Pull<Number> p, Sub v) {
        if(v.is(Supplier.class)) p.let(v.one());
        else if(v.is(Number.class)) p.set(v.one());
    }

    private static void pullString(Pull<String> p, Sub v) {
        if(v.is(Supplier.class)) p.let(v.one());
        else if(v.is(String.class)) p.set(v.one());
    }

    private static void pushString(Push<String> p, Sub v) {
        if(v.is(String.class)) p.set(v.one());
    }

    private static void pullColor(Pull<Color> p, Sub v) {
        if(v.is(Supplier.class)) p.let(v.one());
        else if(v.is(Color.class)) p.set(v.one());
        else if(v.is(String.class)) p.set(Color.hex(v.asString()));
    }
}
