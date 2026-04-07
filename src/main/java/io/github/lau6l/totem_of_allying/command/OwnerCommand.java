package io.github.lau6l.totem_of_allying.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Uuids;

import java.util.Collection;

public class OwnerCommand {
    private static final SimpleCommandExceptionType NOT_TAMEABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("totem_of_allying.commands.not_tameable"));
    private static final SimpleCommandExceptionType NOT_LIVING_ENTITY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("totem_of_allying.commands.not_alive"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("owner")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(
                                CommandManager.argument("tameables", EntityArgumentType.entities())
                                        .then(
                                                CommandManager.literal("clear")
                                                        .executes(
                                                                context -> executeClearOwner(
                                                                        EntityArgumentType.getEntities(context, "tameables")
                                                                )
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("set")
                                                        .then(
                                                                CommandManager.argument("target", EntityArgumentType.entity())
                                                                        .executes(
                                                                                context -> executeSetOwner(
                                                                                        EntityArgumentType.getEntities(context, "tameables"),
                                                                                        EntityArgumentType.getEntity(context, "target")
                                                                                )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int executeClearOwner(Collection<? extends Entity> entities) throws CommandSyntaxException {
        if (!areAllEntitiesTameable(entities))
            throw NOT_TAMEABLE_EXCEPTION.create();

        entities.forEach(entity -> {
            if (entity instanceof TameableEntity tameable) {
                tameable.setOwner((LazyEntityReference<LivingEntity>) null);
                tameable.setTamed(false, true);
            } else if (entity instanceof AbstractHorseEntity horse) {
                horse.setOwner(null);
                horse.setTame(false);
            } else if (entity instanceof AllayEntity allay) {
                allay.getBrain()
                        .forget(MemoryModuleType.LIKED_PLAYER);
            } else {
                NbtWriteView view = NbtWriteView.create(ErrorReporter.EMPTY);
                entity.writeData(view);

                view.remove("Owner");
                view.put("Tame", Codec.BOOL, false);

                entity.readData(NbtReadView.create(
                        ErrorReporter.EMPTY,
                        entity.getRegistryManager(),
                        view.getNbt()
                ));
            }
        });

        return entities.size();
    }

    private static int executeSetOwner(Collection<? extends Entity> entities, Entity owner) throws CommandSyntaxException {
        if (!areAllEntitiesTameable(entities))
            throw NOT_TAMEABLE_EXCEPTION.create();
        if (!(owner instanceof LivingEntity livingOwner))
            throw NOT_LIVING_ENTITY_EXCEPTION.create();

        entities.forEach(entity -> {
            if (entity instanceof TameableEntity tameable) {
                tameable.setTamed(true, true);
                tameable.setOwner(livingOwner);
            } else if (entity instanceof AbstractHorseEntity horse) {
                horse.setOwner(livingOwner);
                horse.setTame(true);
                horse.getEntityWorld().sendEntityStatus(horse, (byte) 7);
            } else if (entity instanceof AllayEntity allay) {
                allay.getBrain()
                        .remember(MemoryModuleType.LIKED_PLAYER, livingOwner.getUuid());
            } else {
                NbtWriteView view = NbtWriteView.create(ErrorReporter.EMPTY);
                entity.writeData(view);

                view.put("Owner", Uuids.INT_STREAM_CODEC, livingOwner.getUuid());
                view.put("Tame", Codec.BOOL, true);

                entity.readData(NbtReadView.create(
                        ErrorReporter.EMPTY,
                        entity.getRegistryManager(),
                        view.getNbt()
                ));
            }
        });

        return entities.size();
    }

    private static boolean areAllEntitiesTameable(Collection<? extends Entity> entities) {
        return entities
                .stream()
                .allMatch(entity ->
                        entity instanceof Tameable
                                || entity.getType() == EntityType.ALLAY
                                || entity.getType() == EntityType.HAPPY_GHAST);
    }
}
