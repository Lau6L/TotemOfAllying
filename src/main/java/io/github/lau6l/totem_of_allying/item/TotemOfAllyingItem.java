package io.github.lau6l.totem_of_allying.item;

import io.github.lau6l.totem_of_allying.TotemOfAllying;
import io.github.lau6l.totem_of_allying.component.AlliedEntityComponent;
import io.github.lau6l.totem_of_allying.component.ToAComponents;
import io.github.lau6l.totem_of_allying.mixin.AccessorAllayEntity;
import io.github.lau6l.totem_of_allying.world.AlliedEntityState;
import io.github.lau6l.totem_of_allying.world.TickExecutor;
import io.github.lau6l.totem_of_allying.world.ToAPersistentState;
import net.fabricmc.fabric.api.item.v1.ComponentTooltipAppenderRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.UUID;

public class TotemOfAllyingItem extends Item {
    public TotemOfAllyingItem(Item.Settings settings) {
        super(settings);
    }

    private static final Style SUCCESS = Style.EMPTY.withColor(Formatting.AQUA),
            FAILURE = Style.EMPTY.withColor(Formatting.RED);

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getEntityWorld();
        stack = user.getStackInHand(hand);
        if (world.isClient() ||
                !doesTotemOfAllyingApply(entity)
                || !entity.isAlive()) {
            return super.useOnEntity(stack, user, entity, hand);
        }

        AlliedEntityComponent previousAlly = stack.get(ToAComponents.ALLIED_ENTITY_COMPONENT);
        ServerPlayerEntity serverUser = (ServerPlayerEntity) user;
        Text notificationMessage;

        if (previousAlly != null) {
            notificationMessage = managePreviousAlly(previousAlly, stack, entity);
        } else {
            notificationMessage = manageNoPreviousAlly(stack, user, entity, (ServerWorld) world);
        }
        serverUser.networkHandler.sendPacket(new OverlayMessageS2CPacket(notificationMessage));

        return ActionResult.SUCCESS;
    }

    private boolean doesTotemOfAllyingApply(Entity entity) {
        return entity.getType() == EntityType.ALLAY || entity instanceof Tameable;
    }

    private boolean isOwner(PlayerEntity owner, Entity entity) {
        if (entity instanceof Tameable tameable) {
            return owner.equals(tameable.getOwner());
        } else if (entity instanceof AllayEntity allay) {
            return ((AccessorAllayEntity) allay).totem_of_allying$isLikedBy(owner);
        } else return false;
    }

    private Text manageNoPreviousAlly(ItemStack stack, PlayerEntity user, LivingEntity entity, ServerWorld world) {
        if (!isOwner(user, entity)) {
            return Text.translatable("totem_of_allying.interaction.not_owner")
                    .setStyle(FAILURE);
        }

        ToAPersistentState persistenceManager = ToAPersistentState.getServerState(world.getServer());
        UUID uuid = entity.getUuid();
        persistenceManager.addAlliedEntity(uuid, new AlliedEntityState(
                entity.getBlockPos().asVector3i(),
                entity.getEntityWorld().getRegistryKey()
        ));
        stack.set(ToAComponents.ALLIED_ENTITY_COMPONENT, new AlliedEntityComponent(
                uuid,
                entity.hasCustomName() ?
                        entity.getCustomName() :
                        entity.getType().getName()
        ));
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        ComponentTooltipAppenderRegistry.addFirst(ToAComponents.ALLIED_ENTITY_COMPONENT);

        return Text.translatable("totem_of_allying.interaction.bound")
                .setStyle(SUCCESS);
    }

    private Text managePreviousAlly(AlliedEntityComponent previousAlly, ItemStack stack, LivingEntity entity) {
        if (!previousAlly.uuid().equals(entity.getUuid())) {
            return Text.translatable("totem_of_allying.interaction.bound_to_another_entity")
                    .setStyle(FAILURE);
        }

        Text entityName = entity.hasCustomName() ?
                entity.getCustomName() :
                entity.getType().getName();
        if (previousAlly.typeOrName().equals(entityName)) {
            return Text.translatable("totem_of_allying.interaction.already_bound")
                    .setStyle(FAILURE);
        }

        stack.set(ToAComponents.ALLIED_ENTITY_COMPONENT, new AlliedEntityComponent(
                previousAlly.uuid(),
                entityName
        ));
        return Text.translatable("totem_of_allying.interaction.updated")
                .setStyle(SUCCESS);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ActionResult superResult = super.use(world, user, hand);
        if (world.isClient() || superResult.isAccepted()) return superResult;

        ItemStack stack = user.getStackInHand(hand);
        AlliedEntityComponent alliedEntityComponent = stack.get(ToAComponents.ALLIED_ENTITY_COMPONENT);
        if (alliedEntityComponent == null) return ActionResult.PASS;

        ServerPlayerEntity serverUser = (ServerPlayerEntity) user;
        MinecraftServer server = world.getServer();
        ToAPersistentState persistenceManager = ToAPersistentState.getServerState(server);
        AlliedEntityState state = persistenceManager.getAlliedEntity(alliedEntityComponent.uuid());

        if (state == null) {
            onAlliedEntityDeath(stack, serverUser);
            return ActionResult.SUCCESS;
        }

        Entity entity = world.getEntity(alliedEntityComponent.uuid());
        if (entity == null) {
            loadAndTeleport(server, state, stack, serverUser, alliedEntityComponent, world);
        } else {
            teleportAlliedEntityToPlayer(entity, world, user);
        }

        return ActionResult.SUCCESS;
    }

    private static void teleportAlliedEntityToPlayer(Entity entity, World world, PlayerEntity player) {
        entity.teleportTo(new TeleportTarget(
                (ServerWorld) world,
                player.getEntityPos(),
                Vec3d.ZERO,
                0, 0,
                TeleportTarget.NO_OP
        ));
    }

    private static void loadAndTeleport(MinecraftServer server, AlliedEntityState state, ItemStack stack, ServerPlayerEntity serverUser, AlliedEntityComponent alliedEntityComponent, World world) {
        ServerWorld allyWorld = server.getWorld(state.world());
        if (allyWorld == null) {
            TotemOfAllying.LOGGER.error("Allied entity was in an unknown world! ({})", state.world().toString());
            onAlliedEntityDeath(stack, serverUser);
            return;
        }
        ServerChunkManager chunkManager = allyWorld.getChunkManager();
        ChunkPos pos = new ChunkPos(
                ChunkSectionPos.getSectionCoord(state.position().x()),
                ChunkSectionPos.getSectionCoord(state.position().z())
        );
        chunkManager.addTicket(
                ChunkTicketType.FORCED,
                pos,
                1
        );

        TickExecutor.schedule(() -> {
            if (!chunkManager.isChunkLoaded(pos.x, pos.z)) return false;

            Entity loadedEntity = world.getEntity(alliedEntityComponent.uuid());
            if (loadedEntity == null)
                onAlliedEntityDeath(stack, serverUser);
            else teleportAlliedEntityToPlayer(loadedEntity, world, serverUser);

            return true;
        });
    }

    private static void onAlliedEntityDeath(ItemStack stack, ServerPlayerEntity serverUser) {
        stack.remove(ToAComponents.ALLIED_ENTITY_COMPONENT);
        stack.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        serverUser.networkHandler.sendPacket(new OverlayMessageS2CPacket(
                Text.translatable("totem_of_allying.use.died")
                        .setStyle(FAILURE)
        ));
    }
}
