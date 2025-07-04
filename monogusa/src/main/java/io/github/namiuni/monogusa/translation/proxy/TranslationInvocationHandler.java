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

import io.github.namiuni.monogusa.translation.annotation.MessageKey;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// TODO: WIP
@NullMarked
final class TranslationInvocationHandler implements InvocationHandler {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private final ArgumentResolver argumentResolver;
    private final ComponentSender componentSender;

    TranslationInvocationHandler(
            final ArgumentResolver argumentResolver,
            final ComponentSender componentSender
    ) {
        this.argumentResolver = argumentResolver;
        this.componentSender = componentSender;
    }

    @Override
    public @Nullable Object invoke(final Object proxy, final Method method, final @Nullable Object @Nullable [] args) throws Throwable {

        // FIXME
        if (method.getDeclaringClass().equals(Object.class)) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "Proxy for " + proxy.getClass().getInterfaces()[0].getName();
                default -> method.invoke(this, args);
            };
        }

        final Object[] arguments = args == null ? EMPTY_OBJECT_ARRAY : Arrays.stream(args)
                .map(arg -> Objects.requireNonNull(arg, "Argument in array must not be null")) // TODO: exception handling
                .toArray();

        // Get the message key
        final MessageKey annotation = method.getAnnotation(MessageKey.class);
        if (annotation == null || annotation.value().isEmpty() || annotation.value().isBlank()) {
            throw new UnsupportedOperationException();  // TODO: exception handling
        }
        final String messageKey = annotation.value();

        // Create invocation context
        final Audience audience = arguments[0] instanceof Audience it ? it : Audience.empty();
        final InvocationContext context = new InvocationContext(method, arguments, audience);

        // Create translatable component
        final TagResolver tagResolver = this.argumentResolver.resolve(context);
        final TranslatableComponent component = Component.translatable(messageKey, Argument.tagResolver(tagResolver));

        // Post process
        return this.postProcess(method.getReturnType(), context.audience(), component);
    }

    private @Nullable Object postProcess(final Class<?> returnType, final Audience audience, final TranslatableComponent component) throws IllegalArgumentException {
        if (returnType == void.class || returnType == Void.class) {
            this.componentSender.send(audience, component);
            return null;
        } else if (returnType == ComponentLike.class || returnType == Component.class || returnType == TranslatableComponent.class) {
            return component;
        }

        throw new IllegalArgumentException(); // TODO exception handling
    }
}
