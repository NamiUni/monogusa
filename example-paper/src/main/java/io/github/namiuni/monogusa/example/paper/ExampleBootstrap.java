/*
 * MonogusaPaper
 *
 * Copyright (c) 2025. Namiu (うにたろう)
 *                     Contributors []
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
package io.github.namiuni.monogusa.example.paper;

import io.github.namiuni.monogusa.common.ReloadableHolder;
import io.github.namiuni.monogusa.common.configuration.ConfigurationHolder;
import io.github.namiuni.monogusa.example.paper.configurations.PrimaryConfig;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import java.util.Objects;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

@NullMarked
@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class ExampleBootstrap implements PluginBootstrap {

    private @Nullable ReloadableHolder<PrimaryConfig> configHolder;

    @Override
    public void bootstrap(final BootstrapContext context) {
        this.initializeResources(context);
    }

    @Override
    public JavaPlugin createPlugin(final PluginProviderContext context) {
        Objects.requireNonNull(this.configHolder);
        return new ExamplePaper(this.configHolder);
    }

    private void initializeResources(final BootstrapContext context) {
        // Create ConfigurationLoader instance.
        final HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder()
                .defaultOptions(options -> options
                        .shouldCopyDefaults(true)
                        .serializers(builder -> {
                            final var adventureSerializer = ConfigurateComponentSerializer.builder()
                                    .scalarSerializer(MiniMessage.miniMessage())
                                    .outputStringComponents(true)
                                    .build()
                                    .serializers();
                            builder.registerAll(adventureSerializer);
                        }))
                .path(context.getDataDirectory().resolve("config.conf"))
                .build();

        // Create ReloadableHolder instance.
        this.configHolder = ConfigurationHolder
                .builder()
                .loader(configurationLoader)
                .clazz(PrimaryConfig.class)
                .create("Failed to load primary config");
    }
}
