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

import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceKey;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceSection;
import java.lang.reflect.Method;
import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A data carrier for pre-scanned information about a proxied method.
 *
 * @param key the translation key or section name
 * @param resourceSectionAnnotation the {@code Section} annotation, if present
 */
@NullMarked
record ScannedMethod(
        String key,
        @Nullable ResourceSection resourceSectionAnnotation
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

            final ResourceKey resourceKeyAnnotation = method.getAnnotation(ResourceKey.class);
            final ResourceSection resourceSectionAnnotation = method.getAnnotation(ResourceSection.class);

            if (resourceKeyAnnotation != null && resourceSectionAnnotation != null) {
                throw new IllegalStateException("Method " + method.getName() + " cannot be annotated with both @Key and @Section");
            }

            if (resourceKeyAnnotation != null) {
                cache.put(method, new ScannedMethod(resourceKeyAnnotation.value(), null));
            } else if (resourceSectionAnnotation != null) {
                final Class<?> returnType = method.getReturnType();
                if (!returnType.isInterface()) {
                    throw new IllegalStateException("@Section method must return an interface: " + method.getName());
                }
                cache.put(method, new ScannedMethod(resourceSectionAnnotation.prefix(), resourceSectionAnnotation));
                // Recursively scan subinterfaces
                scanRecursively(cache, returnType);
            }
        }
    }
}
