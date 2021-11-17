package airbricks;

import airbricks.selection.KeyboardClient;
import bricks.Located;
import bricks.trade.Host;
import bricks.wall.Brick;
import bricks.wall.MouseClient;
import suite.suite.Subject;

import static suite.suite.$uite.$;

public abstract class FantomBrick<H extends Host> extends Brick<H> implements MouseClient, KeyboardClient {

    protected CursorOver cursorOver;

    public FantomBrick(H host) {
        super(host);
        cursorOver = CursorOver.NO;
    }

    @Override
    public CursorOver acceptCursor(Located crd) {
        CursorOver brickCursorOver = CursorOver.NO;
        for (var mo : $bricks.reverse().list().each(MouseClient.class)) {
            if (brickCursorOver != CursorOver.NO) mo.depriveCursor();
            else brickCursorOver = mo.acceptCursor(crd);
        }
        return cursorOver = brickCursorOver == CursorOver.NO ? CursorOver.NO : CursorOver.INDIRECT;
    }

    @Override
    public void depriveCursor() {
        for(var mc : $bricks.list().each(MouseClient.class)) {
            mc.depriveCursor();
        }
        cursorOver = CursorOver.NO;
    }

    @Override
    public CursorOver cursorOver() {
        return cursorOver;
    }

    @Override
    public Subject order(Subject trade) {
        var t = trade.one();
        if(t instanceof KeyboardClient.KeyboardTransfer kt) {
            if(transferKeyboard(kt)) return $(true);
            else return super.order($(new KeyboardClient.KeyboardTransfer(this, kt.front())));
        }
        return super.order(trade);
    }

    protected boolean transferKeyboard(KeyboardClient.KeyboardTransfer transfer) {
        var clients = (transfer.front() ?
                bricks().front(transfer.current()) :
                bricks().reverse(transfer.current()))
                .each(KeyboardClient.class)
                .cascade();
        clients.next();
        for(var c : clients) {
            if(c.acceptKeyboard(transfer.front())) return true;
        }
        return false;
    }

    @Override
    public boolean acceptKeyboard(boolean front) {
        var clients = (front ?
                bricks().front() :
                bricks().reverse())
                .each(KeyboardClient.class);
        for(var c : clients) {
            if(c.acceptKeyboard(front)) return true;
        }
        return false;
    }

    @Override
    public void depriveKeyboard() {}

    @Override
    public void requestKeyboard() {}

    @Override
    public HasKeyboard hasKeyboard() {
        return HasKeyboard.NO;
    }
}
