package io.github.lau6l.totem_of_allying.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AllayEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AllayEntity.class)
public interface AccessorAllayEntity {
    @Invoker("isLikedBy")
    boolean totem_of_allying$isLikedBy(@Nullable Entity player);
}
