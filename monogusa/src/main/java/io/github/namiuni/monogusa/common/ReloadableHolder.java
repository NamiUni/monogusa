/*
 * monogusa
 *
 * Copyright (c) 2025. Namiu (うにたろう)
 *                     Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.namiuni.monogusa.common;

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
public sealed interface ReloadableHolder<T> extends Supplier<T> permits ReloadableHolderImpl {

    /**
     * Creates a simple {@code ReloadableHolder} backed by the given instantiation.
     *
     * <p>Each call to {@link #reload()} on the returned holder will invoke
     * {@link Supplier#get()} on the provided supplier to fetch a new value.</p>
     *
     * @param    instantiation the supplier to be used for initial creation and subsequent reloads
     * @param    <T> the type of value
     * @return   a new {@code ReloadableHolder} instance
     */
    static <T> ReloadableHolder<T> simple(final Instantiation<T> instantiation) {
        return new ReloadableHolderImpl<>(instantiation);
    }

    /**
     * Reloads the held value by re-invoking the underlying data source.
     * After this method completes, subsequent calls to {@link #get()} will
     * return the newly loaded value.
     */
    void reload();
}
