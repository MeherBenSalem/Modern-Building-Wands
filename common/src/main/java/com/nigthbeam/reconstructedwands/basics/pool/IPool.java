package com.nigthbeam.reconstructedwands.basics.pool;

import org.jetbrains.annotations.Nullable;

/**
 * Pool interface for ordered or random item selection.
 */
public interface IPool<T> {
    void add(T element);

    void remove(T element);

    @Nullable
    T draw();

    void reset();
}
