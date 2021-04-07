package airbricks.model;

import bricks.var.Var;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AutoState<T> implements Var<T> {

    public final Var<T> formal;
    public final Var<T> inner;

    public AutoState(Var<T> formal, Var<T> inner) {
        this.formal = formal;
        this.inner = inner;
    }

    @Override
    public void set(T t) {
        formal.set(t);
    }

    @Override
    public void let(Supplier<T> supplier) {
        formal.let(supplier);
    }

    @Override
    public void let(Consumer<T> consumer) {

    }

    @Override
    public void let(Consumer<T> consumer, Supplier<T> supplier) {

    }

    @Override
    public void reset(T value) {

    }

    @Override
    public T get() {
        return inner.get();
    }
}
