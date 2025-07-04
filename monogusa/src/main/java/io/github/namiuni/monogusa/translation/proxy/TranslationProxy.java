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

import io.github.namiuni.monogusa.common.InstanceFactory;
import io.github.namiuni.monogusa.common.ReloadableHolder;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.translation.Translator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A factory for creating reloadable, proxied translation service interfaces.
 *
 * <p>This is the main entry point for creating a type-safe translation service.</p>
 */
@NullMarked
public final class TranslationProxy {

    private TranslationProxy() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Starts the process of building a reloadable translation service proxy.
     *
     * @return the first step of the builder
     */
    public static ITranslator builder() {
        return new Builder<>();
    }

    /**
     * The first step: specifying the {@link Translator} source.
     */
    public interface ITranslator {

        /**
         * Sets the factory that will create new {@link Translator} instances on reload.
         *
         * @param factory a factory for the translator instance
         * @return the next step of the builder
         */
        IService translator(InstanceFactory<Translator> factory);

        /**
         * Uses an existing {@link ReloadableHolder} as the source for the {@link Translator}.
         * This is useful for sharing a single reloader between multiple proxies.
         *
         * @param holder a reloadable holder for the translator instance
         * @return the next step of the builder
         */
        IService translator(ReloadableHolder<Translator> holder);
    }

    /**
     * The second step: specifying the service interface to be proxied.
     */
    public interface IService {

        /**
         * Sets the service interface that declares translation methods.
         *
         * @param serviceInterface the interface to be implemented by a dynamic proxy
         * @param <I>              the type of the service interface
         * @return the final, loadable builder stage
         */
        <I> ILoadable<I> proxy(Class<I> serviceInterface);
    }

    /**
     * The final step: configuring proxy options and creating the holder.
     *
     * @param <I> the type of the service interface
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
         * Creates the {@link ReloadableHolder} for the translation service.
         *
         * <p>The underlying {@link Translator} is reloaded on the first call to
         * {@link ReloadableHolder#get()} or {@link ReloadableHolder#reload()}.</p>
         *
         * @return a reloadable holder for the translation service proxy
         */
        ReloadableHolder<I> create();
    }

    private static final class Builder<I> implements ITranslator, IService, ILoadable<I> {
        private @Nullable ReloadableHolder<Translator> translatorHolder;
        private @Nullable Class<I> proxyInterface;
        private final ArgumentResolver.Builder argumentBuilder = ArgumentResolver.builder();
        private ComponentSender componentSender = ComponentSender.SIMPLE;

        @Override
        public IService translator(final InstanceFactory<Translator> factory) {
            this.translatorHolder = ReloadableHolder.of(factory);

            return this;
        }

        @Override
        public IService translator(final ReloadableHolder<Translator> translatorHolder) {
            this.translatorHolder = translatorHolder;

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> ILoadable<U> proxy(final Class<U> serviceInterface) {
            if (!serviceInterface.isInterface()) {
                throw new IllegalArgumentException("proxy(Class<I>) only accepts interfaces. Provided: " + serviceInterface.getName());
            }
            this.proxyInterface = (Class<I>) serviceInterface;

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
        public ReloadableHolder<I> create() {
            Objects.requireNonNull(this.translatorHolder, "A translator source must be provided");
            Objects.requireNonNull(this.proxyInterface, "A proxy interface must be provided");

            final InvocationHandler invocationHandler = new TranslationInvocationHandler(
                    this.argumentBuilder.build(),
                    this.componentSender);

            @SuppressWarnings("unchecked")
            final I proxyInstance = (I) Proxy.newProxyInstance(
                    this.proxyInterface.getClassLoader(),
                    new Class<?>[] {this.proxyInterface},
                    invocationHandler);

            final InstanceFactory<I> proxyAndLifecycleManager = () -> {
                this.translatorHolder.reload();
                return proxyInstance;
            };

            return ReloadableHolder.of(proxyAndLifecycleManager);
        }
    }
}
