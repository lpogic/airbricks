package airbricks.model.button;

import airbricks.model.Note;
import airbricks.model.PowerBrick;
import airbricks.model.assistance.Assistance;
import airbricks.model.selection.SelectionClient;
import bricks.Color;
import bricks.Coordinated;
import bricks.Sized;
import bricks.graphic.ColorText;
import bricks.input.InputEvent;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Contract;
import bricks.trade.Host;
import bricks.var.Var;

import static suite.suite.$.set$;

public class OptionPowerButton extends PowerBrick<Assistance> implements SelectionClient {

    public static final Contract<Boolean> LIGHT_REQUEST = new Contract<>();

    public final Note note;

    public OptionPowerButton(Assistance host) {
        super(host);

        note = note();
        note.text.color().set(Color.hex("#1d100e0"));
        note.aim(this);

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
