package airbricks.model.tool;

import airbricks.model.Airbrick;
import airbricks.model.Int;
import airbricks.model.PowerBrick;
import airbricks.model.WithRectangleBody;
import airbricks.model.button.OptionPowerButton;
import airbricks.model.button.SliderPowerButton;
import bricks.Coordinate;
import bricks.Coordinated;
import bricks.graphic.ColorRectangle;
import bricks.graphic.Rectangle;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.Constant;
import bricks.var.impulse.Impulse;
import bricks.var.special.Num;
import bricks.wall.Brick;
import bricks.wall.FantomBrick;
import suite.suite.Subject;
import suite.suite.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static suite.suite.$uite.set$;

public class ToolBrick extends Airbrick<Host> implements WithRectangleBody {

    ColorRectangle bg;
    FantomBrick tools;

    boolean wrapped;

    public ToolBrick(Host host) {
        super(host);

        bg = rect();

        wrapped = false;

        tools = new FantomBrick(this);

        bg.height().let(() -> {
            float sum = 0f;
            for(var b : tools.bricks().eachAs(Brick.class)) {
                sum += b.height().getFloat();
            }
            return sum;
        });

        $bricks.set(tools);
    }

    Var<Int> picked;

    public void pick(int i) {
        picked.set(new Int(i));
    }

    public Var<Int> picked() {
        return picked;
    }

    public void build(Subject $config) {
        tools.bricks().unset();

        OptionPowerButton prevButton = null;
        var c = $config.cascade();
        for(var $ : c) {
            var action = $.in().as(Action.class, Action.identity());
            var button = new OptionPowerButton(this){
                @Override
                public void click() {
                    super.click();
                    action.play();
                }
            };
            button.width().let(bg.width());
            button.x().let(bg.x());
            button.string().set(Objects.toString($.raw()));
            if(c.firstFall()) {
                button.top().let(bg.top());
            } else {
                button.top().let(prevButton.bottom());
            }
            tools.bricks().set(button);
            prevButton = button;
        }
    }

    public void lightFirst() {
        var b = tools.bricks();
        if(b.present()) {
            OptionPowerButton pb = b.asExpected();
            pb.light(requestLight());
        }
    }

    public void lightNext(boolean up_down) {
        boolean lightedFound = false;
        boolean lightedLast = false;
        var bricks = tools.bricks();
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
                if (up_down) {
                    bricks.last().as(OptionPowerButton.class).light();
                }
                else {
                    bricks.first().as(OptionPowerButton.class).light();
                }
            } else if(lightedLast) {
                if (up_down) {
                    bricks.first().as(OptionPowerButton.class).light();
                } else {
                    bricks.last().as(OptionPowerButton.class).light();
                }
            }
        }
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }

    public void attach(Coordinated crd, Subject $config) {
        build($config);
        float crdX = crd.x().getFloat();
        float crdY = crd.y().getFloat();
        float w = width().getFloat();
        float h = height().getFloat();
        float wallR = wall().right().getFloat();
        float wallB = wall().bottom().getFloat();

        if(crdX + w > wallR && crdX - w >= 0) right().let(crd.x());
        else left().let(crd.x());

        if(crdY + h > wallB && crdY - h >= 0) bottom().let(crd.y());
        else top().let(crd.y());
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
                        } else {
                            if(wall.mouseTrappedBy(this)) {
                                wall.freeMouse();
                            }
                            wall.pop(this);
                        }
                    } else if(buttonEvent.isRelease()) {
                        if(wall.mouseTrappedBy(this)) {
                            wall.freeMouse();
                        }
                        wall.pop(this);
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
                    case ESCAPE -> {
                        wall.pop(this);
                    }
                }
            }
        }
    }

    @Override
    public Subject order(Subject trade) {
        if(OptionPowerButton.LIGHT_REQUEST.equals(trade.raw())) {
            return set$(requestLight());
        }
        return super.order(trade);
    }

    public boolean requestLight() {
        for(var b : tools.bricks().eachAs(OptionPowerButton.class)) {
            b.dim();
        }
        return true;
    }
}
