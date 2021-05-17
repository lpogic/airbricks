package airbricks.model;

import bricks.Sized;
import bricks.graphic.Rectangle;
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
        note.aim(this);

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
        boolean mouseIn = hasMouse.get();
        boolean leftButton = mouse.leftButton().isPressed();
        boolean leftButtonPressEvent = false;
        var mEvents = mouse.getEvents();
        for(var e : mEvents.eachAs(Mouse.ButtonEvent.class)) {
            switch (e.button) {
                case LEFT -> {
                    if(e.isPress()) {
                        leftButtonPressEvent = true;
                    }
                }
            }
        }

        if (selected().get()) {
            var keyboard = keyboard();
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
            highlight(false);
            select(!(tabPressEvent || (!mouseIn && leftButtonPressEvent)));
        } else {
            highlight(mouseIn && !leftButton);
            select(leftButtonPressEvent && mouseIn);
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
}
