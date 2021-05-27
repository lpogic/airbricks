package airbricks.model;

import bricks.Color;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.var.special.NumSource;

public class NoteCars extends Airbrick<Note> {

    ColorRectangle body;
    Var<Integer> headIndex;
    Var<Integer> tailIndex;

    public NoteCars(Note note) {
        super(note);

        headIndex = Vars.set(0);
        tailIndex = Vars.set(0);

        body = rect(Color.hex("#2e5fa6"));
        body.height().let(host.height());

        body.left().let(() -> {
            int begin = getMinIndex();
            LoadedFont font = order(FontManager.class).getFont(host.text.font().get());
            float xOffset = font.getStringWidth(host.text.string().get().substring(0, begin), host.text.height().getFloat());
            return host.text.left().getFloat() + xOffset;
        }, host.height(), host.text.font(), host.left(), headIndex, tailIndex);

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

        $bricks.set(body);
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

    @Override
    public NumSource x() {
        return body.x();
    }

    @Override
    public NumSource y() {
        return body.y();
    }

    @Override
    public NumSource width() {
        return body.width();
    }

    @Override
    public NumSource height() {
        return body.height();
    }

    @Override
    public NumSource left() {
        return body.left();
    }

    @Override
    public NumSource right() {
        return body.right();
    }

    @Override
    public NumSource top() {
        return body.top();
    }

    @Override
    public NumSource bottom() {
        return body.bottom();
    }
}
