package airbricks.intercom;

import airbricks.PowerBrick;
import airbricks.note.NoteBrick;
import airbricks.selection.SelectionClient;
import bricks.Located;
import bricks.Sized;
import bricks.graphic.Rectangle;
import bricks.input.Key;
import bricks.input.Mouse;
import bricks.input.Story;
import bricks.trade.Host;
import bricks.var.Var;
import suite.suite.Subject;

import static suite.suite.$uite.$;

public class IntercomBrick extends PowerBrick<Host> implements Rectangle, SelectionClient {

    protected NoteBrick note;

    private final Story story;

    public IntercomBrick(Host host) {
        super(host);

        note = new NoteBrick(this);
        note.left().let(this.left().plus(10));
        note.y().let(this.y());

        adjust(Sized.relative(note, 20));
        when(selected(), () -> {
            note.select();
            note.updateCursorPosition(true);
        }, note::unselect);

        story = new Story(10);
        $bricks.set(note);
    }

    @Override
    public void frontUpdate() {

        var input = input();
        boolean mouseIn = mouseIn();
        boolean leftButton = input.state.isPressed(Mouse.Button.Code.LEFT);
        boolean leftButtonPressEvent = false;
        boolean leftButtonReleaseEvent = false;
        for(var e : input.getEvents().select(Mouse.ButtonEvent.class)) {
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
    }

    public Subject order(Subject $) {
        if(Story.class.equals($.raw())) {
            return $(story);
        }
        return super.order($);
    }

    public Var<String> string() {
        return note.string();
    }

    public Var<Boolean> editable() {
        return note.editable();
    }

    public NoteBrick getNote() {
        return note;
    }
}
