package airbricks.model.switches;

import airbricks.model.PowerBrick;
import airbricks.model.selection.SelectionClient;
import bricks.Color;
import bricks.Coordinated;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;

import static suite.suite.$uite.set$;

public class PowerSwitch extends PowerBrick<Host> implements SelectionClient {

    public final ColorRectangle stateBox;
    public final ColorText text;
    public final ColorText stateRune;

    public PowerSwitch(Host host) {
        super(host);

        stateBox = rect();
        stateBox.color().set(Color.hex("#393B3B"));
        stateBox.y().let(y());
        stateBox.height().let(height().perFloat(h -> h - 14));
        stateBox.width().let(stateBox.height());
        stateBox.left().let(left().perFloat(l -> l + 7));

        text = text();
        text.y().let(y());
        text.left().let(stateBox.right().perFloat(r -> r + 5));
        height().let(text.height().perFloat(h -> h + 20));
        width().let(() -> {
            var textWidth = text.width().getFloat();
            return textWidth > 0 ? height().getFloat() + textWidth + 10 : height().getFloat();
        });

        stateRune = text();
        stateRune.color().set(Color.hex("#1d100e0"));
        stateRune.aim(stateBox);

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
            if(mouseIn) mouseIn = contains(Coordinated.of(input.state.mouseCursorX(), input.state.mouseCursorY()));
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
