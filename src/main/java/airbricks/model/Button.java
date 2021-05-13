package airbricks.model;

import bricks.Color;
import bricks.Debug;
import bricks.Sized;
import bricks.graphic.ColorText;
import bricks.graphic.Rectangle;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;

import static suite.suite.$.set$;

public class Button extends InputBase implements Rectangle, Selectable {

    public final ColorText text;

    public Button(Host host) {
        super(host);
        text = text();
        text.color().set(Color.hex("#cdb432"));
        adjust(Sized.relative(text, 40, 20));
        text.aim(this);

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
            wall.lockMouse();
        } else if(leftButtonReleaseEvent && wall.isMouseLocked()) {
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
            highlight(mouseIn && !pressState);
            select(!(tabPressEvent || (!mouseIn && leftButtonPressEvent)));
        } else {
            press(false);
            highlight(mouseIn && !leftButton);
            if(leftButtonPressEvent && mouseIn) {
                select(true);
            }
        }
    }

    public Var<String> string() {
        return text.string();
    }
}
