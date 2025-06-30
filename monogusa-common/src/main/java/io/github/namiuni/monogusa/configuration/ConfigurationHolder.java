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

import io.github.namiuni.monogusa.common.ReloadableHolder;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

/**
 * A factory and builder for creating a {@link ReloadableHolder} for a
 * configuration managed by SpongePowered Configurate.
 *
 * <p>This class provides a fluent step-builder to construct a holder that can
 * load, parse, and reload a configuration file using a user-provided
 * {@link ConfigurationLoader}.</p>
 */
@NullMarked
public final class ConfigurationHolder {

    private ConfigurationHolder() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Starts the process of building a reloadable configuration holder.
     *
     * @param   <L> the type of the ConfigurationLoader
     * @return  the first step of the builder
     */
    public static <L extends ConfigurationLoader<? extends ConfigurationNode>> IConfigurationLoader<L> builder() {
        return new Builder<>();
    }

    /**
     * The first step: specifying the {@link ConfigurationLoader}.
     *
     * @param   <L> the type of the ConfigurationLoader
     */
    public interface IConfigurationLoader<L extends ConfigurationLoader<? extends ConfigurationNode>> {

        /**
         * Sets the {@link ConfigurationLoader} instance to be used.
         *
         * @param    loader the fully configured loader instance
         * @return   the next step of the builder
         */
        IConfigurationClass loader(L loader);

        /**
         * Sets a supplier that provides the {@link ConfigurationLoader} instance.
         *
         * <p>This is useful for lazy initialization or for constructing the loader
         * using its own builder within a lambda.</p>
         * <pre>{@code
         * .loader(() -> YamlConfigurationLoader.builder()
         *     .path(path)
         *     .build())
         * }</pre>
         *
         * @param    loaderSupplier a supplier for the fully configured loader instance
         * @return   the next step of the builder
         */
        IConfigurationClass loader(Supplier<L> loaderSupplier);
    }

    /**
     * The second step: specifying the target configuration class.
     */
    public interface IConfigurationClass {

        /**
         * Sets the target class for deserialization.
         *
         * <p>The generic type of the final holder is inferred from this class.</p>
         *
         * @param    clazz the target class to map the configuration to
         * @param    <C>   the type of the configuration class
         * @return   the final, loadable builder stage
         */
        <C> ILoadable<C> clazz(Class<C> clazz);
    }

    /**
     * The final step: applying transformations and creating the holder.
     *
     * @param <C> the type of the configuration class.
     */
    public interface ILoadable<C> {

        /**
         * (Optional) Applies a {@link ConfigurationTransformation} to the configuration node
         * after loading, typically used for migrating older configuration formats.
         *
         * @param    transformation The transformation to apply
         * @return   the current builder instance for further chaining
         */
        ILoadable<C> transformation(ConfigurationTransformation transformation);

        /**
         * Creates the {@link ReloadableHolder}. This method triggers the initial
         * load of the configuration.
         *
         * @return   a fully configured, reloadable holder for the configuration
         * @throws   UncheckedConfigurateException if the initial load fails
         */
        ReloadableHolder<C> create() throws UncheckedConfigurateException;

        /**
         * Creates the {@link ReloadableHolder} with a custom message for exceptions.
         * This method triggers the initial load of the configuration.
         *
         * @param    exceptionMessage a custom message to include if an exception is thrown
         * @return   a fully configured, reloadable holder for the configuration
         * @throws   UncheckedConfigurateException if the initial load fails
         */
        ReloadableHolder<C> create(String exceptionMessage) throws UncheckedConfigurateException;
    }

    private static final class Builder<L extends ConfigurationLoader<? extends ConfigurationNode>, C> implements IConfigurationLoader<L>, IConfigurationClass, ILoadable<C> {

        private @SuppressWarnings("NotNullFieldNotInitialized") Supplier<L> loaderSupplier;
        private @SuppressWarnings("NotNullFieldNotInitialized") Class<C> clazz;
        private @Nullable ConfigurationTransformation transformation;

        @Override
        public IConfigurationClass loader(final L loader) {
            Objects.requireNonNull(loader, "loader");
            this.loaderSupplier = () -> loader;
            return this;
        }

        @Override
        public IConfigurationClass loader(final Supplier<L> loaderSupplier) {
            this.loaderSupplier = Objects.requireNonNull(loaderSupplier, "loaderSupplier");
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> ILoadable<T> clazz(final Class<T> clazz) {
            this.clazz = (Class<C>) Objects.requireNonNull(clazz, "clazz");
            return (ILoadable<T>) this;
        }

        @Override
        public ILoadable<C> transformation(final ConfigurationTransformation transformation) {
            this.transformation = transformation;
            return this;
        }

        @Override
        public ReloadableHolder<C> create() throws UncheckedConfigurateException {
            return this.create(null);
        }

        @Override
        public ReloadableHolder<C> create(@Nullable final String exceptionMessage) throws UncheckedConfigurateException {
            final Supplier<C> loadingAction = () -> {
                try {
                    return this.loadInternal();
                } catch (final ConfigurateException exception) {
                    throw new UncheckedConfigurateException(exceptionMessage, exception);
                }
            };

            return ReloadableHolder.simple(loadingAction);
        }

        private C loadInternal() throws ConfigurateException {
            final ConfigurationLoader<? extends ConfigurationNode> loader = this.loaderSupplier.get();
            final ConfigurationNode root = loader.load();

            if (!this.clazz.isAnnotationPresent(ConfigSerializable.class)) {
                throw new ConfigurateException(root, "%s not marked with @ConfigSerializable".formatted(this.clazz.getName()));
            }

            if (this.transformation != null) {
                this.transformation.apply(root);
            }

            final C config = root.get(this.clazz);
            if (config == null) {
                throw new ConfigurateException(root, "Failed to deserialize %s from node".formatted(this.clazz.getName()));
            }

            loader.save(root);

            return config;
        }
    }
}
