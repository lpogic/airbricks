package airbricks.table;

import airbricks.Airbrick;
import bricks.slab.WithShape;
import bricks.Color;
import bricks.Location;
import bricks.slab.RectangleSlab;
import bricks.slab.Printable;
import bricks.slab.Shape;
import bricks.trade.Host;
import bricks.var.special.NumPull;
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

        public Column add(Rectangle rectangle) {
            if(table.columnsSize() <= col) table.addColumns($(100));
            var sector = table.sector(col, col, row, row);
            rectangle.fill(sector);
            if(rectangle instanceof Printable || rectangle instanceof Updatable) $bricks.set(rectangle);
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

        public Row add(Rectangle rectangle) {
            if(table.rowsSize() <= row) table.addRows($(35));
            var sector = table.sector(col, col, row, row);
            rectangle.fill(sector);
            if(rectangle instanceof Printable || rectangle instanceof Updatable) $bricks.set(rectangle);
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
    public NumPull x() {
        return table.x();
    }

    @Override
    public NumPull y() {
        return table.y();
    }
}
