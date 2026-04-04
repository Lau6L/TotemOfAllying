package io.github.lau6l.totem_of_allying.sound;

import io.github.lau6l.totem_of_allying.TotemOfAllying;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ToASounds {
    public static final SoundEvent
            TOTEM_OF_ALLYING_BOND = register("totem_bond"),
            TOTEM_OF_ALLYING_RELEASE = register("totem_release"),
            TOTEM_OF_ALLYING_FAIL = register("totem_fail"),
            TOTEM_OF_ALLYING_TP = register("totem_active");

    private static SoundEvent register(String name) {
        Identifier id = TotemOfAllying.of(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {}
}
