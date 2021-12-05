package airbricks;

import airbricks.assistance.AssistanceDealer;
import airbricks.assistance.ExclusiveAssistanceDealer;
import airbricks.button.TextButtonBrick;
import airbricks.note.AssistedNoteBrick;
import airbricks.note.NoteBrick;
import airbricks.keyboard.ExclusiveKeyboardDealer;
import airbricks.keyboard.KeyboardClient;
import airbricks.keyboard.KeyboardDealer;
import airbricks.tool.ExclusiveToolDealer;
import airbricks.tool.ToolDealer;
import bricks.Color;
import bricks.Located;
import bricks.input.keyboard.Key;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.input.mouse.MouseButton;
import bricks.slab.BluntLineSlab;
import bricks.slab.CircleSlab;
import bricks.slab.RectangleSlab;
import bricks.slab.TextSlab;
import bricks.var.Pull;
import bricks.var.Push;
import bricks.wall.Brick;
import bricks.wall.MouseClient;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import suite.suite.Sub;
import suite.suite.Subject;
import suite.suite.action.Statement;

import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.*;
import static suite.suite.$uite.$;

public abstract class Wall extends bricks.wall.Wall implements KeyboardClient, MouseClient {

    static Subject $walls = $();

    public static void play(Subject $sub) {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        if ( !glfwInit() ) throw new IllegalStateException("Unable to initialize GLFW");
        Wall wall = Wall.create(
                $sub.in(Wall.class).asExpected(),
                $sub.get("width", "w").in().asInt(800),
                $sub.get("height", "h").in().asInt(600),
                Color.mix(
                        $sub.get("red", "r").in().asFloat(.2f),
                        $sub.get("green", "g").in().asFloat(.5f),
                        $sub.get("blue", "b").in().asFloat(.4f)
                ),
                $sub.get("title", "t").in().asString("New Wall"));
        glfwShowWindow(wall.getGlid());

        glfwSwapInterval(1);

        while($walls.present())
        {
//            float currentFrame = (float)glfwGetTime();
//            deltaTime = currentFrame - lastFrame;
//            lastFrame = currentFrame;

            glfwPollEvents();
            for(Wall win : $walls.eachIn().eachAs(Wall.class)) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                win.update_();
                glfwSwapBuffers(win.getGlid());
                if(glfwWindowShouldClose(win.getGlid())) $walls.unset(win.getGlid());
            }
        }

        glfwTerminate();
    }

    public static Wall create(Wall wall, int width, int height, Color color, String title) {
        wall.setup0(width, height, color, title);

        glfwMakeContextCurrent(wall.getGlid());

        GL.createCapabilities();
        GLUtil.setupDebugMessageCallback();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        wall.setupResources();
        wall.setup1();
        wall.setup();

        long glid = wall.getGlid();

        $walls.aimedPut($walls.first().raw(), glid, wall);

        return wall;
    }

    protected MouseClient.CursorOver cursorOver;
    MouseClient mouseRoot;

    @Override
    protected void setup0(int width, int height, Color color, String title) {
        super.setup0(width, height, color, title);
        this.mouseRoot = this;
        this.cursorOver = CursorOver.NO;
    }

    @Override
    protected void setupResources() {
        super.setupResources();
        $resources
                .put(Wall.class, this)
                .put(bricks.wall.Wall.class, this)
                .put(KeyboardDealer.class, new ExclusiveKeyboardDealer())
                .put(AssistanceDealer.class, new ExclusiveAssistanceDealer(this))
                .put(ToolDealer.class, new ExclusiveToolDealer(this));
    }

    @Override
    protected void update__() {
        mouseRoot.acceptCursor(input.state.mouseCursor());
    }

    @Override
    public void update() {

        super.update();

        for(var e : input.getEvents()) {
            if(e instanceof Mouse.ButtonEvent be) {
                if (be.button == MouseButton.Code.LEFT && be.isPress() && seeCursor(true)) {
                    order(KeyboardDealer.class).requestKeyboard(this);
                }
            } else if(e instanceof Keyboard.KeyEvent ke) {
                if(ke.key == Key.Code.TAB && ke.isPress()) {
                    acceptKeyboard(!ke.isShifted());
                }
            }
        }
    }

    @Override
    public Subject order(Subject trade) {
        var t = trade.one();
        if(t instanceof KeyboardClient.KeyboardTransfer kt) {
            if(transferKeyboard(kt)) return $(true);
            else return $(acceptKeyboard(kt.front()));
        } else if(t instanceof Defaults d) {
            return $resources.in(Defaults.class).in(d.object()).get();
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

    @Override
    public HasKeyboard hasKeyboard() {
        return HasKeyboard.NO;
    }

    @Override
    public void depriveKeyboard() {}

    @Override
    public void requestKeyboard() {
        order(KeyboardDealer.class).requestKeyboard(this);
    }

    protected boolean transferKeyboard(KeyboardClient.KeyboardTransfer transfer) {
        var clients = (transfer.front() ?
                bricks().front(transfer.current()) :
                bricks().reverse(transfer.current()))
                .skip(1)
                .each(KeyboardClient.class);
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
    public CursorOver acceptCursor(Located crd) {
        if(contains(crd)) {
            CursorOver brickCursorOver = CursorOver.NO;
            for (var mo : $bricks.reverse().each(MouseClient.class)) {
                if (brickCursorOver != CursorOver.NO) mo.depriveCursor();
                else brickCursorOver = mo.acceptCursor(crd);
            }
            return cursorOver = brickCursorOver == CursorOver.NO ? CursorOver.DIRECT : CursorOver.INDIRECT;
        } else {
            depriveCursor();
            return CursorOver.NO;
        }
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

    public void trapMouse(MouseClient client) {
        mouseRoot = client;
    }

    public void freeMouse() {
        mouseRoot = this;
    }

    public boolean mouseTrappedBy(Brick<?> brick) {
        return mouseRoot == brick;
    }


    public class Rectangle extends RectangleSlab {

        public Rectangle() {
            this($());
        }

        public Rectangle(Subject data) {
            super(Wall.this);
            setup($().alter(Wall.this.getDefaults(this)).alter(data));
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
            super(Wall.this);
            setup($().alter(Wall.this.getDefaults(this)).alter(data));
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
            super(Wall.this);
            setup($().alter(Wall.this.getDefaults(this)).alter(data));
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
            super(Wall.this);
            setup($().alter(Wall.this.getDefaults(this)).alter(data));
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
            super(Wall.this);
            setup($().alter(Wall.this.getDefaults(this)).alter(data));
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
            super(Wall.this);
            setup($().alter(Wall.this.getDefaults(this)).alter(data));
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
            super(Wall.this);
            setup($().alter(Wall.this.getDefaults(this)).alter(data));
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
