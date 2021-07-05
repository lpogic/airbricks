package airbricks.assistance;

import airbricks.Airbrick;
import airbricks.PowerBrick;
import airbricks.Int;
import bricks.graphic.WithRectangleBody;
import airbricks.button.OptionButtonBrick;
import airbricks.button.SliderButtonBrick;
import bricks.graphic.RectangleBrick;
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

import java.util.ArrayList;
import java.util.List;

import static suite.suite.$uite.set$;

public class AssistanceBrick extends Airbrick<Host> implements WithRectangleBody {

    RectangleBrick bg;
    List<OptionButtonBrick> buttons;
    FantomBrick usedButtons;
    List<String> options;
    String searchString;
    SliderButtonBrick slider;
    Var<Integer> offset;

    boolean wrapped;

    Monitor offsetMonitor;

    public AssistanceBrick(Host host) {
        super(host);

        picked = Vars.get();

        bg = new RectangleBrick(this);

        wrapped = false;

        buttons = new ArrayList<>();

        offset = Vars.set(0);
        offsetMonitor = when(offset.willChange(), this::updateButtons);

        OptionButtonBrick prevButton = null;
        for(int i = 0;i < 3; ++i) {
            var button = new OptionButtonBrick(this);
            button.width().let(bg.width());
            button.x().let(bg.x());
            if(i == 0) {
                button.top().let(bg.top());
            } else {
                button.top().let(prevButton.bottom());
            }
            int finalI = i;
            when(button.clicked()).then(() -> pick(finalI + offset.get()));
            buttons.add(button);
            prevButton = button;
        }
        bg.height().let(() -> {
            float sum = 0f;
            for(var b : usedButtons.bricks().eachAs(Brick.class)) {
                sum += b.height().getFloat();
            }
            return sum;
        });

        usedButtons = new FantomBrick(this);

        slider = new SliderButtonBrick(this);
        slider.width().set(15);
        slider.height().set(40);
        slider.right().let(bg.right());

        $bricks.set(usedButtons, slider);
    }

    Var<Int> picked;

    public void pick(int i) {
        picked.set(new Int(i));
    }

    public Var<Int> picked() {
        return picked;
    }

    public void lightFirst() {
        var b = usedButtons.bricks();
        if(b.present()) {
            OptionButtonBrick pb = b.asExpected();
            pb.light(requestLight());
        }
    }

    public void lightNext(boolean up_down) {
        boolean lightedFound = false;
        boolean lightedLast = false;
        var bricks = usedButtons.bricks();
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
                    slider.bottom().set(bottom().get());
                }
                else {
                    bricks.first().as(OptionButtonBrick.class).light();
                    slider.top().set(top().get());
                }
            } else if(lightedLast) {
                var off = offset.get();
                var offMax = options.size() - bricks.size();
                if (up_down) {
                    if(off > 0) {
                        offset.set(off - 1);
                        slider.top().set(top().getFloat() + (off - 1f) / offMax *
                                (height().getFloat() - slider.height().getFloat()));
                    }
                    bricks.first().as(OptionButtonBrick.class).light();
                } else {
                    if(off < offMax) {
                        offset.set(off + 1);
                        slider.top().set(top().getFloat() + (off + 1f) / offMax *
                                (height().getFloat() - slider.height().getFloat()));
                    }
                    bricks.last().as(OptionButtonBrick.class).light();
                }
            }
        }
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }

    public void setOptions(List<String> options) {
        this.options = options;
        this.searchString = null;
        if(options.size() > buttons.size()) $bricks.set(slider);
        else $bricks.unset(slider);
        offset.set(0);
        updateButtons();
    }

    public void setOptions(List<String> options, String searchString) {
        this.options = options;
        this.searchString = searchString;
        if(options.size() > buttons.size()) $bricks.set(slider);
        else $bricks.unset(slider);
        offset.set(0);
        updateButtons();
    }

    void updateButtons() {
        var off = offset.get();
        usedButtons.bricks().unset();
        for(int i = 0;i < buttons.size();++i) {
            var button = buttons.get(i);
            var offI = i + off;
            if(options.size() > offI) {
                var str = options.get(offI);
                if(searchString != null) {
                    var index = str.indexOf(searchString);
                    if (index >= 0) button.note.select(index, searchString.length());
                    else button.note.select(0,0);
                } else button.note.select(0,0);
                button.string().set(str);
                usedButtons.bricks().set(button);
            } else {
                usedButtons.bricks().unset(button);
            }
        }
    }

    public void attach(Brick<?> brick) {
        if(brick.bottom().getFloat() + bg.height().getFloat() <= wall().bottom().getFloat()) {
            bg.top().let(brick.bottom());
        } else {
            bg.bottom().let(brick.top());
        }
        bg.left().let(brick.left());
        bg.width().let(brick.width());
    }

    public void attach(PowerBrick<?> input, List<String> options) {
        setOptions(options);
        offset.set(0);
        updateButtons();
        attach(input);
        slider.y().set(top().getFloat() + slider.height().getFloat() / 2);
        sliderYChange = slider.y().willChange();
    }

    @Override
    public Rectangle getBody() {
        return bg;
    }

    Impulse sliderYChange = Constant.getInstance();

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
                            keyEvent.suppress();
                        }
                    }
                    case UP -> {
                        if (keyEvent.isHold()) {
                            lightNext(true);
                            keyEvent.suppress();
                        }
                    }
                }
            }
        }

        if(sliderYChange.occur()) {
            var part = (slider.top().getFloat() - top().getFloat()) / (height().getFloat() - slider.height().getFloat());
            var maxOffset = options.size() - usedButtons.bricks().size();
            offset.set(Num.trim(Math.round((maxOffset) * part), 0, maxOffset));
        }
    }

    @Override
    public Subject order(Subject trade) {
        if(OptionButtonBrick.LIGHT_REQUEST.equals(trade.raw())) {
            return set$(requestLight());
        }
        return super.order(trade);
    }

    public boolean requestLight() {
        for(var b : buttons) {
            b.dim();
        }
        return true;
    }
}
