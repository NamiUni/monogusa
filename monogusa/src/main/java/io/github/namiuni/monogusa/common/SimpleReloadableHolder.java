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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class SimpleReloadableHolder<T> implements ReloadableHolder<T> {

    private final Supplier<T> supplier;
    private final AtomicReference<T> reference;

    SimpleReloadableHolder(final Supplier<T> supplier) {
        this.supplier = supplier;
        this.reference = new AtomicReference<>(supplier.get());
    }

    @Override
    public void reload() {
        this.reference.set(this.supplier.get());
    }

    @Override
    public T get() {
        return this.reference.get();
    }
}
