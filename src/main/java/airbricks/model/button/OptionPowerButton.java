package airbricks.model.button;

import airbricks.model.PowerBrick;
import airbricks.model.selection.SelectionClient;
import bricks.Color;
import bricks.Coordinated;
import bricks.Sized;
import bricks.graphic.ColorText;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;

import static suite.suite.$.set$;

public class OptionPowerButton extends PowerBrick implements SelectionClient {

    public final ColorText text;

    public OptionPowerButton(Host host) {
        super(host);

        text = text();
        text.color().set(Color.hex("#1d100e0"));
        text.aim(this);

        adjust(Sized.relative(text, 40, 20));

        outlineThick.set(0);

        $bricks.set(text);
    }

    @Override
    public void update() {
        super.update();

        var input = input();
        boolean mouseIn = mouseIn();
        boolean leftButton = input.state.isPressed(Mouse.Button.Code.LEFT);
        boolean leftButtonPressEvent = false;
        boolean leftButtonReleaseEvent = false;
        for(var e : input.getEvents().selectAs(Mouse.ButtonEvent.class)) {
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

        if (selected().get()) {
            if(mouseIn) mouseIn = contains(Coordinated.of(input.state.mouseCursorX(), input.state.mouseCursorY()));
            boolean space = input.state.isPressed(Key.Code.SPACE);
            boolean pressState = space || (mouseIn && leftButton);
            boolean tabPressEvent = false;
            boolean spaceReleaseEvent = false;
            for(var e : input.getEvents().selectAs(Keyboard.KeyEvent.class)) {
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
            if(leftButton && hasMouse.get() == HasMouse.DIRECT) {
                select(true);
            }
        }
    }

    @Override
    public void select(boolean state) {
        if(state != selected.get()) {
            selected.setState(state);
            updateState();
        }
    }

    public Var<String> string() {
        return text.string();
    }
}
