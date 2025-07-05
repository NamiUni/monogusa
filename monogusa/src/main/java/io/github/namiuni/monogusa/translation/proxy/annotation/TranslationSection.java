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
package io.github.namiuni.monogusa.translation.proxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NullMarked;

/**
 * Marks an interface or a method returning an interface as a translation section.
 * The value of this annotation is used as a prefix for nested translation keys.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@NullMarked
public @interface TranslationSection {

    /**
     * The key prefix for this section.
     *
     * @return the section key prefix
     */
    String value();

    /**
     * The delimiter to be used after this section's key.
     *
     * @return the delimiter character
     */
    char delimiter() default '.';
}
