package airbricks.button;

import airbricks.PowerBrick;
import airbricks.selection.SelectionClient;
import bricks.Color;
import bricks.Located;
import bricks.Sized;
import bricks.graphic.TextBrick;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;

import static suite.suite.$uite.$;

public class TextButtonBrick extends PowerBrick<Host> implements SelectionClient {

    public final TextBrick text;

    public TextButtonBrick(Host host) {
        super(host);

        text = new TextBrick(this) {{
            color().set(Color.hex("#1d100e0"));
        }};
        text.aim(this);

        adjust(Sized.relative(text, 40, 20));

        $bricks.set(text);
    }

    @Override
    public void frontUpdate() {

        var input = input();
        boolean mouseIn = mouseIn();
        boolean leftButton = input.state.isPressed(Mouse.Button.Code.LEFT);
        boolean leftButtonPressEvent = false;
        boolean leftButtonReleaseEvent = false;
        for(var e : input.getEvents().filter(Mouse.ButtonEvent.class)) {
            switch (e.button) {
                case LEFT -> {
                    if(e.isPress()) {
                        leftButtonPressEvent = true;
                    }
                    if(e.isRelease()) {
                        leftButtonReleaseEvent = true;
                    }
                }
            }
        }

        var wall = wall();
        if(leftButtonPressEvent && mouseIn) {
            wall.trapMouse(this);
        } else if(leftButtonReleaseEvent && wall.mouseTrappedBy(this)) {
            wall.freeMouse();
        }

        if (selected().get()) {
            if(mouseIn) mouseIn = contains(Located.of(input.state.mouseCursorX(), input.state.mouseCursorY()));
            boolean space = input.state.isPressed(Key.Code.SPACE);
            boolean pressState = space || (mouseIn && leftButton);
            boolean tabPressEvent = false;
            boolean spaceReleaseEvent = false;
            for(var e : input.getEvents().filter(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case TAB -> {
                        if(e.isHold()) {
                            if(e.isShifted()) {
                                order($("selectPrev", this));
                            } else {
                                order($("selectNext", this));
                            }
                            tabPressEvent = true;
                        }
                    }
                    case SPACE -> {
                        if(e.isRelease()) {
                            spaceReleaseEvent = true;
                        }
                    }
                }
            }
            if(!pressState && (spaceReleaseEvent || (mouseIn && leftButtonReleaseEvent))) {
                click();
            }
            press(pressState);
            light(mouseIn && !pressState);
            select(!(tabPressEvent || (!mouseIn && leftButtonPressEvent)));
        } else {
            press(false);
            light(mouseIn && !leftButton);
            if(leftButtonPressEvent && hasMouse.get() == HasMouse.DIRECT) {
                select(true);
            }
        }
    }

    public Var<String> string() {
        return text.string();
    }
}
