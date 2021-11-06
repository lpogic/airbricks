package airbricks.note;

import airbricks.Airbrick;
import bricks.Color;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.slab.RectangleSlab;
import bricks.slab.Shape;
import bricks.slab.TextSlab;
import bricks.slab.WithShape;
import bricks.var.Pull;
import bricks.var.Var;

public class NoteCars extends Airbrick<NoteBrick> implements WithShape {

    RectangleSlab body;
    Pull<Integer> headIndex;
    Pull<Integer> tailIndex;

    public NoteCars(NoteBrick note) {
        super(note);

        headIndex = Var.pull(0);
        tailIndex = Var.pull(0);

        body = new RectangleSlab(this) {{
            color().set(Color.hex("#2e5fa6"));
        }};

        body.height().let(host.height());

        body.left().let(() -> {
            int begin = getMinIndex();
            LoadedFont font = order(FontManager.class).getFont(host.text.font().get());
            float xOffset = font.getStringWidth(host.text.text().get().substring(0, begin), host.text.height().getFloat());
            return host.text.left().getFloat() + xOffset;
        }, host.height(), host.text.font(), host.left(), headIndex, tailIndex);

        body.y().let(() -> {
            TextSlab text = host.text;
            BackedFont font = order(FontManager.class).getFont(text.font().get(), text.height().getFloat());
            return text.y().getFloat() + font.getScaledDescent() / 2;
        });

        body.width().let(() -> {
            var minMax = getMinMax();
            String str = host.text.text().get();
            LoadedFont font = order(FontManager.class).getFont(host.text.font().get());
            str = substr(str, minMax.min, minMax.max);
            return font.getStringWidth(str, host.text.height().getFloat());
        }, host.text.font(), host.text(), host.height(), headIndex, tailIndex);

        $bricks.set(body);
    }

    String substr(String str, int begin, int end) {
        int len = str.length();
        if(begin > len) return str;
        if(end > len) return str.substring(begin);
        return str.substring(begin, end);
    }

    public record MinMax(int min, int max){}

    public MinMax getMinMax() {
        int head = headIndex.get();
        int tail = tailIndex.get();
        return head < tail ? new MinMax(head, tail) : new MinMax(tail, head);
    }

    public int getMinIndex() {
        return Math.min(headIndex.get(), tailIndex.get());
    }

    public int getMaxIndex() {
        return Math.max(headIndex.get(), tailIndex.get());
    }

    public Pull<Integer> head() {
        return headIndex;
    }

    public Pull<Integer> tail() {
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
    public Shape getShape() {
        return body;
    }
}
