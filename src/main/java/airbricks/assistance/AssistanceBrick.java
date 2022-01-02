package airbricks.assistance;

import airbricks.Airbrick;
import airbricks.PowerBrick;
import bricks.BricksMath;
import bricks.Color;
import bricks.input.mouse.MouseButton;
import bricks.slab.Shape;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import airbricks.button.OptionButtonBrick;
import airbricks.button.SliderButtonBrick;
import bricks.slab.RectangleSlab;
import bricks.input.keyboard.Keyboard;
import bricks.input.mouse.Mouse;
import bricks.trade.Host;
import bricks.trait.Push;
import bricks.trait.PushVar;
import bricks.trait.StoredPushVar;
import bricks.trait.sensor.Constant;
import bricks.trait.sensor.Sensor;
import airbricks.FantomBrick;
import suite.suite.Subject;
import suite.suite.util.Series;

import java.util.ArrayList;
import java.util.List;

import static suite.suite.$uite.$;

public class AssistanceBrick extends Airbrick<Host> implements WithSlab {

    class OptionButtonSet extends FantomBrick<Host> {

        List<OptionButton> availableOptionButtons;

        public OptionButtonSet(Host host) {
            super(host);
            availableOptionButtons = new ArrayList<>();
        }

        public int size() {
            return bricks().size();
        }

        public void setSize(int size) {
            var currentSize = size();
            if(currentSize > size) {
                var bricks = bricks();
                for(var k : bricks.reverse().first(currentSize - size).each()) bricks.unset(k);
            } else if(currentSize < size) {
                var bricks = bricks();
                for(int i = currentSize;i < size;++i) {
                    bricks.set(availableOptionButtons.get(i));
                }
            }
        }

        public OptionButton getMarked() {
            for(var ob : bricks().eachAs(OptionButton.class)) {
                if(ob.isMarked()) return ob;
            }
            return null;
        }

        public void markFirst(boolean uprising) {
            var bricks = bricks();
            var b = uprising ? bricks.reverse().first() : bricks.first();
            if(b.present()) {
                OptionButton ob = b.asExpected();
                ob.mark(requestMark());
            }
        }

        public void mark(int index) {
            for(var ob : bricks().eachAs(OptionButton.class)) {
                if(ob.getIndex() == index) {
                    ob.mark(requestMark());
                }
            }
        }

        public boolean requestMark() {
            for(var b : bricks().eachAs(OptionButton.class)) {
                b.mark(false);
            }
            return true;
        }

        @Override
        public Subject order(Subject trade) {
            if(OptionButtonBrick.MARK_REQUEST.equals(trade.raw())) {
                return $(requestMark());
            }
            return super.order(trade);
        }
    }

    public class OptionButton extends OptionButtonBrick {
        int index;

