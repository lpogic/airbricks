package airbricks.table;

import bricks.graphic.Rectangular;
import bricks.var.Source;
import bricks.var.Vars;
import bricks.var.special.Num;
import bricks.var.special.NumSource;

public interface Tabular extends Rectangular {
    Tabular sector(int column, int row);
    Tabular sector(int left, int right, int top, int bottom);

    class Column {
        Num left;
        Num width;

        public Column(Source<Number> width) {
            this.width = Vars.num(width);
            this.left = Vars.num(left);
        }

        public NumSource x() {
            return () -> left.get().floatValue() + width.get().floatValue() / 2;
        }

        public NumSource left() {
            return left;
        }

        public NumSource right() {
            return () -> left.get().floatValue() + width.get().floatValue();
        }

        public Num width() {
            return width;
        }

        public boolean contains(float x) {
            float l = left.getFloat();
            return x > l && x < l + width.getFloat();
        }

        @Override
        public String toString() {
            return "Column{" +
                    "left=" + left.getFloat() +
                    ", width=" + width.getFloat() +
                    '}';
        }
    }

    class Row {
        Num top;
        Num height;

        public Row(Source<Number> height) {
            this.height = Vars.num(height);
            this.top = Vars.num(0);
        }

        public NumSource y() {
            return () -> top.get().floatValue() + height.get().floatValue() / 2;
        }

        public NumSource top() {
            return top;
        }

        public NumSource bottom() {
            return () -> top.get().floatValue() + height.get().floatValue();
        }

        public Num height() {
            return height;
        }

        public boolean contains(float y) {
            float t = top.getFloat();
            return y > t && y < t + height.getFloat();
        }
    }
}
