package io.github.lau6l.totem_of_allying.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.joml.Vector3ic;

public record AlliedEntityState(Vector3ic position, RegistryKey<World> world, int references) {
    public static final Codec<AlliedEntityState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codecs.VECTOR_3I.fieldOf("pos").forGetter(AlliedEntityState::position),
                    World.CODEC.fieldOf("world").forGetter(AlliedEntityState::world),
                    Codec.INT.fieldOf("references").forGetter(AlliedEntityState::references)
            ).apply(instance, AlliedEntityState::new)
    );
}
