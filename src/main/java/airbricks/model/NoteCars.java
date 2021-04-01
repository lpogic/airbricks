package airbricks.model;

import bricks.Color;
import bricks.Point;
import bricks.XOrigin;
import bricks.font.BackedFont;
import bricks.font.FontManager;
import bricks.font.LoadedFont;
import bricks.graphic.ColorRectangle;
import bricks.graphic.ColorText;
import bricks.var.Var;
import bricks.var.Vars;
import bricks.wall.Brick;

public class NoteCars extends Brick<Note> {

    ColorRectangle body;
    Var<Integer> headIndex;
    Var<Integer> tailIndex;
    Var<Color> color;

    public NoteCars(Note note) {
        super(note);

        color = Vars.set(Color.mix(0,.8,.6));
        headIndex = Vars.set(0);
        tailIndex = Vars.set(0);

        body = rect().setXOrigin(XOrigin.LEFT);
        body.height().let(host.at(n -> n.text.size()));
        body.color().let(color);
        body.yOrigin().let(host.at(n -> n.text.yOrigin()));
        body.position().let(() -> {
            ColorText text = getHost().text;
            int begin = getMinIndex();
            BackedFont font = order(FontManager.class).getFont(text.getFont(), text.getSize());
            float xOffset = font.getLoadedFont().getStringWidth(text.getString().substring(0, begin), text.getSize());
            Point textPosition = text.getPosition();
            XOrigin xOrigin = text.getXOrigin();
            return switch (xOrigin) {
                case LEFT -> new Point(textPosition.getX() + xOffset,
                        textPosition.getY() + font.getScaledDescent() / 2);
                case CENTER -> new Point(textPosition.getX() - text.getWidth() / 2 + xOffset,
                        textPosition.getY() + font.getScaledDescent() / 2);
                case RIGHT -> new Point(textPosition.getX() - text.getWidth() + xOffset,
                        textPosition.getY() + font.getScaledDescent() / 2);
            };
        }, host, host.at(n -> n.text.font()), host.at(Note::string), host.at(Note::width),
                host.at(n -> n.text.size()), host.at(n -> n.text.position()),
                host.at(n -> n.text.xOrigin()), headIndex, tailIndex);
        body.width().let(() -> {
            var text = getHost().text;
            int[] minMax = getMinMax();
            LoadedFont font = order(FontManager.class).getFont(text.getFont());
            String str = text.getString().substring(minMax[0], minMax[1]);
            return font.getStringWidth(str, text.getSize());
        }, host.at(n -> n.text.font()), host.at(Note::string), host.at(n -> n.text.size()),
                headIndex, tailIndex);
    }

    @Override
    public void show() {
        show(body, getHost().text);
    }

    @Override
    public void hide() {
        hide(body);
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
