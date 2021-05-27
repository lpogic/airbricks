package airbricks.model.button;

import airbricks.model.InputBase;
import airbricks.model.Selectable;
import bricks.Color;
import bricks.Sized;
import bricks.graphic.ColorText;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;

import static suite.suite.$.set$;

public class TextButton extends InputBase implements Selectable {

    public final ColorText text;

    public TextButton(Host host) {
        super(host);

        text = text();
        text.color().set(Color.hex("#104bf1"));
        text.aim(this);

        adjust(Sized.relative(text, 40, 20));

        $bricks.set(text);
    }

    public void update() {
        super.update();

        var mouse = mouse();
        boolean mouseIn = hasMouse.get();
        boolean leftButton = mouse.leftButton().isPressed();
        boolean leftButtonPressEvent = false;
        boolean leftButtonReleaseEvent = false;
        var mEvents = mouse.getEvents();
        for(var e : mEvents.eachAs(Mouse.ButtonEvent.class)) {
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
            wall.lockMouse(this);
        } else if(leftButtonReleaseEvent && wall.mouseLocked()) {
            wall.unlockMouse();
        }

        if (selected().get()) {
            var keyboard = keyboard();
            if(mouseIn) mouseIn = contains(mouse.position());
            boolean space = keyboard.key(Key.Code.SPACE).isPressed();
            boolean pressState = space || (mouseIn && leftButton);
            boolean tabPressEvent = false;
            boolean spaceReleaseEvent = false;
            var kEvents = keyboard.getEvents();
            for(var e : kEvents.eachAs(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case TAB -> {
                        if(e.isHold()) {
                            kEvents.unset(e);
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
            if(leftButtonPressEvent && mouseIn) {
                select(true);
            }
        }
    }

    public Var<String> string() {
        return text.string();
    }
}
