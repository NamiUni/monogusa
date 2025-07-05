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

import io.github.namiuni.monogusa.translation.proxy.annotation.TranslationKey;
import io.github.namiuni.monogusa.translation.proxy.annotation.TranslationSection;
import java.lang.reflect.Method;
import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A data carrier for pre-scanned information about a proxied method.
 *
 * @param key the translation key or section name
 * @param sectionAnnotation the {@code TranslationSection} annotation, if present
 */
@NullMarked
record ScannedMethod(
        String key,
        @Nullable TranslationSection sectionAnnotation
) {
    /**
     * Scans an entire interface recursively and populates the cache.
     *
     * @param cache the map to store scanned methods
     * @param interfaceClass the interface to scan
     */
    static void scanRecursively(final Map<Method, ScannedMethod> cache, final Class<?> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Target must be an interface: " + interfaceClass.getName());
        }

        for (final Method method : interfaceClass.getMethods()) {
            if (method.isDefault() || method.getDeclaringClass().equals(Object.class) || cache.containsKey(method)) {
                continue;
            }

            final TranslationKey keyAnnotation = method.getAnnotation(TranslationKey.class);
            final TranslationSection sectionAnnotation = method.getAnnotation(TranslationSection.class);

            if (keyAnnotation != null && sectionAnnotation != null) {
                throw new IllegalStateException("Method " + method.getName() + " cannot be annotated with both @TranslationKey and @TranslationSection");
            }

            if (keyAnnotation != null) {
                cache.put(method, new ScannedMethod(keyAnnotation.value(), null));
            } else if (sectionAnnotation != null) {
                final Class<?> returnType = method.getReturnType();
                if (!returnType.isInterface()) {
                    throw new IllegalStateException("@TranslationSection method must return an interface: " + method.getName());
                }
                cache.put(method, new ScannedMethod(sectionAnnotation.value(), sectionAnnotation));
                // Recursively scan subinterfaces
                scanRecursively(cache, returnType);
            }
        }
    }
}
