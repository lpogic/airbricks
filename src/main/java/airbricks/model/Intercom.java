package airbricks.model;

import airbricks.model.selection.SelectionClient;
import bricks.Coordinated;
import bricks.Sized;
import bricks.graphic.Rectangle;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.input.Story;
import bricks.trade.Host;
import bricks.var.Var;
import suite.suite.Subject;

import static suite.suite.$.set$;

public class Intercom extends PowerBrick implements Rectangle, SelectionClient {

    protected Note note;

    private final Story story;

    public Intercom(Host host) {
        super(host);

        note = note();
        note.left().let(this.left().perFloat(l -> l + 10));
        note.y().let(this.y());

        adjust(Sized.relative(note, 20));
        when(selected(), () -> {
            note.select();
            note.updateCursorPosition(true);
        }, note::unselect);

        story = new Story(10);
        $bricks.set(note);
    }

    public void update() {

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

        var wall = wall();
        if(leftButtonPressEvent && mouseIn) {
            wall.lockMouse(this);
        } else if(leftButtonReleaseEvent && wall.mouseLocked()) {
            wall.unlockMouse();
        }

        if (selected().get()) {
            if(mouseIn) mouseIn = contains(Coordinated.of(input.state.mouseCursorX(), input.state.mouseCursorY()));
            boolean space = input.state.isPressed(Key.Code.SPACE);
            boolean pressState = space || (mouseIn && leftButton);
            for(var e : input.getEvents().selectAs(Keyboard.KeyEvent.class)) {
                switch (e.key) {
                    case TAB -> {
                        if(e.isHold()) {
                            if(e.isShifted()) {
                                order(set$("selectPrev", this));
                            } else {
                                order(set$("selectNext", this));
                            }
                        }
                    }
                }
            }
            if(!pressState && (mouseIn && leftButtonReleaseEvent)) {
                click();
            }
            press(pressState);
            light(mouseIn && !pressState);
        } else {
            press(false);
            light(mouseIn && !leftButton);
            if(leftButtonPressEvent && mouseIn()) {
                select();
            }
        }

        super.update();
    }

    public Subject order(Subject $) {
        if(Story.class.equals($.raw())) {
            return set$(story);
        }
        return super.order($);
    }

    public Var<String> string() {
        return note.string();
    }

    public Var<Boolean> editable() {
        return note.editable();
    }

    public Note getNote() {
        return note;
    }
}
