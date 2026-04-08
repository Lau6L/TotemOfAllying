package io.github.lau6l.totem_of_allying.mixin;

import io.github.lau6l.totem_of_allying.world.AlliedEntityState;
import io.github.lau6l.totem_of_allying.world.ToAPersistentState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Tameable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
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
    private void onCopyFrom(ServerWorld world, TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        Entity self = (Entity)(Object) this;
        if (self.getType() == EntityType.ALLAY || self instanceof Tameable) {
            World selfWorld = self.getWorld();
            if (selfWorld.isClient()) return;

            ToAPersistentState persistenceManager = ToAPersistentState.getServerState(selfWorld.getServer());
            UUID uuid = self.getUuid();
            if (!persistenceManager.isBoundToTotemOfAllying(uuid)) return;

            BlockPos selfPos = self.getBlockPos();
            persistenceManager.addAlliedEntity(uuid, new AlliedEntityState(
                    new Vec3i(selfPos.getX(), selfPos.getY(), selfPos.getZ()),
                    world.getRegistryKey(),
                    0
            ));
        }
    }
}
