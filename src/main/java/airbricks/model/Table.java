package airbricks.model;

import bricks.Point;
import bricks.XOrigin;
import bricks.YOrigin;
import bricks.graphic.Rectangular;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.Vars;
import suite.suite.Subject;

import java.util.ArrayList;
import java.util.List;

public class Table implements Rectangular {

    public static class Column {
        Source<Number> width;
        Source<Number> center;

        public Column(Source<Number> width) {
            this.width = width;
        }
    }

    public static class Row {
        Source<Number> height;
        Source<Number> center;

        public Row(Source<Number> height) {
            this.height = height;
        }
    }

    public static abstract class Cell implements Rectangular {
    }

    final Var<Point> position;
    final Var<Number> width;
    final Var<Number> height;
    final Var<XOrigin> xOrigin;
    final Var<YOrigin> yOrigin;
    List<Column> columns = new ArrayList<>();
    List<Row> rows = new ArrayList<>();

    public Table() {

        width = Vars.let(() -> {
            float acc = 0f;
            for (var c : columns) {
                acc += c.width.get().floatValue();
            }
            return acc;
        });

        height = Vars.let(() -> {
            float acc = 0f;
            for (var r : rows) {
                acc += r.height.get().floatValue();
            }
            return acc;
        });

        position = Vars.set(Point.zero());
        xOrigin = Vars.set(XOrigin.CENTER);
        yOrigin = Vars.set(YOrigin.CENTER);
    }

    public Cell getCell(int c, int r) {
        return new Cell() {
            final Column col = columns.get(c);
            final Row row = rows.get(r);

            @Override
            public Source<Point> position() {
                return () -> new Point(col.center.get(), row.center.get());
            }

            @Override
            public Source<XOrigin> xOrigin() {
                return () -> XOrigin.CENTER;
            }

            @Override
            public Source<YOrigin> yOrigin() {
                return () -> YOrigin.CENTER;
            }

            @Override
            public Source<Number> width() {
                return col.width;
            }

            @Override
            public Source<Number> height() {
                return row.height;
            }
        };
    }

    public void addColumns(Subject $cols) {
        for(var $ : $cols.eachIn()) {
            if($.is(Source.class)) {
                $.reset(new Column($.asExpected()));
            } else if($.is(Number.class)) {
                $.reset(new Column(Vars.set($.asExpected())));
            }

            if($.is(Column.class)) {
                Column col = $.asExpected();
                int size = columns.size();
                if(size > 0) {
                    Column prev = columns.get(size - 1);
                    col.center = Vars.let(() -> prev.center.get().floatValue() +
                            prev.width.get().floatValue() / 2 +
                            col.width.get().floatValue() / 2);
                } else {
                    col.center = Vars.let(() -> switch (xOrigin.get()) {
                        case LEFT -> position.get().x() +
                                col.width.get().floatValue() / 2;
                        case CENTER -> position.get().x() -
                                width.get().floatValue() / 2 +
                                col.width.get().floatValue() / 2;
                        case RIGHT -> position.get().x() -
                                width.get().floatValue() +
                                col.width.get().floatValue() / 2;
                    });
                }
                columns.add(col);
            }
        }
    }

    public void addRows(Subject $rows) {
        for(var $ : $rows.eachIn()) {
            if($.is(Source.class)) {
                $.reset(new Row($.asExpected()));
            } else if($.is(Number.class)) {
                $.reset(new Row(Vars.set($.asExpected())));
            }

            if($.is(Row.class)) {
                Row row = $.asExpected();
                int size = rows.size();
                if(size > 0) {
                    Row prev = rows.get(size - 1);
                    row.center = Vars.let(() -> prev.center.get().floatValue() +
                            prev.height.get().floatValue() / 2 +
                            row.height.get().floatValue() / 2);
                } else {
                    row.center = Vars.let(() -> switch (yOrigin.get()) {
                        case TOP -> position.get().y() +
                                row.height.get().floatValue() / 2;
                        case CENTER -> position.get().y() -
//                                height.get().floatValue() / 2 +
                                row.height.get().floatValue() / 2;
                        case BOTTOM -> position.get().y() -
                                height.get().floatValue() +
                                row.height.get().floatValue() / 2;
                    });
                }
                rows.add(row);
            }
        }
    }

    public Var<Point> position() {
        return position;
    }

    public Source<Number> width() {
        return width;
    }

    public Source<Number> height() {
        return height;
    }

    public Var<XOrigin> xOrigin() {
        return xOrigin;
    }

    public Var<YOrigin> yOrigin() {
        return yOrigin;
    }
}
