package airbricks;

import airbricks.button.TextButtonBrick;
import airbricks.intercom.AssistedIntercomBrick;
import airbricks.intercom.IntercomBrick;
import bricks.Located;
import bricks.slab.*;
import bricks.trade.Host;
import bricks.wall.MouseClient;
import suite.suite.action.Statement;


public abstract class Airbrick<H extends Host> extends FantomBrick<H> implements Shape {

    public Airbrick(H host) {
        super(host);
    }

    @Override
    protected Wall wall() {
        return order(Wall.class);
    }

    @Override
    public CursorOver acceptCursor(Located crd) {
        if(contains(crd)) {
            CursorOver brickCursorOver = CursorOver.NO;
            for (var mo : $bricks.reverse().each(MouseClient.class)) {
                if (brickCursorOver != CursorOver.NO) mo.depriveCursor();
                else brickCursorOver = mo.acceptCursor(crd);
            }
            return cursorOver = brickCursorOver == CursorOver.NO ? CursorOver.DIRECT : CursorOver.INDIRECT;
        } else {
            depriveCursor();
            return CursorOver.NO;
        }
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

    public class AssistedNote extends AssistedIntercomBrick {

        public AssistedNote() {
            super(Airbrick.this);
        }
    }
}
