package airbricks.switches;

import airbricks.PowerBrick;
import airbricks.selection.SelectionClient;
import bricks.Color;
import bricks.Located;
import bricks.graphic.RectangleBrick;
import bricks.graphic.TextBrick;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;
import bricks.var.special.Num;

import static suite.suite.$uite.set$;

public class SwitchBrick extends PowerBrick<Host> implements SelectionClient {

    public final RectangleBrick stateBox;
    public final TextBrick text;
    public final TextBrick stateRune;

    public SwitchBrick(Host host) {
        super(host);

        stateBox = new RectangleBrick(this) {{
            color().set(Color.hex("#393B3B"));
        }};
        stateBox.y().let(y());
        stateBox.height().let(height().perFloat(h -> h - 14));
        stateBox.width().let(stateBox.height());
        stateBox.left().let(left().perFloat(l -> l + 7));

        text = new TextBrick(this);
        text.y().let(y());
        text.left().let(Num.sum(stateBox.right(), +5));
        height().let(Num.sum(text.height(), +20));
        width().let(() -> {
            var textWidth = text.width().getFloat();
            return textWidth > 0 ? height().getFloat() + textWidth + 10 : height().getFloat();
        });

        stateRune = new TextBrick(this) {{
            color().set(Color.hex("#1d100e0"));
            aim(stateBox);
        }};

        $bricks.set(text, stateBox, stateRune);
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
                                order(set$("selectPrev", this));
                            } else {
                                order(set$("selectNext", this));
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
