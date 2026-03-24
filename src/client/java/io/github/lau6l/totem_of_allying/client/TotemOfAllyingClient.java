package io.github.lau6l.totem_of_allying.client;

import io.github.lau6l.totem_of_allying.client.particle.ToAParticlesClient;
import net.fabricmc.api.ClientModInitializer;

public class TotemOfAllyingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ToAParticlesClient.registerParticles();
    }
}
