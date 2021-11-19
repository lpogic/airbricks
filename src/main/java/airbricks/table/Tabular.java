package airbricks.table;

import bricks.slab.Shape;
import bricks.var.Source;
import bricks.var.Var;
import bricks.var.num.NumPull;
import bricks.var.num.NumSource;

public interface Tabular extends Shape {
    Tabular sector(int column, int row);
    Tabular sector(int left, int right, int top, int bottom);

    class Column {
        NumPull left;
        NumPull width;

        public Column(Source<Number> width) {
            this.width = Var.num(width);
            this.left = Var.num(left);
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

        public NumPull width() {
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
        NumPull top;
        NumPull height;

        public Row(Source<Number> height) {
            this.height = Var.num(height);
            this.top = Var.num(0);
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

        public NumPull height() {
            return height;
        }

        public boolean contains(float y) {
            float t = top.getFloat();
            return y > t && y < t + height.getFloat();
        }
    }
}
