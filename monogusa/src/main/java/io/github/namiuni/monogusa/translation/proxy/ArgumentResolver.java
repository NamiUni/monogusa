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

import com.google.common.base.CaseFormat;
import io.github.namiuni.monogusa.translation.annotation.PlaceholderKey;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A sophisticated resolver that converts an entire method invocation into a single,
 * composite {@link TagResolver}. It uses a collection of key-specific and type-specific
 * {@link TypeResolver}s to process method arguments.
 */
@NullMarked
public final class ArgumentResolver {

    private final UnaryOperator<String> keyFormatter;
    private final Map<String, TypeResolver<?>> keyResolvers;
    private final Map<Class<?>, TypeResolver<?>> typeResolvers;
    private final Function<Audience, TagResolver> placeholders;

    private ArgumentResolver(final Builder builder) {
        this.keyFormatter = builder.keyFormatter;
        this.keyResolvers = Collections.unmodifiableMap(builder.keyResolvers);
        this.typeResolvers = Collections.unmodifiableMap(builder.typeResolvers);
        this.placeholders = builder.placeholders;
    }

    /**
     * Creates a new builder for an {@code ArgumentResolver}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Resolves all arguments from an invocation context into a single composite {@link TagResolver}.
     *
     * @param context the invocation context
     * @return a single {@code TagResolver} containing all resolved placeholders
     */
    TagResolver resolve(final InvocationContext context) {
        final TagResolver.Builder tagBuilder = TagResolver.builder();
        tagBuilder.resolver(this.placeholders.apply(context.audience()));

        final Parameter[] parameters = context.method().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final String key = this.resolveKey(parameter);
            final Object value = Objects.requireNonNull(context.args()[i], "value");
            tagBuilder.resolver(this.resolveValue(key, value));
        }

        return tagBuilder.build();
    }

    private String resolveKey(final Parameter parameter) {
        final PlaceholderKey annotation = parameter.getAnnotation(PlaceholderKey.class);
        if (annotation == null || annotation.value().isEmpty() || annotation.value().isBlank()) {
            return this.keyFormatter.apply(parameter.getName());
        }

        return annotation.value();
    }

    @SuppressWarnings("PatternValidation")
    private TagResolver resolveValue(final String key, final Object value) {

        // 1. Prioritize key-specific resolvers
        final TypeResolver<?> keyResolver = this.keyResolvers.get(key);
        if (keyResolver != null) {
            return Placeholder.component(key, this.applyResolver(keyResolver, value));
        }

        // 2. Fall back to type-specific resolvers (including supertypes)
        final TypeResolver<?> typeResolver = this.findTypeResolver(value.getClass());
        if (typeResolver != null) {
            return Placeholder.component(key, this.applyResolver(typeResolver, value));
        }

        // 3. Handle special adventure types
        return switch (value) {
            case TagResolver tagResolver -> tagResolver;
            case ComponentLike componentLike -> Placeholder.component(key, componentLike);
            case Audience ignored -> TagResolver.empty(); // Ignore audiences
            default -> Placeholder.parsed(key, String.valueOf(value)); // Default to string representation
        };
    }

    private @Nullable TypeResolver<?> findTypeResolver(final Class<?> type) {

        // Exact match
        final TypeResolver<?> resolver = this.typeResolvers.get(type);
        if (resolver != null) return resolver;

        // Superclass and interface matching (can be expanded)
        for (final Map.Entry<Class<?>, TypeResolver<?>> entry : this.typeResolvers.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> ComponentLike applyResolver(final TypeResolver<T> resolver, final Object value) {
        return resolver.resolve((T) value);
    }

    /**
     * A builder for creating {@link ArgumentResolver} instances.
     */
    @SuppressWarnings("unused")
    public static final class Builder {
        private UnaryOperator<String> keyFormatter = string -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
        private final Map<String, TypeResolver<?>> keyResolvers = new ConcurrentHashMap<>();
        private final Map<Class<?>, TypeResolver<?>> typeResolvers = new ConcurrentHashMap<>();
        private Function<Audience, TagResolver> placeholders = audience -> TagResolver.empty();

        private Builder() {
        }

        /**
         * Sets the function to convert method parameter names into placeholder keys.
         * Defaults to converting lowerCamelCase to lower_underscore_case.
         *
         * @param keyFormatter the function to convert parameter names
         * @return this builder
         */
        public Builder keyFormatter(final UnaryOperator<String> keyFormatter) {
            this.keyFormatter = keyFormatter;

            return this;
        }

        /**
         * Registers a resolver for a specific placeholder key. This has the highest priority.
         *
         * @param key      the placeholder key
         * @param resolver the resolver for this key
         * @param <T>      the type
         * @return this builder
         */
        @SuppressWarnings("UnusedReturnValue")
        public <T> Builder keyResolver(final String key, final TypeResolver<T> resolver) {
            this.keyResolvers.put(key, resolver);

            return this;
        }

        /**
         * Registers a resolver for a specific type. This is used if no key-specific resolver is found.
         *
         * @param type     the class of the type to resolve
         * @param resolver the resolver for this type
         * @param <T>      the type
         * @return this builder
         */
        public <T> Builder typeResolver(final Class<T> type, final TypeResolver<T> resolver) {
            this.typeResolvers.put(type, resolver);

            return this;
        }

        /**
         * Integrates a provider of contextual placeholders (e.g., global or audience-specific).
         *
         * @param placeholders a function that provides a TagResolver based on the message receiver
         * @return this builder
         */
        public Builder placeholders(final Function<Audience, TagResolver> placeholders) {
            this.placeholders = placeholders;

            return this;
        }

        /**
         * Builds the {@link ArgumentResolver} with the configured settings.
         *
         * @return a new, immutable {@code ArgumentResolver} instance
         */
        ArgumentResolver build() {

            return new ArgumentResolver(this);
        }
    }
}
