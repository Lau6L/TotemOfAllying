package io.github.lau6l.totem_of_allying.particle;

import io.github.lau6l.totem_of_allying.TotemOfAllying;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ToAParticles {
    public static final SimpleParticleType TOTEM_OF_ALLYING = register("totem_of_allying", true);

    private static SimpleParticleType register(String id, boolean alwaysShow) {
        return Registry.register(Registries.PARTICLE_TYPE, TotemOfAllying.of(id), FabricParticleTypes.simple(alwaysShow));
    }

    public static void registerParticles() {
    }
}
