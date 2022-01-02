package airbricks.table;

import bricks.slab.Shape;
import bricks.trait.Source;
import bricks.trait.Traits;
import bricks.trait.number.NumberTrait;
import bricks.trait.number.NumberSource;

public interface Tabular extends Shape {
    Tabular sector(int column, int row);
    Tabular sector(int left, int right, int top, int bottom);

    class Column {
        NumberTrait left;
        NumberTrait width;

        public Column(Source<Number> width) {
            this.width = Traits.num(width);
            this.left = Traits.num(left);
        }

        public NumberSource x() {
            return () -> left.get().floatValue() + width.get().floatValue() / 2;
        }

        public NumberSource left() {
            return left;
        }

        public NumberSource right() {
            return () -> left.get().floatValue() + width.get().floatValue();
        }

        public NumberTrait width() {
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
        NumberTrait top;
        NumberTrait height;

        public Row(Source<Number> height) {
            this.height = Traits.num(height);
            this.top = Traits.num(0);
        }

        public NumberSource y() {
            return () -> top.get().floatValue() + height.get().floatValue() / 2;
        }

        public NumberSource top() {
            return top;
        }

        public NumberSource bottom() {
            return () -> top.get().floatValue() + height.get().floatValue();
        }

        public NumberTrait height() {
            return height;
        }

        public boolean contains(float y) {
            float t = top.getFloat();
            return y > t && y < t + height.getFloat();
        }
    }
}
