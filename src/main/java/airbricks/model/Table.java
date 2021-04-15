package airbricks.model;

import bricks.Coordinate;
import bricks.Coordinated;
import bricks.var.Source;
import bricks.var.Vars;
import bricks.var.special.Num;
import bricks.var.special.NumSource;
import suite.suite.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Table implements Tabular, Coordinate {

    public class Sector implements Tabular {
        int l;
        int r;
        int t;
        int b;

        public Sector(int left, int right, int top, int bottom) {
            this.l = left;
            this.r = right;
            this.t = top;
            this.b = bottom;
        }

        @Override
        public Tabular sector(int column, int row) {
            return Table.this.sector(l + column, t + row);
        }

        @Override
        public Tabular sector(int left, int right, int top, int bottom) {
            return Table.this.sector(l + left,
                    l + right,
                    t + top,
                    t + bottom);
        }

        @Override
        public NumSource x() {
            return () -> (right().getFloat() + left().getFloat()) / 2;
        }

        @Override
        public NumSource y() {
            return () -> (bottom().getFloat() + top().getFloat()) / 2;
        }

        @Override
        public NumSource width() {
            return () -> right().getFloat() - left().getFloat();
        }

        @Override
        public NumSource height() {
            return () -> bottom().getFloat() - top().getFloat();
        }

        @Override
        public NumSource left() {
            return columns.get(l).left();
        }

        @Override
        public NumSource right() {
            return columns.get(r).right();
        }

        @Override
        public NumSource top() {
            return rows.get(t).top();
        }

        @Override
        public NumSource bottom() {
            return rows.get(b).bottom();
        }
    }

    final Num left;
    final Num top;
    final Num width;
    final Num height;
    List<Column> columns = new ArrayList<>();
    List<Row> rows = new ArrayList<>();

    public Table() {

        width = Vars.num(() -> {
            float acc = 0f;
            for (var c : columns) {
                acc += c.width.get().floatValue();
            }
            return acc;
        });

        height = Vars.num(() -> {
            float acc = 0f;
            for (var r : rows) {
                acc += r.height.get().floatValue();
            }
            return acc;
        });

        left = Vars.num(0);
        top = Vars.num(0);
    }

    @Override
    public Tabular sector(int column, int row) {
        return new Tabular() {
            final Column c = columns.get(column);
            final Row r = rows.get(row);

            @Override
            public Tabular sector(int column, int row) {
                if(column != 0 || row != 0) throw new RuntimeException("Index out of range");
                return this;
            }

            @Override
            public Tabular sector(int left, int right, int top, int bottom) {
                if(left < 0 || left > right || right > 0 ||
                        top < 0 || top > bottom || bottom > 0) throw new RuntimeException();
                return this;
            }

            @Override
            public NumSource x() {
                return c.x();
            }

            @Override
            public NumSource y() {
                return r.y();
            }

            @Override
            public NumSource width() {
                return c.width();
            }

            @Override
            public NumSource height() {
                return r.height();
            }

            @Override
            public NumSource left() {
                return c.left();
            }

            @Override
            public NumSource right() {
                return c.right();
            }

            @Override
            public NumSource top() {
                return r.top();
            }

            @Override
            public NumSource bottom() {
                return r.bottom();
            }
        };
    }

    @Override
    public Sector sector(int left, int right, int top, int bottom) {
        return new Sector(left, right, top, bottom);
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
                    col.left.let(prev.right());
                } else {
                    col.left.let(left());
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
                    row.top.let(prev.bottom());
                } else {
                    row.top.let(top());
                }
                rows.add(row);
            }
        }
    }

    @Override
    public Num x() {
        return new Num() {
            @Override
            public void let(Supplier<Number> s) {
                left.let(() -> s.get().floatValue() - width.getFloat() / 2);
            }

            @Override
            public Number get() {
                return left.getFloat() + width.getFloat() / 2;
            }
        };
    }

    public Num y() {
        return new Num() {
            @Override
            public void let(Supplier<Number> s) {
                top.let(() -> s.get().floatValue() - height.getFloat() / 2);
            }

            @Override
            public Number get() {
                return top.getFloat() + height.getFloat() / 2;
            }
        };
    }

    public NumSource width() {
        return width;
    }

    public NumSource height() {
        return height;
    }

    public Num left() {
        return left;
    }

    public Num right() {
        return new Num() {
            @Override
            public void let(Supplier<Number> s) {
                left.let(() -> s.get().floatValue() - width.getFloat());
            }

            @Override
            public Number get() {
                return left.getFloat() + width.getFloat();
            }
        };
    }

    public Num top() {
        return top;
    }

    public Num bottom() {
        return new Num() {
            @Override
            public void let(Supplier<Number> s) {
                top.let(() -> s.get().floatValue() - height.getFloat());
            }

            @Override
            public Number get() {
                return top.getFloat() + height.getFloat();
            }
        };
    }
}
