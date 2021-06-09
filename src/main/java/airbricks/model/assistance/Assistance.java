package airbricks.model.assistance;

import airbricks.model.Airbrick;
import airbricks.model.PowerBrick;
import airbricks.model.Int;
import airbricks.model.WithRectangleBody;
import airbricks.model.button.OptionPowerButton;
import bricks.graphic.ColorRectangle;
import bricks.graphic.Rectangle;
import bricks.input.Key;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Brick;
import bricks.wall.FantomBrick;
import suite.suite.Subject;

import java.util.ArrayList;
import java.util.List;

import static suite.suite.$.set$;

public class Assistance extends Airbrick<Host> implements WithRectangleBody {

    ColorRectangle bg;
    List<OptionPowerButton> buttons;
    FantomBrick buttonsBrick;
    List<String> options;
    int offset;

    boolean wrapped;

    public Assistance(Host host) {
        super(host);

        picked = Vars.get();

        bg = rect();

        wrapped = false;

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

    public void lightFirst() {
        var b = buttonsBrick.bricks();
        if(b.present()) {
            OptionPowerButton pb = b.asExpected();
            pb.light(requestLight(pb));
        }
    }

    public void lightNext(boolean up_down) {
        boolean lightedFound = false;
        boolean lightedLast = false;
        var bricks = buttonsBrick.bricks();
        var it = up_down ? bricks.reverse() : bricks.front();
        for(var button : it.eachAs(OptionPowerButton.class)) {
            if(lightedFound) {
                button.light(lightedLast);
                lightedLast = false;
            } else {
                lightedLast = lightedFound = button.lighted().get();
                button.dim();
            }
        }
        if(bricks.present()) {
            if (!lightedFound || (lightedLast && wrapped)) {
                if (up_down) bricks.last().as(OptionPowerButton.class).light();
                else bricks.first().as(OptionPowerButton.class).light();
            } else if(lightedLast) {
                if (up_down) bricks.first().as(OptionPowerButton.class).light();
                else bricks.last().as(OptionPowerButton.class).light();
            }
        }
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
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

    public void attach(PowerBrick<?> input, List<String> options) {
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
    public void frontUpdate() {

        var input = input();
        var wall = wall();

        for(var e : input.getEvents()) {
            if(e instanceof Mouse.ButtonEvent buttonEvent) {
                if(buttonEvent.button == Mouse.Button.Code.LEFT) {
                    if(buttonEvent.isPress()) {
                        boolean mouseIn = mouseIn();
                        if(mouseIn) {
                            wall.trapMouse(this);
                        }
                    } else if(buttonEvent.isRelease()) {
                        if(wall.mouseTrappedBy(this)) {
                            wall.freeMouse();
                        }
                    }
                }
            } else if(e instanceof Keyboard.KeyEvent keyEvent) {
                switch (keyEvent.key) {
                    case DOWN -> {
                        if (keyEvent.isHold()) {
                            lightNext(false);
                            suppressEvent(e);
                        }
                    }
                    case UP -> {
                        if (keyEvent.isHold()) {
                            lightNext(true);
                            suppressEvent(e);
                        }
                    }
                }
            }
        }
    }

    public boolean requestLight(OptionPowerButton optionPowerButton) {
        for(var b : buttons) {
            b.dim();
        }
        return true;
    }
}
