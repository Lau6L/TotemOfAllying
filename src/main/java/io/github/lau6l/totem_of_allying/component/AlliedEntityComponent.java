package io.github.lau6l.totem_of_allying.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;

import java.util.UUID;
import java.util.function.Consumer;

public record AlliedEntityComponent(UUID uuid, Text typeOrName) implements TooltipAppender {
    public static final Codec<AlliedEntityComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Uuids.CODEC.fieldOf("uuid").forGetter(AlliedEntityComponent::uuid),
                    TextCodecs.CODEC.fieldOf("name").forGetter(AlliedEntityComponent::typeOrName)
            ).apply(instance, AlliedEntityComponent::new)
    );
    public static final PacketCodec<RegistryByteBuf, AlliedEntityComponent> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC,
            AlliedEntityComponent::uuid,
            TextCodecs.PACKET_CODEC,
            AlliedEntityComponent::typeOrName,
            AlliedEntityComponent::new
    );
    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(
                Text.translatable("totem_of_allying.component.bonded_entity")
                        .formatted(Formatting.DARK_AQUA)
                        .append(": ")
                        .append(typeOrName)
        );
    }
}