        public OptionButton(int index) {
            super(optionButtonSet);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    RectangleSlab bg;
    OptionButtonSet optionButtonSet;
    SliderButtonBrick slider;

    List<Object> options;

    StoredPushVar<Integer> optionOffset;
    StoredPushVar<Object> picks;

    boolean wrapped;
    boolean uprising;
    int maxDisplayedOptions;

    public AssistanceBrick(Host host) {
        super(host);

        picks = PushVar.store(0);
        bg = new RectangleSlab(this);
        bg.color().set(Color.BLACK);
        wrapped = false;
        uprising = false;
        optionOffset = PushVar.store(0);
        optionButtonSet = new OptionButtonSet(this);
        options = new ArrayList<>();
        setMaxDisplayedOptions(3);

        bg.height().let(() -> {
            float sum = 0;
            for(var b : optionButtonSet.bricks().eachAs(Shape.class)) {
                sum += b.height().getFloat();
            }
            return sum;
        });

        slider = new SliderButtonBrick(this);
        slider.width().set(15);
        slider.height().set(40);
        slider.right().let(bg.right());

        $bricks.set(optionButtonSet);
    }

    public Object getOption(int index) {
        if(uprising) {
            return options.get(optionOffset.get() + optionButtonSet.size() - 1 - index);
        } else return options.get(optionOffset.get() + index);
    }

    public void pick(Object o) {
        // TODO
    }

    public Push<Object> picks() {
        return picks;
    }

    public void setOptionOffset(int offset) {
        optionOffset.set(offset);
        var overlayOptions = options.size() - optionButtonSet.size();
        var part = (bottom().getFloat() - top().getFloat() - slider.height().getFloat()) / overlayOptions;
        if(uprising) {
            slider.y().set(bottom().getFloat() - slider.height().getFloat() / 2 - part * offset);
        } else {
            slider.y().set(top().getFloat() + slider.height().getFloat() / 2 + part * offset);
        }
        sliderYChange.check();
    }

    public void setMaxDisplayedOptions(int max) {
        var aobs = optionButtonSet.availableOptionButtons.size();
        var button = aobs > 0 ? optionButtonSet.availableOptionButtons.get(aobs - 1) : null;
        for(int i = aobs;i < max; ++i) {
            var pb = button;
            button = new OptionButton(i){{
                width().let(bg.width());
                x().let(bg.x());
                if (index == 0) top().let(bg.top());
                else top().let(pb.bottom());
                text().let(() -> getOption(index).toString());
                clicks().act(() -> picks.set(getOption(index)));
            }};
            optionButtonSet.availableOptionButtons.add(button);
        }
        maxDisplayedOptions = max;
        optionButtonsRefresh();
    }

    public void markUpperOption() {
        var ob = optionButtonSet.getMarked();
        if(ob == null) {
            optionButtonSet.markFirst(uprising);
        } else {
            var selectionIndex = ob.getIndex();
            if (selectionIndex > 0) {
                optionButtonSet.mark(selectionIndex - 1);
            } else {
                var offset = optionOffset.get();
                if(uprising) {
                    var maxOffset = options.size() - optionButtonSet.size();
                    if (offset < maxOffset) {
                        setOptionOffset(offset + 1);
                    }
                } else {
                    if (offset > 0) {
                        setOptionOffset(offset - 1);
                    }
                }
            }
        }
    }

    public void markLowerOption() {
        var ob = optionButtonSet.getMarked();
        if(ob == null) {
            optionButtonSet.markFirst(uprising);
        } else {
            var selectionIndex = ob.getIndex();
            var maxIndex = optionButtonSet.size() - 1;
            if (selectionIndex < maxIndex) {
                optionButtonSet.mark(selectionIndex + 1);
            } else {
                var offset = optionOffset.get();
                if(uprising) {
                    if (offset > 0) {
                        setOptionOffset(offset - 1);
                    }
                } else {
                    var maxOffset = options.size() - optionButtonSet.size();
                    if (offset < maxOffset) {
                        setOptionOffset(offset + 1);
                    }
                }
            }
        }
    }

    public void setWrapped(boolean should) {
        wrapped = should;
    }

    public void setOptions(Series options) {
        this.options = new ArrayList<>();
        for(var o : options.list().each()) {
            this.options.add(o);
        }
        optionOffset.set(0);
        optionButtonsRefresh();
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

    public void attach(PowerBrick<?> input, Subject options, boolean preferTop) {
        uprising = preferTop;
        optionOffset.set(0);
        setOptions(options);
        attach(input, preferTop);
        optionButtonsRefresh();
        optionButtonSet.markFirst(uprising);
        slider.y().set(uprising ? bottom().getFloat() - slider.height().getFloat() / 2 :
                top().getFloat() + slider.height().getFloat() / 2);
        sliderYChange = slider.y().willChange();
    }

    @Override
    public Slab getShape() {
        return bg;
    }

    Sensor sliderYChange = Constant.getInstance();

    @Override
    public void update() {

        var input = input();
        var wall = wall();

        for(var e : input.getEvents()) {
            if(e instanceof Mouse.ButtonEvent buttonEvent) {
                if(buttonEvent.button == MouseButton.Code.LEFT) {
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
                            markLowerOption();
                            keyEvent.suppress();
                        }
                    }
                    case UP -> {
                        if (keyEvent.isHold()) {
                            markUpperOption();
                            keyEvent.suppress();
                        }
                    }
                }
            } else if(e instanceof Mouse.ScrollEvent se) {
                if((se.y > 0 && !uprising) || (se.y < 0 && uprising)) {
                    var offset = optionOffset.get();
                    if (offset > 0) {
                        setOptionOffset(offset - 1);
                    }
                    se.suppress();
                } else if(se.y < 0 || se.y > 0) {
                    var offset = optionOffset.get();
                    var maxOffset = options.size() - optionButtonSet.size();
                    if (offset < maxOffset) {
                        setOptionOffset(offset + 1);
                    }
                    se.suppress();
                }
            }
        }

        if(sliderYChange.check()) {
            var part = (slider.top().getFloat() - top().getFloat()) / (height().getFloat() - slider.height().getFloat());
            var maxOffset = options.size() - optionButtonSet.bricks().size();
            if(uprising) optionOffset.set(maxOffset - BricksMath.trim(Math.round((maxOffset) * part), 0, maxOffset));
            else optionOffset.set(BricksMath.trim(Math.round((maxOffset) * part), 0, maxOffset));
        }

        super.update();
    }

    private void optionButtonsRefresh() {
        int x = Math.min(maxDisplayedOptions, options.size());
        if(x < options.size()) lay(slider);
        else drop(slider);
        optionButtonSet.setSize(x);
    }
}
