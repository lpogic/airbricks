package airbricks.model;

import bricks.Color;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.var.Var;
import bricks.var.Vars;

public class NoteCars extends Airbrick<Note> {

    ColorRectangle body;
    Var<Integer> headIndex;
    Var<Integer> tailIndex;
    Var<Color> color;

    public NoteCars(Note note) {
        super(note);

        color = Vars.set(Color.mix(0,.8,.6));
        headIndex = Vars.set(0);
        tailIndex = Vars.set(0);

        body = rect();
        body.height().let(host.height());
        body.color().let(color);
        body.left().let(() -> {
            int begin = getMinIndex();
            LoadedFont font = order(FontManager.class).getFont(host.text.font().get());
            float xOffset = font.getStringWidth(host.text.string().get().substring(0, begin), host.text.height().getFloat());
            return host.text.left().getFloat() + xOffset;
        }, host.height(), host.text.font(), host.left(), headIndex);

        body.y().let(() -> {
            ColorText text = host.text;
            BackedFont font = order(FontManager.class).getFont(text.font().get(), text.height().getFloat());
            return text.y().getFloat() + font.getScaledDescent() / 2;
        });

        body.width().let(() -> {
            int[] minMax = getMinMax();
            LoadedFont font = order(FontManager.class).getFont(host.text.font().get());
            String str = host.text.string().get().substring(minMax[0], minMax[1]);
            return font.getStringWidth(str, host.text.height().getFloat());
        }, host.text.font(), host.string(), host.height(), headIndex, tailIndex);
    }

    @Override
    public void show() {
        show(body, host.text);
    }

    @Override
    public void hide() {
        hide(body);
    }

    @Override
    public void move() {

    }

    @Override
    public void stop() {

    }

    public int[] getMinMax() {
        int head = headIndex.get();
        int tail = tailIndex.get();
        return head < tail ? new int[]{head, tail} : new int[]{tail, head};
    }

    public int getMinIndex() {
        return Math.min(headIndex.get(), tailIndex.get());
    }

    public int getMaxIndex() {
        return Math.max(headIndex.get(), tailIndex.get());
    }

    public Var<Integer> head() {
        return headIndex;
    }

    public Var<Integer> tail() {
        return tailIndex;
    }

    public int getHead() {
        return headIndex.get();
    }

    public int getTail() {
        return tailIndex.get();
    }

    public boolean isAny() {
        return getHead() != getTail();
    }

    public void reset() {
        headIndex.set(0);
        tailIndex.set(0);
    }
}
