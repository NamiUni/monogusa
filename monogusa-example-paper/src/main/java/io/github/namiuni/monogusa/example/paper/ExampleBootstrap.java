/*
 * MonogusaExamplePaper
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

import com.google.common.base.CaseFormat;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.namiuni.monogusa.common.ReloadableHolder;
import io.github.namiuni.monogusa.configuration.ReloadableConfiguration;
import io.github.namiuni.monogusa.translation.proxy.TranslationProxy;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import java.util.Objects;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

@NullMarked
@SuppressWarnings({"UnstableApiUsage", "unused"})
public final class ExampleBootstrap implements PluginBootstrap {

    private @Nullable ReloadableHolder<ExampleConfiguration> configuration;
    private @Nullable ReloadableHolder<ExampleTranslation> translation;

    @Override
    public void bootstrap(final BootstrapContext context) {
        this.initializeConfiguration(context);
        this.initializeTranslation(context);
    }

    @Override
    public JavaPlugin createPlugin(final PluginProviderContext context) {
        Objects.requireNonNull(this.configuration);
        Objects.requireNonNull(this.translation);
        return new ExamplePaper(this.configuration, this.translation);
    }

    private void initializeConfiguration(final BootstrapContext context) {
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
        this.configuration = ReloadableConfiguration.builder()
                .loader(configurationLoader)
                .raw(ExampleConfiguration.class)
                .create();
    }

    private void initializeTranslation(final BootstrapContext bootstrapContext) {
        this.translation = TranslationProxy.builder()
                .translator(() -> MiniMessageTranslationStore.create(Key.key("monogusa", "message")))
                .proxy(ExampleTranslation.class)
                .arguments(arguments -> arguments
                        .keyFormatter(string -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, string))
                        .placeholders(audience -> {
                            final TagResolver.Builder tagBuilder = TagResolver.builder();
                            if (Bukkit.getPluginManager().isPluginEnabled("MiniPlaceholders")) {
                                tagBuilder.resolver(MiniPlaceholders.getAudienceGlobalPlaceholders(audience));
                                if (audience instanceof RelationalAudience relational) {
                                    tagBuilder.resolver(MiniPlaceholders.getRelationalPlaceholders(relational.audience(), relational.other()));
                                }
                            }
                            return tagBuilder.build();
                        })
                        .typeResolver(int.class, Component::text)
                        .typeResolver(World.class, world -> Component.text(world.getName()))
                        .typeResolver(Player.class, Player::displayName)
                        .keyResolver("Admin", (Player player) -> player.name().color(NamedTextColor.RED))
                )
                .sender((audience, component) -> {
                    if (audience instanceof RelationalAudience relational) {
                        relational.audience().sendMessage(component);
                    }
                })
                .create();
    }
}
