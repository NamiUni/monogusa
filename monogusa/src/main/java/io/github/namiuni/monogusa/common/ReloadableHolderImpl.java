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
import org.jspecify.annotations.NullMarked;

@NullMarked
final class ReloadableHolderImpl<T> implements ReloadableHolder<T> {

    private final Instantiation<T> instantiation;
    private final AtomicReference<T> reference;

    ReloadableHolderImpl(final Instantiation<T> instantiation) {
        this.instantiation = instantiation;
        this.reference = new AtomicReference<>(instantiation.instantiate());
    }

    @Override
    public void reload() {
        this.reference.set(this.instantiation.instantiate());
    }

    @Override
    public T get() {
        return this.reference.get();
    }
}
