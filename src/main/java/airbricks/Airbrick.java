package airbricks;

import airbricks.button.TextButtonBrick;
import airbricks.intercom.IntercomBrick;
import bricks.Located;
import bricks.slab.*;
import bricks.trade.Host;
import bricks.wall.Brick;
import bricks.wall.MouseClient;
import suite.suite.action.Statement;

public abstract class Airbrick<H extends Host> extends Brick<H> implements MouseClient, Shape {

    protected CursorOver cursorOver;

    public Airbrick(H host) {
        super(host);
        cursorOver = CursorOver.NO;
    }

    @Override
    protected Wall wall() {
        return order(Wall.class);
    }

    @Override
    public CursorOver acceptCursor(Located crd) {
        if(contains(crd)) {
            CursorOver brickCursorOver = CursorOver.NO;
            for (var mo : $bricks.reverse().list().selectAs(MouseClient.class)) {
                if (brickCursorOver != CursorOver.NO) mo.depriveCursor();
                else brickCursorOver = mo.acceptCursor(crd);
            }
            return cursorOver = brickCursorOver == CursorOver.NO ? CursorOver.DIRECT : CursorOver.INDIRECT;
        } else {
            depriveCursor();
            return CursorOver.NO;
        }
    }

    @Override
    public void depriveCursor() {
        for(var mc : $bricks.list().selectAs(MouseClient.class)) {
            mc.depriveCursor();
        }
        cursorOver = CursorOver.NO;
    }

    @Override
    public CursorOver cursorOver() {
        return cursorOver;
    }

    public class Rectangle extends RectangleSlab {

        public Rectangle() {
            super(Airbrick.this);
        }
    }

    public class Circle extends CircleSlab {

        public Circle() {
            super(Airbrick.this);
        }
    }

    public class Line extends BluntLineSlab {

        public Line() {
            super(Airbrick.this);
        }
    }

    public class Text extends TextSlab {

        public Text() {
            super(Airbrick.this);
        }
    }

    public class Button extends TextButtonBrick {

        public Button() {
            super(Airbrick.this);
        }

        public void onClick(Statement st) {
            when(this::getClicks, (a, b) -> a < b, st);
        }
    }

    public class Note extends IntercomBrick {

        public Note() {
            super(Airbrick.this);
        }
    }
}
