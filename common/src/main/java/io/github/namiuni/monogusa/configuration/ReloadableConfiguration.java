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

import io.github.namiuni.monogusa.common.InstanceFactory;
import io.github.namiuni.monogusa.common.ReloadableHolder;
import java.util.Objects;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * A factory and builder for creating a {@link ReloadableHolder} for a
 * configuration managed by SpongePowered Configurate.
 *
 * <p>This class provides a fluent step-builder to construct a holder that can
 * load, parse, and reload a configuration file using a user-provided
 * {@link ConfigurationLoader}.</p>
 */
@NullMarked
public final class ReloadableConfiguration {

    private ReloadableConfiguration() {
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
        <C> ILoadable<C> raw(Class<C> clazz);
    }

    /**
     * The final step: applying transformations and creating the holder.
     *
     * @param <C> the type of the configuration class.
     */
    public interface ILoadable<C> {

        /**
         * Applies a function to the raw {@link ConfigurationNode} after loading
         * but before deserialization. This can be used for migrations, transformations,
         * or any custom node manipulation.
         *
         * @param nodeConsumer A consumer that accepts the root configuration node.
         * @return The current builder instance for further chaining.
         */
        ILoadable<C> postProcess(Consumer<ConfigurationNode> nodeConsumer);

        /**
         * Creates the {@link ReloadableHolder}. This method triggers the initial
         * load of the configuration.
         *
         * @return a fully configured, reloadable holder for the configuration
         * @throws UncheckedConfigurateException if the initial load fails
         */
        ReloadableHolder<C> create() throws UncheckedConfigurateException;
    }

    private static final class Builder<L extends ConfigurationLoader<? extends ConfigurationNode>, C> implements IConfigurationLoader<L>, IConfigurationClass, ILoadable<C> {

        private L loader;
        private @SuppressWarnings("NotNullFieldNotInitialized") Class<C> clazz;
        private @Nullable Consumer<ConfigurationNode> nodeConsumer;

        @Override
        public IConfigurationClass loader(final L loader) {
            Objects.requireNonNull(loader, "loader");
            this.loader = loader;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> ILoadable<T> raw(final Class<T> clazz) {
            Objects.requireNonNull(clazz, "clazz");
            this.clazz = (Class<C>) clazz;
            return (ILoadable<T>) this;
        }

        @Override
        public ILoadable<C> postProcess(final Consumer<ConfigurationNode> nodeConsumer) {
            this.nodeConsumer = nodeConsumer;
            return this;
        }

        public ReloadableHolder<C> create() throws UncheckedConfigurateException { // TODO: Seek the best exception handling.
            final InstanceFactory<@Nullable C> instantiate = () -> {
                try {
                    final ConfigurationNode rootNode = this.loader.load();

                    if (!this.clazz.isAnnotationPresent(ConfigSerializable.class)) {
                        throw new ConfigurateException(rootNode, "Not marked with @Serializable annotation: %s".formatted(this.clazz.getName()));
                    }

                    if (this.nodeConsumer != null) {
                        this.nodeConsumer.accept(rootNode);
                    }

                    final C config = rootNode.get(this.clazz);
                    if (config == null) {
                        throw new ConfigurateException(rootNode, "Failed to deserialize %s from node".formatted(this.clazz.getName()));
                    }
                    this.loader.save(rootNode);
                    return config;
                } catch (final ConfigurateException exception) {
                    throw new UncheckedConfigurateException("Failed to load configuration", exception);
                }
            };

            return ReloadableHolder.of(instantiate);
        }
    }
}
