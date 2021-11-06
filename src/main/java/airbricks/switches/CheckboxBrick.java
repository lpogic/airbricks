package airbricks.switches;

import bricks.trade.Host;

public class CheckboxBrick extends SwitchBrick {

    public boolean checked;

    public CheckboxBrick(Host host) {
        super(host);

        checked = false;
    }

    public void check(boolean check) {
        if(check) {
            stateRune.text().set("x");
        } else {
            stateRune.text().set("");
        }
        checked = check;
    }

    public boolean isChecked() {
        return checked;
    }

    public void click() {
        check(!checked);
        super.click();
    }
}
