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
package io.github.namiuni.monogusa.configuration;

import java.io.Serial;
import org.jspecify.annotations.NullUnmarked;
import org.spongepowered.configurate.ConfigurateException;

/**
 * Wraps an ConfigurateException with an unchecked exception.
 */
@NullUnmarked
@SuppressWarnings("unused")
public class UncheckedConfigurateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5943062407038988900L;

    /**
     * Constructs an instance of this class.
     *
     * @param   cause the {@code IOException}
     * @throws  NullPointerException if the cause is {@code null}
     */
    public UncheckedConfigurateException(final ConfigurateException cause) {
        super(cause);
    }

    /**
     * Constructs an instance of this class.
     *
     * @param   message the detail message, can be null
     * @param   cause the {@code ConfigurateException}
     * @throws  NullPointerException if the cause is {@code null}
     */
    public UncheckedConfigurateException(final String message, final ConfigurateException cause) {
        super(message, cause);
    }
}
