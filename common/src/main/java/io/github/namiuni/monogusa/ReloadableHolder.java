package io.github.namiuni.monogusa;

import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;

/**
 * Represents a holder for a value that can be reloaded.
 *
 * <p>This interface extends {@link Supplier}, allowing it to serve as a source
 * for the contained value. The {@link #reload()} method provides a mechanism
 * to refresh the value from its original source.</p>
 *
 * @param <T> the type of value held
 */
@NullMarked
public interface ReloadableHolder<T> extends Supplier<T> {

    /**
     * Creates a simple {@code ReloadableHolder} backed by the given supplier.
     *
     * <p>Each call to {@link #reload()} on the returned holder will invoke
     * {@link Supplier#get()} on the provided supplier to fetch a new value.</p>
     *
     * @param    supplier the supplier to be used for initial creation and subsequent reloads
     * @param    <T> the type of value
     * @return   a new {@code ReloadableHolder} instance
     */
    static <T> ReloadableHolder<T> simple(final Supplier<T> supplier) {
        return new SimpleReloadableHolder<>(supplier);
    }

    /**
     * Reloads the held value by re-invoking the underlying data source.
     * After this method completes, subsequent calls to {@link #get()} will
     * return the newly loaded value.
     */
    void reload();
}
