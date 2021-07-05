package airbricks.button;

import airbricks.PowerBrick;
import airbricks.selection.SelectionClient;
import bricks.Color;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.var.special.Num;
import bricks.wall.Brick;

public class SliderButtonBrick extends PowerBrick<Brick<?>> implements SelectionClient {

    public SliderButtonBrick(Brick<?> host) {
        super(host);

        outlineThick.set(3);
        outlineColor.set(Color.hex("#0000"));
        backgroundColor.set(Color.hex("#4b735d88"));
        backgroundLightColor.set(Color.hex("#4b735d"));
        backgroundPressColor.set(Color.hex("#4b735d"));
    }

    double pressOffsetY;

    @Override
    public void frontUpdate() {

        var input = input();
        boolean mouseIn = mouseIn();
        for(var e : input.getEvents()) {
            if(e instanceof Mouse.PositionEvent positionEvent) {
                light(mouseIn);
                if(pressed().get()) {
                    var h2 = height().getFloat() / 2;
                    y().set(Num.trim(positionEvent.y - pressOffsetY,
                            host.top().getFloat() + h2,
                            host.bottom().getFloat() - h2));
                }
            } else if(e instanceof Mouse.ButtonEvent buttonEvent) {
                if(buttonEvent.button == Mouse.Button.Code.LEFT) {
                    if(buttonEvent.isPress()) {
                        if(mouseIn) {
                            wall().trapMouse(this);
                            press();
                            pressOffsetY = buttonEvent.state.mouseCursorY() - y().getFloat();
                        }
                    } else if(buttonEvent.isRelease()) {
                        if(pressed().get()) {
                            wall().freeMouse();
                            if(mouseIn) click();
                            press(false);
                        }
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
}
