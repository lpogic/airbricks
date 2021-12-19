package airbricks.text;

import bricks.Color;
import bricks.Location;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.slab.RectangleSlab;
import bricks.slab.Shape;
import bricks.slab.TextSlab;
import bricks.slab.WithShape;
import bricks.trade.Host;
import bricks.var.Pull;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.num.NumPull;
import bricks.wall.Brick;

public class SelectableTextBrick extends Brick<Host> implements WithShape, Location {

    public record Bounds(int begin, int end) {
        static boolean disjoint(Bounds a, Bounds b) {
            if(a.begin < b.begin) {
                return a.end < b.begin;
            } else if(a.begin > b.end) {
                return a.end > b.end;
            } else return false;
        }

        public boolean contains(int p) {
            return p >= begin && p <= end;
        }

    }

    TextSlab textSlab;
    RectangleSlab selection;
    Pull<Integer> selectionHead;
    Pull<Integer> selectionTail;

    public SelectableTextBrick(Host host) {
        super(host);

        textSlab = new TextSlab(host);
        selection = new RectangleSlab(host);
        selectionHead = Var.pull(0);
        selectionTail = Var.pull(0);

        selection.color().set(Color.hex("#2e5fa6"));

        selection.height().let(textSlab.height());

        selection.left().let(() -> {
            int begin = getSelectionMin();
            LoadedFont font = order(FontManager.class).getFont(textSlab.font().get());
            float xOffset = font.getStringWidth(textSlab.text().get().substring(0, begin),
                    textSlab.height().getFloat(), textSlab.isHideEol());
            return textSlab.left().getFloat() + xOffset;
        }, this::getSelectionMin, textSlab.font(), textSlab.text(), textSlab.height(), textSlab.left());

        selection.y().let(() -> {
            BackedFont font = order(FontManager.class).getFont(textSlab.font().get(), textSlab.height().getFloat());
            return textSlab.y().getFloat() + font.getScaledDescent() / 2;
        });

        selection.width().let(() -> {
            String str = textSlab.text().get();
            LoadedFont font = order(FontManager.class).getFont(textSlab.font().get());
            str = str.substring(getSelectionMin(), getSelectionMax());
            return font.getStringWidth(str, textSlab.height().getFloat(), textSlab.isHideEol());
        }, textSlab.text(), textSlab.font(), this::getSelectionMin, this::getSelectionMax, textSlab.height());

        bricks().set(selection, textSlab);
    }

    public void setSelectionHead(int selectionHead) {
        setSelection(selectionHead, selectionTail.get());
    }

    public void setSelectionTail(int selectionTail) {
        setSelection(selectionHead.get(), selectionTail);
    }

    public Pull<Integer> selectionHead() {
        return selectionHead;
    }

    public Pull<Integer> selectionTail() {
        return selectionTail;
    }

    public void setSelection(int head, int tail) {
        this.selectionHead.set(head);
        this.selectionTail.set(tail);
    }

    public void resetSelection() {
        setSelection(0, 0);
    }

    public boolean anySelected() {
        int head = selectionHead.get();
        int tail = selectionTail.get();
        return head != tail;
    }

    public Bounds getSelectionBounds() {
        var head = selectionHead.get();
        var tail = selectionTail.get();
        return head < tail ? new Bounds(head, tail) : new Bounds(tail, head);
    }

    public int getSelectionMin() {
        return Math.max(0, Math.min(selectionTail.get(), selectionHead.get()));
    }

    public int getSelectionMax() {
        return Math.min(text().get().length(), Math.max(selectionTail.get(), selectionHead.get()));
    }

    public Source<String> selectedText() {
        return text().per(s -> s.substring(getSelectionMin(), getSelectionMax()));
    }

    @Override
    public Shape getShape() {
        return textSlab;
    }

    public NumPull height() {
        return textSlab.height();
    }

    public NumPull left() {
        return textSlab.left();
    }

    public NumPull right() {
        return textSlab.right();
    }

    public NumPull top() {
        return textSlab.top();
    }

    public NumPull bottom() {
        return textSlab.bottom();
    }

    public NumPull x() {
        return textSlab.x();
    }

    public NumPull y() {
        return textSlab.y();
    }

    public Pull<Color> color() {
        return textSlab.color();
    }

    public Pull<String> text() {
        return textSlab.text();
    }

    public TextSlab textSlab() {
        return textSlab;
    }
}
