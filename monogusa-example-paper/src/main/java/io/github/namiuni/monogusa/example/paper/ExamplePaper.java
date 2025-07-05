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

import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("unused")
public final class ExamplePaper extends JavaPlugin {

    private final Supplier<ExampleConfiguration> configuration;
    private final ExampleTranslation translation;

    // If the argument type is Supplier, the reload method will not be accidentally called.
    public ExamplePaper(
            final Supplier<ExampleConfiguration> configuration,
            final ExampleTranslation translation
    ) {
        this.configuration = configuration;
        this.translation = translation;
    }

    @Override
    public void onEnable() {
        this.translation.pluginEnableBroadCast(Bukkit.getConsoleSender(), this.configuration.get().miniMessage());
        super.onEnable(); // TODO: Register listeners
    }
}
