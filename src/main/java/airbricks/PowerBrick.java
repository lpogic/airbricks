package airbricks;

import airbricks.selection.KeyboardClient;
import airbricks.selection.KeyboardDealer;
import bricks.slab.Slab;
import bricks.input.Mouse;
import bricks.trade.Host;


public abstract class PowerBrick<H extends Host> extends Airbrick<H> implements KeyboardClient, Slab {

    public HasKeyboard hasKeyboard;

    public PowerBrick(H host) {
        super(host);
        hasKeyboard = HasKeyboard.NO;
    }

    @Override
    public HasKeyboard hasKeyboard() {
        return hasKeyboard;
    }

    @Override
    public void depriveKeyboard() {
        hasKeyboard = HasKeyboard.NO;
    }

    @Override
    public void requestKeyboard() {
        var keyboardDealer = order(KeyboardDealer.class);
        hasKeyboard = keyboardDealer.requestKeyboard(this);
    }

    @Override
    public void update() {
        super.update();
        for(var e : input().getEvents()) {
            if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == Mouse.Button.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            if(!seeKeyboard()) {
                                requestKeyboard();
                            }
                        }
                    }
                }
            }
        }
    }
}
