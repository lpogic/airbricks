package airbricks.assistance;

import airbricks.Airbrick;
import airbricks.PowerBrick;
import airbricks.Int;
import bricks.slab.Shape;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import airbricks.button.OptionButtonBrick;
import airbricks.button.SliderButtonBrick;
import bricks.slab.RectangleSlab;
import bricks.input.Keyboard;
import bricks.input.Mouse;
import bricks.monitor.Monitor;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Var;
import bricks.var.impulse.Constant;
import bricks.var.impulse.Impulse;
import bricks.var.special.NumPull;
import bricks.wall.FantomBrick;
import suite.suite.Subject;

import java.util.ArrayList;
import java.util.List;

import static suite.suite.$uite.$;

public class AssistanceBrick extends Airbrick<Host> implements WithSlab {

    RectangleSlab bg;
    List<OptionButtonBrick> buttons;
    FantomBrick usedButtons;
    List<String> options;
    String searchString;
    SliderButtonBrick slider;
    Pull<Integer> offset;
    Pull<Int> picked;

    boolean wrapped;

    Monitor offsetMonitor;

    public AssistanceBrick(Host host) {
        super(host);

        picked = Var.let();
        bg = new RectangleSlab(this);
        wrapped = false;
        buttons = new ArrayList<>();
        offset = Var.pull(0);
        offsetMonitor = when(offset, this::updateButtons);

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
            when(button::getClicks, (a, b) -> a < b, () -> pick(finalI + offset.get()));
            buttons.add(button);
            prevButton = button;
        }
        bg.height().let(() -> {
            float sum = 0f;
            for(var b : usedButtons.bricks().eachAs(Shape.class)) {
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

    public void pick(int i) {
        picked.set(new Int(i));
    }

    public Pull<Int> picked() {
        return picked;
    }

    public void indicateFirst() {
        var b = usedButtons.bricks();
        if(b.present()) {
            OptionButtonBrick pb = b.asExpected();
            pb.indicate(requestIndication());
        }
    }

    public void indicateNext(boolean up_down) {
        boolean indicatedFound = false;
        boolean indicatedLast = false;
        var bricks = usedButtons.bricks();
        var it = up_down ? bricks.reverse() : bricks.front();
        for(var button : it.eachAs(OptionButtonBrick.class)) {
            if(indicatedFound) {
                button.indicate(indicatedLast);
                indicatedLast = false;
            } else {
                indicatedLast = indicatedFound = button.isIndicated();
                button.indicate(false);
            }
        }
        if(bricks.present()) {
            if (!indicatedFound || (indicatedLast && wrapped)) {
                if (up_down) {
                    bricks.last().as(OptionButtonBrick.class).indicate(true);
                    slider.bottom().set(bottom().get());
                }
                else {
                    bricks.first().as(OptionButtonBrick.class).indicate(true);
                    slider.top().set(top().get());
                }
            } else if(indicatedLast) {
                var off = offset.get();
                var offMax = options.size() - bricks.size();
                if (up_down) {
                    if(off > 0) {
                        offset.set(off - 1);
                        slider.top().set(top().getFloat() + (off - 1f) / offMax *
                                (height().getFloat() - slider.height().getFloat()));
                    }
                    bricks.first().as(OptionButtonBrick.class).indicate(true);
                } else {
                    if(off < offMax) {
                        offset.set(off + 1);
                        slider.top().set(top().getFloat() + (off + 1f) / offMax *
                                (height().getFloat() - slider.height().getFloat()));
                    }
                    bricks.last().as(OptionButtonBrick.class).indicate(true);
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
                button.text().set(str);
                usedButtons.bricks().set(button);
            } else {
                usedButtons.bricks().unset(button);
            }
        }
    }

    public void attach(Shape shape, boolean preferTop) {
        if(preferTop) {
            bg.y().let(() -> {
                float bgh = bg.height().getFloat();
                if(shape.top().getFloat() - bgh < 0) {
                    return shape.bottom().getFloat() + bgh / 2;
                } else {
                    return shape.top().getFloat() - bgh / 2;
                }
            });
        } else {
            bg.y().let(() -> {
                float bgh = bg.height().getFloat();
                if(shape.bottom().getFloat() + bgh <= wall().bottom().getFloat()) {
                    return shape.bottom().getFloat() + bgh / 2;
                } else {
                    return shape.top().getFloat() - bgh / 2;
                }
            });
        }
        bg.left().let(shape.left());
        bg.width().let(shape.width());
    }

    public void attach(PowerBrick<?> input, List<String> options, boolean preferTop) {
        setOptions(options);
        offset.set(0);
        updateButtons();
        attach(input, preferTop);
        slider.y().set(top().getFloat() + slider.height().getFloat() / 2);
        sliderYChange = slider.y().willChange();
    }

    @Override
    public Slab getShape() {
        return bg;
    }

    Impulse sliderYChange = Constant.getInstance();

    @Override
    public void update() {

        var input = input();
        var wall = wall();

        for(var e : input.getEvents()) {
            if(e instanceof Mouse.ButtonEvent buttonEvent) {
                if(buttonEvent.button == Mouse.Button.Code.LEFT) {
                    if(buttonEvent.isPress()) {
                        boolean mouseIn = seeCursor();
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
                }
            }
        }

        if(sliderYChange.occur()) {
            var part = (slider.top().getFloat() - top().getFloat()) / (height().getFloat() - slider.height().getFloat());
            var maxOffset = options.size() - usedButtons.bricks().size();
            offset.set(NumPull.trim(Math.round((maxOffset) * part), 0, maxOffset));
        }

        super.update();
    }

    @Override
    public Subject order(Subject trade) {
        if(OptionButtonBrick.INDICATE_REQUEST.equals(trade.raw())) {
            return $(requestIndication());
        }
        return super.order(trade);
    }

    public boolean requestIndication() {
        for(var b : buttons) {
            b.indicate(false);
        }
        return true;
    }
}
