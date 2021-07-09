package airbricks.button;

import airbricks.note.NoteBrick;
import airbricks.PowerBrick;
import airbricks.selection.SelectionClient;
import bricks.Color;
import bricks.Sized;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Contract;
import bricks.trade.Host;
import bricks.var.Var;

public class OptionButtonBrick extends PowerBrick<Host> implements SelectionClient {

    public static final Contract<Boolean> LIGHT_REQUEST = new Contract<>();

    public final NoteBrick note;

    public OptionButtonBrick(Host host) {
        super(host);

        note = new NoteBrick(this) {{
            text.color().set(Color.hex("#1d100e0"));
            aim(OptionButtonBrick.this);
        }};

        adjust(Sized.relative(note, 40, 20));

        outlineThick.set(0);

        $bricks.set(note);
    }

    boolean pressedIn = false;

    @Override
    public void frontUpdate() {

        var input = input();
        boolean mouseIn = mouseIn();
        for(var e : input.getEvents()) {
            if(e instanceof Mouse.PositionEvent positionEvent) {
                if(mouseIn) light(order(LIGHT_REQUEST));
            } else if(e instanceof Mouse.ButtonEvent buttonEvent) {
                if(buttonEvent.button == Mouse.Button.Code.LEFT) {
                    if(buttonEvent.isPress()) {
                        pressedIn = mouseIn;
                    } else if(buttonEvent.isRelease()) {
                        if(pressedIn && mouseIn) click();
                    }
                }
            } else if(e instanceof Keyboard.KeyEvent keyEvent) {
                if(keyEvent.key == Key.Code.ENTER) {
                    if(keyEvent.isPress()) {
                        if(lighted.get()) click();
                    }
                }
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
        return note.string();
    }
}
