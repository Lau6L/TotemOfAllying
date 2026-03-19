package io.github.lau6l.totem_of_allying.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;

import java.util.UUID;
import java.util.function.Consumer;

public record AlliedEntityComponent(UUID uuid, String typeOrName) implements TooltipAppender {
    public static final Codec<AlliedEntityComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Uuids.CODEC.fieldOf("uuid").forGetter(AlliedEntityComponent::uuid),
                    Codec.STRING.fieldOf("name").forGetter(AlliedEntityComponent::typeOrName)
            ).apply(instance, AlliedEntityComponent::new)
    );
    public static final PacketCodec<RegistryByteBuf, AlliedEntityComponent> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC,
            AlliedEntityComponent::uuid,
            PacketCodecs.STRING,
            AlliedEntityComponent::typeOrName,
            AlliedEntityComponent::new
    );
    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(Text.of("§3Bound Entity: ").copy().append(Text.translatable(typeOrName)));
    }
}
