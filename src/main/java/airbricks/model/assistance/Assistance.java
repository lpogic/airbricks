package airbricks.model.assistance;

import airbricks.model.Airbrick;
import airbricks.model.PowerBrick;
import airbricks.model.Int;
import airbricks.model.WithRectangleBody;
import airbricks.model.button.OptionPowerButton;
import bricks.graphic.ColorRectangle;
import bricks.graphic.Rectangle;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Brick;
import bricks.wall.FantomBrick;

import java.util.ArrayList;
import java.util.List;

import static suite.suite.$.set$;

public class Assistance extends Airbrick<Host> implements WithRectangleBody {

    ColorRectangle bg;
    List<OptionPowerButton> buttons;
    FantomBrick buttonsBrick;
    List<String> options;
    int offset;

    public Assistance(Host host) {
        super(host);

        picked = Vars.get();

        bg = rect();

        buttons = new ArrayList<>();

        OptionPowerButton prevButton = null;
        for(int i = 0;i < 6; ++i) {
            var button = new OptionPowerButton(this);
            button.width().let(bg.width());
            button.x().let(bg.x());
            if(i == 0) {
                button.top().let(bg.top());
            } else {
                button.top().let(prevButton.bottom());
            }
            int finalI = i;
            when(button.clicked()).then(() -> pick(finalI));
            buttons.add(button);
            prevButton = button;
        }
        bg.height().let(() -> buttons.stream().map(ob -> ob.height().getFloat()).reduce(0f, Float::sum));

        buttonsBrick = new FantomBrick(this);
        $bricks.set(buttonsBrick);
    }

    Var<Int> picked;

    public void pick(int i) {
        picked.set(new Int(i));
    }

    public Var<Int> picked() {
        return picked;
    }

    public void select(int i) {
        buttons.get(i).light();
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    void resetOffset() {
        offset = 0;
    }

    void updateButtons() {
        for(int i = 0;i < buttons.size();++i) {
            var button = buttons.get(i);
            if(options.size() > i + offset) {
                button.string().set(options.get(i));
                buttonsBrick.bricks().set(button);
            } else {
                buttonsBrick.bricks().unset(button);
            }
        }
    }

    public void attach(Brick<?> brick) {
        bg.top().let(brick.bottom());
        bg.left().let(brick.left());
        bg.width().let(brick.width());
    }

    public void attach(PowerBrick input, List<String> options) {
        attach(input);
        setOptions(options);
        resetOffset();
        updateButtons();
    }

    @Override
    public Rectangle getBody() {
        return bg;
    }

    @Override
    public void update() {

        var input = input();
        boolean mouseIn = mouseIn();
        boolean leftButtonPressEvent = false;
        boolean leftButtonReleaseEvent = false;
        for(var e : input.getEvents().selectAs(Mouse.ButtonEvent.class)) {
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
            wall.lockMouse(this);
        } else if(leftButtonReleaseEvent && wall.mouseLocked()) {
            wall.unlockMouse();
        }

        super.update();
    }
}
