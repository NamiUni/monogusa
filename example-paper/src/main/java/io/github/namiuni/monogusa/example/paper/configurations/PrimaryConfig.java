package io.github.namiuni.monogusa.example.paper.configurations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@NullMarked
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
public final class PrimaryConfig {

    @Comment("minimessage for example")
    private Component miniMessage = MiniMessage.miniMessage().deserialize("<rainbow>HOCON is GOAT");

    public Component miniMessage() {
        return this.miniMessage;
    }
}
