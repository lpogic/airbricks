package airbricks.table;

import airbricks.Airbrick;
import bricks.slab.*;
import bricks.Color;
import bricks.Location;
import bricks.trade.Host;
import bricks.trait.number.NumberTrait;
import bricks.wall.Updatable;

import static suite.suite.$uite.$;

public abstract class TableBrick extends Airbrick<Host> implements WithShape, Location {

    Table table;
    RectangleSlab bg;

    public TableBrick(Host host) {
        super(host);

        table = new Table();

        bg = new RectangleSlab(this) {{
            color().set(Color.hex("#082837"));
            fill(table);
        }};

        $bricks.set(bg);
    }

    protected Row addRow() {
        table.addRows($(35));
        return new Row(table.rowsSize() - 1);
    }

    protected Column addColumn() {
        table.addColumns($(100));
        return new Column(table.columnsSize() - 1);
    }

    protected class Row {
        final int row;
        int col;

        public Row(int row) {
            this.row = row;
        }

        public Column add(Slab slab) {
            if(table.columnsSize() <= col) table.addColumns($(100));
            var sector = table.sector(col, col, row, row);
            slab.fill(sector);
            if(slab instanceof Printable || slab instanceof Updatable) $bricks.set(slab);
            return new Column(col++);
        }

        public Column add(Location location) {
            if(table.columnsSize() <= col) table.addColumns($(100));
            var sector = table.sector(col, col, row, row);
            location.aim(sector);
            if(location instanceof Printable || location instanceof Updatable) $bricks.set(location);
            return new Column(col++);
        }
    }

    protected class Column {
        final int col;
        int row;

        public Column(int col) {
            this.col = col;
        }

        public Row add(Slab slab) {
            if(table.rowsSize() <= row) table.addRows($(35));
            var sector = table.sector(col, col, row, row);
            slab.fill(sector);
            if(slab instanceof Printable || slab instanceof Updatable) $bricks.set(slab);
            return new Row(row++);
        }

        public Row add(Location location) {
            if(table.rowsSize() <= row) table.addRows($(100));
            var sector = table.sector(col, col, row, row);
            location.aim(sector);
            if(location instanceof Printable || location instanceof Updatable) $bricks.set(location);
            return new Row(row++);
        }
    }

    @Override
    public Shape getShape() {
        return table;
    }

    @Override
    public NumberTrait x() {
        return table.x();
    }

    @Override
    public NumberTrait y() {
        return table.y();
    }
}
