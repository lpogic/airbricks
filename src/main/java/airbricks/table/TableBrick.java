package airbricks.table;

import airbricks.Airbrick;
import bricks.graphic.WithRectangularBody;
import bricks.Color;
import bricks.Location;
import bricks.graphic.RectangleBrick;
import bricks.graphic.Printable;
import bricks.graphic.Rectangle;
import bricks.graphic.Rectangular;
import bricks.trade.Host;
import bricks.var.special.Num;
import bricks.wall.Updatable;

import static suite.suite.$uite.set$;

public abstract class TableBrick extends Airbrick<Host> implements WithRectangularBody, Location {

    Table table;
    RectangleBrick bg;

    public TableBrick(Host host) {
        super(host);

        table = new Table();

        bg = new RectangleBrick(this) {{
            color().set(Color.hex("#082837"));
            fill(table);
        }};

        $bricks.set(bg);
    }

    protected Row addRow() {
        table.addRows(set$(35));
        return new Row(table.rowsSize() - 1);
    }

    protected Column addColumn() {
        table.addColumns(set$(100));
        return new Column(table.columnsSize() - 1);
    }

    protected class Row {
        final int row;
        int col;

        public Row(int row) {
            this.row = row;
        }

        public Column add(Rectangle rectangle) {
            if(table.columnsSize() <= col) table.addColumns(set$(100));
            var sector = table.sector(col, col, row, row);
            rectangle.fill(sector);
            if(rectangle instanceof Printable || rectangle instanceof Updatable) $bricks.set(rectangle);
            return new Column(col++);
        }

        public Column add(Location location) {
            if(table.columnsSize() <= col) table.addColumns(set$(100));
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
            if(table.rowsSize() <= row) table.addRows(set$(35));
            var sector = table.sector(col, col, row, row);
            rectangle.fill(sector);
            if(rectangle instanceof Printable || rectangle instanceof Updatable) $bricks.set(rectangle);
            return new Row(row++);
        }

        public Row add(Location location) {
            if(table.rowsSize() <= row) table.addRows(set$(100));
            var sector = table.sector(col, col, row, row);
            location.aim(sector);
            if(location instanceof Printable || location instanceof Updatable) $bricks.set(location);
            return new Row(row++);
        }
    }

    @Override
    protected void frontUpdate() {

    }

    @Override
    public Rectangular getBody() {
        return table;
    }

    @Override
    public Num x() {
        return table.x();
    }

    @Override
    public Num y() {
        return table.y();
    }
}
