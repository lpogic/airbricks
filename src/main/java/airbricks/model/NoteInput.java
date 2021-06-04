package airbricks.model;

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

public class NoteInput extends InputBase implements Rectangle, Selectable {

    protected Note note;

    private final Story story;

    public NoteInput(Host host) {
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

        var mouse = mouse();
        boolean mouseIn = mouseIn();
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
                selector().select(this);
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
