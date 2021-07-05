package airbricks;

import airbricks.button.TextButtonBrick;
import airbricks.assistance.AssistanceDealer;
import airbricks.assistance.ExclusiveAssistanceDealer;
import airbricks.intercom.IntercomBrick;
import airbricks.note.NoteBrick;
import airbricks.selection.ExclusiveSelectionDealer;
import airbricks.selection.SelectionClient;
import airbricks.selection.SelectionDealer;
import airbricks.tool.ExclusiveToolDealer;
import airbricks.tool.ToolDealer;
import bricks.Color;
import bricks.input.Mouse;
import bricks.var.Var;
import bricks.var.Vars;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import suite.suite.Subject;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.*;
import static suite.suite.$uite.set$;

public abstract class Wall extends bricks.wall.Wall implements SelectionClient {

    static Subject $walls = set$();

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

    @Override
    protected void setupResources() {
        super.setupResources();
        $resources
                .put(Wall.class, this)
                .put(bricks.wall.Wall.class, this)
                .put(SelectionDealer.class, new ExclusiveSelectionDealer())
                .put(AssistanceDealer.class, new ExclusiveAssistanceDealer(this))
                .put(ToolDealer.class, new ExclusiveToolDealer(this));
    }

    @Override
    public void frontUpdate() {

        boolean mouseIn = mouseIn(true);
        for(var e : input.getEvents().filter(Mouse.ButtonEvent.class)) {
            if(e.button == Mouse.Button.Code.LEFT && e.isPress() && mouseIn) {
                order(SelectionDealer.class).requestSelection(this);
            }
        }
    }

    @Override
    public Var<Boolean> selected() {
        return Vars.set(false);
    }

    @Override
    public void depriveSelection() {}

    @Override
    public void requestSelection() {
        order(SelectionDealer.class).requestSelection(this);
    }

    protected TextButtonBrick button() {
        return new TextButtonBrick(this);
    }

    protected TextButtonBrick button(String text) {
        var button = button();
        button.string().set(text);
        return button;
    }

    protected NoteBrick note() {
        return new NoteBrick(this);
    }

    protected NoteBrick note(String text) {
        var note = note();
        note.string().set(text);
        return note;
    }

    protected IntercomBrick intercom() {
        return new IntercomBrick(this);
    }
}
