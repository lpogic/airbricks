package airbricks.tool;

import airbricks.Airbrick;
import airbricks.Int;
import bricks.input.mouse.MouseButton;
import bricks.slab.Shape;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import airbricks.button.OptionButtonBrick;
import bricks.Color;
import bricks.Located;
import bricks.slab.RectangleSlab;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.trade.Host;
import bricks.var.Pull;
import airbricks.FantomBrick;
import suite.suite.Subject;
import suite.suite.action.Action;

import java.util.Objects;

import static suite.suite.$uite.$;

public class ToolBrick extends Airbrick<Host> implements WithSlab {

    RectangleSlab bg;
    FantomBrick<Host> tools;

    boolean wrapped;

    Pull<Int> picked;

    boolean optionIndicated;

    public ToolBrick(Host host) {
        super(host);

        tools = new FantomBrick<>(this){};

        bg = new RectangleSlab(this){{
            color().set(Color.BLACK);
            height().let(() -> {
                float sum = 6f;
                for(var b : tools.bricks().eachAs(Shape.class)) {
                    sum += b.height().getFloat();
                }
                return sum;
            });
        }};

        wrapped = true;

        $bricks.set(bg, tools);
    }

    public void pick(int i) {
        picked.set(new Int(i));
    }

    public Pull<Int> picked() {
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
            button.width().let(bg.width().plus(-5));
            button.x().let(bg.x());
            button.text().set(Objects.toString($.raw()));
            if(c.opening()) {
                button.top().let(bg.top().plus(3));
            } else {
                button.top().let(prevButton.bottom());
            }
            tools.bricks().set(button);
            prevButton = button;
        }
        float width = 100;
        for (var opb : tools.bricks().eachAs(OptionButtonBrick.class)) {
            width = Float.max(width, opb.textSlab.width().getFloat() + 20);
        }
        bg.width().set(width);
    }

    public void indicateFirst() {
        var b = tools.bricks();
        if(b.present()) {
            OptionButtonBrick pb = b.asExpected();
            pb.mark(order(OptionButtonBrick.MARK_REQUEST));
        }
    }

    public void indicateNext(boolean up_down) {
        boolean indicatedFound = false;
        boolean indicatedLast = false;
        var bricks = tools.bricks();
        var it = up_down ? bricks.reverse() : bricks.front();
        for(var button : it.eachAs(OptionButtonBrick.class)) {
            if(indicatedFound) {
                button.mark(indicatedLast);
                indicatedLast = false;
            } else {
                indicatedLast = indicatedFound = button.isMarked();
                button.mark(false);
            }
        }
        if(bricks.present()) {
            if (!indicatedFound || (indicatedLast && wrapped)) {
                if (up_down) {
                    bricks.last().as(OptionButtonBrick.class).mark(true);
                }
                else {
                    bricks.first().as(OptionButtonBrick.class).mark(true);
                }
            } else if(indicatedLast) {
                if (up_down) {
                    bricks.first().as(OptionButtonBrick.class).mark(true);
                } else {
                    bricks.last().as(OptionButtonBrick.class).mark(true);
                }
            }
            optionIndicated = true;
        }
        optionIndicated = false;
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
    public Slab getShape() {
        return bg;
    }

    @Override
    public void update() {

        var input = input();
        var wall = wall();
        boolean mouseIn = seeCursor();

        if(optionIndicated && !mouseIn) {
            dimOptions();
        }

        for(var e : input.getEvents()) {
            if(e instanceof Mouse.ButtonEvent buttonEvent) {
                if(buttonEvent.button == MouseButton.Code.LEFT) {
                    if(buttonEvent.isPress()) {
                        if(mouseIn) {
                            wall.trapMouse(this);
                        } else {
                            if(wall.mouseTrappedBy(this)) {
                                wall.freeMouse();
                            }
                            order(ToolDealer.class).deprive(this);
                        }
                    } else if(buttonEvent.isRelease()) {
                        if(wall.mouseTrappedBy(this)) {
                            wall.freeMouse();
                        }
                        order(ToolDealer.class).deprive(this);
                    }
                }
            } else if(e instanceof Keyboard.KeyEvent keyEvent) {
                switch (keyEvent.key) {
                    case DOWN -> {
                        if (keyEvent.isHold()) {
                            indicateNext(false);
                            keyEvent.suppress();
                        }
                    }
                    case UP -> {
                        if (keyEvent.isHold()) {
                            indicateNext(true);
                            keyEvent.suppress();
                        }
                    }
                    case ESCAPE -> {
                        if(keyEvent.isRelease()) {
                            order(ToolDealer.class).deprive(this);
                            keyEvent.suppress();
                        }
                    }
                }
            }
        }

        super.update();
    }

    @Override
    public Subject order(Subject trade) {
        if(OptionButtonBrick.MARK_REQUEST.equals(trade.raw())) {
            dimOptions();
            return $(optionIndicated = true);
        }
        return super.order(trade);
    }

    public void dimOptions() {
        for(var b : tools.bricks().eachAs(OptionButtonBrick.class)) {
            b.mark(false);
        }
        optionIndicated = false;
    }
}
