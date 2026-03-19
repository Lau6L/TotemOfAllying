package io.github.lau6l.totem_of_allying.item;

import io.github.lau6l.totem_of_allying.TotemOfAllying;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class ToAItems {
    public static final Item TOTEM_OF_ALLYING = register(
            "totem_of_allying",
            TotemOfAllyingItem::new,
            new Item.Settings()
                    .rarity(Rarity.RARE)
                    .maxCount(1)
                    .useCooldown(5)
    );

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, TotemOfAllying.of(name));
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void registerItems() {
    }
}
