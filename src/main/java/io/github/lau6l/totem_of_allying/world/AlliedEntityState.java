package io.github.lau6l.totem_of_allying.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public record AlliedEntityState(Vec3i position, RegistryKey<World> world, int references) {
    public static final Codec<AlliedEntityState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Vec3i.CODEC.fieldOf("pos").forGetter((s) -> s.position),
                    World.CODEC.fieldOf("world").forGetter(AlliedEntityState::world),
                    Codec.INT.fieldOf("references").forGetter(AlliedEntityState::references)
            ).apply(instance, AlliedEntityState::new)
    );

    public AlliedEntityState(Vec3i position, RegistryKey<World> world) {
        this(position, world, 1);
    }
}
