package airbricks.model.prompt;

import airbricks.model.Airbrick;
import airbricks.model.InputBase;
import airbricks.model.Int;
import airbricks.model.WithRectangleBody;
import airbricks.model.button.OptionButton;
import airbricks.model.button.TextButton;
import bricks.Color;
import bricks.graphic.ColorRectangle;
import bricks.graphic.Rectangle;
import bricks.graphic.Rectangular;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.State;
import bricks.var.special.NumSource;
import bricks.wall.Brick;
import bricks.wall.FantomBrick;
import suite.suite.$;
import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.action.Statement;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static suite.suite.$.set$;

public class Prompt extends Airbrick<Host> implements WithRectangleBody {

    ColorRectangle bg;
    List<OptionButton> buttons;
    FantomBrick buttonsBrick;
    List<String> options;
    int offset;

    public Prompt(Host host) {
        super(host);

        picked = Vars.get();

        bg = rect();

        buttons = new ArrayList<>();

        OptionButton prevButton = null;
        for(int i = 0;i < 6; ++i) {
            var button = new OptionButton(this);
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
        buttons.get(i).select();
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

    public void attach(InputBase input, List<String> options) {
        attach(input);
        setOptions(options);
        resetOffset();
        updateButtons();
        Statement push = () -> wall().push(this);
        input.when(input.selected(), push, () -> wall().pop(this));
        input.when(input.clicked(), push);
        input.when(keyboard().key(Key.Code.DOWN).willBe(Key.Event::isPress), () -> wall().push(this));
    }

    @Override
    public Rectangle getBody() {
        return bg;
    }

    @Override
    public void update() {

        var mouse = mouse();
        boolean mouseIn = mouseIn();
        boolean leftButtonPressEvent = false;
        boolean leftButtonReleaseEvent = false;
        var mEvents = mouse.getEvents();
        for(var e : mEvents.eachAs(Mouse.ButtonEvent.class)) {
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
