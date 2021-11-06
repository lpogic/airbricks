package airbricks.table;

import bricks.Location;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.special.NumPull;
import bricks.var.special.NumSource;
import suite.suite.Subject;
import suite.suite.Suite;
import suite.suite.util.Sequence;
import suite.suite.util.Series;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Table implements Tabular, Location {

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

    final NumPull left;
    final NumPull top;
    final NumPull width;
    final NumPull height;
    List<Column> columns = new ArrayList<>();
    List<Row> rows = new ArrayList<>();

    public Table() {

        width = Var.num(() -> {
            float acc = 0f;
            for (var c : columns) {
                acc += c.width.get().floatValue();
            }
            return acc;
        });

        height = Var.num(() -> {
            float acc = 0f;
            for (var r : rows) {
                acc += r.height.get().floatValue();
            }
            return acc;
        });

        left = Var.num(0);
        top = Var.num(0);
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

    public int addColumns(Series $cols) {
        int index = 0;
        for(var $ : $cols.list()) {

            if($.is(Source.class)) {
                $.reset(new Column($.asExpected()));
            } else if($.is(Number.class)) {
                $.reset(new Column(Var.pull($.asExpected())));
            }

            if($.is(Column.class)) {
                Column col = $.asExpected();
                index = columns.size();
                if(index > 0) {
                    Column prev = columns.get(index - 1);
                    col.left.let(prev.right());
                } else {
                    col.left.let(left());
                }
                columns.add(col);
            }
        }
        return index;
    }

    public int columnsSize() {
        return columns.size();
    }

    public void removeColumn(int index) {
        if(index >= columns.size()) return;
        if(index == 0) {
            if(columns.size() > 1) columns.get(1).left.let(left());
        } else if(index + 1 < columns.size()){
            columns.get(index + 1).left.let(columns.get(index - 1).right());
        }
        columns.remove(index);
    }

    public void insertColumns(int index, Series $cols) {
        if(index >= columns.size()) {
            addColumns($cols);
            return;
        }
        for(var $ : $cols.list()) {

            if($.is(Source.class)) {
                $.reset(new Column($.asExpected()));
            } else if($.is(Number.class)) {
                $.reset(new Column(Var.pull($.asExpected())));
            }

            if($.is(Column.class)) {
                Column col = $.asExpected();
                if(index > 0) {
                    Column prev = columns.get(index - 1);
                    col.left.let(prev.right());
                } else {
                    col.left.let(left());
                }
                columns.add(index, col);
                ++index;
            }
        }
        if(index > 0) {
            columns.get(index).left.let(columns.get(index - 1).right());
        }
    }

    public Column getColumn(int index) {
        return columns.get(index);
    }

    public Sequence<Column> columns() {
        return Sequence.ofEntire(columns);
    }

    public int indexOfContaining(float x) {
        float w = left.getFloat();
        if(x < w) return -1;
        int i = 0;
        for(var c : columns) {
            w += c.width.getFloat();
            if(x < w) return i;
        }
        return -1;
    }

    public void addRows(Subject $rows) {
        for(var $ : $rows) {
            if($.is(Suite.Auto.class)) $ = $.at();

            if($.is(Source.class)) {
                $.reset(new Row($.asExpected()));
            } else if($.is(Number.class)) {
                $.reset(new Row(Var.pull($.asExpected())));
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

    public int rowsSize() {
        return rows.size();
    }

    @Override
    public NumPull x() {
        return new NumPull() {
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

    public NumPull y() {
        return new NumPull() {
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

    public NumPull left() {
        return left;
    }

    public NumPull right() {
        return new NumPull() {
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

    public NumPull top() {
        return top;
    }

    public NumPull bottom() {
        return new NumPull() {
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
