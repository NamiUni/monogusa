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

import java.lang.reflect.Method;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;

/**
 * Provides context about a method invocation within the translation proxy.
 * This is an immutable data carrier passed to resolvers.
 *
 * @param method the invoked method
 * @param args the arguments passed to the method
 * @param audience the audience resolved from the method arguments, or {@link Audience#empty()} if none was found
 */
@NullMarked
public record InvocationContext(Method method, Object[] args, Audience audience) {
}
