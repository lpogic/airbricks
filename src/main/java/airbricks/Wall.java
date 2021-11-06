package airbricks;

import airbricks.assistance.AssistanceDealer;
import airbricks.assistance.ExclusiveAssistanceDealer;
import airbricks.button.TextButtonBrick;
import airbricks.intercom.AssistedIntercomBrick;
import airbricks.intercom.IntercomBrick;
import airbricks.selection.ExclusiveKeyboardDealer;
import airbricks.selection.KeyboardClient;
import airbricks.selection.KeyboardDealer;
import airbricks.tool.ExclusiveToolDealer;
import airbricks.tool.ToolDealer;
import bricks.Color;
import bricks.Located;
import bricks.input.Mouse;
import bricks.slab.BluntLineSlab;
import bricks.slab.CircleSlab;
import bricks.slab.RectangleSlab;
import bricks.slab.TextSlab;
import bricks.wall.Brick;
import bricks.wall.MouseClient;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import suite.suite.Subject;
import suite.suite.action.Statement;

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

        for(var e : input.getEvents().select(Mouse.ButtonEvent.class)) {
            if(e.button == Mouse.Button.Code.LEFT && e.isPress() && seeCursor(true)) {
                order(KeyboardDealer.class).requestKeyboard(this);
            }
        }
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

    @Override
    public CursorOver acceptCursor(Located crd) {
        if(contains(crd)) {
            CursorOver brickCursorOver = CursorOver.NO;
            for (var mo : $bricks.reverse().selectAs(MouseClient.class)) {
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
        for(var mc : $bricks.list().selectAs(MouseClient.class)) {
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
            super(Wall.this);
        }
    }

    public class Circle extends CircleSlab {

        public Circle() {
            super(Wall.this);
        }
    }

    public class Line extends BluntLineSlab {

        public Line() {
            super(Wall.this);
        }
    }

    public class Text extends TextSlab {

        public Text() {
            super(Wall.this);
        }
    }

    public class Button extends TextButtonBrick {

        public Button() {
            super(Wall.this);
        }

        public void onClick(Statement st) {
            when(this::getClicks, (a, b) -> a < b, st);
        }
    }

    public class Note extends IntercomBrick {

        public Note() {
            super(Wall.this);
        }
    }

    public class AssistedNote extends AssistedIntercomBrick {

        public AssistedNote() {
            super(Wall.this);
        }
    }
}
