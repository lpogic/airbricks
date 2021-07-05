package airbricks;

import airbricks.selection.SelectionClient;
import airbricks.selection.SelectionDealer;
import bricks.*;
import bricks.graphic.RectangleBrick;
import bricks.graphic.Rectangle;
import bricks.graphic.WithRectangleBody;
import bricks.trade.Host;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.impulse.State;
import bricks.var.special.Num;


public abstract class PowerBrick<H extends Host> extends Airbrick<H> implements Rectangle, WithRectangleBody, SelectionClient {

    public final Num outlineThick;

    public final RectangleBrick background;
    public final Var<Color> backgroundColor;
    public final Var<Color> backgroundLightColor;
    public final Var<Color> backgroundPressColor;

    public final RectangleBrick outline;
    public final Var<Color> outlineColor;
    public final Var<Color> outlineSelectColor;

    public PowerBrick(H host) {
        super(host);

        selected = state(false, this::select);
        pressed = state(false, this::press);
        lighted = state(false, this::light);
        clicked = Vars.get();

        backgroundColor = Vars.set(Color.hex("#292B2B"));
        backgroundLightColor = Vars.set(Color.hex("#212323"));
        backgroundPressColor = Vars.set(Color.hex("#191B1B"));

        outlineColor = Vars.set(Color.hex("#1e1a2c"));
        outlineSelectColor = Vars.set(Color.mix(1, .8, .6));

        outlineThick = Vars.num(4);

        outline = new RectangleBrick(this) {{
            color().let(outlineColor);
        }};

        background = new RectangleBrick(this) {{
            color().let(backgroundColor);
            aim(outline);
            adjust(Sized.relative(outline, outlineThick.perFloat(t -> -t)));
        }};

        $bricks.set(outline, background);
    }

    public void updateState() {
        boolean pressed = this.pressed.get();
        boolean lighted = this.lighted.get();
        boolean selected = this.selected.get();
        if(pressed) {
            background.color().let(backgroundPressColor);
        } else if(lighted) {
            background.color().let(backgroundLightColor);
        } else {
            background.color().let(backgroundColor);
        }

        if(selected) {
            outline.color().let(outlineSelectColor);
        } else {
            outline.color().let(outlineColor);
        }
    }

    protected State<Boolean> pressed;

    public void press(boolean state) {
        if(pressed.get() != state) {
            pressed.setState(state);
            updateState();
        }
    }

    public void press() {
        press(true);
    }

    public void release() {
        press(false);
    }

    public Var<Boolean> pressed() {
        return pressed;
    }

    protected State<Boolean> lighted;

    public void light(boolean state) {
        if(lighted.get() != state) {
            lighted.setState(state);
            updateState();
        }
    }

    public void light() {
        light(true);
    }

    public void dim() {
        light(false);
    }

    public Var<Boolean> lighted() {
        return lighted;
    }

    protected State<Boolean> selected;

    public void select(boolean state) {
        if(state != selected.get()) {
            var selectionDealer = order(SelectionDealer.class);
            if(state) {
                if (selectionDealer.requestSelection(this)) {
                    selected.setState(true);
                }
            } else {
                selected.setState(false);
            }
            updateState();
        }
    }

    @Override
    public void depriveSelection() {
        select(false);
    }

    @Override
    public void requestSelection() {
        select(true);
    }

    public void select() {
        select(true);
    }

    public void unselect() {
        select(false);
    }

    public Var<Boolean> selected() {
        return selected;
    }

    protected Var<Number> clicked;

    public void click() {
        clicked.set(System.currentTimeMillis());
    }

    public Source<Number> clicked() {
        return clicked;
    }

    @Override
    public Rectangle getBody() {
        return outline;
    }
}
