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
package io.github.namiuni.monogusa.translation.proxy;

import net.kyori.adventure.text.ComponentLike;
import org.jspecify.annotations.NullMarked;

/**
 * Resolves a single object of a specific type into a representation that can be used as a placeholder.
 *
 * @param <T> the type of value to resolve
 */
@FunctionalInterface
@NullMarked
public interface TypeResolver<T> {
    /**
     * Resolves a value into a {@link ComponentLike}.
     *
     * @param value the value of the argument, guaranteed to be non-null
     * @return a {@code ComponentLike} representing the resolved placeholder
     */
    ComponentLike resolve(T value);
}
