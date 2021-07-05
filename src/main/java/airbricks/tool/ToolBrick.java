package airbricks.tool;

import airbricks.Airbrick;
import airbricks.Int;
import bricks.graphic.WithRectangleBody;
import airbricks.button.OptionButtonBrick;
import bricks.Color;
import bricks.Located;
import bricks.graphic.RectangleBrick;
import bricks.graphic.Rectangle;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.trade.Host;
import bricks.var.Var;
import bricks.var.special.Num;
import bricks.wall.Brick;
import bricks.wall.FantomBrick;
import suite.suite.Subject;
import suite.suite.action.Action;

import java.util.Objects;

import static suite.suite.$uite.set$;

public class ToolBrick extends Airbrick<Host> implements WithRectangleBody {

    RectangleBrick bg;
    FantomBrick tools;

    boolean wrapped;

    public ToolBrick(Host host) {
        super(host);

        tools = new FantomBrick(this);

        bg = new RectangleBrick(this){{
            color().set(Color.BLACK);
            height().let(() -> {
                float sum = 6f;
                for(var b : tools.bricks().eachAs(Brick.class)) {
                    sum += b.height().getFloat();
                }
                return sum;
            });
        }};

        wrapped = true;

        $bricks.set(bg, tools);
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

        OptionButtonBrick prevButton = null;
        var c = $config.cascade();
        for(var $ : c) {
            var action = $.in().as(Action.class, Action.identity());
            var button = new OptionButtonBrick(this){
                @Override
                public void click() {
                    super.click();
                    action.play();
                }
            };
            button.width().let(Num.sum(bg.width(), -5));
            button.x().let(bg.x());
            button.string().set(Objects.toString($.raw()));
            if(c.firstFall()) {
                button.top().let(Num.sum(bg.top(), 3));
            } else {
                button.top().let(prevButton.bottom());
            }
            tools.bricks().set(button);
            prevButton = button;
        }
        float width = 100;
        for (var opb : tools.bricks().eachAs(OptionButtonBrick.class)) {
            width = Float.max(width, opb.note.text.width().getFloat() + 20);
        }
        bg.width().set(width);
    }

    public void lightFirst() {
        var b = tools.bricks();
        if(b.present()) {
            OptionButtonBrick pb = b.asExpected();
            pb.light(order(OptionButtonBrick.LIGHT_REQUEST));
        }
    }

    public void lightNext(boolean up_down) {
        boolean lightedFound = false;
        boolean lightedLast = false;
        var bricks = tools.bricks();
        var it = up_down ? bricks.reverse() : bricks.front();
        for(var button : it.eachAs(OptionButtonBrick.class)) {
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
                    bricks.last().as(OptionButtonBrick.class).light();
                }
                else {
                    bricks.first().as(OptionButtonBrick.class).light();
                }
            } else if(lightedLast) {
                if (up_down) {
                    bricks.first().as(OptionButtonBrick.class).light();
                } else {
                    bricks.last().as(OptionButtonBrick.class).light();
                }
            }
            optionLighted = true;
        }
        optionLighted = false;
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }

    public void attach(Located crd, Subject $config) {
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
        boolean mouseIn = mouseIn();

        if(optionLighted && !mouseIn) {
            dimOptions();
        }

        for(var e : input.getEvents()) {
            if(e instanceof Mouse.ButtonEvent buttonEvent) {
                if(buttonEvent.button == Mouse.Button.Code.LEFT) {
                    if(buttonEvent.isPress()) {
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
                            keyEvent.suppress();
                        }
                    }
                    case UP -> {
                        if (keyEvent.isHold()) {
                            lightNext(true);
                            keyEvent.suppress();
                        }
                    }
                    case ESCAPE -> {
                        if(keyEvent.isRelease()) {
                            wall.pop(this);
                            keyEvent.suppress();
                        }
                    }
                }
            }
        }
    }

    boolean optionLighted;

    @Override
    public Subject order(Subject trade) {
        if(OptionButtonBrick.LIGHT_REQUEST.equals(trade.raw())) {
            dimOptions();
            return set$(optionLighted = true);
        }
        return super.order(trade);
    }

    public void dimOptions() {
        for(var b : tools.bricks().eachAs(OptionButtonBrick.class)) {
            b.dim();
        }
        optionLighted = false;
    }
}
