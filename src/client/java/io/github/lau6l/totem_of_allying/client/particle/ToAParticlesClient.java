package io.github.lau6l.totem_of_allying.client.particle;

import io.github.lau6l.totem_of_allying.particle.ToAParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.SonicBoomParticle;

public class ToAParticlesClient {
    public static void registerParticles() {
        ParticleFactoryRegistry.getInstance().register(
                ToAParticles.TOTEM_OF_ALLYING,
                SonicBoomParticle.Factory::new
        );
    }
}
