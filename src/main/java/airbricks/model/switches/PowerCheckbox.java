package airbricks.model.switches;

import bricks.trade.Host;
import bricks.var.impulse.State;

import static suite.suite.$uite.set$;

public class PowerCheckbox extends PowerSwitch {

    public final State<Boolean> checked;

    public PowerCheckbox(Host host) {
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
