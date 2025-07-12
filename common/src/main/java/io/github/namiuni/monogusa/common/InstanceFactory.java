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

/**
 * A factory responsible for creating a new instance of a service or component.
 * It's expected that each call to {@link #create()} produces a fresh instance.
 *
 * @param <T> the type of instance to create
 */
@FunctionalInterface
public interface InstanceFactory<T> {

    /**
     * Creates a new instance.
     *
     * @return a new instance of type T
     */
    T create();
}
