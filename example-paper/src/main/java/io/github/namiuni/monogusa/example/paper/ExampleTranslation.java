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
package io.github.namiuni.monogusa.example.paper;

import io.github.namiuni.monogusa.translation.annotation.Locales;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceBundle;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceKey;
import io.github.namiuni.monogusa.translation.annotation.annotations.ResourceValue;

@ResourceBundle(baseName = "translations/messages", prefix = "monogusa")
public interface ExampleTranslation {

    @ResourceKey
    @ResourceValue(locale = Locales.EN_US, content = "Configuration reloaded successfully.")
    @ResourceValue(locale = Locales.JA_JP, content = "設定の再読み込みに成功しました。")
    void commandReloadSuccess();
}
