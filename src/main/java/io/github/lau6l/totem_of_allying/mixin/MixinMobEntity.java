package io.github.lau6l.totem_of_allying.mixin;

import io.github.lau6l.totem_of_allying.item.ToAItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MixinMobEntity {
    @Inject(
            method = "interactWithItem",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onInteractWithItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(ToAItems.TOTEM_OF_ALLYING)) {
            itemStack.useOnEntity(player, (LivingEntity)(Object) this, hand);
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}

