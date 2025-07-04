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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

/**
 * A functional interface responsible for sending a rendered component to an audience.
 */
@NullMarked
@FunctionalInterface
public interface ComponentSender {

    /**
     * A simple sender that calls {@link Audience#sendMessage(Component)}.
     */
    ComponentSender SIMPLE = Audience::sendMessage;

    /**
     * Sends the component to the audience.
     *
     * @param audience the receiver of the message
     * @param component the fully rendered component to send
     */
    void send(Audience audience, Component component);
}
