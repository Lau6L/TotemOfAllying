package io.github.lau6l.totem_of_allying.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record AlliedEntityComponent(UUID uuid, String name) {
    public static final Codec<AlliedEntityComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Uuids.CODEC.fieldOf("uuid").forGetter(AlliedEntityComponent::uuid),
                    Codec.STRING.fieldOf("name").forGetter(AlliedEntityComponent::name)
            ).apply(instance, AlliedEntityComponent::new)
    );
    public static final PacketCodec<RegistryByteBuf, AlliedEntityComponent> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC,
            AlliedEntityComponent::uuid,
            PacketCodecs.STRING,
            AlliedEntityComponent::name,
            AlliedEntityComponent::new
    );
}
