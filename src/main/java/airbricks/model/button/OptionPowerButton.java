package airbricks.model.button;

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
import bricks.trade.Host;
import bricks.var.Var;

import static suite.suite.$.set$;

public class OptionPowerButton extends PowerBrick<Assistance> implements SelectionClient {

    public final ColorText text;

    public OptionPowerButton(Assistance host) {
        super(host);

        text = text();
        text.color().set(Color.hex("#1d100e0"));
        text.aim(this);

        adjust(Sized.relative(text, 40, 20));

        outlineThick.set(0);

        $bricks.set(text);
    }

    boolean pressedIn = false;

    @Override
    public void frontUpdate() {

        var input = input();
        boolean mouseIn = mouseIn();
        for(var e : input.getEvents()) {
            if(e instanceof Mouse.PositionEvent positionEvent) {
                if(mouseIn) light(host.requestLight(this));
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
        return text.string();
    }
}
