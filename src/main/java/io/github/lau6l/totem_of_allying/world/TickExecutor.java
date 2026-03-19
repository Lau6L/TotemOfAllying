package io.github.lau6l.totem_of_allying.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TickExecutor {
    private static final List<Supplier<Boolean>> tasks = new ArrayList<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(s -> tick());
    }

    private static void tick() {
        if (tasks.isEmpty()) return;
        tasks.removeIf(Supplier::get);
    }

    public static void schedule(Supplier<Boolean> task) {
        tasks.add(task);
    }
}
