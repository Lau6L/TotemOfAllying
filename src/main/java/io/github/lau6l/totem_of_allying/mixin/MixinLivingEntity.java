package io.github.lau6l.totem_of_allying.mixin;

import io.github.lau6l.totem_of_allying.component.AlliedEntityComponent;
import io.github.lau6l.totem_of_allying.component.ToAComponents;
import io.github.lau6l.totem_of_allying.item.ToAItems;
import io.github.lau6l.totem_of_allying.world.AlliedEntityState;
import io.github.lau6l.totem_of_allying.world.ToAPersistentState;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(
            method = "remove",
            at = @At("HEAD")
    )
    private void onOnRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = ((Entity)(Object) this);
        if (self.getType() == EntityType.ALLAY || self instanceof Tameable) {
            removeAlly(reason, self);
        } else if (self instanceof ItemEntity item && item.getStack().isOf(ToAItems.TOTEM_OF_ALLYING)) {
            removeItem(item);
        }
    }

    @Unique
    private static void removeItem(ItemEntity item) {
        ItemStack stack = item.getStack();
        AlliedEntityComponent alliedEntityComponent = stack.get(ToAComponents.ALLIED_ENTITY_COMPONENT);
        if (alliedEntityComponent == null) return;
        World world = item.getWorld();
        if (world.isClient()) return;
        ToAPersistentState state = ToAPersistentState.getServerState(world.getServer());

        state.removeAlliedEntityReference(alliedEntityComponent.uuid());
    }

    @Unique
    private static void removeAlly(Entity.RemovalReason reason, Entity self) {
        World world = self.getWorld();
        if (world.isClient()) return;

        ToAPersistentState persistenceManager = ToAPersistentState.getServerState(world.getServer());
        UUID uuid = self.getUuid();
        if (!persistenceManager.isBoundToTotemOfAllying(uuid)) return;

        if (reason.shouldSave()) {
            BlockPos selfPos = self.getBlockPos();
            persistenceManager.addAlliedEntity(uuid, new AlliedEntityState(
                    new Vec3i(selfPos.getX(), selfPos.getY(), selfPos.getZ()),
                    world.getRegistryKey(),
                    0
            ));
        } else if (reason != Entity.RemovalReason.CHANGED_DIMENSION) {
            persistenceManager.removeAlliedEntityEntry(uuid);
        }
    }
}
