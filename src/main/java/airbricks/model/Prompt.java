package airbricks.model;

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
import bricks.wall.Brick;
import suite.suite.$;
import suite.suite.Subject;
import suite.suite.Suite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static suite.suite.$.set$;

public class Prompt<O> extends Airbrick<Host> implements WithRectangleBody {

    ColorRectangle bg;
    List<O> options;
    int index;
    List<OptionButton> buttons;
    Function<O, String> labelizer;

    public Prompt(Host host) {
        super(host);

        picked = Vars.set(0);

        bg = rect();

        options = new ArrayList<>();
        labelizer = String::valueOf;

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

        $bricks.setEntire(buttons);
    }

    Var<Integer> picked;

    public void pick(int i) {
        picked.set(i);
    }

    public Var<Integer> picked() {
        return picked;
    }

    public void setOptions(Collection<O> newOptions) {
        options = new ArrayList<>(newOptions);
        for(int i = 0;i < buttons.size();++i) {
            buttons.get(i).string().set(String.valueOf(options.get(i)));
        }
    }

    public O getOption(int i) {
        return options.get(i);
    }

    public void attach(Brick<?> brick) {
        bg.top().let(brick.bottom());
        bg.left().let(brick.left());
        bg.width().let(brick.width());
    }

    public void attach(InputBase input) {
        bg.top().let(input.bottom());
        bg.left().let(input.left());
        bg.width().let(input.width());
        input.when(input.selected(), () -> wall().push(this), () -> wall().pop(this));
    }

    @Override
    public Rectangle getBody() {
        return bg;
    }

    @Override
    public void update() {

        var mouse = mouse();
        boolean mouseIn = hasMouse.get();
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
            System.out.println("lock");
        } else if(leftButtonReleaseEvent && wall.mouseLocked()) {
            System.out.println("unlock");
            wall.unlockMouse();
        }

        super.update();
    }
}
