package io.github.lau6l.totem_of_allying.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lau6l.totem_of_allying.TotemOfAllying;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ToAPersistentState extends PersistentState {
    private Map<UUID, AlliedEntityState> alliedEntities = new HashMap<>();

    private ToAPersistentState() {
    }

    private ToAPersistentState(Map<UUID, AlliedEntityState> totemOfAllyingSavedEntities) {
        this.alliedEntities = totemOfAllyingSavedEntities;
    }

    public AlliedEntityState getAlliedEntity(UUID uuid) {
        return alliedEntities.get(uuid);
    }
    public void addAlliedEntity(UUID uuid, AlliedEntityState state) {
        AlliedEntityState previousState = alliedEntities.get(uuid);
        if (previousState != null) {
            state = new AlliedEntityState(
                    state.position(),
                    state.world(),
                    previousState.references() + state.references()
            );
        }
        alliedEntities.put(uuid, state);
        markDirty();
    }
    public AlliedEntityState removeAlliedEntity(UUID uuid) {
        AlliedEntityState state = alliedEntities.get(uuid);
        if (state == null) return null;

        markDirty();
        if (state.references() <= 1) {
            return alliedEntities.remove(uuid);
        } else {
            state = new AlliedEntityState(
                    state.position(),
                    state.world(),
                    state.references() - 1
            );
            return alliedEntities.put(uuid, state);
        }
    }
    public AlliedEntityState removeAlliedEntityEntry(UUID uuid) {
        return alliedEntities.remove(uuid);
    }
    public boolean isBoundToTotemOfAllying(UUID uuid) {
        return alliedEntities.containsKey(uuid);
    }

    private static final Codec<ToAPersistentState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(Uuids.STRING_CODEC, AlliedEntityState.CODEC)
                            .optionalFieldOf("allied_entities", Map.of())
                            .forGetter(state -> state.alliedEntities)
            ).apply(instance, ToAPersistentState::new)
    );

    private static final PersistentStateType<ToAPersistentState> TYPE = new PersistentStateType<>(
            TotemOfAllying.MOD_ID,
            ToAPersistentState::new,
            CODEC,
            null
    );

    public static ToAPersistentState getServerState(MinecraftServer server) {
        return server
                .getWorld(World.OVERWORLD)
                .getPersistentStateManager()
                .getOrCreate(TYPE);
    }
}
