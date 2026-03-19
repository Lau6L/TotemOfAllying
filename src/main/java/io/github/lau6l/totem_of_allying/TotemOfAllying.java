package io.github.lau6l.totem_of_allying;

import io.github.lau6l.totem_of_allying.component.Components;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotemOfAllying implements ModInitializer {
    public static final String MOD_ID = "totem_of_allying";
    public static final Logger LOGGER = LoggerFactory.getLogger("Totem of Allying");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Totem of Allying...");

        Components.registerComponents();
    }
}
