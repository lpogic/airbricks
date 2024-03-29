package airbricks.button;

import airbricks.Airbrick;
import bricks.BricksMath;
import bricks.Color;
import bricks.Sized;
import bricks.input.mouse.MouseButton;
import bricks.slab.RectangleSlab;
import bricks.slab.Slab;
import bricks.slab.WithSlab;
import bricks.input.mouse.Mouse;
import bricks.trait.Trait;
import bricks.trait.Traits;
import bricks.trait.number.NumberTrait;

public class SliderButtonBrick extends Airbrick<Airbrick<?>> implements WithSlab {

    public boolean pressed;
    public boolean indicated;

    public final RectangleSlab background;
    public final Trait<Color> backgroundColorDefault;
    public final Trait<Color> backgroundColorIndicated;

    public final RectangleSlab outline;
    public final Trait<Color> outlineColorDefault;

    public final NumberTrait outlineThick;


    public SliderButtonBrick(Airbrick<?> host) {
        super(host);

        pressed = false;
        indicated = false;

        backgroundColorDefault = Traits.set(Color.hex("#4b735d88"));
        backgroundColorIndicated = Traits.set(Color.hex("#4b735d"));

        outlineColorDefault = Traits.set(Color.hex("#0000"));

        outlineThick = Traits.num(3);

        outline = new RectangleSlab(this) {{
            color().let(outlineColorDefault);
        }};

        background = new RectangleSlab(this) {{
            color().let(() -> indicated ?
                    backgroundColorIndicated.get() :
                    backgroundColorDefault.get());
            aim(outline);
            adjust(Sized.relative(outline, outlineThick.perFloat(t -> -t)));
        }};

        $bricks.set(outline, background);
    }

    @Override
    public Slab getShape() {
        return outline;
    }

    double pressOffsetY;

    @Override
    public void update() {

        var in = input();
        var wall = wall();
        boolean seeCursor = seeCursor();
        for(var e : in.getEvents()) {
            if(e instanceof Mouse.PositionEvent pe) {
                indicated = seeCursor;
                if(pressed) {
                    var h2 = height().getFloat() / 2;
                    y().set(BricksMath.trim(pe.y - pressOffsetY,
                            host.top().getFloat() + h2,
                            host.bottom().getFloat() - h2));
                }
            } else if(e instanceof Mouse.ButtonEvent be) {
                if(be.button == MouseButton.Code.LEFT) {
                    if(be.isPress()) {
                        if(seeCursor()) {
                            pressed = true;
                            wall.trapMouse(this);
                            pressOffsetY = be.state.mouseCursorY() - y().getFloat();
                        }
                    } else {
                        pressed = false;
                        if(wall.mouseTrappedBy(this)) {
                            wall.freeMouse();
                        }
                    }
                }
            }
        }

        super.update();
    }
}
