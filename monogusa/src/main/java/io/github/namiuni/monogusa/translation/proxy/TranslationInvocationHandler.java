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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class TranslationInvocationHandler implements InvocationHandler {

    private static final MethodHandles.Lookup PRIVATE_LOOKUP; // This is necessary to invoke default methods on interfaces.
    private static final Object[] EMPTY_OBJECT_ARRAY;

    static {
        try {
            PRIVATE_LOOKUP = MethodHandles.privateLookupIn(MethodHandles.lookup().lookupClass(), MethodHandles.lookup());
            EMPTY_OBJECT_ARRAY = new Object[0];
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final String keyPrefix;
    private final ArgumentResolver argumentResolver;
    private final ComponentSender componentSender;
    private final Map<Method, ScannedMethod> scannedMethods;

    TranslationInvocationHandler(
            final String keyPrefix,
            final ArgumentResolver argumentResolver,
            final ComponentSender componentSender,
            final Map<Method, ScannedMethod> scannedMethods
    ) {
        this.keyPrefix = keyPrefix;
        this.argumentResolver = argumentResolver;
        this.componentSender = componentSender;
        this.scannedMethods = scannedMethods;
    }

    @Override
    public @Nullable Object invoke(final Object proxy, final Method method, final @Nullable Object @Nullable [] nullableArgs) throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();

        // 1. Handle methods from the Object class to ensure correct proxy behavior.
        if (declaringClass.equals(Object.class)) {
            return switch (method.getName()) {
                case "equals" -> nullableArgs != null && nullableArgs.length == 1 && proxy == nullableArgs[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "TranslationProxy<" + proxy.getClass().getInterfaces()[0].getSimpleName() + ">";
                default -> throw new UnsupportedOperationException("Unsupported Object method: %s".formatted(method.getName()));
            };
        }

        // 2. Handle default interface methods.
        if (method.isDefault()) {
            return PRIVATE_LOOKUP.unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(nullableArgs);
        }

        // 3. Get pre-scanned method information from the cache for high performance.
        final ScannedMethod scanned = this.scannedMethods.get(method);
        if (scanned == null) {
            // This should not happen if the scanning process is correct.
            throw new IllegalStateException("Method not scanned, or it's not a valid translation method: %s".formatted(method));
        }

        // 4. Handle nested sections by creating a child proxy.
        if (scanned.sectionAnnotation() != null) {
            final String nextPrefix = this.keyPrefix + scanned.key() + scanned.sectionAnnotation().delimiter();
            final InvocationHandler childHandler = new TranslationInvocationHandler(
                    nextPrefix,
                    this.argumentResolver,
                    this.componentSender,
                    this.scannedMethods
            );

            return Proxy.newProxyInstance(
                    method.getReturnType().getClassLoader(),
                    new Class<?>[]{method.getReturnType()},
                    childHandler
            );
        }

        // 5. Process a standard translation method.
        final String finalKey = this.keyPrefix + scanned.key();
        final InvocationContext context = this.createContext(method.getParameters(), nullableArgs);
        final TagResolver tagResolver = this.argumentResolver.resolve(context);
        final Component component = Component.translatable(finalKey, Argument.tagResolver(tagResolver));

        // 6. Send the component or return it based on the method's return type.
        final Class<?> returnType = method.getReturnType();
        if (returnType == void.class || returnType == Void.class) {
            this.componentSender.send(context.audience(), component);
            return null;
        } else {
            return component;
        }
    }

    private InvocationContext createContext(final Parameter[] parameters, final @Nullable Object @Nullable [] nullableArgs) {
        final Object[] args = nullableArgs == null ? EMPTY_OBJECT_ARRAY : nullableArgs;

        final Audience audience;
        if (args.length > 0 && args[0] instanceof Audience foundAudience) {
            audience = foundAudience;
        } else {
            audience = Audience.empty();
        }

        final Map<Parameter, @Nullable Object> arguments = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            // Ensure we don't go out of bounds if args is shorter than parameters
            arguments.put(parameters[i], i < args.length ? args[i] : null);
        }
        return new InvocationContext(Collections.unmodifiableMap(arguments), audience);
    }
}
