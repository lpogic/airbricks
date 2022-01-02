package airbricks.table;

import bricks.Location;
import bricks.trait.Source;
import bricks.trait.Traits;
import bricks.trait.number.NumberTrait;
import bricks.trait.number.NumberSource;
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
        public NumberSource x() {
            return () -> (right().getFloat() + left().getFloat()) / 2;
        }

        @Override
        public NumberSource y() {
            return () -> (bottom().getFloat() + top().getFloat()) / 2;
        }

        @Override
        public NumberSource width() {
            return () -> right().getFloat() - left().getFloat();
        }

        @Override
        public NumberSource height() {
            return () -> bottom().getFloat() - top().getFloat();
        }

        @Override
        public NumberSource left() {
            return columns.get(l).left();
        }

        @Override
        public NumberSource right() {
            return columns.get(r).right();
        }

        @Override
        public NumberSource top() {
            return rows.get(t).top();
        }

        @Override
        public NumberSource bottom() {
            return rows.get(b).bottom();
        }
    }

    final NumberTrait left;
    final NumberTrait top;
    final NumberTrait width;
    final NumberTrait height;
    List<Column> columns = new ArrayList<>();
    List<Row> rows = new ArrayList<>();

    public Table() {

        width = Traits.num(() -> {
            float acc = 0f;
            for (var c : columns) {
                acc += c.width.get().floatValue();
            }
            return acc;
        });

        height = Traits.num(() -> {
            float acc = 0f;
            for (var r : rows) {
                acc += r.height.get().floatValue();
            }
            return acc;
        });

        left = Traits.num(0);
        top = Traits.num(0);
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
            public NumberSource x() {
                return c.x();
            }

            @Override
            public NumberSource y() {
                return r.y();
            }

            @Override
            public NumberSource width() {
                return c.width();
            }

            @Override
            public NumberSource height() {
                return r.height();
            }

            @Override
            public NumberSource left() {
                return c.left();
            }

            @Override
            public NumberSource right() {
                return c.right();
            }

            @Override
            public NumberSource top() {
                return r.top();
            }

            @Override
            public NumberSource bottom() {
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
                $.reset(new Column(Traits.set($.asExpected())));
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
                $.reset(new Column(Traits.set($.asExpected())));
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
                $.reset(new Row(Traits.set($.asExpected())));
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
    public NumberTrait x() {
        return new NumberTrait() {
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

    public NumberTrait y() {
        return new NumberTrait() {
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

    public NumberSource width() {
        return width;
    }

    public NumberSource height() {
        return height;
    }

    public NumberTrait left() {
        return left;
    }

    public NumberTrait right() {
        return new NumberTrait() {
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

    public NumberTrait top() {
        return top;
    }

    public NumberTrait bottom() {
        return new NumberTrait() {
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
