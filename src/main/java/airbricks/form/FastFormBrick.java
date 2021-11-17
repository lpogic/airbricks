package airbricks.form;

import airbricks.Airbrick;
import airbricks.PowerBrick;
import airbricks.button.TextButtonBrick;
import airbricks.note.NoteBrick;
import airbricks.selection.KeyboardClient;
import airbricks.table.Table;
import bricks.Color;
import bricks.Location;
import bricks.Sized;
import bricks.slab.RectangleSlab;
import bricks.slab.Shape;
import bricks.slab.WithShape;
import bricks.var.Var;
import bricks.var.special.NumPull;
import bricks.wall.Brick;
import suite.suite.Subject;
import suite.suite.action.Statement;

import static suite.suite.$uite.*;

public class FastFormBrick extends Airbrick<Brick<?>> implements WithShape, Location {

    protected Table table;
    RectangleSlab bg;
    RectangleSlab frame;

    NumPull labelColumnWidth;

    Statement submit;
    Statement cancel;

    public FastFormBrick(Brick<?> host) {
        super(host);
        table = new Table();
        labelColumnWidth = Var.num(120);
        table.addColumns($($(labelColumnWidth), $(200)));

        frame = new RectangleSlab(this) {{
            color().set(Color.hex("0"));
            aim(table);
            adjust(Sized.relative(table, 6));
        }};

        bg = new RectangleSlab(this) {{
           color().set(Color.hex("#082837"));
           fill(table);
        }};

        submit = Statement::vain;
        cancel = Statement::vain;

        $bricks.set(frame, bg);
    }

    @Override
    public void update() {
        super.update();

        for (var e : input().getKeyEvents()) {
            switch (e.key) {
                case ESCAPE -> {
                    if (e.isRelease()) {
                        e.suppress();
                        cancel.play();
                    }
                }
                case ENTER -> {
                    if (e.isRelease()) {
                        e.suppress();
                        submit.play();
                    }
                }
            }
        }
    }

    protected void addInputRow(String label, PowerBrick<?> powerBrick) {
        table.addRows($(50));
        int row = table.rowsSize() - 1;
        var sector = table.sector(0, row);

        NoteBrick note = new NoteBrick(this) {{
            text().set(label);
        }};
        note.y().let(sector.y());
        note.right().let(sector.right().perFloat(r -> r - 10));
        if(note.width().getFloat() + 20 > labelColumnWidth.getFloat()) {
            labelColumnWidth.set(note.width().getFloat() + 20);
        }

        sector = table.sector(1, row);
        powerBrick.aim(sector);
        powerBrick.adjust(Sized.relative(sector, -8));
        $bricks.set(note, powerBrick);
    }

    protected void addSeparator() {
        table.addRows($(20));
    }

    protected Row addRow() {
        table.addRows($(50));
        return new Row(table.rowsSize() - 1);
    }

    public class Row {
        int row;
        PowerBrick<?> last;

        public Row(int row) {
            this.row = row;
        }

        public void add(PowerBrick<?> brick) {
            var sector = table.sector(0, 1, row, row);
            brick.y().let(sector.y());
            brick.height().let(sector.height().perFloat(h -> h - 8));
            if(last == null) {
                brick.right().let(sector.right().perFloat(r -> r - 4));
            } else {
                brick.right().let(last.left().perFloat(l -> l - 8));
            }
            last = brick;
            $bricks.set(brick);
        }
    }

    protected void addControlButtons() {
        var row = addRow();
        row.add(new TextButtonBrick(this) {
            {
                text().set("Zapisz");
            }

            @Override
            public void click() {
                super.click();
                submit.play();
            }
        });
        row.add(new TextButtonBrick(this) {
            {
                text().set("Anuluj");
            }

            @Override
            public void click() {
                super.click();
                cancel.play();
            }
        });
    }

    protected void setSubmit(Statement statement) {
        submit = statement;
    }

    protected void setCancel(Statement statement) {
        cancel = statement;
    }

    @Override
    public Subject order(Subject trade) {
        if("selectNext".equals(trade.raw())) {
            KeyboardClient s = trade.last().asExpected();

            KeyboardClient keyboardClient = null;
            for(var sc : $bricks.reverse().each(KeyboardClient.class)) {
                if(s.equals(sc)) break;
                keyboardClient = sc;
            }
            if(keyboardClient == null) keyboardClient = $bricks.each(KeyboardClient.class).cascade().nextOrNull();
            keyboardClient.requestKeyboard();
            return $();
        }
        if("selectPrev".equals(trade.raw())) {
            KeyboardClient s = trade.last().asExpected();

            KeyboardClient keyboardClient = null;
            for(var sc : $bricks.each(KeyboardClient.class)) {
                if(s.equals(sc)) break;
                keyboardClient = sc;
            }
            if(keyboardClient == null) keyboardClient = $bricks.reverse().each(KeyboardClient.class).cascade().nextOrNull();
            keyboardClient.requestKeyboard();
            return $();
        }

        return super.order(trade);
    }

    @Override
    public Shape getShape() {
        return table;
    }

    @Override
    public NumPull x() {
        return table.x();
    }

    @Override
    public NumPull y() {
        return table.y();
    }
}
