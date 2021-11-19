package airbricks;

import bricks.Located;
import bricks.slab.*;
import bricks.trade.Host;
import bricks.wall.MouseClient;


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
}
