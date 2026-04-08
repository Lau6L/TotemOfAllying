package io.github.lau6l.totem_of_allying.mixin;

import io.github.lau6l.totem_of_allying.world.AlliedEntityState;
import io.github.lau6l.totem_of_allying.world.ToAPersistentState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Tameable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Inject(
            method = "teleportCrossDimension",
            at = @At("TAIL")
    )
    private void onCopyFrom(ServerWorld from, ServerWorld to, TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        Entity self = (Entity)(Object) this;
        if (self instanceof Tameable
                || self.getType() == EntityType.ALLAY
                || self.getType() == EntityType.HAPPY_GHAST) {
            World world = self.getEntityWorld();
            if (world.isClient()) return;

            ToAPersistentState persistenceManager = ToAPersistentState.getServerState(from.getServer());
            UUID uuid = self.getUuid();
            if (!persistenceManager.isBoundToTotemOfAllying(uuid)) return;

            persistenceManager.addAlliedEntity(uuid, new AlliedEntityState(
                    self.getBlockPos().asVector3i(),
                    to.getRegistryKey(),
                    0
            ));
        }
    }
}
