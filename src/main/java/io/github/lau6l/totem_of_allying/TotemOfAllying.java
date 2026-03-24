package io.github.lau6l.totem_of_allying;

import io.github.lau6l.totem_of_allying.command.OwnerCommand;
import io.github.lau6l.totem_of_allying.component.ToAComponents;
import io.github.lau6l.totem_of_allying.item.ToAItems;
import io.github.lau6l.totem_of_allying.particle.ToAParticles;
import io.github.lau6l.totem_of_allying.world.TickExecutor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemOfAllying implements ModInitializer {
    public static final String MOD_ID = "totem_of_allying";
    public static final Logger LOGGER = LoggerFactory.getLogger("Totem of Allying");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Totem of Allying...");

        ToAComponents.registerComponents();
        ToAItems.registerItems();
        ToAParticles.registerParticles();
        TickExecutor.initialize();
        registerCommands();
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            OwnerCommand.register(dispatcher);
        });
    }

    public static Identifier of(String value) {
        return Identifier.of(MOD_ID, value);
    }
}
