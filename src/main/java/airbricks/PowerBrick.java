package airbricks;

import airbricks.selection.KeyboardClient;
import airbricks.selection.KeyboardDealer;
import bricks.input.keyboard.Key;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.MouseButton;
import bricks.slab.Slab;
import bricks.input.mouse.Mouse;
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
    public boolean acceptKeyboard(boolean front) {
        requestKeyboard();
        return true;
    }

    @Override
    protected boolean transferKeyboard(KeyboardTransfer transfer) {
        return false;
    }

    @Override
    public void update() {
        super.update();
        for(var e : input().getEvents()) {
            if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == MouseButton.Code.LEFT) {
                    if(seeCursor()) {
                        if (be.isPress()) {
                            if (!seeKeyboard()) {
                                requestKeyboard();
                            }
                        }
                    }
                }
            } else if(e instanceof Keyboard.KeyEvent ke) {
                if(seeKeyboard()) {
                    if(ke.key == Key.Code.TAB && ke.isPress()) {
                        order(new KeyboardTransfer(this, !ke.isShifted()));
                        ke.suppress();
                    }
                }
            }
        }
    }
}
