package airbricks.switches;

import bricks.trade.Host;
import bricks.var.impulse.State;

public class CheckboxBrick extends SwitchBrick {

    public final State<Boolean> checked;

    public CheckboxBrick(Host host) {
        super(host);

        checked = state(false, this::check);
    }

    public void check(boolean check) {
        if(checked.get() != check) {
            if(check) {
                stateRune.string().set("x");
            } else {
                stateRune.string().set("");
            }
            checked.setState(check);
        }
    }

    @Override
    public void click() {
        check(!checked.get());
        super.click();
    }
}
