package io.github.lau6l.totem_of_allying.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.List;

public class TickExecutor {
    private static final List<TpRequest> tasks = new ArrayList<>();

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(s -> tick());
    }

    private static void tick() {
        if (tasks.isEmpty()) return;
        tasks.removeIf(TpRequest::tick);
    }

    public static void schedule(TpRequest request) {
        tasks.add(request);
    }
}
