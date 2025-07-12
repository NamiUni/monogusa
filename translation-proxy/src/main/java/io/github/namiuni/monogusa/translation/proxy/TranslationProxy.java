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

import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceSection;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A builder for proxied translation interfaces.
 */
@NullMarked
public final class TranslationProxy {

    private TranslationProxy() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * The process of building a translation proxy.
     *
     * @return the translation proxy builder
     */
    public static IProxy builder() {
        return new Builder<>();
    }

    /**
     * The specifying the translation interface to be proxied.
     */
    public interface IProxy {

        /**
         * Sets the translation interface that declares translation methods.
         *
         * @param interfaces the translation interface to be implemented by a proxy
         * @param <I>            the type of the translation interface
         * @return the loadable builder stage
         */
        <I> ILoadable<I> proxy(Class<I> interfaces);
    }

    /**
     * The configuring proxy options and creating the translation proxy.
     *
     * @param <I> the type of the translation interface
     */
    public interface ILoadable<I> {

        /**
         * Configures how method arguments are resolved into placeholders.
         *
         * @param arguments a consumer to configure the {@link ArgumentResolver.Builder}
         * @return the current builder instance
         */
        ILoadable<I> arguments(Consumer<ArgumentResolver.Builder> arguments);

        /**
         * Sets the {@link ComponentSender} used when a proxied method returns {@code void}.
         * Defaults to {@link ComponentSender#SIMPLE}.
         *
         * @param sender the component sender
         * @return the current builder instance
         */
        ILoadable<I> sender(ComponentSender sender);

        /**
         * Create the translation proxy.
         *
         * @return the translation proxy
         */
        I create();
    }

    static final class Builder<I> implements IProxy, ILoadable<I> {

        final ArgumentResolver.Builder argumentBuilder = ArgumentResolver.builder();
        private ComponentSender componentSender = ComponentSender.SIMPLE;
        private @Nullable Class<I> proxyInterfaces;

        @Override
        @SuppressWarnings("unchecked")
        public <U> ILoadable<U> proxy(final Class<U> interfaces) {
            if (!interfaces.isInterface()) {
                throw new IllegalArgumentException("proxy(Class<I>) only accepts interfaces. Provided: " + interfaces.getName());
            }

            this.proxyInterfaces = (Class<I>) interfaces;
            return (ILoadable<U>) this;
        }

        @Override
        public ILoadable<I> arguments(final Consumer<ArgumentResolver.Builder> arguments) {
            arguments.accept(this.argumentBuilder);
            return this;
        }

        @Override
        public ILoadable<I> sender(final ComponentSender sender) {
            this.componentSender = sender;
            return this;
        }

        @Override
        public I create() {
            // Capture the builder's state into final local variables for thread safety.
            final Class<I> interfaces = Objects.requireNonNull(this.proxyInterfaces, "Translation interface must be provided");
            final ArgumentResolver arguments = this.argumentBuilder.build();
            final ComponentSender sender = this.componentSender;

            // 1. Pre-scan all methods for maximum runtime performance.
            final Map<Method, ScannedMethod> scannedMethods = new ConcurrentHashMap<>();
            ScannedMethod.scanRecursively(scannedMethods, interfaces);

            // 2. Determine the root key prefix from the interface annotation.
            final ResourceSection rootSection = interfaces.getAnnotation(ResourceSection.class);
            final String rootPrefix = rootSection != null ? rootSection.prefix() + rootSection.delimiter() : "";

            // 3. Create the root InvocationHandler with the captured, immutable state.
            final InvocationHandler rootHandler = new TranslationInvocationHandler(
                    rootPrefix,
                    arguments,
                    sender,
                    Collections.unmodifiableMap(scannedMethods)
            );

            // 4. Create the initial proxy instance.
            @SuppressWarnings("unchecked")
            final I proxyInstance = (I) Proxy.newProxyInstance(
                    interfaces.getClassLoader(),
                    new Class<?>[] {interfaces},
                    rootHandler);

            // 5. Finish!!
            return proxyInstance;
        }
    }
}
