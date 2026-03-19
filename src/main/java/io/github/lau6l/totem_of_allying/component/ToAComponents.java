package io.github.lau6l.totem_of_allying.component;

import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class ToAComponents {
    public static final ComponentType<AlliedEntityComponent> ALLIED_ENTITY_COMPONENT = register(
            "allied_entity", builder -> builder.codec(AlliedEntityComponent.CODEC).packetCodec(AlliedEntityComponent.PACKET_CODEC).cache()
    );

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, builderOperator.apply(ComponentType.builder()).build());
    }

    public static void registerComponents() {
    }
}
